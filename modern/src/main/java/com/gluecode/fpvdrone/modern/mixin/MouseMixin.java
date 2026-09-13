package com.gluecode.fpvdrone.modern.mixin;
import com.gluecode.fpvdrone.modern.FpvClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void fpv$lockView(CallbackInfo ci) {
        if (FpvClient.flight.isArmed()) ci.cancel();
    }
}
