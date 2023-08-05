package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.*;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingBoss;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingCorpseEntity;
import io.redspace.ironsspellbooks.entity.mobs.debug_wizard.DebugWizard;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoid;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import io.redspace.ironsspellbooks.entity.mobs.necromancer.NecromancerEntity;
import io.redspace.ironsspellbooks.entity.mobs.summoned_frog.SummonedFrog;
import io.redspace.ironsspellbooks.entity.mobs.wizards.archevoker.ArchevokerEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cryomancer.CryomancerEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.priest.PriestEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer.PyromancerEntity;
import io.redspace.ironsspellbooks.entity.spells.ChainLightning;
import io.redspace.ironsspellbooks.entity.spells.ExtendedWitherSkull;
import io.redspace.ironsspellbooks.entity.spells.devour_jaw.DevourJaw;
import io.redspace.ironsspellbooks.entity.spells.firefly_swarm.FireflySwarmProjectile;
import io.redspace.ironsspellbooks.entity.spells.gust.GustCollider;
import io.redspace.ironsspellbooks.entity.spells.HealingAoe;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrb;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashProjectile;
import io.redspace.ironsspellbooks.entity.spells.comet.Comet;
import io.redspace.ironsspellbooks.entity.spells.cone_of_cold.ConeOfColdProjectile;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.entity.spells.dragon_breath.DragonBreathPool;
import io.redspace.ironsspellbooks.entity.spells.dragon_breath.DragonBreathProjectile;
import io.redspace.ironsspellbooks.entity.spells.electrocute.ElectrocuteProjectile;
import io.redspace.ironsspellbooks.entity.spells.fire_breath.FireBreathProjectile;
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
import io.redspace.ironsspellbooks.entity.spells.firebolt.FireboltProjectile;
import io.redspace.ironsspellbooks.entity.spells.guiding_bolt.GuidingBoltProjectile;
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleProjectile;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceProjectile;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowProjectile;
import io.redspace.ironsspellbooks.entity.spells.magic_missile.MagicMissileProjectile;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireField;
import io.redspace.ironsspellbooks.entity.spells.poison_arrow.PoisonArrow;
import io.redspace.ironsspellbooks.entity.spells.poison_breath.PoisonBreathProjectile;
import io.redspace.ironsspellbooks.entity.spells.poison_cloud.PoisonCloud;
import io.redspace.ironsspellbooks.entity.spells.poison_cloud.PoisonSplash;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammer;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.Sunbeam;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.entity.spells.void_tentacle.VoidTentacle;
import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final RegistryObject<EntityType<WispEntity>> WISP =
            ENTITIES.register("wisp", () -> EntityType.Builder.<WispEntity>of(WispEntity::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "wisp").toString()));

    public static final RegistryObject<EntityType<SpectralHammer>> SPECTRAL_HAMMER =
            ENTITIES.register("spectral_hammer", () -> EntityType.Builder.<SpectralHammer>of(SpectralHammer::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "spectral_hammer").toString()));

    public static final RegistryObject<EntityType<MagicMissileProjectile>> MAGIC_MISSILE_PROJECTILE =
            ENTITIES.register("magic_missile_projectile", () -> EntityType.Builder.<MagicMissileProjectile>of(MagicMissileProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "magic_missile_projectile").toString()));

    public static final RegistryObject<EntityType<ConeOfColdProjectile>> CONE_OF_COLD_PROJECTILE =
            ENTITIES.register("cone_of_cold_projectile", () -> EntityType.Builder.<ConeOfColdProjectile>of(ConeOfColdProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "cone_of_cold_projectile").toString()));

    public static final RegistryObject<EntityType<BloodSlashProjectile>> BLOOD_SLASH_PROJECTILE =
            ENTITIES.register("blood_slash_projectile", () -> EntityType.Builder.<BloodSlashProjectile>of(BloodSlashProjectile::new, MobCategory.MISC)
                    .sized(2f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "blood_slash_projectile").toString()));

    public static final RegistryObject<EntityType<ElectrocuteProjectile>> ELECTROCUTE_PROJECTILE =
            ENTITIES.register("electrocute_projectile", () -> EntityType.Builder.<ElectrocuteProjectile>of(ElectrocuteProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "electrocute_projectile").toString()));

    public static final RegistryObject<EntityType<FireboltProjectile>> FIREBOLT_PROJECTILE =
            ENTITIES.register("firebolt_projectile", () -> EntityType.Builder.<FireboltProjectile>of(FireboltProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "firebolt_projectile").toString()));

    public static final RegistryObject<EntityType<IcicleProjectile>> ICICLE_PROJECTILE =
            ENTITIES.register("icicle_projectile", () -> EntityType.Builder.<IcicleProjectile>of(IcicleProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "icicle_projectile").toString()));

    public static final RegistryObject<EntityType<FireBreathProjectile>> FIRE_BREATH_PROJECTILE =
            ENTITIES.register("fire_breath_projectile", () -> EntityType.Builder.<FireBreathProjectile>of(FireBreathProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "fire_breath_projectile").toString()));

    public static final RegistryObject<EntityType<DragonBreathProjectile>> DRAGON_BREATH_PROJECTILE =
            ENTITIES.register("dragon_breath_projectile", () -> EntityType.Builder.<DragonBreathProjectile>of(DragonBreathProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "dragon_breath_projectile").toString()));

    public static final RegistryObject<EntityType<DebugWizard>> DEBUG_WIZARD =
            ENTITIES.register("debug_wizard", () -> EntityType.Builder.<DebugWizard>of(DebugWizard::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "simple_wizard").toString()));

    public static final RegistryObject<EntityType<SummonedHorse>> SPECTRAL_STEED =
            ENTITIES.register("spectral_steed", () -> EntityType.Builder.<SummonedHorse>of(SummonedHorse::new, MobCategory.CREATURE)
                    .sized(1.3964844F, 1.6F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "spectral_steed").toString()));

    public static final RegistryObject<EntityType<ShieldEntity>> SHIELD_ENTITY =
            ENTITIES.register("shield", () -> EntityType.Builder.<ShieldEntity>of(ShieldEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "shield").toString()));

    public static final RegistryObject<EntityType<WallOfFireEntity>> WALL_OF_FIRE_ENTITY =
            ENTITIES.register("wall_of_fire", () -> EntityType.Builder.<WallOfFireEntity>of(WallOfFireEntity::new, MobCategory.MISC)
                    .sized(10f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "wall_of_fire").toString()));

    public static final RegistryObject<EntityType<SummonedVex>> SUMMONED_VEX =
            ENTITIES.register("summoned_vex", () -> EntityType.Builder.<SummonedVex>of(SummonedVex::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.8F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "summoned_vex").toString()));

    public static final RegistryObject<EntityType<PyromancerEntity>> PYROMANCER =
            ENTITIES.register("pyromancer", () -> EntityType.Builder.of(PyromancerEntity::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "pyromancer").toString()));

    public static final RegistryObject<EntityType<CryomancerEntity>> CRYOMANCER =
            ENTITIES.register("cryomancer", () -> EntityType.Builder.of(CryomancerEntity::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "cryomancer").toString()));

    public static final RegistryObject<EntityType<LightningLanceProjectile>> LIGHTNING_LANCE_PROJECTILE =
            ENTITIES.register("lightning_lance_projectile", () -> EntityType.Builder.<LightningLanceProjectile>of(LightningLanceProjectile::new, MobCategory.MISC)
                    .sized(1.25f, 1.25f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "lightning_lance_projectile").toString()));

    public static final RegistryObject<EntityType<NecromancerEntity>> NECROMANCER =
            ENTITIES.register("necromancer", () -> EntityType.Builder.of(NecromancerEntity::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "necromancer").toString()));

    public static final RegistryObject<EntityType<SummonedZombie>> SUMMONED_ZOMBIE =
            ENTITIES.register("summoned_zombie", () -> EntityType.Builder.<SummonedZombie>of(SummonedZombie::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "summoned_zombie").toString()));

    public static final RegistryObject<EntityType<SummonedSkeleton>> SUMMONED_SKELETON =
            ENTITIES.register("summoned_skeleton", () -> EntityType.Builder.<SummonedSkeleton>of(SummonedSkeleton::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "summoned_skeleton").toString()));

    public static final RegistryObject<EntityType<ExtendedWitherSkull>> WITHER_SKULL_PROJECTILE =
            ENTITIES.register("wither_skull_projectile", () -> EntityType.Builder.<ExtendedWitherSkull>of(ExtendedWitherSkull::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "wither_skull_projectile").toString()));

    public static final RegistryObject<EntityType<MagicArrowProjectile>> MAGIC_ARROW_PROJECTILE =
            ENTITIES.register("magic_arrow_projectile", () -> EntityType.Builder.<MagicArrowProjectile>of(MagicArrowProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "magic_arrow_projectile").toString()));

    public static final RegistryObject<EntityType<CreeperHeadProjectile>> CREEPER_HEAD_PROJECTILE =
            ENTITIES.register("creeper_head_projectile", () -> EntityType.Builder.<CreeperHeadProjectile>of(CreeperHeadProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "creeper_head_projectile").toString()));

    public static final RegistryObject<EntityType<FrozenHumanoid>> FROZEN_HUMANOID =
            ENTITIES.register("frozen_humanoid", () -> EntityType.Builder.<FrozenHumanoid>of(FrozenHumanoid::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "frozen_humanoid").toString()));

    public static final RegistryObject<EntityType<SmallMagicFireball>> SMALL_FIREBALL_PROJECTILE =
            ENTITIES.register("small_fireball", () -> EntityType.Builder.<SmallMagicFireball>of(SmallMagicFireball::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "small_fireball").toString()));

    public static final RegistryObject<EntityType<MagicFireball>> MAGIC_FIREBALL =
            ENTITIES.register("fireball", () -> EntityType.Builder.<MagicFireball>of(MagicFireball::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(4)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "fireball").toString()));

    public static final RegistryObject<EntityType<SummonedPolarBear>> SUMMONED_POLAR_BEAR =
            ENTITIES.register("summoned_polar_bear", () -> EntityType.Builder.<SummonedPolarBear>of(SummonedPolarBear::new, MobCategory.CREATURE)
                    .immuneTo(Blocks.POWDER_SNOW)
                    .sized(1.4F, 1.4F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "summoned_polar_bear").toString()));

    public static final RegistryObject<EntityType<DeadKingBoss>> DEAD_KING =
            ENTITIES.register("dead_king", () -> EntityType.Builder.<DeadKingBoss>of(DeadKingBoss::new, MobCategory.MONSTER)
                    .sized(.9f, 3.5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "dead_king").toString()));

    public static final RegistryObject<EntityType<DeadKingCorpseEntity>> DEAD_KING_CORPSE =
            ENTITIES.register("dead_king_corpse", () -> EntityType.Builder.<DeadKingCorpseEntity>of(DeadKingCorpseEntity::new, MobCategory.MISC)
                    .sized(1.5f, .95f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "dead_king_corpse").toString()));

    public static final RegistryObject<EntityType<CatacombsZombie>> CATACOMBS_ZOMBIE =
            ENTITIES.register("catacombs_zombie", () -> EntityType.Builder.<CatacombsZombie>of(CatacombsZombie::new, MobCategory.MONSTER)
                    .sized(1.5f, .95f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "catacombs_zombie").toString()));

    public static final RegistryObject<EntityType<ArchevokerEntity>> ARCHEVOKER =
            ENTITIES.register("archevoker", () -> EntityType.Builder.of(ArchevokerEntity::new, MobCategory.MONSTER)
                    .sized(.6f, 2f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "archevoker").toString()));

    public static final RegistryObject<EntityType<MagehunterVindicator>> MAGEHUNTER_VINDICATOR =
            ENTITIES.register("magehunter_vindicator", () -> EntityType.Builder.<MagehunterVindicator>of(MagehunterVindicator::new, MobCategory.MONSTER)
                    .sized(1.5f, .95f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "magehunter_vindicator").toString()));

    public static final RegistryObject<EntityType<KeeperEntity>> KEEPER =
            ENTITIES.register("citadel_keeper", () -> EntityType.Builder.<KeeperEntity>of(KeeperEntity::new, MobCategory.MONSTER)
                    .sized(.85f, 2.3f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "citadel_keeper").toString()));

    public static final RegistryObject<EntityType<VoidTentacle>> VOID_TENTACLE =
            ENTITIES.register("void_tentacle", () -> EntityType.Builder.<VoidTentacle>of(VoidTentacle::new, MobCategory.MISC)
                    .sized(2.5f, 5.5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "void_tentacle").toString()));

    public static final RegistryObject<EntityType<IceBlockProjectile>> ICE_BLOCK_PROJECTILE =
            ENTITIES.register("ice_block_projectile", () -> EntityType.Builder.<IceBlockProjectile>of(IceBlockProjectile::new, MobCategory.MISC)
                    .sized(1.25f, 1)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "ice_block_projectile").toString()));

    public static final RegistryObject<EntityType<PoisonCloud>> POISON_CLOUD =
            ENTITIES.register("poison_cloud", () -> EntityType.Builder.<PoisonCloud>of(PoisonCloud::new, MobCategory.MISC)
                    .sized(4f, .8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "poison_cloud").toString()));

    public static final RegistryObject<EntityType<Sunbeam>> SUNBEAM =
            ENTITIES.register("sunbeam", () -> EntityType.Builder.<Sunbeam>of(Sunbeam::new, MobCategory.MISC)
                    .sized(1.5f, 8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "sunbeam").toString()));

    public static final RegistryObject<EntityType<DragonBreathPool>> DRAGON_BREATH_POOL =
            ENTITIES.register("dragon_breath_pool", () -> EntityType.Builder.<DragonBreathPool>of(DragonBreathPool::new, MobCategory.MISC)
                    .sized(4f, .8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "dragon_breath_pool").toString()));

    public static final RegistryObject<EntityType<PoisonBreathProjectile>> POISON_BREATH_PROJECTILE =
            ENTITIES.register("poison_breath", () -> EntityType.Builder.<PoisonBreathProjectile>of(PoisonBreathProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "poison_breath").toString()));

    public static final RegistryObject<EntityType<PoisonArrow>> POISON_ARROW =
            ENTITIES.register("poison_arrow", () -> EntityType.Builder.<PoisonArrow>of(PoisonArrow::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "poison_arrow").toString()));

    public static final RegistryObject<EntityType<PoisonSplash>> POISON_SPLASH =
            ENTITIES.register("poison_splash", () -> EntityType.Builder.<PoisonSplash>of(PoisonSplash::new, MobCategory.MISC)
                    .sized(3.5f, 4f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "poison_splash").toString()));

    public static final RegistryObject<EntityType<SummonedFrog>> SUMMONED_FROG =
            ENTITIES.register("summoned_frog", () -> EntityType.Builder.<SummonedFrog>of(SummonedFrog::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "summoned_frog").toString()));

    public static final RegistryObject<EntityType<AcidOrb>> ACID_ORB =
            ENTITIES.register("acid_orb", () -> EntityType.Builder.<AcidOrb>of(AcidOrb::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "acid_orb").toString()));

    public static final RegistryObject<EntityType<RootEntity>> ROOT =
            ENTITIES.register("root", () -> EntityType.Builder.<RootEntity>of(RootEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "root").toString()));

    public static final RegistryObject<EntityType<BlackHole>> BLACK_HOLE =
            ENTITIES.register("black_hole", () -> EntityType.Builder.<BlackHole>of(BlackHole::new, MobCategory.MISC)
                    .sized(11, 11)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "black_hole").toString()));

    public static final RegistryObject<EntityType<BloodNeedle>> BLOOD_NEEDLE =
            ENTITIES.register("blood_needle", () -> EntityType.Builder.<BloodNeedle>of(BloodNeedle::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "blood_needle").toString()));

    public static final RegistryObject<EntityType<FireField>> FIRE_FIELD =
            ENTITIES.register("fire_field", () -> EntityType.Builder.<FireField>of(FireField::new, MobCategory.MISC)
                    .sized(4f, .8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "fire_field").toString()));

    public static final RegistryObject<EntityType<FireBomb>> FIRE_BOMB =
            ENTITIES.register("magma_ball", () -> EntityType.Builder.<FireBomb>of(FireBomb::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "magma_ball").toString()));

    public static final RegistryObject<EntityType<Comet>> COMET =
            ENTITIES.register("comet", () -> EntityType.Builder.<Comet>of(Comet::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "comet").toString()));

    public static final RegistryObject<EntityType<TargetedAreaEntity>> TARGET_AREA_ENTITY =
            ENTITIES.register("target_area", () -> EntityType.Builder.<TargetedAreaEntity>of(TargetedAreaEntity::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "target_area").toString()));

    public static final RegistryObject<EntityType<HealingAoe>> HEALING_AOE =
            ENTITIES.register("healing_aoe", () -> EntityType.Builder.<HealingAoe>of(HealingAoe::new, MobCategory.MISC)
                    .sized(4f, .8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "healing_aoe").toString()));

    public static final RegistryObject<EntityType<PriestEntity>> PRIEST =
            ENTITIES.register("priest", () -> EntityType.Builder.of(PriestEntity::new, MobCategory.CREATURE)
                    .sized(.6f, 2f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "priest").toString()));

    public static final RegistryObject<EntityType<GuidingBoltProjectile>> GUIDING_BOLT =
            ENTITIES.register("guiding_bolt", () -> EntityType.Builder.<GuidingBoltProjectile>of(GuidingBoltProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "guiding_bolt").toString()));

    public static final RegistryObject<EntityType<GustCollider>> GUST_COLLIDER =
            ENTITIES.register("gust", () -> EntityType.Builder.<GustCollider>of(GustCollider::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "gust").toString()));

    public static final RegistryObject<EntityType<ChainLightning>> CHAIN_LIGHTNING =
            ENTITIES.register("chain_lightning", () -> EntityType.Builder.<ChainLightning>of(ChainLightning::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "chain_lightning").toString()));

    public static final RegistryObject<EntityType<DevourJaw>> DEVOUR_JAW =
            ENTITIES.register("devour_jaw", () -> EntityType.Builder.<DevourJaw>of(DevourJaw::new, MobCategory.MISC)
                    .sized(2f, 2f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "devour_jaw").toString()));

    public static final RegistryObject<EntityType<FireflySwarmProjectile>> FIREFLY_SWARM =
            ENTITIES.register("firefly_swarm", () -> EntityType.Builder.<FireflySwarmProjectile>of(FireflySwarmProjectile::new, MobCategory.MISC)
                    .sized(.9f, .9f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "firefly_swarm").toString()));
}