package io.redspace.ironsspellbooks.entity.spells;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpinAttackModel extends GeoModel<AbstractSpellCastingMob> {
    private static final ResourceLocation FIRE_TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/fire_riptide.png");
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/trident_riptide.png");

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/spin_attack_model.geo.json");

    public SpinAttackModel() {
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        SpinAttackType spinAttackType = ClientMagicData.getSyncedSpellData(object).getSpinAttackType();
        return switch (spinAttackType) {
            case FIRE -> FIRE_TEXTURE;
            default -> DEFAULT_TEXTURE;
        };

    }

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

}