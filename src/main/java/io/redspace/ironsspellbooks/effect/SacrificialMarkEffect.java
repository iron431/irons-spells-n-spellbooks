package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.blood.SacrificeSpell;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber
public class SacrificialMarkEffect extends MagicMobEffect {
    public SacrificialMarkEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8b1538);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level.isClientSide() || !entity.hasEffect(MobEffectRegistry.SACRIFICIAL_MARK)) {
            return;
        }
        var mark = entity.getEffect(MobEffectRegistry.SACRIFICIAL_MARK);
        if (mark == null) {
            return;
        }

        LivingEntity owner = SummonManager.getOwner(entity) instanceof LivingEntity livingOwner ? livingOwner : entity;

        var spell = (SacrificeSpell) SpellRegistry.SACRIFICE_SPELL.get();

        float damage = spell.getDamage(mark.getAmplifier() + 1, owner) + entity.getHealth() * 0.5f;
        float explosionRadius = spell.getRadius(entity);

        SacrificeSpell.doSacrificeExplosion(
                entity.level(),
                spell.getDamageSource(entity, owner),
                damage,
                explosionRadius,
                entity.getBoundingBox().getCenter()
        );
        entity.remove(Entity.RemovalReason.KILLED);
    }
}
