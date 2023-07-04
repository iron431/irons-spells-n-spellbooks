package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.spells.NoneSpell;
import io.redspace.ironsspellbooks.spells.blood.*;
import io.redspace.ironsspellbooks.spells.holy.*;
import io.redspace.ironsspellbooks.spells.ender.*;
import io.redspace.ironsspellbooks.spells.evocation.*;
import io.redspace.ironsspellbooks.spells.fire.*;
import io.redspace.ironsspellbooks.spells.ice.*;
import io.redspace.ironsspellbooks.spells.lightning.*;
import io.redspace.ironsspellbooks.spells.poison.*;
import io.redspace.ironsspellbooks.spells.void_school.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class SpellRegistry {
    public static final ResourceKey<Registry<AbstractSpell>> SPELL_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(IronsSpellbooks.MODID, "spells"));
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SPELL_REGISTRY_KEY, IronsSpellbooks.MODID);
    public static final Supplier<IForgeRegistry<AbstractSpell>> REGISTRY = SPELLS.makeRegistry(() -> new RegistryBuilder<AbstractSpell>().disableSaving().disableOverrides());
    private static final NoneSpell noneSpell = new NoneSpell();

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    public static NoneSpell none() {
        return noneSpell;
    }

    public static RegistryObject<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellId(), () -> spell);
    }

    public static AbstractSpell getSpell(ResourceLocation resourceLocation) {
        var spell = REGISTRY.get().getValue(resourceLocation);
        if (spell == null) {
            return noneSpell;
        }
        return spell;
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

    // ENDER
    public static final RegistryObject<AbstractSpell> COUNTERSPELL_SPELL = registerSpell(new CounterspellSpell());
    public static final RegistryObject<AbstractSpell> DRAGON_BREATH_SPELL = registerSpell(new DragonBreathSpell());
    public static final RegistryObject<AbstractSpell> EVASION_SPELL = registerSpell(new EvasionSpell());
    public static final RegistryObject<AbstractSpell> MAGIC_ARROW_SPELL = registerSpell(new MagicArrowSpell());
    public static final RegistryObject<AbstractSpell> MAGIC_MISSILE_SPELL = registerSpell(new MagicMissileSpell());
    public static final RegistryObject<AbstractSpell> STARFALL_SPELL = registerSpell(new StarfallSpell());
    public static final RegistryObject<AbstractSpell> TELEPORT_SPELL = registerSpell(new TeleportSpell());

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

    // FIRE
    public static final RegistryObject<AbstractSpell> BLAZE_STORM_SPELL = registerSpell(new BlazeStormSpell());
    public static final RegistryObject<AbstractSpell> BURNING_DASH_SPELL = registerSpell(new BurningDashSpell());
    public static final RegistryObject<AbstractSpell> FIREBALL_SPELL = registerSpell(new FireballSpell());
    public static final RegistryObject<AbstractSpell> FIREBOLT_SPELL = registerSpell(new FireboltSpell());
    public static final RegistryObject<AbstractSpell> FIRE_BREATH_SPELL = registerSpell(new FireBreathSpell());
    public static final RegistryObject<AbstractSpell> MAGMA_BOMB_SPELL = registerSpell(new MagmaBombSpell());
    public static final RegistryObject<AbstractSpell> WALL_OF_FIRE_SPELL = registerSpell(new WallOfFireSpell());

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

    // ICE
    public static final RegistryObject<AbstractSpell> CONE_OF_COLD_SPELL = registerSpell(new ConeOfColdSpell());
    public static final RegistryObject<AbstractSpell> FROSTBITE_SPELL = registerSpell(new FrostbiteSpell());
    public static final RegistryObject<AbstractSpell> FROST_STEP_SPELL = registerSpell(new FrostStepSpell());
    public static final RegistryObject<AbstractSpell> ICE_BLOCK_SPELL = registerSpell(new IceBlockSpell());
    public static final RegistryObject<AbstractSpell> ICICLE_SPELL = registerSpell(new IcicleSpell());
    public static final RegistryObject<AbstractSpell> SUMMON_POLAR_BEAR_SPELL = registerSpell(new SummonPolarBearSpell());

    // LIGHTNING
    public static final RegistryObject<AbstractSpell> ASCENSION_SPELL = registerSpell(new AscensionSpell());
    public static final RegistryObject<AbstractSpell> CHAIN_LIGHTNING_SPELL = registerSpell(new ChainLightningSpell());
    public static final RegistryObject<AbstractSpell> CHARGE_SPELL = registerSpell(new ChargeSpell());
    public static final RegistryObject<AbstractSpell> ELECTROCUTE_SPELL = registerSpell(new ElectrocuteSpell());
    public static final RegistryObject<AbstractSpell> LIGHTNING_BOLT_SPELL = registerSpell(new LightningBoltSpell());
    public static final RegistryObject<AbstractSpell> LIGHTNING_LANCE_SPELL = registerSpell(new LightningLanceSpell());

    //POISON
    public static final RegistryObject<AbstractSpell> ACID_ORB_SPELL = registerSpell(new AcidOrbSpell());
    public static final RegistryObject<AbstractSpell> BLIGHT_SPELL = registerSpell(new BlightSpell());
    public static final RegistryObject<AbstractSpell> POISON_ARROW_SPELL = registerSpell(new PoisonArrowSpell());
    public static final RegistryObject<AbstractSpell> POISON_BREATH_SPELL = registerSpell(new PoisonBreathSpell());
    public static final RegistryObject<AbstractSpell> POISON_SPLASH_SPELL = registerSpell(new PoisonSplashSpell());
    public static final RegistryObject<AbstractSpell> ROOT_SPELL = registerSpell(new RootSpell());
    public static final RegistryObject<AbstractSpell> SPIDER_ASPECT_SPELL = registerSpell(new SpiderAspectSpell());

    //VOID
    public static final RegistryObject<AbstractSpell> ABYSSAL_SHROUD_SPELL = registerSpell(new AbyssalShroudSpell());
    public static final RegistryObject<AbstractSpell> BLACK_HOLE_SPELL = registerSpell(new BlackHoleSpell());
    public static final RegistryObject<AbstractSpell> VOID_TENTACLES_SPELL = registerSpell(new VoidTentaclesSpell());
}
