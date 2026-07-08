package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
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
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.AbyssalShroudSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.EldritchBlastSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.PlanarSightSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.PocketDimensionSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.SculkTentaclesSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.SonicBoomSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.eldritch.TelekinesisSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.ArcaneShackleSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.BlackHoleSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.CounterspellSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.DragonBreathSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.EchoingStrikesSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.EvasionSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.GravityFissureSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.MagicArrowSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.MagicMissileSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.PortalSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.RecallSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.ShadowSlashSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.StarfallSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.SummonEnderChestSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.SummonSwordsSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.ender.TeleportSpell;
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
import io.redspace.skillcasting.irons_spellbooks.spells.fire.BlazeStormSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.BurningDashSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireArrowSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireBreathSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireballSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FireboltSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FlamingBarrageSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.FlamingStrikeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.HeatSurgeSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.MagmaBombSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.RaiseHellSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.ScorchSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.fire.WallOfFireSpell;
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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.redspace.skillcasting.registry.SkillcastingRegistries.SKILL_REGISTRY;

public class SpellRegistry {
    private static final DeferredRegister<AbstractSkill> SPELLS = DeferredRegister.create(SKILL_REGISTRY, IronsSpellbooks.MODID);

    private static final Map<SchoolType, List<AbstractSpellSkill>> SCHOOLS_TO_SPELLS = new HashMap<>();

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    @Nullable
    public static AbstractSpellSkill getSpell(ResourceLocation spellId) {
        return SKILL_REGISTRY.get(spellId) instanceof AbstractSpellSkill spell ? spell : null;
    }

    public static boolean containsKey(ResourceLocation spellId) {
        return getSpell(spellId) != null;
    }

    public static List<AbstractSpellSkill> getEnabledSpells() {
        return SKILL_REGISTRY.stream()
                .filter(AbstractSpellSkill.class::isInstance)
                .map(AbstractSpellSkill.class::cast)
                .filter(AbstractSpellSkill::isEnabled)
                .collect(Collectors.toList());
    }

    public static List<AbstractSpellSkill> getAllSpells() {
        return SKILL_REGISTRY.stream()
                .filter(AbstractSpellSkill.class::isInstance)
                .map(AbstractSpellSkill.class::cast)
                .collect(Collectors.toList());
    }

    public static List<AbstractSpellSkill> getSpellsForSchool(SchoolType schoolType) {
        return SCHOOLS_TO_SPELLS.computeIfAbsent(schoolType, (school) -> SKILL_REGISTRY
                .stream()
                .filter(AbstractSpellSkill.class::isInstance)
                .map(AbstractSpellSkill.class::cast)
                .filter(spell -> spell.getSchoolType() == school)
                .collect(Collectors.toList()));
    }

    public static void onConfigReload() {
        SCHOOLS_TO_SPELLS.clear();
        SKILL_REGISTRY.stream()
                .filter(AbstractSpellSkill.class::isInstance)
                .map(AbstractSpellSkill.class::cast)
                .forEach(AbstractSpellSkill::resetRarityWeights);
    }

