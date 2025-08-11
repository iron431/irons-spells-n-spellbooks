package io.redspace.ironsspellbooks.api.entity;

import net.minecraft.world.entity.projectile.Projectile;
//todo:cleanup, remove?
public interface IMagicEntity {

    void notifyDangerousProjectile(Projectile projectile);
    boolean setTeleportLocationBehindTarget(int distance);
    void setBurningDashDirectionData();
//    @Deprecated(forRemoval = true)
//    /**
//     * seems to be shadowing entity getItemBySlot, should just be removed
//     */
//    ItemStack getItemBySlot(EquipmentSlot pSlot);
    boolean isDrinkingPotion();
    boolean getHasUsedSingleAttack();
    void setHasUsedSingleAttack(boolean bool);
    void startDrinkingPotion();

    //todo: remove this interface? all these getters are now just poking things that used to be hardcoded
    //    MagicData getMagicData();
//    void setSyncedSpellData(SyncedSpellData syncedSpellData);
//    boolean isCasting();
//    void initiateCastSpell(AbstractSpell spell, int spellLevel);
//    void cancelCast();
//    void castComplete();
}
