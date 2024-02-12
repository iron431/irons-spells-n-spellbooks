package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GluttonyEffect extends MagicMobEffect {
    public GluttonyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @SubscribeEvent
    public static void finishEating(LivingEntityUseItemEvent.Finish event) {
        var entity = event.getEntity();
        //This won't account for cake blocks, but that would require more invasive mixins to detect so this is fine
        if (!entity.level.isClientSide) {
            var food = event.getItem().getFoodProperties(entity);
            if (food != null) {
                var gluttony = entity.getEffect(MobEffectRegistry.GLUTTONY.get());
                if (gluttony != null) {
                    var pmg = MagicData.getPlayerMagicData(entity);
                    pmg.addMana(food.getNutrition() * ratioForAmplifier(gluttony.getAmplifier()));
                    if (entity instanceof ServerPlayer serverPlayer) {
                        Messages.sendToPlayer(new ClientboundSyncMana(pmg), serverPlayer);
                    }
                }
            }
        }
    }

    public static float ratioForAmplifier(int amplifier) {
        return (4 + amplifier) * .5f;
    }
}
