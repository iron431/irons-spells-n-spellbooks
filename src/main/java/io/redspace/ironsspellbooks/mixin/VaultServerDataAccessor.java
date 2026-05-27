package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.data.VaultServerData;
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
