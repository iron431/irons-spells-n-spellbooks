package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


//@Mod.EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@Mod.EventBusSubscriber()
public class RootEffect extends MobEffect {
    UUID attrUUID = UUID.fromString("84523495-909d-42c3-9780-e35a86234456");

    public RootEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    public void removeAttributeModifiers(LivingEntity livingEntity, @NotNull AttributeMap attributeMap, int amplifier) {
        var attr = livingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(createRootedModifier());
        }
        super.removeAttributeModifiers(livingEntity, attributeMap, amplifier);
    }

    public void addAttributeModifiers(LivingEntity livingEntity, @NotNull AttributeMap attributeMap, int amplifier) {
        var attr = livingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.addPermanentModifier(createRootedModifier());
        }
        super.addAttributeModifiers(livingEntity, attributeMap, amplifier);
    }

    public AttributeModifier createRootedModifier() {
        return new AttributeModifier(attrUUID, "root", -100, AttributeModifier.Operation.ADDITION);
    }

    @SubscribeEvent
    public static void onLivingJumpEvent(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().hasEffect(MobEffectRegistry.ROOT.get())) {
            Vec3 vec3 = event.getEntity().getDeltaMovement();
            event.getEntity().setDeltaMovement(-vec3.x, -vec3.y, -vec3.z);
        }
    }

//    @SubscribeEvent
//    public static void onMovementInputUpdateEvent(MovementInputUpdateEvent event) {
//        if (event.getEntity().hasEffect(MobEffectRegistry.ROOT.get())) {
//            event.setCanceled(true);
//        }
//    }
}


