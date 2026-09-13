package com.gluecode.fpvdrone.modern.mixin;
import com.gluecode.fpvdrone.modern.FpvClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void fpv$flightMovement(CallbackInfo ci) {
        if (FpvClient.flight.isArmed()) {
            FpvClient.flight.step();
            if (FpvClient.flight.isArmed()) ci.cancel();
        }
    }
}
