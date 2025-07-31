package io.redspace.ironsspellbooks.setup;

import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRenderer;
import io.redspace.ironsspellbooks.block.pedestal.PedestalRenderer;
import io.redspace.ironsspellbooks.block.scroll_forge.ScrollForgeRenderer;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.compat.tetra.TetraProxy;
import io.redspace.ironsspellbooks.effect.PlanarSightEffect;
import io.redspace.ironsspellbooks.entity.VisualFallingBlockRenderer;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingRenderer;
import io.redspace.ironsspellbooks.entity.mobs.debug_wizard.DebugWizardRenderer;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoidRenderer;
import io.redspace.ironsspellbooks.entity.mobs.horse.SpectralSteedRenderer;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperRenderer;
import io.redspace.ironsspellbooks.entity.mobs.necromancer.NecromancerRenderer;
import io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons.SummonedSkeletonMultiRenderer;
import io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons.SummonedZombieMultiRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist.ApothecaristRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.archevoker.ArchevokerRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cryomancer.CryomancerRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cultist.CultistRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.priest.PriestRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer.PyromancerRenderer;
import io.redspace.ironsspellbooks.entity.spells.ball_lightning.BallLightningRenderer;
import io.redspace.ironsspellbooks.entity.spells.flame_strike.FlameStrikeRenderer;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrbRenderer;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHoleRenderer;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedleRenderer;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashRenderer;
import io.redspace.ironsspellbooks.entity.spells.comet.CometRenderer;
import io.redspace.ironsspellbooks.entity.spells.cone_of_cold.ConeOfColdRenderer;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadRenderer;
import io.redspace.ironsspellbooks.entity.spells.devour_jaw.DevourJawRenderer;
import io.redspace.ironsspellbooks.entity.spells.eldritch_blast.EldritchBlastRenderer;
import io.redspace.ironsspellbooks.entity.spells.electrocute.ElectrocuteRenderer;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
import io.redspace.ironsspellbooks.entity.spells.firebolt.FireboltRenderer;
import io.redspace.ironsspellbooks.entity.spells.guiding_bolt.GuidingBoltRenderer;
import io.redspace.ironsspellbooks.entity.spells.gust.GustRenderer;
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockRenderer;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleRenderer;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceRenderer;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowRenderer;
import io.redspace.ironsspellbooks.entity.spells.magic_missile.MagicMissileRenderer;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.MagmaBallRenderer;
import io.redspace.ironsspellbooks.entity.spells.poison_arrow.PoisonArrowRenderer;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalRenderer;
import io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostRenderer;
import io.redspace.ironsspellbooks.entity.spells.root.RootRenderer;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldModel;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldRenderer;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldTrimModel;
import io.redspace.ironsspellbooks.entity.spells.small_magic_arrow.SmallMagicArrowRenderer;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammerRenderer;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamRenderer;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetAreaRenderer;
import io.redspace.ironsspellbooks.entity.spells.void_tentacle.VoidTentacleRenderer;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispRenderer;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.WaywardCompass;
import io.redspace.ironsspellbooks.item.weapons.AutoloaderCrossbow;
import io.redspace.ironsspellbooks.particle.*;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.render.*;
import io.redspace.ironsspellbooks.util.IMinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.util.Map;
import java.util.Optional;

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
        event.registerLayerDefinition(GustRenderer.MODEL_LAYER_LOCATION, GustRenderer::createBodyLayer);
        event.registerLayerDefinition(RayOfFrostRenderer.MODEL_LAYER_LOCATION, RayOfFrostRenderer::createBodyLayer);
        event.registerLayerDefinition(EldritchBlastRenderer.MODEL_LAYER_LOCATION, EldritchBlastRenderer::createBodyLayer);
        event.registerLayerDefinition(FireballRenderer.MODEL_LAYER_LOCATION, FireballRenderer::createBodyLayer);
        event.registerLayerDefinition(FireboltRenderer.MODEL_LAYER_LOCATION, FireboltRenderer::createBodyLayer);
        event.registerLayerDefinition(GuidingBoltRenderer.MODEL_LAYER_LOCATION, GuidingBoltRenderer::createBodyLayer);
        event.registerLayerDefinition(IcicleRenderer.MODEL_LAYER_LOCATION, IcicleRenderer::createBodyLayer);
        event.registerLayerDefinition(ShieldTrimModel.LAYER_LOCATION, ShieldTrimModel::createBodyLayer);
        event.registerLayerDefinition(AngelWingsModel.ANGEL_WINGS_LAYER, AngelWingsModel::createLayer);
        event.registerLayerDefinition(EnergySwirlLayer.Vanilla.ENERGY_LAYER, () -> energyOverlayLayer);
        event.registerLayerDefinition(BallLightningRenderer.MODEL_LAYER_LOCATION, BallLightningRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void replaceRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.SMALL_FIREBALL, (context) -> new ReplacedFireballRenderer(context, 0.75f, .75f));
        event.registerEntityRenderer(EntityType.FIREBALL, (context) -> new ReplacedFireballRenderer(context, 1.25f, 3f));
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.AddLayers event) {
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
        event.registerEntityRenderer(EntityRegistry.FLAME_STRIKE.get(), FlameStrikeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ELECTROCUTE_PROJECTILE.get(), ElectrocuteRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIREBOLT_PROJECTILE.get(), FireboltRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICICLE_PROJECTILE.get(), IcicleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISON_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DRAGON_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DEBUG_WIZARD.get(), (renderManager) -> new DebugWizardRenderer(renderManager));
        event.registerEntityRenderer(EntityRegistry.PYROMANCER.get(), PyromancerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.APOTHECARIST.get(), ApothecaristRenderer::new);
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
        event.registerEntityRenderer(EntityRegistry.MAGIC_FIREBALL.get(), (context) -> new FireballRenderer(context, 1.25f));
        event.registerEntityRenderer(EntityRegistry.SUMMONED_POLAR_BEAR.get(), PolarBearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DEAD_KING.get(), DeadKingRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DEAD_KING_CORPSE.get(), DeadKingRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ARCHEVOKER.get(), ArchevokerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.KEEPER.get(), KeeperRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SCULK_TENTACLE.get(), VoidTentacleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ROOT.get(), RootRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICE_BLOCK_PROJECTILE.get(), IceBlockRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CRYOMANCER.get(), CryomancerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISON_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUNBEAM.get(), SunbeamRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DRAGON_BREATH_POOL.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISON_ARROW.get(), PoisonArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISON_SPLASH.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ACID_ORB.get(), AcidOrbRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLACK_HOLE.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLOOD_NEEDLE.get(), BloodNeedleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_FIELD.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_BOMB.get(), MagmaBallRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TARGET_AREA_ENTITY.get(), TargetAreaRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HEALING_AOE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.PRIEST.get(), PriestRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GUIDING_BOLT.get(), GuidingBoltRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GUST_COLLIDER.get(), GustRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CHAIN_LIGHTNING.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DEVOUR_JAW.get(), DevourJawRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIREFLY_SWARM.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EARTHQUAKE_AOE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FALLING_BLOCK.get(), VisualFallingBlockRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get(), RayOfFrostRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ELDRITCH_BLAST_VISUAL_ENTITY.get(), EldritchBlastRenderer::new);
        event.registerEntityRenderer(EntityRegistry.PORTAL.get(), PortalRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SMALL_MAGIC_ARROW.get(), SmallMagicArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ARROW_VOLLEY_ENTITY.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.STOMP_AOE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ECHOING_STRIKE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.LIGHTNING_STRIKE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CULTIST.get(), CultistRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BALL_LIGHTNING.get(), BallLightningRenderer::new);

        event.registerBlockEntityRenderer(BlockRegistry.SCROLL_FORGE_TILE.get(), ScrollForgeRenderer::new);
        event.registerBlockEntityRenderer(BlockRegistry.PEDESTAL_TILE.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(BlockRegistry.ALCHEMIST_CAULDRON_TILE.get(), AlchemistCauldronRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.WISP_PARTICLE.get(), WispParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_PARTICLE.get(), BloodParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_GROUND_PARTICLE.get(), BloodGroundParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SNOWFLAKE_PARTICLE.get(), SnowflakeParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ELECTRICITY_PARTICLE.get(), ElectricityParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get(), UnstableEnderParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.DRAGON_FIRE_PARTICLE.get(), DragonFireParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.FIRE_PARTICLE.get(), FireParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.EMBER_PARTICLE.get(), EmberParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SIPHON_PARTICLE.get(), SiphonParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.FOG_PARTICLE.get(), FogParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SHOCKWAVE_PARTICLE.get(), ShockwaveParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ACID_PARTICLE.get(), AcidParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ACID_BUBBLE_PARTICLE.get(), AcidBubbleParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ZAP_PARTICLE.get(), ZapParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.FIREFLY_PARTICLE.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.RING_SMOKE_PARTICLE.get(), RingSmokeParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.PORTAL_FRAME_PARTICLE.get(), PortalFrameParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLASTWAVE_PARTICLE.get(), BlastwaveParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SPARK_PARTICLE.get(), SparkParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SNOW_DUST.get(), SnowDustParticle.Provider::new);

    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent e) {
        //Item Properties
        e.enqueueWork(() -> {
            ItemProperties.register(ItemRegistry.WAYWARD_COMPASS.get(), ResourceLocation.withDefaultNamespace("angle"),
                    new CompassItemPropertyFunction((level, itemStack, entity) -> WaywardCompass.getCatacombsLocation(entity, itemStack.getOrCreateTag())));
            MinecraftInstanceHelper.instance = new IMinecraftInstanceHelper() {
                @Nullable
                @Override
                public Player player() {
                    return Minecraft.getInstance().player;
                }
            };
            ItemProperties.register(ItemRegistry.AUTOLOADER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pull"), (itemStack, clientLevel, livingEntity, i) -> CrossbowItem.isCharged(itemStack) ? 0.0F : AutoloaderCrossbow.getLoadingTicks(itemStack) / (float) AutoloaderCrossbow.getChargeDuration(itemStack));
            ItemProperties.register(ItemRegistry.AUTOLOADER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pulling"), (itemStack, clientLevel, livingEntity, i) -> AutoloaderCrossbow.isLoading(itemStack) && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F);
            ItemProperties.register(ItemRegistry.AUTOLOADER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("charged"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F);
            ItemProperties.register(ItemRegistry.AUTOLOADER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("firework"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && CrossbowItem.isCharged(itemStack) && CrossbowItem.containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
            FogRenderer.MOB_EFFECT_FOG.add(new PlanarSightEffect.EcholocationBlindnessFogFunction());
            ItemRegistry.getIronsItems().stream().filter(item -> item.get() instanceof SpellBook).forEach((item) -> CuriosRendererRegistry.register(item.get(), SpellBookCurioRenderer::new));
        });

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                SpellAnimations.ANIMATION_RESOURCE,
                42,
                (player) -> {
                    var animation = new ModifierLayer<>();

                    animation.addModifierLast(new AdjustmentModifier((partName) -> {
                        switch (partName) {
                            case "rightArm", "leftArm" -> {
                                return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(player.getXRot() * Mth.DEG_TO_RAD, Mth.DEG_TO_RAD * (player.yHeadRot - player.yBodyRot), 0), Vec3f.ZERO));

                            }
                            default -> {
                                return Optional.empty();
                            }
                        }
                    }));
                    animation.addModifierLast(new MirrorModifier() {
                        @Override
                        public boolean isEnabled() {
                            //IronsSpellbooks.LOGGER.debug("ANIMATION_DATA_FACTORY.ModifierLayer.MirrorModifier.isEnabled: {} -> {}",ClientMagicData.getSyncedSpellData(player).getSpellSelection().equipmentSlot,ClientMagicData.getSyncedSpellData(player).getSpellSelection().equipmentSlot.equals(SpellSelectionManager.OFFHAND));
                            return ClientMagicData.getSyncedSpellData(player).getCastingEquipmentSlot().equals(SpellSelectionManager.OFFHAND);
                        }
                    });

                    return animation;
                });

        TetraProxy.PROXY.initClient();

    }

    @SubscribeEvent
    public static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        //TODO: use forge's built in system
        event.register(IronsSpellbooks.id("item/keeper_flamberge_gui"));
        event.register(IronsSpellbooks.id("item/keeper_flamberge_normal"));
        event.register(IronsSpellbooks.id("item/magehunter_gui"));
        event.register(IronsSpellbooks.id("item/magehunter_normal"));
        event.register(IronsSpellbooks.id("item/truthseeker_gui"));
        event.register(IronsSpellbooks.id("item/truthseeker_normal"));

        for (SchoolType schoolType : SchoolRegistry.REGISTRY.get().getValues()) {
            event.register(AffinityRingRenderer.getAffinityRingModelLocation(schoolType));
            event.register(ScrollModel.getScrollModelLocation(schoolType));
        }
    }

    @SubscribeEvent
    public static void replaceItemModels(ModelEvent.ModifyBakingResult event) {
        var key = new ModelResourceLocation(IronsSpellbooks.id("scroll"), "inventory");
        BakedModel model = event.getModels().get(key);
        IronsSpellbooks.LOGGER.debug("replaceItemModels {}: {}", key, model.getClass());
        event.getModels().computeIfPresent(key, (k, oldModel) -> new ScrollModel(oldModel, event.getModelBakery()));
    }

}

