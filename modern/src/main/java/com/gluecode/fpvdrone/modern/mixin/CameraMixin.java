package com.gluecode.fpvdrone.modern.mixin;
import com.gluecode.fpvdrone.modern.FpvClient;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;
    @Shadow @Final private static Vector3fc FORWARDS;
    @Shadow @Final private static Vector3fc UP;
    @Shadow @Final private static Vector3fc LEFT;
    @Shadow private int matrixPropertiesDirty;
    @Unique private float fpv$partialTick;

    @Inject(method = "alignWithEntity", at = @At("HEAD"))
    private void fpv$rememberInterpolation(float partialTick, CallbackInfo ci) { fpv$partialTick = partialTick; }

    @Inject(method = "setRotation", at = @At("TAIL"))
    private void fpv$fullOrientation(float yaw, float pitch, CallbackInfo ci) {
        if (!FpvClient.flight.isArmed()) return;
        rotation.set(FpvClient.flight.rotation(fpv$partialTick));
        FORWARDS.rotate(rotation, forwards);
        UP.rotate(rotation, up);
        LEFT.rotate(rotation, left);
        matrixPropertiesDirty |= 3;
    }
}
