package io.redspace.ironsspellbooks.setup;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.block.pedestal.PedestalRenderer;
import io.redspace.ironsspellbooks.block.scroll_forge.ScrollForgeRenderer;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.armor.*;
import io.redspace.ironsspellbooks.entity.armor.pumpkin.PumpkinArmorModel;
import io.redspace.ironsspellbooks.entity.armor.pumpkin.PumpkinArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.simple_wizard.WizardArmorRenderer;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingRenderer;
import io.redspace.ironsspellbooks.entity.mobs.debug_wizard.DebugWizardRenderer;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoidRenderer;
import io.redspace.ironsspellbooks.entity.mobs.horse.SpectralSteedRenderer;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperRenderer;
import io.redspace.ironsspellbooks.entity.mobs.necromancer.NecromancerRenderer;
import io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons.SummonedSkeletonMultiRenderer;
import io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons.SummonedZombieMultiRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.archevoker.ArchevokerRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cryomancer.CryomancerRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer.PyromancerRenderer;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrbRenderer;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHoleRenderer;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedleRenderer;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashRenderer;
import io.redspace.ironsspellbooks.entity.spells.comet.CometRenderer;
import io.redspace.ironsspellbooks.entity.spells.cone_of_cold.ConeOfColdRenderer;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadRenderer;
import io.redspace.ironsspellbooks.entity.spells.electrocute.ElectrocuteRenderer;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
import io.redspace.ironsspellbooks.entity.spells.firebolt.FireboltRenderer;
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockRenderer;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleRenderer;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceRenderer;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowRenderer;
import io.redspace.ironsspellbooks.entity.spells.magic_missile.MagicMissileRenderer;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.MagmaBallRenderer;
import io.redspace.ironsspellbooks.entity.spells.poison_arrow.PoisonArrowRenderer;
import io.redspace.ironsspellbooks.entity.spells.root.RootRenderer;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldModel;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldRenderer;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldTrimModel;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammerRenderer;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetAreaRenderer;
import io.redspace.ironsspellbooks.entity.spells.void_tentacle.VoidTentacleRenderer;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispRenderer;
import io.redspace.ironsspellbooks.item.WaywardCompass;
import io.redspace.ironsspellbooks.item.armor.*;
import io.redspace.ironsspellbooks.particle.*;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.render.*;
import io.redspace.ironsspellbooks.tetra.TetraProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

import java.util.Map;

import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.CHARGE_TEXTURE;
import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.EVASION_TEXTURE;

@Mod.EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        //LayerDefinition basicHumanLayer = LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64);

        //See LayerDefinitions.createRoots
        LayerDefinition energyOverlayLayer = LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F), 64, 64);
        LayerDefinition outerLayer = LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.OUTER_ARMOR_DEFORMATION, 0.0F), 64, 32);
        LayerDefinition innerLayer = LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.INNER_ARMOR_DEFORMATION, 0.0F), 64, 32);

        //event.registerLayerDefinition(PyromancerRenderer.PYROMANCER_MODEL_LAYER, PyromancerModel::createBodyLayer);
        //event.registerLayerDefinition(PyromancerRenderer.PYROMANCER_INNER_ARMOR, () -> innerLayer);
        //event.registerLayerDefinition(PyromancerRenderer.PYROMANCER_OUTER_ARMOR, () -> outerLayer);

