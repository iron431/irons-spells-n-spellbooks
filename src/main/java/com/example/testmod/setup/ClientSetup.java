package com.example.testmod.setup;

import com.example.testmod.TestMod;
import com.example.testmod.block.scroll_forge.ScrollForgeRenderer;
import com.example.testmod.entity.armor.simple_wizard.WizardArmorRenderer;
import com.example.testmod.entity.armor.wandering_magician.WanderingMagicianRenderer;
import com.example.testmod.entity.blood_slash.BloodSlashRenderer;
import com.example.testmod.entity.cone_of_cold.ConeOfColdRenderer;
import com.example.testmod.entity.electrocute.ElectrocuteRenderer;
import com.example.testmod.entity.firebolt.FireboltRenderer;
import com.example.testmod.entity.icicle.IcicleRenderer;
import com.example.testmod.entity.magic_missile.MagicMissileRenderer;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizard;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizardModel;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizardRenderer;
import com.example.testmod.entity.mobs.horse.SpectralSteed;
import com.example.testmod.entity.mobs.horse.MagicHorseRenderer;
import com.example.testmod.entity.shield.ShieldModel;
import com.example.testmod.entity.shield.ShieldRenderer;
import com.example.testmod.entity.shield.ShieldTrimModel;
import com.example.testmod.entity.wisp.WispEntity;
import com.example.testmod.entity.wisp.WispRenderer;
import com.example.testmod.item.armor.WanderMagicianArmorItem;
import com.example.testmod.item.armor.WizardArmorItem;
import com.example.testmod.particle.*;
import com.example.testmod.registries.BlockRegistry;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.registries.ParticleRegistry;
import com.example.testmod.render.AngelWingsLayer;
import com.example.testmod.render.AngelWingsModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

//TODO: find if there is a better place for this code to live (tutorial said to put it here)

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {


    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SIMPLE_WIZARD.get(), SimpleWizard.prepareAttributes().build());
        event.put(EntityRegistry.SPECTRAL_STEED.get(), SpectralSteed.prepareAttributes().build());
        event.put(EntityRegistry.WISP.get(), WispEntity.prepareAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SimpleWizardModel.SIMPLE_WIZARD_LAYER, SimpleWizardModel::createBodyLayer);
        event.registerLayerDefinition(ShieldModel.LAYER_LOCATION, ShieldModel::createBodyLayer);
        event.registerLayerDefinition(ShieldTrimModel.LAYER_LOCATION, ShieldTrimModel::createBodyLayer);
        event.registerLayerDefinition(AngelWingsModel.ANGEL_WINGS_LAYER, AngelWingsModel::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.AddLayers event) {
        GeoArmorRenderer.registerArmorRenderer(WizardArmorItem.class, WizardArmorRenderer::new);
        GeoArmorRenderer.registerArmorRenderer(WanderMagicianArmorItem.class, WanderingMagicianRenderer::new);

        TestMod.LOGGER.debug("registerRenderers: EntityRenderersEvent.AddLayers event: {}", event.toString());

        addLayerToPlayerSkin(event, "default");
        addLayerToPlayerSkin(event, "slim");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
        EntityRenderer<? extends Player> render = event.getSkin(skinName);
        if (render instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new AngelWingsLayer<>(livingRenderer));
        }
    }

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), MagicMissileRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CONE_OF_COLD_PROJECTILE.get(), ConeOfColdRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLOOD_SLASH_PROJECTILE.get(), BloodSlashRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ELECTROCUTE_PROJECTILE.get(), ElectrocuteRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIREBOLT_PROJECTILE.get(), FireboltRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICICLE_PROJECTILE.get(), IcicleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SIMPLE_WIZARD.get(), SimpleWizardRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SPECTRAL_STEED.get(), MagicHorseRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHIELD_ENTITY.get(), ShieldRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WALL_OF_FIRE_ENTITY.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WISP.get(), WispRenderer::new);
        event.registerBlockEntityRenderer(BlockRegistry.SCROLL_FORGE_TILE.get(), ScrollForgeRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.register(ParticleRegistry.BLOOD_PARTICLE.get(), BloodParticle.Provider::new);
        event.register(ParticleRegistry.BLOOD_GROUND_PARTICLE.get(), BloodGroundParticle.Provider::new);
        event.register(ParticleRegistry.SNOWFLAKE_PARTICLE.get(), SnowflakeParticle.Provider::new);
        event.register(ParticleRegistry.ELECTRICITY_PARTICLE.get(), ElectricityParticle.Provider::new);
        event.register(ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get(), UnstableEnderParticle.Provider::new);
        event.register(ParticleRegistry.DRAGON_FIRE_PARTICLE.get(), DragonFireParticle.Provider::new);
        event.register(ParticleRegistry.FIRE_PARTICLE.get(), FireParticle.Provider::new);
        event.register(ParticleRegistry.EMBER_PARTICLE.get(), EmberParticle.Provider::new);
    }
}

