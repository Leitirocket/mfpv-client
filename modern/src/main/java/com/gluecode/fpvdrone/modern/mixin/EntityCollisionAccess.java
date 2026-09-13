package com.gluecode.fpvdrone.modern.mixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Entity.class)
public interface EntityCollisionAccess {
    @Invoker("collide") Vec3 fpv$collide(Vec3 movement);
}
