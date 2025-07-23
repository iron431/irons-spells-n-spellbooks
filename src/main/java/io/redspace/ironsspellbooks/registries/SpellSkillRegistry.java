package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.fire.*;
import io.redspace.skillcastingapi.data.AbstractSkill;
import io.redspace.skillcastingapi.registry.SkillRegistry;
import io.redspace.skillcastingapi.test.FireboltSkill;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SpellSkillRegistry {
    private static final DeferredRegister<AbstractSkill> SKILLS = DeferredRegister.create(SkillRegistry.REGISTRY, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }

    public static final DeferredHolder<AbstractSkill, FireboltSpell> FIREBOLT = SKILLS.register("firebolt", FireboltSpell::new);
    public static final DeferredHolder<AbstractSkill, BlazeStormSpell> BLAZE_STORM = SKILLS.register("blaze_storm", BlazeStormSpell::new);
    public static final DeferredHolder<AbstractSkill, BurningDashSpell> BURNING_DASH = SKILLS.register("burning_dash", BurningDashSpell::new);
    public static final DeferredHolder<AbstractSkill, FireballSpell> FIREBALL = SKILLS.register("fireball", FireballSpell::new);
    public static final DeferredHolder<AbstractSkill, WallOfFireSpell> WALL_OF_FIRE = SKILLS.register("wall_of_fire", WallOfFireSpell::new);

}
