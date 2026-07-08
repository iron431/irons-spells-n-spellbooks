package io.redspace.skillcasting.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.demo.DemoBlessingOfLifeSkill;
import io.redspace.skillcasting.demo.DemoContinuousArrowsSkill;
import io.redspace.skillcasting.demo.DemoInstantSkill;
import io.redspace.skillcasting.demo.DemoProjectileSkill;
import io.redspace.skillcasting.demo.DemoRecastSkill;
import io.redspace.skillcasting.demo.PortalSkill;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public final class SkillRegistry {
    // fixme: public?
    private static final DeferredRegister<AbstractSkill> SKILLS =
            DeferredRegister.create(SkillcastingRegistries.SKILL_REGISTRY_KEY, IronsSpellbooks.MODID);

    private SkillRegistry() {
    }

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }

    private static <T extends AbstractSkill> DeferredHolder<AbstractSkill, T> registerSkill(String name, Supplier<T> skill) {
        return SKILLS.register(name, skill);
    }

    @Deprecated
    public static Holder<AbstractSkill> holder(ResourceLocation id) {
        return holder(get(id));
    }

    @Deprecated
    public static Holder<AbstractSkill> holder(AbstractSkill skill) {
        return SkillcastingRegistries.SKILL_REGISTRY.wrapAsHolder(skill);
    }

    @Deprecated
    public static ResourceLocation id(AbstractSkill skill) {
        return SkillcastingRegistries.SKILL_REGISTRY.getKey(skill);
    }

    @Deprecated
    public static AbstractSkill get(ResourceLocation id) {
        return SkillcastingRegistries.SKILL_REGISTRY.get(id);
    }

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

}
