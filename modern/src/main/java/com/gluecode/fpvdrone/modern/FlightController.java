package com.gluecode.fpvdrone.modern;

import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.physics.*;
import com.gluecode.fpvdrone.util.Transforms;
import com.gluecode.fpvdrone.modern.mixin.EntityCollisionAccess;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/** Client flight adapter. Vanilla position packets still carry player movement. */
public final class FlightController {
    private IPhysicsCore physics;
    private LocalPlayer pilot;
    private boolean armed, savedFlying, savedGravity;
    private CameraType savedCamera;
    public float cameraYaw, cameraPitch, cameraRoll, oldCameraRoll;
    public float distance, flightSeconds, peakSpeed;
    private Vec3 lastPosition;
    private final org.joml.Quaternionf cameraRotation = new org.joml.Quaternionf();
    private final org.joml.Quaternionf previousRotation = new org.joml.Quaternionf();
    public org.joml.Quaternionf rotation(float partialTick) {
        return new org.joml.Quaternionf(previousRotation).slerp(cameraRotation, Math.clamp(partialTick, 0f, 1f));
    }

    public boolean isArmed() { return armed; }
    public float speed() { return physics == null ? 0 : physics.getVelocity().length(); }
    public boolean overheated() { return physics != null && physics.isOverheat(); }

    public void toggle() {
        if (armed) { disarm(); return; }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) return;
        if (!mc.player.getAbilities().mayfly) {
            message("FPV requires Creative or Spectator mode on this server."); return;
        }
        if (!ControllerReader.connected) { message("FPV: controller unavailable. Check axes in settings."); return; }
        if (FpvConfig.current.controller >= 0 && ControllerReader.getThrottle() > -0.8f && !FpvConfig.current.mode3d) {
            message("FPV: lower throttle before arming."); return;
        }
        FpvConfig.current.validate();
        pilot = mc.player;
        savedFlying = pilot.getAbilities().flying;
        savedGravity = pilot.isNoGravity();
        savedCamera = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        pilot.getAbilities().flying = true;
        pilot.onUpdateAbilities();
        pilot.setNoGravity(true);
        physics = FpvConfig.current.advancedPhysics ? new AdvancedPhysicsCore() : new DefaultPhysicsCore();
        Quaternion initial = new Quaternion().fromAngleAxis(-pilot.getYRot() * PhysicsConstants.rads, Vector3f.UNIT_Y);
        physics.setDroneLook(initial.mult(Vector3f.UNIT_Z));
        physics.setDroneUp(new Vector3f(0, 1, 0));
        physics.setDroneLeft(initial.mult(Vector3f.UNIT_X));
        physics.setVelocity(new Vector3f());
        distance = flightSeconds = peakSpeed = 0;
        lastPosition = pilot.position();
        PhysicsState.movement = this::move;
        armed = true;
        updateCamera(); oldCameraRoll = cameraRoll;
        previousRotation.set(cameraRotation);
        message("FPV armed — R to disarm, O for settings");
    }

    public void disarm() {
        if (!armed) return;
        armed = false;
        if (pilot != null) {
            pilot.getAbilities().flying = savedFlying && pilot.getAbilities().mayfly;
            pilot.setNoGravity(savedGravity);
            pilot.setDeltaMovement(Vec3.ZERO);
            pilot.setXRot(Math.clamp(pilot.getXRot(), -90f, 90f));
            if (Minecraft.getInstance().player == pilot) pilot.onUpdateAbilities();
        }
        if (savedCamera != null) Minecraft.getInstance().options.setCameraType(savedCamera);
        PhysicsState.movement = (v, dt) -> v;
        ControllerReader.reset();
        cameraRoll = oldCameraRoll = 0;
        pilot = null;
    }

    public void checkState() {
        if (!armed) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != pilot || mc.level == null || !pilot.isAlive() || !pilot.getAbilities().mayfly
                || mc.gui.screen() != null || !mc.isWindowActive() || !ControllerReader.connected) disarm();
    }

    /** Called in place of vanilla local-player movement while flying. */
    public void step() {
        checkState();
        if (!armed || Minecraft.getInstance().isPaused()) return;
        // A server teleport/correction must not be turned into a large physics jump.
        if (lastPosition != null && lastPosition.distanceTo(pilot.position()) > 8) physics.setVelocity(new Vector3f());
        oldCameraRoll = cameraRoll;
        previousRotation.set(cameraRotation);
        Vec3 before = pilot.position();
        physics.step(0.05f);
        Vector3f velocity = physics.getVelocity();
        if (!Float.isFinite(velocity.x) || !Float.isFinite(velocity.y) || !Float.isFinite(velocity.z)) {
            disarm(); message("FPV stopped: invalid simulation state."); return;
        }
        distance += (float)before.distanceTo(pilot.position());
        flightSeconds += 0.05f;
        peakSpeed = Math.max(peakSpeed, speed());
        lastPosition = pilot.position();
        pilot.setDeltaMovement(Vec3.ZERO);
        pilot.fallDistance = 0;
        updateCamera();
    }

    private Vector3f move(Vector3f velocity, Float dt) {
        if (!armed || pilot == null || dt <= 0 || !Float.isFinite(dt)) return new Vector3f();
        if (!Float.isFinite(velocity.x) || !Float.isFinite(velocity.y) || !Float.isFinite(velocity.z)) return new Vector3f();
        if (velocity.length() > 100f) velocity = velocity.normalize().mult(100f);
        Vec3 desired = new Vec3(velocity.x * dt, velocity.y * dt, velocity.z * dt);
        Vec3 clipped = pilot.noPhysics ? desired : ((EntityCollisionAccess)pilot).fpv$collide(desired);
        // Keep world position absolute on all axes. The upstream code lost the Z origin on collisions.
        pilot.setPos(pilot.getX() + clipped.x, pilot.getY() + clipped.y, pilot.getZ() + clipped.z);
        return new Vector3f(
            Math.abs(clipped.x - desired.x) > 1e-7 ? -velocity.x * 0.2f : velocity.x,
            Math.abs(clipped.y - desired.y) > 1e-7 ? -velocity.y * 0.2f : velocity.y,
            Math.abs(clipped.z - desired.z) > 1e-7 ? -velocity.z * 0.2f : velocity.z);
    }

    private void updateCamera() {
        Quaternion tilt = new Quaternion().fromAngleAxis(-FpvConfig.current.cameraAngle * PhysicsConstants.rads, physics.getDroneLeft());
        float[] angles = Transforms.getWorldEulerAngles(tilt.mult(physics.getDroneLook()), tilt.mult(physics.getDroneUp()));
        Quaternion orientation = new Quaternion().lookAt(tilt.mult(physics.getDroneLook()), tilt.mult(physics.getDroneUp()));
        cameraRotation.set(orientation.getX(), orientation.getY(), orientation.getZ(), orientation.getW()).rotateY((float)Math.PI);
        cameraYaw = angles[0]; cameraPitch = angles[1]; cameraRoll = angles[2];
        pilot.setYRot(cameraYaw); pilot.setXRot(cameraPitch);
    }

    private static void message(String text) {
        if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.sendOverlayMessage(Component.literal(text));
    }
}
