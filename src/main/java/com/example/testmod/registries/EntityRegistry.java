package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.entity.DebugEntity;
import com.example.testmod.entity.blood_slash.BloodSlashProjectile;
import com.example.testmod.entity.cone_of_cold.ConeOfColdProjectile;
import com.example.testmod.entity.electrocute.ElectrocuteProjectile;
import com.example.testmod.entity.fire_breath.FireBreathProjectile;
import com.example.testmod.entity.firebolt.FireboltProjectile;
import com.example.testmod.entity.icicle.IcicleProjectile;
import com.example.testmod.entity.lightning_lance.LightningLanceProjectile;
import com.example.testmod.entity.magic_arrow.MagicArrowProjectile;
import com.example.testmod.entity.magic_missile.MagicMissileProjectile;
import com.example.testmod.entity.mobs.SummonedSkeleton;
import com.example.testmod.entity.mobs.SummonedVex;
import com.example.testmod.entity.mobs.SummonedZombie;
import com.example.testmod.entity.mobs.horse.SpectralSteed;
import com.example.testmod.entity.mobs.necromancer.NecromancerEntity;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizard;
import com.example.testmod.entity.mobs.wizards.pyromancer.PyromancerEntity;
import com.example.testmod.entity.shield.ShieldEntity;
import com.example.testmod.entity.wall_of_fire.WallOfFireEntity;
import com.example.testmod.entity.wisp.WispEntity;
import com.example.testmod.entity.wither_skull.WitherSkullProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final RegistryObject<EntityType<DebugEntity>> DEBUG =
            ENTITIES.register("debug", () -> EntityType.Builder.<DebugEntity>of(DebugEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "debug").toString()));

    public static final RegistryObject<EntityType<WispEntity>> WISP =
            ENTITIES.register("wisp", () -> EntityType.Builder.<WispEntity>of(WispEntity::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "wisp").toString()));

    public static final RegistryObject<EntityType<MagicMissileProjectile>> MAGIC_MISSILE_PROJECTILE =
            ENTITIES.register("magic_missile_projectile", () -> EntityType.Builder.<MagicMissileProjectile>of(MagicMissileProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "magic_missile_projectile").toString()));

    public static final RegistryObject<EntityType<ConeOfColdProjectile>> CONE_OF_COLD_PROJECTILE =
            ENTITIES.register("cone_of_cold_projectile", () -> EntityType.Builder.<ConeOfColdProjectile>of(ConeOfColdProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "cone_of_cold_projectile").toString()));

    public static final RegistryObject<EntityType<BloodSlashProjectile>> BLOOD_SLASH_PROJECTILE =
            ENTITIES.register("blood_slash_projectile", () -> EntityType.Builder.<BloodSlashProjectile>of(BloodSlashProjectile::new, MobCategory.MISC)
                    .sized(2f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "blood_slash_projectile").toString()));

    public static final RegistryObject<EntityType<ElectrocuteProjectile>> ELECTROCUTE_PROJECTILE =
            ENTITIES.register("electrocute_projectile", () -> EntityType.Builder.<ElectrocuteProjectile>of(ElectrocuteProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "electrocute_projectile").toString()));

    public static final RegistryObject<EntityType<FireboltProjectile>> FIREBOLT_PROJECTILE =
            ENTITIES.register("firebolt_projectile", () -> EntityType.Builder.<FireboltProjectile>of(FireboltProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "firebolt_projectile").toString()));

    public static final RegistryObject<EntityType<IcicleProjectile>> ICICLE_PROJECTILE =
            ENTITIES.register("icicle_projectile", () -> EntityType.Builder.<IcicleProjectile>of(IcicleProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "icicle_projectile").toString()));

    public static final RegistryObject<EntityType<FireBreathProjectile>> FIRE_BREATH_PROJECTILE =
            ENTITIES.register("fire_breath_projectile", () -> EntityType.Builder.<FireBreathProjectile>of(FireBreathProjectile::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "fire_breath_projectile").toString()));

    public static final RegistryObject<EntityType<SimpleWizard>> SIMPLE_WIZARD =
            ENTITIES.register("simple_wizard", () -> EntityType.Builder.<SimpleWizard>of(SimpleWizard::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "simple_wizard").toString()));

    public static final RegistryObject<EntityType<SpectralSteed>> SPECTRAL_STEED =
            ENTITIES.register("spectral_steed", () -> EntityType.Builder.<SpectralSteed>of(SpectralSteed::new, MobCategory.CREATURE)
                    .sized(1.3964844F, 1.6F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(TestMod.MODID, "spectral_steed").toString()));

    public static final RegistryObject<EntityType<ShieldEntity>> SHIELD_ENTITY =
            ENTITIES.register("shield", () -> EntityType.Builder.<ShieldEntity>of(ShieldEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "shield").toString()));

    public static final RegistryObject<EntityType<WallOfFireEntity>> WALL_OF_FIRE_ENTITY =
            ENTITIES.register("wall_of_fire", () -> EntityType.Builder.<WallOfFireEntity>of(WallOfFireEntity::new, MobCategory.MISC)
                    .sized(10f, 1f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "wall_of_fire").toString()));

    public static final RegistryObject<EntityType<SummonedVex>> SUMMONED_VEX =
            ENTITIES.register("summoned_vex", () -> EntityType.Builder.<SummonedVex>of(SummonedVex::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.8F)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "summoned_vex").toString()));

    public static final RegistryObject<EntityType<PyromancerEntity>> PYROMANCER =
            ENTITIES.register("pyromancer", () -> EntityType.Builder.of(PyromancerEntity::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "pyromancer").toString()));

    public static final RegistryObject<EntityType<LightningLanceProjectile>> LIGHTNING_LANCE_PROJECTILE =
            ENTITIES.register("lightning_lance_projectile", () -> EntityType.Builder.<LightningLanceProjectile>of(LightningLanceProjectile::new, MobCategory.MISC)
                    .sized(1.25f, 1.25f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "lightning_lance_projectile").toString()));

    public static final RegistryObject<EntityType<NecromancerEntity>> NECROMANCER =
            ENTITIES.register("necromancer", () -> EntityType.Builder.of(NecromancerEntity::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "necromancer").toString()));

    public static final RegistryObject<EntityType<SummonedZombie>> SUMMONED_ZOMBIE =
            ENTITIES.register("summoned_zombie", () -> EntityType.Builder.<SummonedZombie>of(SummonedZombie::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "summoned_zombie").toString()));
    
    public static final RegistryObject<EntityType<SummonedSkeleton>> SUMMONED_SKELETON =
            ENTITIES.register("summoned_skeleton", () -> EntityType.Builder.<SummonedSkeleton>of(SummonedSkeleton::new, MobCategory.MONSTER)
                    .sized(.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "summoned_skeleton").toString()));

    public static final RegistryObject<EntityType<WitherSkullProjectile>> WITHER_SKULL_PROJECTILE =
            ENTITIES.register("wither_skull_projectile", () -> EntityType.Builder.<WitherSkullProjectile>of(WitherSkullProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "wither_skull_projectile").toString()));

    public static final RegistryObject<EntityType<MagicArrowProjectile>> MAGIC_ARROW_PROJECTILE =
            ENTITIES.register("magic_arrow_projectile", () -> EntityType.Builder.<MagicArrowProjectile>of(MagicArrowProjectile::new, MobCategory.MISC)
                    .sized(.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(TestMod.MODID, "magic_arrow_projectile").toString()));
}