package io.redspace.ironsspellbooks.api.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

public interface IMagicEntity {
    default MagicData getMagicData() {
        return MagicData.getPlayerMagicData((Entity) this);
    }
    @Deprecated(forRemoval = true)
    default void setSyncedSpellData(SyncedSpellData syncedSpellData) {
        getMagicData().setSyncedData(syncedSpellData);
    }
    boolean isCasting();
    void initiateCastSpell(AbstractSpell spell, int spellLevel);
    void cancelCast();
    void castComplete();
    void notifyDangerousProjectile(Projectile projectile);
    boolean setTeleportLocationBehindTarget(int distance);
    void setBurningDashDirectionData();
    @Deprecated(forRemoval = true)
    /**
     * seems to be shadowing entity getItemBySlot, should just be removed
     */
    ItemStack getItemBySlot(EquipmentSlot pSlot);
    boolean isDrinkingPotion();
    boolean getHasUsedSingleAttack();
    void setHasUsedSingleAttack(boolean bool);
    void startDrinkingPotion();
}
