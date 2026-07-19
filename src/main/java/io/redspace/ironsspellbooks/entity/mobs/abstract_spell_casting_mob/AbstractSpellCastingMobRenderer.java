package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.HumanoidRenderer;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.render.EchoingStrikesHologramLayer;
import io.redspace.ironsspellbooks.render.EnergySwirlLayer;
import io.redspace.ironsspellbooks.render.GeoSpinAttackLayer;
import io.redspace.ironsspellbooks.render.GlowingEyesLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;

import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.CHARGE_TEXTURE;
import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.EVASION_TEXTURE;

public abstract class AbstractSpellCastingMobRenderer extends HumanoidRenderer<AbstractSpellCastingMob> {
    private ResourceLocation textureResource;

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager, AbstractSpellCastingMobModel model) {
        super(renderManager, model);
        this.shadowRadius = 0.5f;
        //this.addLayer(new GeoEvasionLayer(this));
        addRenderLayer(new EnergySwirlLayer.Geo(this, EVASION_TEXTURE, MobEffectRegistry.EVASION));
        addRenderLayer(new EnergySwirlLayer.Geo(this, CHARGE_TEXTURE, MobEffectRegistry.CHARGED));
        addRenderLayer(new GlowingEyesLayer.Geo(this));
        addRenderLayer(new EchoingStrikesHologramLayer.Geo(this));
        addRenderLayer(new GeoSpinAttackLayer(this));
    }

    public static ItemStack makePotion(AbstractSpellCastingMob entity) {
        ItemStack healthPotion = new ItemStack(Items.POTION);
        return Utils.setPotion(healthPotion, entity.isInvertedHealAndHarm() ? Potions.HARMING : Potions.HEALING);
    }

    @Override
    public RenderType getRenderType(AbstractSpellCastingMob animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return animatable.isInvisible() ? RenderType.entityTranslucent(texture) : super.getRenderType(animatable, texture, bufferSource, partialTick);
    }
}