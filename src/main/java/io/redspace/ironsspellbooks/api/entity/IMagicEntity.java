package io.redspace.ironsspellbooks.api.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

public interface IMagicEntity {
    MagicData getMagicData();
    void setSyncedSpellData(SyncedSpellData syncedSpellData);
    boolean isCasting();
    void initiateCastSpell(AbstractSpell spell, int spellLevel);
    void cancelCast();
    void castComplete();
    void notifyDangerousProjectile(Projectile projectile);
    boolean setTeleportLocationBehindTarget(int distance);
    void setBurningDashDirectionData();
    ItemStack getItemBySlot(EquipmentSlot pSlot);
    boolean isDrinkingPotion();
    boolean getHasUsedSingleAttack();
    void setHasUsedSingleAttack(boolean bool);
    void startDrinkingPotion();
}
