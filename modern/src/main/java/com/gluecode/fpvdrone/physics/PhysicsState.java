package com.gluecode.fpvdrone.physics;

import com.gluecode.fpvdrone.modern.FpvConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.jme3.math.Vector3f;
import java.util.function.BiFunction;

/** Minecraft collision is supplied by the client adapter; physics uses SI units. */
public final class PhysicsState {
    public static BiFunction<Vector3f, Float, Vector3f> movement = (velocity, dt) -> velocity;
    public static Vector3f collideAndMove(Vector3f velocity, float dt) { return movement.apply(velocity, dt); }
    public static Vector3f getGravity() { return new Vector3f(0, -9.80665f, 0); }
    public static float getThrottle() { return ControllerReader.getThrottle(); }
    public static float getDroneMass() { return FpvConfig.current.massKg; }
    public static float getPropRadius() { return FpvConfig.current.propDiameterInches * 0.0254f * 0.5f; }
    public static int getBatteryCells() { return FpvConfig.current.batteryCells; }
    public static float getKv() { return FpvConfig.current.motorKv * PhysicsConstants.toSIKv; }
    public static int getBlades() { return FpvConfig.current.blades; }
    public static float getMotorWidth() { return FpvConfig.current.motorWidthMm / 1000f; }
    public static float getMotorHeight() { return FpvConfig.current.motorHeightMm / 1000f; }
    public static float getPropPitch() { return FpvConfig.current.propPitchInches * 0.0254f; }
    public static float getPropWidth() { return FpvConfig.current.propWidthMm / 1000f; }
    public static boolean getFlightMode3d() { return FpvConfig.current.mode3d; }
    public static float getMotorMass() {
        float r = getMotorWidth() * 0.5f;
        return PhysicsConstants.motorMassCoefficient * (float)Math.PI * r * r * getMotorHeight();
    }
    public static float getBladeMass(float diameter) {
        return PhysicsConstants.polycarbonateDensity * diameter * 0.5f * getPropWidth() * 0.002f;
    }
    public static float getBellMass() {
        float r = getMotorWidth() * 0.5f;
        float h = (float)Math.PI * getMotorHeight();
        return h * (r*r - (r-0.001f)*(r-0.001f)) * PhysicsConstants.steelDensity
            + h * ((r-0.001f)*(r-0.001f) - (r-0.002f)*(r-0.002f)) * PhysicsConstants.neodymiumDensity;
    }
}
