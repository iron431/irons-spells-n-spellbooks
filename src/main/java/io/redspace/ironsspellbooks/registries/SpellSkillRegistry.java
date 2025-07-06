package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.spells.fire.BlazeStormSpell;
import io.redspace.ironsspellbooks.spells.fire.FireboltSpell;
import io.redspace.skillcastingapi.data.AbstractSkill;
import io.redspace.skillcastingapi.registry.SkillRegistry;
import io.redspace.skillcastingapi.test.FireboltSkill;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SpellSkillRegistry {
    private static final DeferredRegister<AbstractSkill> SKILLS = DeferredRegister.create(SkillRegistry.REGISTRY, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }

    public static final DeferredHolder<AbstractSkill, FireboltSpell> FIREBOLT = SKILLS.register("firebolt", FireboltSpell::new);
    public static final DeferredHolder<AbstractSkill, BlazeStormSpell> BLAZE_STORM = SKILLS.register("blaze_storm", BlazeStormSpell::new);

}
