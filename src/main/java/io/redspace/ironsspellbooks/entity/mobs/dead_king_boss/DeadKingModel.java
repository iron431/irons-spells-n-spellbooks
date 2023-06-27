package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

public class DeadKingModel extends AbstractSpellCastingMobModel {
    //private static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king.png");
    //private static final ResourceLocation TEXTURE2 = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_resting.png");
    //private static final ResourceLocation TEXTURE3 = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_enraged.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_texture.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/corpse.png");
    private static final ResourceLocation TEXTURE3 = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_phase2_texture.png");
    private static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/dead_king.geo.json");

    public DeadKingModel() {
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        if (object instanceof DeadKingBoss boss) {
            if (boss.isPhase(DeadKingBoss.Phases.FinalPhase))
                return TEXTURE3;
            else
                return TEXTURE;
        } else
            return TEXTURE2;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

}