package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.worldgen.IceSpiderPatrolSpawner;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Unique
    IceSpiderPatrolSpawner irons_spellbooks$spiderPatrolSpawner = new IceSpiderPatrolSpawner();

    @Inject(method = "tickCustomSpawners", at = @At("HEAD"))
    private void tickCustomSpawners(boolean pSpawnEnemies, boolean pSpawnFriendlies, CallbackInfo ci) {
        irons_spellbooks$spiderPatrolSpawner.tick((ServerLevel) (Object) this, pSpawnEnemies, pSpawnFriendlies);
    }
}
