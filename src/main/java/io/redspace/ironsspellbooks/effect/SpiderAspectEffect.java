package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class SpiderAspectEffect extends MagicMobEffect {
    public static final float DAMAGE_PER_LEVEL = .05f;
    public static final TagKey<MobEffect> AFFECTED_BY_SPIDER_ASPECT =
            TagKey.create(Registries.MOB_EFFECT, new ResourceLocation(IronsSpellbooks.MODID, "affected_by_spider_aspect"));

    public SpiderAspectEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @SubscribeEvent
    public static void increaseDamage(LivingIncomingDamageEvent event) {
        var attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            //IronsSpellbooks.LOGGER.debug("onLivingTakeDamage: attacker: {} target:{}", livingAttacker.getName().getString(), event.getEntity());
            /**
             * Spider aspect handling
             */
            
            if (livingAttacker.hasEffect(MobEffectRegistry.SPIDER_ASPECT)) {
                boolean targetHasTagEffect = event.getEntity().getActiveEffects().stream()
                        .anyMatch(instance -> instance.getEffect().is(AFFECTED_BY_SPIDER_ASPECT));

                if (targetHasTagEffect) {
                    int lvl = livingAttacker.getEffect(MobEffectRegistry.SPIDER_ASPECT).getAmplifier() + 1;
                    float before = event.getAmount();
                    float multiplier = 1 + SpiderAspectEffect.DAMAGE_PER_LEVEL * lvl;
                    event.setAmount(event.getAmount() * multiplier);
                    //IronsSpellbooks.LOGGER.debug("spider mode {}->{}", before, event.getAmount());
                }
            }
        }
    }
}
