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
}
