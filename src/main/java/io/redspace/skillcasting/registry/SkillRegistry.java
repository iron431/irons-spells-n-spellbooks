package io.redspace.skillcasting.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.demo.DemoBlessingOfLifeSkill;
import io.redspace.skillcasting.demo.DemoContinuousArrowsSkill;
import io.redspace.skillcasting.demo.DemoInstantSkill;
import io.redspace.skillcasting.demo.DemoProjectileSkill;
import io.redspace.skillcasting.demo.DemoRecastSkill;
import io.redspace.skillcasting.demo.PortalSkill;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.BlazeStormSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.BurningDashSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireArrowSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireballSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireboltSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireBreathSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FlamingBarrageSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FlamingStrikeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.HeatSurgeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.MagmaBombSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.RaiseHellSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.ScorchSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.WallOfFireSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.ConeOfColdSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.FrostStepSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.FrostbiteSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.FrostwaveSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.IceBlockSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.IceSpikesSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.IceTombSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.IcicleSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.RayOfFrostSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.SnowballSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.ArrowVolleySpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.ChainCreeperSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.FangStrikeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.FangSwirlSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.FangWardSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.FirecrackerSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.GustSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.InvisibilitySpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.LobCreeperSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.ShieldSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.SlowSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.SpectralHammerSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.SummonHorseSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.SummonVexSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.ThrowSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.WololoSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.AbyssalShroudSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.EldritchBlastSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.PlanarSightSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.PocketDimensionSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.SculkTentaclesSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.SonicBoomSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.TelekinesisSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.SummonPolarBearSpell;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Deferred registration surface for {@link AbstractSkill}s, plus lookup helpers.
 */
public final class SkillRegistry {
    private static final DeferredRegister<AbstractSkill> SKILLS =
            DeferredRegister.create(SkillcastingRegistries.SKILL_REGISTRY_KEY, IronsSpellbooks.MODID);

