package io.redspace.ironsspellbooks.mixin;

import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public interface VaultServerDataAccessor {
    @Accessor("rewardedPlayers")
    Set<UUID> irons_spellbooks$getRewardedPlayers();

    @Invoker("markChanged")
    void irons_spellbooks$markChanged();
}
