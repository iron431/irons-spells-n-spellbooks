package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.NoneSpell;
import io.redspace.ironsspellbooks.spells.blood.*;
import io.redspace.ironsspellbooks.spells.holy.*;
import io.redspace.ironsspellbooks.spells.ender.*;
import io.redspace.ironsspellbooks.spells.evocation.*;
import io.redspace.ironsspellbooks.spells.fire.*;
import io.redspace.ironsspellbooks.spells.ice.*;
import io.redspace.ironsspellbooks.spells.lightning.*;
import io.redspace.ironsspellbooks.spells.nature.*;
import io.redspace.ironsspellbooks.spells.eldritch.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpellRegistry {
    public static final ResourceKey<Registry<AbstractSpell>> SPELL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "spells"));
    private static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SPELL_REGISTRY_KEY, IronsSpellbooks.MODID);
    public static final Supplier<IForgeRegistry<AbstractSpell>> REGISTRY = SPELLS.makeRegistry(() -> new RegistryBuilder<AbstractSpell>().disableSaving().disableOverrides());
    private static final NoneSpell noneSpell = new NoneSpell();
    private static final Map<SchoolType, List<AbstractSpell>> SCHOOLS_TO_SPELLS = new HashMap<>();
    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    public static NoneSpell none() {
        return noneSpell;
    }

    private static RegistryObject<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static AbstractSpell getSpell(String spellId) {
        return getSpell(ResourceLocation.parse(spellId));
    }

    public static List<AbstractSpell> getEnabledSpells() {
        return SpellRegistry.REGISTRY.get()
                .getValues()
                .stream()
                .filter(AbstractSpell::isEnabled)
                .toList();
    }

    public static List<AbstractSpell> getSpellsForSchool(SchoolType schoolType) {
        return SCHOOLS_TO_SPELLS.computeIfAbsent(schoolType, (school) -> SpellRegistry.REGISTRY.get()
                .getValues()
                .stream()
                .filter(spell -> spell.getSchoolType() == school).collect(Collectors.toList()));
    }

    public static AbstractSpell getSpell(ResourceLocation resourceLocation) {
        var spell = REGISTRY.get().getValue(resourceLocation);
        if (spell == null) {
            return noneSpell;
        }
        return spell;
    }

    public static void onConfigReload() {
        SCHOOLS_TO_SPELLS.clear();
    }

    //TODO: should the none spell be registered?

    // BLOOD
    public static final RegistryObject<AbstractSpell> ACUPUNCTURE_SPELL = registerSpell(new AcupunctureSpell());
    public static final RegistryObject<AbstractSpell> BLOOD_NEEDLES_SPELL = registerSpell(new BloodNeedlesSpell());
    public static final RegistryObject<AbstractSpell> BLOOD_SLASH_SPELL = registerSpell(new BloodSlashSpell());
    public static final RegistryObject<AbstractSpell> BLOOD_STEP_SPELL = registerSpell(new BloodStepSpell());
    public static final RegistryObject<AbstractSpell> DEVOUR_SPELL = registerSpell(new DevourSpell());
    public static final RegistryObject<AbstractSpell> HEARTSTOP_SPELL = registerSpell(new HeartstopSpell());
    public static final RegistryObject<AbstractSpell> RAISE_DEAD_SPELL = registerSpell(new RaiseDeadSpell());
    public static final RegistryObject<AbstractSpell> RAY_OF_SIPHONING_SPELL = registerSpell(new RayOfSiphoningSpell());
    public static final RegistryObject<AbstractSpell> WITHER_SKULL_SPELL = registerSpell(new WitherSkullSpell());
    public static final RegistryObject<AbstractSpell> SACRIFICE_SPELL = registerSpell(new SacrificeSpell());

    // ENDER
    public static final RegistryObject<AbstractSpell> COUNTERSPELL_SPELL = registerSpell(new CounterspellSpell());
    public static final RegistryObject<AbstractSpell> DRAGON_BREATH_SPELL = registerSpell(new DragonBreathSpell());
    public static final RegistryObject<AbstractSpell> EVASION_SPELL = registerSpell(new EvasionSpell());
    public static final RegistryObject<AbstractSpell> MAGIC_ARROW_SPELL = registerSpell(new MagicArrowSpell());
    public static final RegistryObject<AbstractSpell> MAGIC_MISSILE_SPELL = registerSpell(new MagicMissileSpell());
    public static final RegistryObject<AbstractSpell> STARFALL_SPELL = registerSpell(new StarfallSpell());
    public static final RegistryObject<AbstractSpell> TELEPORT_SPELL = registerSpell(new TeleportSpell());
    public static final RegistryObject<AbstractSpell> SUMMON_ENDER_CHEST_SPELL = registerSpell(new SummonEnderChestSpell());
    public static final RegistryObject<AbstractSpell> RECALL_SPELL = registerSpell(new RecallSpell());
    public static final RegistryObject<AbstractSpell> PORTAL_SPELL = registerSpell(new PortalSpell());
    public static final RegistryObject<AbstractSpell> ECHOING_STRIKES_SPELL = registerSpell(new EchoingStrikesSpell());
    public static final RegistryObject<AbstractSpell> BLACK_HOLE_SPELL = registerSpell(new BlackHoleSpell());

    // EVOCATION
    public static final RegistryObject<AbstractSpell> CHAIN_CREEPER_SPELL = registerSpell(new ChainCreeperSpell());
    public static final RegistryObject<AbstractSpell> FANG_STRIKE_SPELL = registerSpell(new FangStrikeSpell());
    public static final RegistryObject<AbstractSpell> FANG_WARD_SPELL = registerSpell(new FangWardSpell());
    public static final RegistryObject<AbstractSpell> FIRECRACKER_SPELL = registerSpell(new FirecrackerSpell());
    public static final RegistryObject<AbstractSpell> GUST_SPELL = registerSpell(new GustSpell());
    public static final RegistryObject<AbstractSpell> INVISIBILITY_SPELL = registerSpell(new InvisibilitySpell());
    public static final RegistryObject<AbstractSpell> LOB_CREEPER_SPELL = registerSpell(new LobCreeperSpell());
    public static final RegistryObject<AbstractSpell> SHIELD_SPELL = registerSpell(new ShieldSpell());
    public static final RegistryObject<AbstractSpell> SPECTRAL_HAMMER_SPELL = registerSpell(new SpectralHammerSpell());
    public static final RegistryObject<AbstractSpell> SUMMON_HORSE_SPELL = registerSpell(new SummonHorseSpell());
    public static final RegistryObject<AbstractSpell> SUMMON_VEX_SPELL = registerSpell(new SummonVexSpell());
    public static final RegistryObject<AbstractSpell> SLOW_SPELL = registerSpell(new SlowSpell());
    public static final RegistryObject<AbstractSpell> ARROW_VOLLEY_SPELL = registerSpell(new ArrowVolleySpell());
    public static final RegistryObject<AbstractSpell> WOLOLO_SPELL = registerSpell(new WololoSpell());

    // FIRE
    public static final RegistryObject<AbstractSpell> BLAZE_STORM_SPELL = registerSpell(new BlazeStormSpell());
    public static final RegistryObject<AbstractSpell> BURNING_DASH_SPELL = registerSpell(new BurningDashSpell());
    public static final RegistryObject<AbstractSpell> FIREBALL_SPELL = registerSpell(new FireballSpell());
    public static final RegistryObject<AbstractSpell> FIREBOLT_SPELL = registerSpell(new FireboltSpell());
    public static final RegistryObject<AbstractSpell> FIRE_BREATH_SPELL = registerSpell(new FireBreathSpell());
    public static final RegistryObject<AbstractSpell> MAGMA_BOMB_SPELL = registerSpell(new MagmaBombSpell());
    public static final RegistryObject<AbstractSpell> WALL_OF_FIRE_SPELL = registerSpell(new WallOfFireSpell());
    public static final RegistryObject<AbstractSpell> HEAT_SURGE_SPELL = registerSpell(new HeatSurgeSpell());
    public static final RegistryObject<AbstractSpell> FLAMING_STRIKE_SPELL = registerSpell(new FlamingStrikeSpell());
    public static final RegistryObject<AbstractSpell> SCORCH_SPELL = registerSpell(new ScorchSpell());
    public static final RegistryObject<AbstractSpell> FLAMING_BARRAGE_SPELL = registerSpell(new FlamingBarrageSpell());


    // HOLY
    public static final RegistryObject<AbstractSpell> ANGEL_WINGS_SPELL = registerSpell(new AngelWingsSpell());
    public static final RegistryObject<AbstractSpell> BLESSING_OF_LIFE_SPELL = registerSpell(new BlessingOfLifeSpell());
    public static final RegistryObject<AbstractSpell> CLOUD_OF_REGENERATION_SPELL = registerSpell(new CloudOfRegenerationSpell());
    public static final RegistryObject<AbstractSpell> FORTIFY_SPELL = registerSpell(new FortifySpell());
    public static final RegistryObject<AbstractSpell> GREATER_HEAL_SPELL = registerSpell(new GreaterHealSpell());
    public static final RegistryObject<AbstractSpell> GUIDING_BOLT_SPELL = registerSpell(new GuidingBoltSpell());
    public static final RegistryObject<AbstractSpell> HEALING_CIRCLE_SPELL = registerSpell(new HealingCircleSpell());
    public static final RegistryObject<AbstractSpell> HEAL_SPELL = registerSpell(new HealSpell());
    public static final RegistryObject<AbstractSpell> SUNBEAM_SPELL = registerSpell(new SunbeamSpell());
    public static final RegistryObject<AbstractSpell> WISP_SPELL = registerSpell(new WispSpell());
    public static final RegistryObject<AbstractSpell> DIVINE_SMITE_SPELL = registerSpell(new DivineSmiteSpell());
    public static final RegistryObject<AbstractSpell> HASTE_SPELL = registerSpell(new HasteSpell());
    public static final RegistryObject<AbstractSpell> CLEANSE_SPELL = registerSpell(new CleanseSpell());

    // ICE
    public static final RegistryObject<AbstractSpell> CONE_OF_COLD_SPELL = registerSpell(new ConeOfColdSpell());
    //    public static final RegistryObject<AbstractSpell> FROSTBITE_SPELL = registerSpell(new FrostbiteSpell());
    public static final RegistryObject<AbstractSpell> FROST_STEP_SPELL = registerSpell(new FrostStepSpell());
    public static final RegistryObject<AbstractSpell> ICE_BLOCK_SPELL = registerSpell(new IceBlockSpell());
    public static final RegistryObject<AbstractSpell> ICICLE_SPELL = registerSpell(new IcicleSpell());
    public static final RegistryObject<AbstractSpell> SUMMON_POLAR_BEAR_SPELL = registerSpell(new SummonPolarBearSpell());
    public static final RegistryObject<AbstractSpell> RAY_OF_FROST_SPELL = registerSpell(new RayOfFrostSpell());
    public static final RegistryObject<AbstractSpell> FROSTWAVE_SPELL = registerSpell(new FrostwaveSpell());

    // LIGHTNING
    public static final RegistryObject<AbstractSpell> ASCENSION_SPELL = registerSpell(new AscensionSpell());
    public static final RegistryObject<AbstractSpell> CHAIN_LIGHTNING_SPELL = registerSpell(new ChainLightningSpell());
    public static final RegistryObject<AbstractSpell> CHARGE_SPELL = registerSpell(new ChargeSpell());
    public static final RegistryObject<AbstractSpell> ELECTROCUTE_SPELL = registerSpell(new ElectrocuteSpell());
    public static final RegistryObject<AbstractSpell> LIGHTNING_BOLT_SPELL = registerSpell(new LightningBoltSpell());
    public static final RegistryObject<AbstractSpell> LIGHTNING_LANCE_SPELL = registerSpell(new LightningLanceSpell());
    //public static final RegistryObject<AbstractSpell> THUNDER_STEP_SPELL = registerSpell(new ThunderStepSpell());
    public static final RegistryObject<AbstractSpell> SHOCKWAVE_SPELL = registerSpell(new ShockwaveSpell());
    public static final RegistryObject<AbstractSpell> THUNDERSTORM_SPELL = registerSpell(new ThunderstormSpell());
    public static final RegistryObject<AbstractSpell> BALL_LIGHTNING_SPELL = registerSpell(new BallLightningSpell());

    // NATURE
    public static final RegistryObject<AbstractSpell> ACID_ORB_SPELL = registerSpell(new AcidOrbSpell());
    public static final RegistryObject<AbstractSpell> BLIGHT_SPELL = registerSpell(new BlightSpell());
    public static final RegistryObject<AbstractSpell> POISON_ARROW_SPELL = registerSpell(new PoisonArrowSpell());
    public static final RegistryObject<AbstractSpell> POISON_BREATH_SPELL = registerSpell(new PoisonBreathSpell());
    public static final RegistryObject<AbstractSpell> POISON_SPLASH_SPELL = registerSpell(new PoisonSplashSpell());
    public static final RegistryObject<AbstractSpell> ROOT_SPELL = registerSpell(new RootSpell());
    public static final RegistryObject<AbstractSpell> SPIDER_ASPECT_SPELL = registerSpell(new SpiderAspectSpell());
    public static final RegistryObject<AbstractSpell> FIREFLY_SWARM_SPELL = registerSpell(new FireflySwarmSpell());
    public static final RegistryObject<AbstractSpell> OAKSKIN_SPELL = registerSpell(new OakskinSpell());
    public static final RegistryObject<AbstractSpell> EARTHQUAKE_SPELL = registerSpell(new EarthquakeSpell());
    public static final RegistryObject<AbstractSpell> STOMP_SPELL = registerSpell(new StompSpell());
    public static final RegistryObject<AbstractSpell> GLUTTONY_SPELL = registerSpell(new GluttonySpell());

    //VOID
    public static final RegistryObject<AbstractSpell> ABYSSAL_SHROUD_SPELL = registerSpell(new AbyssalShroudSpell());
    public static final RegistryObject<AbstractSpell> SCULK_TENTACLES_SPELL = registerSpell(new SculkTentaclesSpell());
    public static final RegistryObject<AbstractSpell> SONIC_BOOM_SPELL = registerSpell(new SonicBoomSpell());
    public static final RegistryObject<AbstractSpell> PLANAR_SIGHT_SPELL = registerSpell(new PlanarSightSpell());
    public static final RegistryObject<AbstractSpell> TELEKINESIS_SPELL = registerSpell(new TelekinesisSpell());
    public static final RegistryObject<AbstractSpell> ELDRITCH_BLAST_SPELL = registerSpell(new EldritchBlastSpell());
}