    public static final DeferredHolder<AbstractSkill, IcicleSpell> ICICLE_SPELL =
            registerSpell("icicle", IcicleSpell::new);
    public static final DeferredHolder<AbstractSkill, ConeOfColdSpell> CONE_OF_COLD_SPELL =
            registerSpell("cone_of_cold", ConeOfColdSpell::new);
    public static final DeferredHolder<AbstractSkill, FrostbiteSpell> FROSTBITE_SPELL =
            registerSpell("frostbite", FrostbiteSpell::new);
    public static final DeferredHolder<AbstractSkill, FrostStepSpell> FROST_STEP_SPELL =
            registerSpell("frost_step", FrostStepSpell::new);
    public static final DeferredHolder<AbstractSkill, FrostwaveSpell> FROSTWAVE_SPELL =
            registerSpell("frostwave", FrostwaveSpell::new);
    public static final DeferredHolder<AbstractSkill, IceBlockSpell> ICE_BLOCK_SPELL =
            registerSpell("ice_block", IceBlockSpell::new);
    public static final DeferredHolder<AbstractSkill, IceSpikesSpell> ICE_SPIKES_SPELL =
            registerSpell("ice_spikes", IceSpikesSpell::new);
    public static final DeferredHolder<AbstractSkill, IceTombSpell> ICE_TOMB_SPELL =
            registerSpell("ice_tomb", IceTombSpell::new);
    public static final DeferredHolder<AbstractSkill, RayOfFrostSpell> RAY_OF_FROST_SPELL =
            registerSpell("ray_of_frost", RayOfFrostSpell::new);
    public static final DeferredHolder<AbstractSkill, SnowballSpell> SNOWBALL_SPELL =
            registerSpell("snowball", SnowballSpell::new);
    public static final DeferredHolder<AbstractSkill, BlizzardSpell> BLIZZARD_SPELL =
            registerSpell("blizzard", BlizzardSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonPolarBearSpell> SUMMON_POLAR_BEAR_SPELL =
            registerSpell("summon_polar_bear", SummonPolarBearSpell::new);

    public static final DeferredHolder<AbstractSkill, BlazeStormSpell> BLAZE_STORM_SPELL =
            registerSpell("blaze_storm", BlazeStormSpell::new);
    public static final DeferredHolder<AbstractSkill, BurningDashSpell> BURNING_DASH_SPELL =
            registerSpell("burning_dash", BurningDashSpell::new);
    public static final DeferredHolder<AbstractSkill, FireArrowSpell> FIRE_ARROW_SPELL =
            registerSpell("fire_arrow", FireArrowSpell::new);
    public static final DeferredHolder<AbstractSkill, FireballSpell> FIREBALL_SPELL =
            registerSpell("fireball", FireballSpell::new);
    public static final DeferredHolder<AbstractSkill, FireboltSpell> FIREBOLT_SPELL =
            registerSpell("firebolt", FireboltSpell::new);
    public static final DeferredHolder<AbstractSkill, FireBreathSpell> FIRE_BREATH_SPELL =
            registerSpell("fire_breath", FireBreathSpell::new);
    public static final DeferredHolder<AbstractSkill, FlamingBarrageSpell> FLAMING_BARRAGE_SPELL =
            registerSpell("flaming_barrage", FlamingBarrageSpell::new);
    public static final DeferredHolder<AbstractSkill, FlamingStrikeSpell> FLAMING_STRIKE_SPELL =
            registerSpell("flaming_strike", FlamingStrikeSpell::new);
    public static final DeferredHolder<AbstractSkill, HeatSurgeSpell> HEAT_SURGE_SPELL =
            registerSpell("heat_surge", HeatSurgeSpell::new);
    public static final DeferredHolder<AbstractSkill, MagmaBombSpell> MAGMA_BOMB_SPELL =
            registerSpell("magma_bomb", MagmaBombSpell::new);
    public static final DeferredHolder<AbstractSkill, RaiseHellSpell> RAISE_HELL_SPELL =
            registerSpell("raise_hell", RaiseHellSpell::new);
    public static final DeferredHolder<AbstractSkill, ScorchSpell> SCORCH_SPELL =
            registerSpell("scorch", ScorchSpell::new);
    public static final DeferredHolder<AbstractSkill, WallOfFireSpell> WALL_OF_FIRE_SPELL =
            registerSpell("wall_of_fire", WallOfFireSpell::new);

    public static final DeferredHolder<AbstractSkill, ArrowVolleySpell> ARROW_VOLLEY_SPELL =
            registerSpell("arrow_volley", ArrowVolleySpell::new);
    public static final DeferredHolder<AbstractSkill, ChainCreeperSpell> CHAIN_CREEPER_SPELL =
            registerSpell("chain_creeper", ChainCreeperSpell::new);
    public static final DeferredHolder<AbstractSkill, FangStrikeSpell> FANG_STRIKE_SPELL =
            registerSpell("fang_strike", FangStrikeSpell::new);
    public static final DeferredHolder<AbstractSkill, FangSwirlSpell> FANG_SWIRL_SPELL =
            registerSpell("fang_swirl", FangSwirlSpell::new);
    public static final DeferredHolder<AbstractSkill, FangWardSpell> FANG_WARD_SPELL =
            registerSpell("fang_ward", FangWardSpell::new);
    public static final DeferredHolder<AbstractSkill, FirecrackerSpell> FIRECRACKER_SPELL =
            registerSpell("firecracker", FirecrackerSpell::new);
    public static final DeferredHolder<AbstractSkill, GustSpell> GUST_SPELL =
            registerSpell("gust", GustSpell::new);
    public static final DeferredHolder<AbstractSkill, InvisibilitySpell> INVISIBILITY_SPELL =
            registerSpell("invisibility", InvisibilitySpell::new);
    public static final DeferredHolder<AbstractSkill, LobCreeperSpell> LOB_CREEPER_SPELL =
            registerSpell("lob_creeper", LobCreeperSpell::new);
    public static final DeferredHolder<AbstractSkill, ShieldSpell> SHIELD_SPELL =
            registerSpell("shield", ShieldSpell::new);
    public static final DeferredHolder<AbstractSkill, SlowSpell> SLOW_SPELL =
            registerSpell("slow", SlowSpell::new);
    public static final DeferredHolder<AbstractSkill, SpectralHammerSpell> SPECTRAL_HAMMER_SPELL =
            registerSpell("spectral_hammer", SpectralHammerSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonHorseSpell> SUMMON_HORSE_SPELL =
            registerSpell("summon_horse", SummonHorseSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonVexSpell> SUMMON_VEX_SPELL =
            registerSpell("summon_vex", SummonVexSpell::new);
    public static final DeferredHolder<AbstractSkill, ThrowSpell> THROW_SPELL =
            registerSpell("throw", ThrowSpell::new);
    public static final DeferredHolder<AbstractSkill, WololoSpell> WOLOLO_SPELL =
            registerSpell("wololo", WololoSpell::new);
    public static final DeferredHolder<AbstractSkill, ScapegoatSpell> SCAPEGOAT_SPELL =
            registerSpell("scapegoat", ScapegoatSpell::new);

    public static final DeferredHolder<AbstractSkill, AbyssalShroudSpell> ABYSSAL_SHROUD_SPELL =
            registerSpell("abyssal_shroud", AbyssalShroudSpell::new);
    public static final DeferredHolder<AbstractSkill, SculkTentaclesSpell> SCULK_TENTACLES_SPELL =
            registerSpell("sculk_tentacles", SculkTentaclesSpell::new);
    public static final DeferredHolder<AbstractSkill, SonicBoomSpell> SONIC_BOOM_SPELL =
            registerSpell("sonic_boom", SonicBoomSpell::new);
    public static final DeferredHolder<AbstractSkill, PlanarSightSpell> PLANAR_SIGHT_SPELL =
            registerSpell("planar_sight", PlanarSightSpell::new);
    public static final DeferredHolder<AbstractSkill, TelekinesisSpell> TELEKINESIS_SPELL =
            registerSpell("telekinesis", TelekinesisSpell::new);
    public static final DeferredHolder<AbstractSkill, EldritchBlastSpell> ELDRITCH_BLAST_SPELL =
            registerSpell("eldritch_blast", EldritchBlastSpell::new);
    public static final DeferredHolder<AbstractSkill, PocketDimensionSpell> POCKET_DIMENSION_SPELL =
            registerSpell("pocket_dimension", PocketDimensionSpell::new);

    public static final DeferredHolder<AbstractSkill, AcupunctureSpell> ACUPUNCTURE_SPELL =
            registerSpell("acupuncture", AcupunctureSpell::new);
    public static final DeferredHolder<AbstractSkill, BloodNeedlesSpell> BLOOD_NEEDLES_SPELL =
            registerSpell("blood_needles", BloodNeedlesSpell::new);
    public static final DeferredHolder<AbstractSkill, BloodSlashSpell> BLOOD_SLASH_SPELL =
            registerSpell("blood_slash", BloodSlashSpell::new);
    public static final DeferredHolder<AbstractSkill, BloodStepSpell> BLOOD_STEP_SPELL =
            registerSpell("blood_step", BloodStepSpell::new);
    public static final DeferredHolder<AbstractSkill, DevourSpell> DEVOUR_SPELL =
            registerSpell("devour", DevourSpell::new);
    public static final DeferredHolder<AbstractSkill, HeartstopSpell> HEARTSTOP_SPELL =
            registerSpell("heartstop", HeartstopSpell::new);
    public static final DeferredHolder<AbstractSkill, RaiseDeadSpell> RAISE_DEAD_SPELL =
            registerSpell("raise_dead", RaiseDeadSpell::new);
    public static final DeferredHolder<AbstractSkill, RayOfSiphoningSpell> RAY_OF_SIPHONING_SPELL =
            registerSpell("ray_of_siphoning", RayOfSiphoningSpell::new);
    public static final DeferredHolder<AbstractSkill, SacrificeSpell> SACRIFICE_SPELL =
            registerSpell("sacrifice", SacrificeSpell::new);
    public static final DeferredHolder<AbstractSkill, WitherSkullSpell> WITHER_SKULL_SPELL =
            registerSpell("wither_skull", WitherSkullSpell::new);

    public static final DeferredHolder<AbstractSkill, MagicMissileSpell> MAGIC_MISSILE_SPELL =
            registerSpell("magic_missile", MagicMissileSpell::new);
    public static final DeferredHolder<AbstractSkill, MagicArrowSpell> MAGIC_ARROW_SPELL =
            registerSpell("magic_arrow", MagicArrowSpell::new);
    public static final DeferredHolder<AbstractSkill, TeleportSpell> TELEPORT_SPELL =
            registerSpell("teleport", TeleportSpell::new);
    public static final DeferredHolder<AbstractSkill, CounterspellSpell> COUNTERSPELL_SPELL =
            registerSpell("counterspell", CounterspellSpell::new);
    public static final DeferredHolder<AbstractSkill, EvasionSpell> EVASION_SPELL =
            registerSpell("evasion", EvasionSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonEnderChestSpell> SUMMON_ENDER_CHEST_SPELL =
            registerSpell("summon_ender_chest", SummonEnderChestSpell::new);
    public static final DeferredHolder<AbstractSkill, StarfallSpell> STARFALL_SPELL =
            registerSpell("starfall", StarfallSpell::new);
    public static final DeferredHolder<AbstractSkill, PortalSpell> PORTAL_SPELL =
            registerSpell("portal", PortalSpell::new);
    public static final DeferredHolder<AbstractSkill, RecallSpell> RECALL_SPELL =
            registerSpell("recall", RecallSpell::new);
    public static final DeferredHolder<AbstractSkill, DragonBreathSpell> DRAGON_BREATH_SPELL =
            registerSpell("dragon_breath", DragonBreathSpell::new);
    public static final DeferredHolder<AbstractSkill, ArcaneShackleSpell> ARCANE_SHACKLE_SPELL =
            registerSpell("arcane_shackle", ArcaneShackleSpell::new);
    public static final DeferredHolder<AbstractSkill, BlackHoleSpell> BLACK_HOLE_SPELL =
            registerSpell("black_hole", BlackHoleSpell::new);
    public static final DeferredHolder<AbstractSkill, EchoingStrikesSpell> ECHOING_STRIKES_SPELL =
            registerSpell("echoing_strikes", EchoingStrikesSpell::new);
    public static final DeferredHolder<AbstractSkill, SummonSwordsSpell> SUMMON_SWORDS_SPELL =
            registerSpell("summon_swords", SummonSwordsSpell::new);
    public static final DeferredHolder<AbstractSkill, ShadowSlashSpell> SHADOW_SLASH_SPELL =
            registerSpell("shadow_slash", ShadowSlashSpell::new);
    public static final DeferredHolder<AbstractSkill, GravityFissureSpell> GRAVITY_FISSURE_SPELL =
            registerSpell("gravity_fissure", GravityFissureSpell::new);

    public static final DeferredHolder<AbstractSkill, HealSpell> HEAL_SPELL =
            registerSpell("heal", HealSpell::new);
    public static final DeferredHolder<AbstractSkill, GreaterHealSpell> GREATER_HEAL_SPELL =
            registerSpell("greater_heal", GreaterHealSpell::new);
    public static final DeferredHolder<AbstractSkill, BlessingOfLifeSpell> BLESSING_OF_LIFE_SPELL =
            registerSpell("blessing_of_life", BlessingOfLifeSpell::new);
    public static final DeferredHolder<AbstractSkill, CleanseSpell> CLEANSE_SPELL =
            registerSpell("cleanse", CleanseSpell::new);
    public static final DeferredHolder<AbstractSkill, CloudOfRegenerationSpell> CLOUD_OF_REGENERATION_SPELL =
            registerSpell("cloud_of_regeneration", CloudOfRegenerationSpell::new);
    public static final DeferredHolder<AbstractSkill, DivineSmiteSpell> DIVINE_SMITE_SPELL =
            registerSpell("divine_smite", DivineSmiteSpell::new);
    public static final DeferredHolder<AbstractSkill, FortifySpell> FORTIFY_SPELL =
            registerSpell("fortify", FortifySpell::new);
    public static final DeferredHolder<AbstractSkill, GuidingBoltSpell> GUIDING_BOLT_SPELL =
            registerSpell("guiding_bolt", GuidingBoltSpell::new);
    public static final DeferredHolder<AbstractSkill, HasteSpell> HASTE_SPELL =
            registerSpell("haste", HasteSpell::new);
    public static final DeferredHolder<AbstractSkill, HealingCircleSpell> HEALING_CIRCLE_SPELL =
            registerSpell("healing_circle", HealingCircleSpell::new);
    public static final DeferredHolder<AbstractSkill, SunbeamSpell> SUNBEAM_SPELL =
            registerSpell("sunbeam", SunbeamSpell::new);
    public static final DeferredHolder<AbstractSkill, AngelWingsSpell> ANGEL_WINGS_SPELL =
            registerSpell("angel_wing", AngelWingsSpell::new);
    public static final DeferredHolder<AbstractSkill, WispSpell> WISP_SPELL =
            registerSpell("wisp", WispSpell::new);

    public static final DeferredHolder<AbstractSkill, AscensionSpell> ASCENSION_SPELL =
            registerSpell("ascension", AscensionSpell::new);
    public static final DeferredHolder<AbstractSkill, BallLightningSpell> BALL_LIGHTNING_SPELL =
            registerSpell("ball_lightning", BallLightningSpell::new);
    public static final DeferredHolder<AbstractSkill, ChainLightningSpell> CHAIN_LIGHTNING_SPELL =
            registerSpell("chain_lightning", ChainLightningSpell::new);
    public static final DeferredHolder<AbstractSkill, ChargeSpell> CHARGE_SPELL =
            registerSpell("charge", ChargeSpell::new);
    public static final DeferredHolder<AbstractSkill, ElectrocuteSpell> ELECTROCUTE_SPELL =
            registerSpell("electrocute", ElectrocuteSpell::new);
    public static final DeferredHolder<AbstractSkill, LightningBoltSpell> LIGHTNING_BOLT_SPELL =
            registerSpell("lightning_bolt", LightningBoltSpell::new);
    public static final DeferredHolder<AbstractSkill, LightningLanceSpell> LIGHTNING_LANCE_SPELL =
            registerSpell("lightning_lance", LightningLanceSpell::new);
    public static final DeferredHolder<AbstractSkill, ShockwaveSpell> SHOCKWAVE_SPELL =
            registerSpell("shockwave", ShockwaveSpell::new);
    public static final DeferredHolder<AbstractSkill, ThunderstormSpell> THUNDERSTORM_SPELL =
            registerSpell("thunderstorm", ThunderstormSpell::new);
    public static final DeferredHolder<AbstractSkill, VoltStrikeSpell> VOLT_STRIKE_SPELL =
            registerSpell("volt_strike", VoltStrikeSpell::new);

    public static final DeferredHolder<AbstractSkill, AcidOrbSpell> ACID_ORB_SPELL =
            registerSpell("acid_orb", AcidOrbSpell::new);
    public static final DeferredHolder<AbstractSkill, BlightSpell> BLIGHT_SPELL =
            registerSpell("blight", BlightSpell::new);
    public static final DeferredHolder<AbstractSkill, EarthquakeSpell> EARTHQUAKE_SPELL =
            registerSpell("earthquake", EarthquakeSpell::new);
    public static final DeferredHolder<AbstractSkill, FireflySwarmSpell> FIREFLY_SWARM_SPELL =
            registerSpell("firefly_swarm", FireflySwarmSpell::new);
    public static final DeferredHolder<AbstractSkill, GluttonySpell> GLUTTONY_SPELL =
            registerSpell("gluttony", GluttonySpell::new);
    public static final DeferredHolder<AbstractSkill, OakskinSpell> OAKSKIN_SPELL =
            registerSpell("oakskin", OakskinSpell::new);
    public static final DeferredHolder<AbstractSkill, PoisonArrowSpell> POISON_ARROW_SPELL =
            registerSpell("poison_arrow", PoisonArrowSpell::new);
    public static final DeferredHolder<AbstractSkill, PoisonBreathSpell> POISON_BREATH_SPELL =
            registerSpell("poison_breath", PoisonBreathSpell::new);
    public static final DeferredHolder<AbstractSkill, PoisonSplashSpell> POISON_SPLASH_SPELL =
            registerSpell("poison_splash", PoisonSplashSpell::new);
    public static final DeferredHolder<AbstractSkill, RootSpell> ROOT_SPELL =
            registerSpell("root", RootSpell::new);
    public static final DeferredHolder<AbstractSkill, SpiderAspectSpell> SPIDER_ASPECT_SPELL =
            registerSpell("spider_aspect", SpiderAspectSpell::new);
    public static final DeferredHolder<AbstractSkill, StompSpell> STOMP_SPELL =
            registerSpell("stomp", StompSpell::new);
    public static final DeferredHolder<AbstractSkill, TouchDigSpell> TOUCH_DIG_SPELL =
            registerSpell("touch_dig", TouchDigSpell::new);

    private static <T extends AbstractSpellSkill> DeferredHolder<AbstractSkill, T> registerSpell(String name, Supplier<T> skill) {
        return SPELLS.register(name, skill);
    }
}