    private SkillRegistry() {
    }

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }

    public static <T extends AbstractSkill> Supplier<T> registerSkill(String name, Supplier<T> skill) {
        return SKILLS.register(name, skill);
    }

    @Deprecated
    public static Holder<AbstractSkill> holder(ResourceLocation id) {
        return holder(get(id));
    }

    @Deprecated
    public static Holder<AbstractSkill> holder(AbstractSkill skill) {
        return SkillcastingRegistries.SKILLS.wrapAsHolder(skill);
    }

    @Deprecated
    public static ResourceLocation id(AbstractSkill skill) {
        return SkillcastingRegistries.SKILLS.getKey(skill);
    }

    @Deprecated
    public static AbstractSkill get(ResourceLocation id) {
        return SkillcastingRegistries.SKILLS.get(id);
    }

    // ---- built-in / demo skills ----------------------------------------------------------------

    public static final Supplier<DemoInstantSkill> DEMO_INSTANT =
            registerSkill("demo_instant", DemoInstantSkill::new);

    public static final Supplier<DemoProjectileSkill> DEMO_PROJECTILE =
            registerSkill("demo_projectile", DemoProjectileSkill::new);

    public static final Supplier<DemoContinuousArrowsSkill> DEMO_CONTINUOUS_ARROWS =
            registerSkill("demo_continuous_arrows", DemoContinuousArrowsSkill::new);

    public static final Supplier<DemoRecastSkill> DEMO_RECAST =
            registerSkill("demo_recast", DemoRecastSkill::new);

    public static final Supplier<DemoBlessingOfLifeSkill> DEMO_BLESSING_OF_LIFE =
            registerSkill("demo_blessing_of_life", DemoBlessingOfLifeSkill::new);

    public static final Supplier<PortalSkill> DEMO_PORTAL =
            registerSkill("demo_portal", PortalSkill::new);

    public static final Supplier<IcicleSpell> ICICLE =
            registerSkill("icicle", IcicleSpell::new);
    public static final Supplier<ConeOfColdSpell> CONE_OF_COLD =
            registerSkill("cone_of_cold", ConeOfColdSpell::new);
    public static final Supplier<FrostbiteSpell> FROSTBITE =
            registerSkill("frostbite", FrostbiteSpell::new);
    public static final Supplier<FrostStepSpell> FROST_STEP =
            registerSkill("frost_step", FrostStepSpell::new);
    public static final Supplier<FrostwaveSpell> FROSTWAVE_SPELL =
            registerSkill("frostwave", FrostwaveSpell::new);
    public static final Supplier<IceBlockSpell> ICE_BLOCK_SPELL =
            registerSkill("ice_block", IceBlockSpell::new);
    public static final Supplier<IceSpikesSpell> ICE_SPIKES_SPELL =
            registerSkill("ice_spikes", IceSpikesSpell::new);
    public static final Supplier<IceTombSpell> ICE_TOMB_SPELL =
            registerSkill("ice_tomb", IceTombSpell::new);
    public static final Supplier<RayOfFrostSpell> RAY_OF_FROST_SPELL =
            registerSkill("ray_of_frost", RayOfFrostSpell::new);
    public static final Supplier<SnowballSpell> SNOWBALL_SPELL =
            registerSkill("snowball", SnowballSpell::new);
    public static final Supplier<SummonPolarBearSpell> SUMMON_POLAR_BEAR_SPELL =
            registerSkill("summon_polar_bear", SummonPolarBearSpell::new);

    public static final Supplier<BlazeStormSpell> BLAZE_STORM_SPELL =
            registerSkill("blaze_storm", BlazeStormSpell::new);
    public static final Supplier<BurningDashSpell> BURNING_DASH_SPELL =
            registerSkill("burning_dash", BurningDashSpell::new);
    public static final Supplier<FireArrowSpell> FIRE_ARROW_SPELL =
            registerSkill("fire_arrow", FireArrowSpell::new);
    public static final Supplier<FireballSpell> FIREBALL_SPELL =
            registerSkill("fireball", FireballSpell::new);
    public static final Supplier<FireboltSpell> FIREBOLT_SPELL =
            registerSkill("firebolt", FireboltSpell::new);
    public static final Supplier<FireBreathSpell> FIRE_BREATH_SPELL =
            registerSkill("fire_breath", FireBreathSpell::new);
    public static final Supplier<FlamingBarrageSpell> FLAMING_BARRAGE_SPELL =
            registerSkill("flaming_barrage", FlamingBarrageSpell::new);
    public static final Supplier<FlamingStrikeSpell> FLAMING_STRIKE_SPELL =
            registerSkill("flaming_strike", FlamingStrikeSpell::new);
    public static final Supplier<HeatSurgeSpell> HEAT_SURGE_SPELL =
            registerSkill("heat_surge", HeatSurgeSpell::new);
    public static final Supplier<MagmaBombSpell> MAGMA_BOMB_SPELL =
            registerSkill("magma_bomb", MagmaBombSpell::new);
    public static final Supplier<RaiseHellSpell> RAISE_HELL_SPELL =
            registerSkill("raise_hell", RaiseHellSpell::new);
    public static final Supplier<ScorchSpell> SCORCH_SPELL =
            registerSkill("scorch", ScorchSpell::new);
    public static final Supplier<WallOfFireSpell> WALL_OF_FIRE_SPELL =
            registerSkill("wall_of_fire", WallOfFireSpell::new);

    public static final Supplier<ArrowVolleySpell> ARROW_VOLLEY_SPELL =
            registerSkill("arrow_volley", ArrowVolleySpell::new);
    public static final Supplier<ChainCreeperSpell> CHAIN_CREEPER_SPELL =
            registerSkill("chain_creeper", ChainCreeperSpell::new);
    public static final Supplier<FangStrikeSpell> FANG_STRIKE_SPELL =
            registerSkill("fang_strike", FangStrikeSpell::new);
    public static final Supplier<FangSwirlSpell> FANG_SWIRL_SPELL =
            registerSkill("fang_swirl", FangSwirlSpell::new);
    public static final Supplier<FangWardSpell> FANG_WARD_SPELL =
            registerSkill("fang_ward", FangWardSpell::new);
    public static final Supplier<FirecrackerSpell> FIRECRACKER_SPELL =
            registerSkill("firecracker", FirecrackerSpell::new);
    public static final Supplier<GustSpell> GUST_SPELL =
            registerSkill("gust", GustSpell::new);
    public static final Supplier<InvisibilitySpell> INVISIBILITY_SPELL =
            registerSkill("invisibility", InvisibilitySpell::new);
    public static final Supplier<LobCreeperSpell> LOB_CREEPER_SPELL =
            registerSkill("lob_creeper", LobCreeperSpell::new);
    public static final Supplier<ShieldSpell> SHIELD_SPELL =
            registerSkill("shield", ShieldSpell::new);
    public static final Supplier<SlowSpell> SLOW_SPELL =
            registerSkill("slow", SlowSpell::new);
    public static final Supplier<SpectralHammerSpell> SPECTRAL_HAMMER_SPELL =
            registerSkill("spectral_hammer", SpectralHammerSpell::new);
    public static final Supplier<SummonHorseSpell> SUMMON_HORSE_SPELL =
            registerSkill("summon_horse", SummonHorseSpell::new);
    public static final Supplier<SummonVexSpell> SUMMON_VEX_SPELL =
            registerSkill("summon_vex", SummonVexSpell::new);
    public static final Supplier<ThrowSpell> THROW_SPELL =
            registerSkill("throw", ThrowSpell::new);
    public static final Supplier<WololoSpell> WOLOLO_SPELL =
            registerSkill("wololo", WololoSpell::new);

    public static final Supplier<AbyssalShroudSpell> ABYSSAL_SHROUD_SPELL =
            registerSkill("abyssal_shroud", AbyssalShroudSpell::new);
    public static final Supplier<SculkTentaclesSpell> SCULK_TENTACLES_SPELL =
            registerSkill("sculk_tentacles", SculkTentaclesSpell::new);
    public static final Supplier<SonicBoomSpell> SONIC_BOOM_SPELL =
            registerSkill("sonic_boom", SonicBoomSpell::new);
    public static final Supplier<PlanarSightSpell> PLANAR_SIGHT_SPELL =
            registerSkill("planar_sight", PlanarSightSpell::new);
    public static final Supplier<TelekinesisSpell> TELEKINESIS_SPELL =
            registerSkill("telekinesis", TelekinesisSpell::new);
    public static final Supplier<EldritchBlastSpell> ELDRITCH_BLAST_SPELL =
            registerSkill("eldritch_blast", EldritchBlastSpell::new);
    public static final Supplier<PocketDimensionSpell> POCKET_DIMENSION_SPELL =
            registerSkill("pocket_dimension", PocketDimensionSpell::new);
}
