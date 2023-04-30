package io.redspace.ironsspellbooks.effect;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

public class TrueInvisibilityEffect extends MobEffect {
    public TrueInvisibilityEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    int lastHurtTimestamp;

    @Override
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(livingEntity, pAttributeMap, pAmplifier);
        if (livingEntity instanceof Player || livingEntity instanceof AbstractSpellCastingMob) {
            PlayerMagicData.getPlayerMagicData(livingEntity).getSyncedData().addEffects(SyncedSpellData.TRUE_INVIS);
        }
        this.lastHurtTimestamp = livingEntity.getLastHurtMobTimestamp();

    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        //If we attack, we lose invis
        if (lastHurtTimestamp != pLivingEntity.getLastHurtMobTimestamp()){
 //Ironsspellbooks.logger.debug("TrueInvisibilityEffect.applyEffectTick: entity attacked, removing effect");
            pLivingEntity.removeEffect(this);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(livingEntity, pAttributeMap, pAmplifier);
        if (livingEntity instanceof Player || livingEntity instanceof AbstractSpellCastingMob) {
            PlayerMagicData.getPlayerMagicData(livingEntity).getSyncedData().removeEffects(SyncedSpellData.TRUE_INVIS);
        }
    }
}