//        event.registerLayerDefinition(NecromancerRenderer.NECROMANCER_MODEL_LAYER, NecromancerModel::createBodyLayer);
//        event.registerLayerDefinition(NecromancerRenderer.NECROMANCER_INNER_ARMOR, () -> innerLayer);
//        event.registerLayerDefinition(NecromancerRenderer.NECROMANCER_OUTER_ARMOR, () -> outerLayer);

        event.registerLayerDefinition(ShieldModel.LAYER_LOCATION, ShieldModel::createBodyLayer);
        event.registerLayerDefinition(AcidOrbRenderer.MODEL_LAYER_LOCATION, AcidOrbRenderer::createBodyLayer);
        event.registerLayerDefinition(FireballRenderer.MODEL_LAYER_LOCATION, FireballRenderer::createBodyLayer);
        event.registerLayerDefinition(FireboltRenderer.MODEL_LAYER_LOCATION, FireboltRenderer::createBodyLayer);
        event.registerLayerDefinition(ShieldTrimModel.LAYER_LOCATION, ShieldTrimModel::createBodyLayer);
        event.registerLayerDefinition(AngelWingsModel.ANGEL_WINGS_LAYER, AngelWingsModel::createLayer);
        event.registerLayerDefinition(EnergySwirlLayer.Vanilla.ENERGY_LAYER, () -> energyOverlayLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.AddLayers event) {
        GeoArmorRenderer.registerArmorRenderer(WizardArmorItem.class, WizardArmorRenderer::new);
        GeoArmorRenderer.registerArmorRenderer(WanderingMagicianArmorItem.class, () -> new GenericCustomArmorRenderer(new WanderingMagicianModel()));
        GeoArmorRenderer.registerArmorRenderer(PyromancerArmorItem.class, () -> new GenericCustomArmorRenderer(new PyromancerArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(ElectromancerArmorItem.class, () -> new GenericCustomArmorRenderer(new ElectromancerArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(ArchevokerArmorItem.class, () -> new GenericCustomArmorRenderer(new ArchevokerArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(CultistArmorItem.class, () -> new GenericCustomArmorRenderer(new CultistArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(CryomancerArmorItem.class, () -> new GenericCustomArmorRenderer(new CryomancerArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(ShadowwalkerArmorItem.class, () -> new GenericCustomArmorRenderer(new ShadowwalkerArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(PriestArmorItem.class, () -> new GenericCustomArmorRenderer(new PriestArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(TarnishedCrownArmorItem.class, () -> new GenericCustomArmorRenderer(new TarnishedCrownModel()));
        GeoArmorRenderer.registerArmorRenderer(PumpkinArmorItem.class, () -> new PumpkinArmorRenderer(new PumpkinArmorModel()));
        GeoArmorRenderer.registerArmorRenderer(PlaguedArmorItem.class, () -> new GenericCustomArmorRenderer(new PlaguedArmorModel()));

 //Ironsspellbooks.logger.debug("registerRenderers: EntityRenderersEvent.AddLayers event: {}", event.toString());

        addLayerToPlayerSkin(event, "default");
        addLayerToPlayerSkin(event, "slim");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
        EntityRenderer<? extends Player> render = event.getSkin(skinName);
        if (render instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new AngelWingsLayer<>(livingRenderer));
            livingRenderer.addLayer(new EnergySwirlLayer.Vanilla(livingRenderer, EVASION_TEXTURE, SyncedSpellData.EVASION));
            livingRenderer.addLayer(new EnergySwirlLayer.Vanilla(livingRenderer, CHARGE_TEXTURE, SyncedSpellData.CHARGED));
            livingRenderer.addLayer(new ChargeSpellLayer.Vanilla<>(livingRenderer));
            livingRenderer.addLayer(new GlowingEyesLayer.Vanilla<>(livingRenderer));
            livingRenderer.addLayer(new SpellTargetingLayer.Vanilla<>(livingRenderer));
        }

        for (Map.Entry<EntityType<?>, EntityRenderer<?>> entry : Minecraft.getInstance().getEntityRenderDispatcher().renderers.entrySet()) {
            EntityRenderer<?> livingEntityRendererTest = entry.getValue();
            if (livingEntityRendererTest instanceof LivingEntityRenderer) {
                EntityType<?> entityType = entry.getKey();
                //noinspection unchecked,rawtypes
                var renderer = event.getRenderer((EntityType) entityType);
                if (renderer != null) {
                    renderer.addLayer(new SpellTargetingLayer.Vanilla<>(renderer));
                    //IronsSpellbooks.LOGGER.debug("registerRenderers: Found LivingEntityRenderer for {}", entityType);
                } else {
                    //IronsSpellbooks.LOGGER.debug("registerRenderers: Missing LivingEntityRenderer for {}", entityType);
                }

            } else {
                //IronsSpellbooks.LOGGER.debug("registerRenderers: Not a LivingEntityRenderer {}", livingEntityRendererTest);
            }
        }
    /*.valu((entityType)->{
            if(entityType. instanceof EntityType<LivingEntity> livingType)
            if(event.getRenderer(entityType) instanceof EntityRenderer<? extends LivingEntity> livingRenderer)
                livingRenderer.addlayer
        });*/
//        EntityRenderer<? extends LivingEntity> genericRender = event.getRenderer()
        //EntityRenderer<? extends AbstractSpellCastingMob> renderer = event.getRenderer(EntityRegistry.PYROMANCER.get());
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
        event.registerEntityRenderer(EntityRegistry.POISON_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DRAGON_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DEBUG_WIZARD.get(), (renderManager) -> new DebugWizardRenderer(renderManager));
        event.registerEntityRenderer(EntityRegistry.PYROMANCER.get(), PyromancerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.NECROMANCER.get(), NecromancerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SPECTRAL_STEED.get(), SpectralSteedRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHIELD_ENTITY.get(), ShieldRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WALL_OF_FIRE_ENTITY.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SPECTRAL_HAMMER.get(), SpectralHammerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUMMONED_VEX.get(), VexRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUMMONED_ZOMBIE.get(), SummonedZombieMultiRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUMMONED_SKELETON.get(), SummonedSkeletonMultiRenderer::new);
        event.registerEntityRenderer(EntityRegistry.LIGHTNING_LANCE_PROJECTILE.get(), LightningLanceRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WITHER_SKULL_PROJECTILE.get(), WitherSkullRenderer::new);
        event.registerEntityRenderer(EntityRegistry.MAGIC_ARROW_PROJECTILE.get(), MagicArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), CreeperHeadRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FROZEN_HUMANOID.get(), FrozenHumanoidRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SMALL_FIREBALL_PROJECTILE.get(), (context) -> new FireballRenderer(context, 0.75f));
        event.registerEntityRenderer(EntityRegistry.COMET.get(), (context) -> new CometRenderer(context, 0.75f));
        event.registerEntityRenderer(EntityRegistry.MAGIC_FIREBALL.get(), (context) -> new FireballRenderer(context, 1.5f));
        event.registerEntityRenderer(EntityRegistry.SUMMONED_POLAR_BEAR.get(), PolarBearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DEAD_KING.get(), DeadKingRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DEAD_KING_CORPSE.get(), DeadKingRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ARCHEVOKER.get(), ArchevokerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.KEEPER.get(), KeeperRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VOID_TENTACLE.get(), VoidTentacleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ROOT.get(), RootRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICE_BLOCK_PROJECTILE.get(), IceBlockRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CRYOMANCER.get(), CryomancerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISON_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DRAGON_BREATH_POOL.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISON_ARROW.get(), PoisonArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISON_SPLASH.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUMMONED_FROG.get(), FrogRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ACID_ORB.get(), AcidOrbRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLACK_HOLE.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLOOD_NEEDLE.get(), BloodNeedleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_FIELD.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.MAGMA_BALL.get(), MagmaBallRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TARGET_AREA_ENTITY.get(), TargetAreaRenderer::new);

        event.registerBlockEntityRenderer(BlockRegistry.SCROLL_FORGE_TILE.get(), ScrollForgeRenderer::new);
        event.registerBlockEntityRenderer(BlockRegistry.PEDESTAL_TILE.get(), PedestalRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.register(ParticleRegistry.WISP_PARTICLE.get(), WispParticle.Provider::new);
        event.register(ParticleRegistry.BLOOD_PARTICLE.get(), BloodParticle.Provider::new);
        event.register(ParticleRegistry.BLOOD_GROUND_PARTICLE.get(), BloodGroundParticle.Provider::new);
        event.register(ParticleRegistry.SNOWFLAKE_PARTICLE.get(), SnowflakeParticle.Provider::new);
        event.register(ParticleRegistry.ELECTRICITY_PARTICLE.get(), ElectricityParticle.Provider::new);
        event.register(ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get(), UnstableEnderParticle.Provider::new);
        event.register(ParticleRegistry.DRAGON_FIRE_PARTICLE.get(), DragonFireParticle.Provider::new);
        event.register(ParticleRegistry.FIRE_PARTICLE.get(), FireParticle.Provider::new);
        event.register(ParticleRegistry.EMBER_PARTICLE.get(), EmberParticle.Provider::new);
        event.register(ParticleRegistry.SIPHON_PARTICLE.get(), SiphonParticle.Provider::new);
        event.register(ParticleRegistry.FOG_PARTICLE.get(), FogParticle.Provider::new);
        event.register(ParticleRegistry.ACID_PARTICLE.get(), AcidParticle.Provider::new);
        event.register(ParticleRegistry.ACID_BUBBLE_PARTICLE.get(), AcidBubbleParticle.Provider::new);
    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent e) {
        //Item Properties
        e.enqueueWork(() -> ItemProperties.register(ItemRegistry.WAYWARD_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, itemStack, entity) -> WaywardCompass.getCatacombsLocation(entity, itemStack.getOrCreateTag()))));

        TetraProxy.PROXY.initClient();


        //TODO: Citadel reimplementation
        //e.enqueueWork(() -> ItemProperties.register(ItemRegistry.ANTIQUATED_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, itemStack, entity) -> AntiquatedCompass.getCitadelLocation(entity, itemStack.getOrCreateTag()))));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        //Set the player construct callback. It can be a lambda function.
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                new ResourceLocation(IronsSpellbooks.MODID, "animation"),
                42,
                ClientSetup::registerPlayerAnimation);
    }

    //This method will set your mods animation into the library.
    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        //This will be invoked for every new player
        return new ModifierLayer<>();
    }

}

