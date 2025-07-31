package io.redspace.ironsspellbooks.entity.mobs.wizards.priest;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class PriestModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/priest/priest.png");
    public static final ResourceLocation TEXTURE_ARMOR = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/priest/priest_armored.png");
    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/archevoker.geo.json");

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return object.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.PRIEST_HELMET.get()) ? TEXTURE_ARMOR : TEXTURE;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, long instanceId, AnimationState<AbstractSpellCastingMob> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        if (entity instanceof PriestEntity priest && priest.isUnhappy()) {
            if (Minecraft.getInstance().isPaused() || !entity.shouldBeExtraAnimated())
                return;
            CoreGeoBone head = this.getAnimationProcessor().getBone(PartNames.HEAD);
            head.setRotZ(0.3F * Mth.sin(0.45F * (entity.tickCount + animationState.getPartialTick())));
            head.setRotX(-0.4F);
        }
    }
}