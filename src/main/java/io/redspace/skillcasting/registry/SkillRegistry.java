package io.redspace.skillcasting.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.demo.DemoBlessingOfLifeSkill;
import io.redspace.skillcasting.demo.DemoContinuousArrowsSkill;
import io.redspace.skillcasting.demo.DemoInstantSkill;
import io.redspace.skillcasting.demo.DemoProjectileSkill;
import io.redspace.skillcasting.demo.DemoRecastSkill;
import io.redspace.skillcasting.demo.PortalSkill;
import io.redspace.skillcasting.irons_spellbooks.spells.ConeOfColdSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.FrostStepSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.FrostbiteSpell;
import io.redspace.skillcasting.irons_spellbooks.spells.IcicleSpell;
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
}
