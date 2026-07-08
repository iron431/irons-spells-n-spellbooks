package io.redspace.skillcasting.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.demo.DemoBlessingOfLifeSkill;
import io.redspace.skillcasting.demo.DemoContinuousArrowsSkill;
import io.redspace.skillcasting.demo.DemoInstantSkill;
import io.redspace.skillcasting.demo.DemoProjectileSkill;
import io.redspace.skillcasting.demo.DemoRecastSkill;
import io.redspace.skillcasting.demo.PortalSkill;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.GravityFissureSpell;
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
import io.redspace.skillcasting.irons_spellbooks.spells.ice.BlizzardSpell;
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
import io.redspace.skillcasting.irons_spellbooks.spells.evocation.ScapegoatSpell;
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
import io.redspace.skillcasting.irons_spellbooks.spells.blood.AcupunctureSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.BloodNeedlesSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.BloodSlashSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.BloodStepSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.DevourSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.HeartstopSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.RaiseDeadSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.RayOfSiphoningSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.SacrificeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.WitherSkullSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.ArcaneShackleSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.BlackHoleSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.CounterspellSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.DragonBreathSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.EchoingStrikesSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.EvasionSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.MagicArrowSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.MagicMissileSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.PortalSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.RecallSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.ShadowSlashSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.AngelWingsSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.BlessingOfLifeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.CleanseSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.CloudOfRegenerationSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.DivineSmiteSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.FortifySpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.GreaterHealSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.GuidingBoltSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.HasteSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.HealSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.HealingCircleSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.SunbeamSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.holy.WispSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.StarfallSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.SummonEnderChestSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.SummonSwordsSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.TeleportSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ice.SummonPolarBearSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.AscensionSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.BallLightningSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.ChainLightningSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.ChargeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.ElectrocuteSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.LightningBoltSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.LightningLanceSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.ShockwaveSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.ThunderstormSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.lightning.VoltStrikeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.AcidOrbSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.BlightSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.EarthquakeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.FireflySwarmSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.GluttonySpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.OakskinSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.PoisonArrowSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.PoisonBreathSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.PoisonSplashSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.RootSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.SpiderAspectSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.StompSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.nature.TouchDigSpell;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class SkillRegistry {
    // fixme: public?
    public static final DeferredRegister<AbstractSkill> SKILLS =
            DeferredRegister.create(SkillcastingRegistries.SKILL_REGISTRY_KEY, IronsSpellbooks.MODID);

    private SkillRegistry() {
    }

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }

    public static <T extends AbstractSkill> DeferredHolder<AbstractSkill, T> registerSkill(String name, java.util.function.Supplier<T> skill) {
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

    public static final DeferredHolder<AbstractSkill, DemoInstantSkill> DEMO_INSTANT =
            registerSkill("demo_instant", DemoInstantSkill::new);

    public static final DeferredHolder<AbstractSkill, DemoProjectileSkill> DEMO_PROJECTILE =
            registerSkill("demo_projectile", DemoProjectileSkill::new);

    public static final DeferredHolder<AbstractSkill, DemoContinuousArrowsSkill> DEMO_CONTINUOUS_ARROWS =
            registerSkill("demo_continuous_arrows", DemoContinuousArrowsSkill::new);

    public static final DeferredHolder<AbstractSkill, DemoRecastSkill> DEMO_RECAST =
            registerSkill("demo_recast", DemoRecastSkill::new);

    public static final DeferredHolder<AbstractSkill, DemoBlessingOfLifeSkill> DEMO_BLESSING_OF_LIFE =
            registerSkill("demo_blessing_of_life", DemoBlessingOfLifeSkill::new);

    public static final DeferredHolder<AbstractSkill, PortalSkill> DEMO_PORTAL =
            registerSkill("demo_portal", PortalSkill::new);

    public static final DeferredHolder<AbstractSkill, IcicleSpell> ICICLE_SPELL =
            registerSkill("icicle", IcicleSpell::new);
    public static final DeferredHolder<AbstractSkill, ConeOfColdSpell> CONE_OF_COLD_SPELL =
            registerSkill("cone_of_cold", ConeOfColdSpell::new);
    public static final DeferredHolder<AbstractSkill, FrostbiteSpell> FROSTBITE_SPELL =
            registerSkill("frostbite", FrostbiteSpell::new);
    public static final DeferredHolder<AbstractSkill, FrostStepSpell> FROST_STEP_SPELL =
            registerSkill("frost_step", FrostStepSpell::new);
    public static final DeferredHolder<AbstractSkill, FrostwaveSpell> FROSTWAVE_SPELL =
            registerSkill("frostwave", FrostwaveSpell::new);
    public static final DeferredHolder<AbstractSkill, IceBlockSpell> ICE_BLOCK_SPELL =
            registerSkill("ice_block", IceBlockSpell::new);
    public static final DeferredHolder<AbstractSkill, IceSpikesSpell> ICE_SPIKES_SPELL =
            registerSkill("ice_spikes", IceSpikesSpell::new);
    public static final DeferredHolder<AbstractSkill, IceTombSpell> ICE_TOMB_SPELL =
            registerSkill("ice_tomb", IceTombSpell::new);
    public static final DeferredHolder<AbstractSkill, RayOfFrostSpell> RAY_OF_FROST_SPELL =
            registerSkill("ray_of_frost", RayOfFrostSpell::new);
    public static final DeferredHolder<AbstractSkill, SnowballSpell> SNOWBALL_SPELL =
            registerSkill("snowball", SnowballSpell::new);
    public static final DeferredHolder<AbstractSkill, BlizzardSpell> BLIZZARD_SPELL =
            registerSkill("blizzard", BlizzardSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonPolarBearSpell> SUMMON_POLAR_BEAR_SPELL =
            registerSkill("summon_polar_bear", SummonPolarBearSpell::new);

    public static final DeferredHolder<AbstractSkill, BlazeStormSpell> BLAZE_STORM_SPELL =
            registerSkill("blaze_storm", BlazeStormSpell::new);
    public static final DeferredHolder<AbstractSkill, BurningDashSpell> BURNING_DASH_SPELL =
            registerSkill("burning_dash", BurningDashSpell::new);
    public static final DeferredHolder<AbstractSkill, FireArrowSpell> FIRE_ARROW_SPELL =
            registerSkill("fire_arrow", FireArrowSpell::new);
    public static final DeferredHolder<AbstractSkill, FireballSpell> FIREBALL_SPELL =
            registerSkill("fireball", FireballSpell::new);
    public static final DeferredHolder<AbstractSkill, FireboltSpell> FIREBOLT_SPELL =
            registerSkill("firebolt", FireboltSpell::new);
    public static final DeferredHolder<AbstractSkill, FireBreathSpell> FIRE_BREATH_SPELL =
            registerSkill("fire_breath", FireBreathSpell::new);
    public static final DeferredHolder<AbstractSkill, FlamingBarrageSpell> FLAMING_BARRAGE_SPELL =
            registerSkill("flaming_barrage", FlamingBarrageSpell::new);
    public static final DeferredHolder<AbstractSkill, FlamingStrikeSpell> FLAMING_STRIKE_SPELL =
            registerSkill("flaming_strike", FlamingStrikeSpell::new);
    public static final DeferredHolder<AbstractSkill, HeatSurgeSpell> HEAT_SURGE_SPELL =
            registerSkill("heat_surge", HeatSurgeSpell::new);
    public static final DeferredHolder<AbstractSkill, MagmaBombSpell> MAGMA_BOMB_SPELL =
            registerSkill("magma_bomb", MagmaBombSpell::new);
    public static final DeferredHolder<AbstractSkill, RaiseHellSpell> RAISE_HELL_SPELL =
            registerSkill("raise_hell", RaiseHellSpell::new);
    public static final DeferredHolder<AbstractSkill, ScorchSpell> SCORCH_SPELL =
            registerSkill("scorch", ScorchSpell::new);
    public static final DeferredHolder<AbstractSkill, WallOfFireSpell> WALL_OF_FIRE_SPELL =
            registerSkill("wall_of_fire", WallOfFireSpell::new);

    public static final DeferredHolder<AbstractSkill, ArrowVolleySpell> ARROW_VOLLEY_SPELL =
            registerSkill("arrow_volley", ArrowVolleySpell::new);
    public static final DeferredHolder<AbstractSkill, ChainCreeperSpell> CHAIN_CREEPER_SPELL =
            registerSkill("chain_creeper", ChainCreeperSpell::new);
    public static final DeferredHolder<AbstractSkill, FangStrikeSpell> FANG_STRIKE_SPELL =
            registerSkill("fang_strike", FangStrikeSpell::new);
    public static final DeferredHolder<AbstractSkill, FangSwirlSpell> FANG_SWIRL_SPELL =
            registerSkill("fang_swirl", FangSwirlSpell::new);
    public static final DeferredHolder<AbstractSkill, FangWardSpell> FANG_WARD_SPELL =
            registerSkill("fang_ward", FangWardSpell::new);
    public static final DeferredHolder<AbstractSkill, FirecrackerSpell> FIRECRACKER_SPELL =
            registerSkill("firecracker", FirecrackerSpell::new);
    public static final DeferredHolder<AbstractSkill, GustSpell> GUST_SPELL =
            registerSkill("gust", GustSpell::new);
    public static final DeferredHolder<AbstractSkill, InvisibilitySpell> INVISIBILITY_SPELL =
            registerSkill("invisibility", InvisibilitySpell::new);
    public static final DeferredHolder<AbstractSkill, LobCreeperSpell> LOB_CREEPER_SPELL =
            registerSkill("lob_creeper", LobCreeperSpell::new);
    public static final DeferredHolder<AbstractSkill, ShieldSpell> SHIELD_SPELL =
            registerSkill("shield", ShieldSpell::new);
    public static final DeferredHolder<AbstractSkill, SlowSpell> SLOW_SPELL =
            registerSkill("slow", SlowSpell::new);
    public static final DeferredHolder<AbstractSkill, SpectralHammerSpell> SPECTRAL_HAMMER_SPELL =
            registerSkill("spectral_hammer", SpectralHammerSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonHorseSpell> SUMMON_HORSE_SPELL =
            registerSkill("summon_horse", SummonHorseSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonVexSpell> SUMMON_VEX_SPELL =
            registerSkill("summon_vex", SummonVexSpell::new);
    public static final DeferredHolder<AbstractSkill, ThrowSpell> THROW_SPELL =
            registerSkill("throw", ThrowSpell::new);
    public static final DeferredHolder<AbstractSkill, WololoSpell> WOLOLO_SPELL =
            registerSkill("wololo", WololoSpell::new);

    public static final DeferredHolder<AbstractSkill, ScapegoatSpell> SCAPEGOAT_SPELL =
            registerSkill("scapegoat", ScapegoatSpell::new);

    public static final DeferredHolder<AbstractSkill, AbyssalShroudSpell> ABYSSAL_SHROUD_SPELL =
            registerSkill("abyssal_shroud", AbyssalShroudSpell::new);
    public static final DeferredHolder<AbstractSkill, SculkTentaclesSpell> SCULK_TENTACLES_SPELL =
            registerSkill("sculk_tentacles", SculkTentaclesSpell::new);
    public static final DeferredHolder<AbstractSkill, SonicBoomSpell> SONIC_BOOM_SPELL =
            registerSkill("sonic_boom", SonicBoomSpell::new);
    public static final DeferredHolder<AbstractSkill, PlanarSightSpell> PLANAR_SIGHT_SPELL =
            registerSkill("planar_sight", PlanarSightSpell::new);
    public static final DeferredHolder<AbstractSkill, TelekinesisSpell> TELEKINESIS_SPELL =
            registerSkill("telekinesis", TelekinesisSpell::new);
    public static final DeferredHolder<AbstractSkill, EldritchBlastSpell> ELDRITCH_BLAST_SPELL =
            registerSkill("eldritch_blast", EldritchBlastSpell::new);
    public static final DeferredHolder<AbstractSkill, PocketDimensionSpell> POCKET_DIMENSION_SPELL =
            registerSkill("pocket_dimension", PocketDimensionSpell::new);

    public static final DeferredHolder<AbstractSkill, AcupunctureSpell> ACUPUNCTURE_SPELL =
            registerSkill("acupuncture", AcupunctureSpell::new);
    public static final DeferredHolder<AbstractSkill, BloodNeedlesSpell> BLOOD_NEEDLES_SPELL =
            registerSkill("blood_needles", BloodNeedlesSpell::new);
    public static final DeferredHolder<AbstractSkill, BloodSlashSpell> BLOOD_SLASH_SPELL =
            registerSkill("blood_slash", BloodSlashSpell::new);
    public static final DeferredHolder<AbstractSkill, BloodStepSpell> BLOOD_STEP_SPELL =
            registerSkill("blood_step", BloodStepSpell::new);
    public static final DeferredHolder<AbstractSkill, DevourSpell> DEVOUR_SPELL =
            registerSkill("devour", DevourSpell::new);
    public static final DeferredHolder<AbstractSkill, HeartstopSpell> HEARTSTOP_SPELL =
            registerSkill("heartstop", HeartstopSpell::new);
    public static final DeferredHolder<AbstractSkill, RaiseDeadSpell> RAISE_DEAD_SPELL =
            registerSkill("raise_dead", RaiseDeadSpell::new);
    public static final DeferredHolder<AbstractSkill, RayOfSiphoningSpell> RAY_OF_SIPHONING_SPELL =
            registerSkill("ray_of_siphoning", RayOfSiphoningSpell::new);
    public static final DeferredHolder<AbstractSkill, SacrificeSpell> SACRIFICE_SPELL =
            registerSkill("sacrifice", SacrificeSpell::new);
    public static final DeferredHolder<AbstractSkill, WitherSkullSpell> WITHER_SKULL_SPELL =
            registerSkill("wither_skull", WitherSkullSpell::new);

    public static final DeferredHolder<AbstractSkill, MagicMissileSpell> MAGIC_MISSILE_SPELL =
            registerSkill("magic_missile", MagicMissileSpell::new);
    public static final DeferredHolder<AbstractSkill, MagicArrowSpell> MAGIC_ARROW_SPELL =
            registerSkill("magic_arrow", MagicArrowSpell::new);
    public static final DeferredHolder<AbstractSkill, TeleportSpell> TELEPORT_SPELL =
            registerSkill("teleport", TeleportSpell::new);
    public static final DeferredHolder<AbstractSkill, CounterspellSpell> COUNTERSPELL_SPELL =
            registerSkill("counterspell", CounterspellSpell::new);
    public static final DeferredHolder<AbstractSkill, EvasionSpell> EVASION_SPELL =
            registerSkill("evasion", EvasionSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonEnderChestSpell> SUMMON_ENDER_CHEST_SPELL =
            registerSkill("summon_ender_chest", SummonEnderChestSpell::new);
    public static final DeferredHolder<AbstractSkill, StarfallSpell> STARFALL_SPELL =
            registerSkill("starfall", StarfallSpell::new);
    public static final DeferredHolder<AbstractSkill, PortalSpell> PORTAL_SPELL =
            registerSkill("portal", PortalSpell::new);
    public static final DeferredHolder<AbstractSkill, RecallSpell> RECALL_SPELL =
            registerSkill("recall", RecallSpell::new);
    public static final DeferredHolder<AbstractSkill, DragonBreathSpell> DRAGON_BREATH_SPELL =
            registerSkill("dragon_breath", DragonBreathSpell::new);
    public static final DeferredHolder<AbstractSkill, ArcaneShackleSpell> ARCANE_SHACKLE_SPELL =
            registerSkill("arcane_shackle", ArcaneShackleSpell::new);
    public static final DeferredHolder<AbstractSkill, BlackHoleSpell> BLACK_HOLE_SPELL =
            registerSkill("black_hole", BlackHoleSpell::new);
    public static final DeferredHolder<AbstractSkill, EchoingStrikesSpell> ECHOING_STRIKES_SPELL =
            registerSkill("echoing_strikes", EchoingStrikesSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonSwordsSpell> SUMMON_SWORDS_SPELL =
            registerSkill("summon_swords", SummonSwordsSpell::new);
    public static final DeferredHolder<AbstractSkill, ShadowSlashSpell> SHADOW_SLASH_SPELL =
            registerSkill("shadow_slash", ShadowSlashSpell::new);
    public static final DeferredHolder<AbstractSkill, GravityFissureSpell> GRAVITY_FISSURE_SPELL =
            registerSkill("gravity_fissure", GravityFissureSpell::new);

    public static final DeferredHolder<AbstractSkill, HealSpell> HEAL_SPELL =
            registerSkill("heal", HealSpell::new);
    public static final DeferredHolder<AbstractSkill, GreaterHealSpell> GREATER_HEAL_SPELL =
            registerSkill("greater_heal", GreaterHealSpell::new);
    public static final DeferredHolder<AbstractSkill, BlessingOfLifeSpell> BLESSING_OF_LIFE_SPELL =
            registerSkill("blessing_of_life", BlessingOfLifeSpell::new);
    public static final DeferredHolder<AbstractSkill, CleanseSpell> CLEANSE_SPELL =
            registerSkill("cleanse", CleanseSpell::new);
    public static final DeferredHolder<AbstractSkill, CloudOfRegenerationSpell> CLOUD_OF_REGENERATION_SPELL =
            registerSkill("cloud_of_regeneration", CloudOfRegenerationSpell::new);
    public static final DeferredHolder<AbstractSkill, DivineSmiteSpell> DIVINE_SMITE_SPELL =
            registerSkill("divine_smite", DivineSmiteSpell::new);
    public static final DeferredHolder<AbstractSkill, FortifySpell> FORTIFY_SPELL =
            registerSkill("fortify", FortifySpell::new);
    public static final DeferredHolder<AbstractSkill, GuidingBoltSpell> GUIDING_BOLT_SPELL =
            registerSkill("guiding_bolt", GuidingBoltSpell::new);
    public static final DeferredHolder<AbstractSkill, HasteSpell> HASTE_SPELL =
            registerSkill("haste", HasteSpell::new);
    public static final DeferredHolder<AbstractSkill, HealingCircleSpell> HEALING_CIRCLE_SPELL =
            registerSkill("healing_circle", HealingCircleSpell::new);
    public static final DeferredHolder<AbstractSkill, SunbeamSpell> SUNBEAM_SPELL =
            registerSkill("sunbeam", SunbeamSpell::new);
    public static final DeferredHolder<AbstractSkill, AngelWingsSpell> ANGEL_WINGS_SPELL =
            registerSkill("angel_wing", AngelWingsSpell::new);
    public static final DeferredHolder<AbstractSkill, WispSpell> WISP_SPELL =
            registerSkill("wisp", WispSpell::new);

    public static final DeferredHolder<AbstractSkill, AscensionSpell> ASCENSION_SPELL =
            registerSkill("ascension", AscensionSpell::new);
    public static final DeferredHolder<AbstractSkill, BallLightningSpell> BALL_LIGHTNING_SPELL =
            registerSkill("ball_lightning", BallLightningSpell::new);
    public static final DeferredHolder<AbstractSkill, ChainLightningSpell> CHAIN_LIGHTNING_SPELL =
            registerSkill("chain_lightning", ChainLightningSpell::new);
    public static final DeferredHolder<AbstractSkill, ChargeSpell> CHARGE_SPELL =
            registerSkill("charge", ChargeSpell::new);
    public static final DeferredHolder<AbstractSkill, ElectrocuteSpell> ELECTROCUTE_SPELL =
            registerSkill("electrocute", ElectrocuteSpell::new);
    public static final DeferredHolder<AbstractSkill, LightningBoltSpell> LIGHTNING_BOLT_SPELL =
            registerSkill("lightning_bolt", LightningBoltSpell::new);
    public static final DeferredHolder<AbstractSkill, LightningLanceSpell> LIGHTNING_LANCE_SPELL =
            registerSkill("lightning_lance", LightningLanceSpell::new);
    public static final DeferredHolder<AbstractSkill, ShockwaveSpell> SHOCKWAVE_SPELL =
            registerSkill("shockwave", ShockwaveSpell::new);
    public static final DeferredHolder<AbstractSkill, ThunderstormSpell> THUNDERSTORM_SPELL =
            registerSkill("thunderstorm", ThunderstormSpell::new);
    public static final DeferredHolder<AbstractSkill, VoltStrikeSpell> VOLT_STRIKE_SPELL =
            registerSkill("volt_strike", VoltStrikeSpell::new);

    public static final DeferredHolder<AbstractSkill, AcidOrbSpell> ACID_ORB_SPELL =
            registerSkill("acid_orb", AcidOrbSpell::new);
    public static final DeferredHolder<AbstractSkill, BlightSpell> BLIGHT_SPELL =
            registerSkill("blight", BlightSpell::new);
    public static final DeferredHolder<AbstractSkill, EarthquakeSpell> EARTHQUAKE_SPELL =
            registerSkill("earthquake", EarthquakeSpell::new);
    public static final DeferredHolder<AbstractSkill, FireflySwarmSpell> FIREFLY_SWARM_SPELL =
            registerSkill("firefly_swarm", FireflySwarmSpell::new);
    public static final DeferredHolder<AbstractSkill, GluttonySpell> GLUTTONY_SPELL =
            registerSkill("gluttony", GluttonySpell::new);
    public static final DeferredHolder<AbstractSkill, OakskinSpell> OAKSKIN_SPELL =
            registerSkill("oakskin", OakskinSpell::new);
    public static final DeferredHolder<AbstractSkill, PoisonArrowSpell> POISON_ARROW_SPELL =
            registerSkill("poison_arrow", PoisonArrowSpell::new);
    public static final DeferredHolder<AbstractSkill, PoisonBreathSpell> POISON_BREATH_SPELL =
            registerSkill("poison_breath", PoisonBreathSpell::new);
    public static final DeferredHolder<AbstractSkill, PoisonSplashSpell> POISON_SPLASH_SPELL =
            registerSkill("poison_splash", PoisonSplashSpell::new);
    public static final DeferredHolder<AbstractSkill, RootSpell> ROOT_SPELL =
            registerSkill("root", RootSpell::new);
    public static final DeferredHolder<AbstractSkill, SpiderAspectSpell> SPIDER_ASPECT_SPELL =
            registerSkill("spider_aspect", SpiderAspectSpell::new);
    public static final DeferredHolder<AbstractSkill, StompSpell> STOMP_SPELL =
            registerSkill("stomp", StompSpell::new);
    public static final DeferredHolder<AbstractSkill, TouchDigSpell> TOUCH_DIG_SPELL =
            registerSkill("touch_dig", TouchDigSpell::new);
}
