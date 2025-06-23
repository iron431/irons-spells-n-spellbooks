package io.redspace.ironsspellbooks.api.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

public interface NoopMagicEntity extends IMagicEntity {
    default MagicData getMagicData() {
        return ((Entity) this).getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    default void setSyncedSpellData(SyncedSpellData syncedSpellData){}

    default boolean isCasting(){return false;}

    default void initiateCastSpell(AbstractSpell spell, int spellLevel){}

    default void cancelCast(){}

    default void castComplete(){}

    default void notifyDangerousProjectile(Projectile projectile){}

    default boolean setTeleportLocationBehindTarget(int distance){return false;}

    default void setBurningDashDirectionData(){}

    default boolean isDrinkingPotion(){return false;}

    default boolean getHasUsedSingleAttack(){
        return false;
    }

    default void setHasUsedSingleAttack(boolean bool){
    }

    default void startDrinkingPotion(){}
}
