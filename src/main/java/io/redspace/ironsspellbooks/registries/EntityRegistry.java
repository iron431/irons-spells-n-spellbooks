package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.ExtendedSmallFireball;
import io.redspace.ironsspellbooks.entity.ExtendedWitherSkull;
import io.redspace.ironsspellbooks.entity.blood_slash.BloodSlashProjectile;
import io.redspace.ironsspellbooks.entity.cone_of_cold.ConeOfColdProjectile;
import io.redspace.ironsspellbooks.entity.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.entity.dragon_breath.DragonBreathProjectile;
import io.redspace.ironsspellbooks.entity.electrocute.ElectrocuteProjectile;
import io.redspace.ironsspellbooks.entity.fire_breath.FireBreathProjectile;
import io.redspace.ironsspellbooks.entity.firebolt.FireboltProjectile;
import io.redspace.ironsspellbooks.entity.icicle.IcicleProjectile;
import io.redspace.ironsspellbooks.entity.lightning_lance.LightningLanceProjectile;
import io.redspace.ironsspellbooks.entity.magic_arrow.MagicArrowProjectile;
import io.redspace.ironsspellbooks.entity.magic_missile.MagicMissileProjectile;

import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingCorpseEntity;
import io.redspace.ironsspellbooks.entity.mobs.debug_wizard.DebugWizard;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoid;
import io.redspace.ironsspellbooks.entity.mobs.horse.SpectralSteed;
import io.redspace.ironsspellbooks.entity.mobs.necromancer.NecromancerEntity;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingBoss;
import io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer.PyromancerEntity;
import io.redspace.ironsspellbooks.entity.shield.ShieldEntity;
import io.redspace.ironsspellbooks.entity.wall_of_fire.WallOfFireEntity;
import io.redspace.ironsspellbooks.entity.wisp.WispEntity;
import io.redspace.ironsspellbooks.entity.mobs.*;
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

    public static final RegistryObject<EntityType<SpectralSteed>> SPECTRAL_STEED =
            ENTITIES.register("spectral_steed", () -> EntityType.Builder.<SpectralSteed>of(SpectralSteed::new, MobCategory.CREATURE)
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

    public static final RegistryObject<EntityType<ExtendedSmallFireball>> SMALL_FIREBALL_PROJECTILE =
            ENTITIES.register("small_fireball", () -> EntityType.Builder.<ExtendedSmallFireball>of(ExtendedSmallFireball::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "small_fireball").toString()));

    public static final RegistryObject<EntityType<SummonedPolarBear>> SUMMONED_POLAR_BEAR =
            ENTITIES.register("summoned_polar_bear", () -> EntityType.Builder.<SummonedPolarBear>of(SummonedPolarBear::new, MobCategory.CREATURE)
                    .immuneTo(Blocks.POWDER_SNOW)
                    .sized(1.4F, 1.4F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(IronsSpellbooks.MODID, "summoned_polar_bear").toString()));

    public static final RegistryObject<EntityType<DeadKingBoss>> DEAD_KING =
            ENTITIES.register("dead_king", () -> EntityType.Builder.<DeadKingBoss>of(DeadKingBoss::new, MobCategory.MONSTER)
                    .sized(.9f, 3f)
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
}