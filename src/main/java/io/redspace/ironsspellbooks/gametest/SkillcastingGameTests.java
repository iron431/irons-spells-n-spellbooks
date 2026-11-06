package io.redspace.ironsspellbooks.gametest;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.skillcasting.api.event.BuildCooldownEvent;
import io.redspace.skillcasting.api.event.GatherSkillSelectionEvent;
import io.redspace.skillcasting.api.event.SkillRecastEvent;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.recast.RecastManager;
import io.redspace.skillcasting.data.selection.SkillSelection;
import io.redspace.skillcasting.data.selection.SkillSelectionManager;
import io.redspace.skillcasting.data.selection.SkillSelectionPriority;
import io.redspace.skillcasting.data.skill.SkillContainer;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@GameTestHolder(IronsSpellbooks.MODID)
@PrefixGameTestTemplate(false)
public class SkillcastingGameTests {

    @GameTest(template = "empty")
    public static void equalPrioritySortsBySource(GameTestHelper helper) {
        var container = SkillContainer.fromSerialized(1, false, false, List.of());
        var b = new GatherSkillSelectionEvent.Source(container, CastSource.of("b", "head"), SkillSelectionPriority.ARMOR);
        var a = new GatherSkillSelectionEvent.Source(container, CastSource.of("a", "chest"), SkillSelectionPriority.ARMOR);

        var forward = new ArrayList<>(List.of(b, a));
        var reversed = new ArrayList<>(List.of(a, b));
        forward.sort(SkillSelectionPriority.SOURCE_ORDER);
        reversed.sort(SkillSelectionPriority.SOURCE_ORDER);

        helper.assertTrue(forward.equals(List.of(a, b)), "[b, a] should sort to [a, b]");
        helper.assertTrue(reversed.equals(List.of(a, b)), "[a, b] should stay [a, b]");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void managerCopyDoesNotShareSelection(GameTestHelper helper) {
        var original = new SkillSelectionManager(new SkillSelection("mainhand", 0));
        var copy = original.copy();

        copy.getSkillSelection().makeSelection("offhand", 3);

        helper.assertTrue(original.getSkillSelection().index() == 0, "original index changed to " + original.getSkillSelection().index());
        helper.assertTrue("mainhand".equals(original.getSkillSelection().equipmentSlot()), "original slot changed to " + original.getSkillSelection().equipmentSlot());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recastExhaustCooldownOnce(GameTestHelper helper) {
        assertRecastFinishCooldownOnce(helper, (caster, skill) -> {
            RecastManager recasts = caster.skillcastingData().recasts();
            startRecast(caster, skill);
            for (int i = 0; i < 100 && recasts.handleRecastConsumption(recastableContext(caster, skill)); i++) {
            }
        });
    }

    @GameTest(template = "empty")
    public static void recastTimeoutCooldownOnce(GameTestHelper helper) {
        assertRecastFinishCooldownOnce(helper, (caster, skill) -> {
            RecastManager recasts = caster.skillcastingData().recasts();
            startRecast(caster, skill);
            for (int i = 0; i < 1000 && recasts.hasRecast(skill); i++) {
                recasts.tick(caster);
            }
        });
    }

    @GameTest(template = "empty")
    public static void recastExhaustByCastingCooldownOnce(GameTestHelper helper) {
        assertRecastFinishCooldownOnce(helper, (caster, skill) -> {
            RecastManager recasts = caster.skillcastingData().recasts();
            for (int i = 0; i < 100 && (i == 0 || recasts.hasRecast(skill)); i++) {
                SkillcastingManager.initiateCast(caster, recastableContext(caster, skill));
                SkillcastingManager.cancelCast(caster, CastEndReason.COMPLETED, false);
            }
        });
    }

    @GameTest(template = "empty")
    public static void attributesModifyCastComponents(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 1, 1, 1);
        CasterRef caster = CasterRef.entity(zombie);
        Holder<AbstractSkill> firebolt = SkillRegistry.holder(SpellRegistry.FIREBOLT_SPELL.get());

        CastContext unmodified = fireboltContext(caster, firebolt);
        helper.assertTrue(unmodified.getOrNull(SkillcastingComponentTypes.PROJECTILE_SPEED) == 1.75f, "firebolt speed should be 1.75");
        helper.assertFalse(unmodified.has(SkillcastingComponentTypes.CAST_RANGE), "firebolt should not set a range");
        helper.assertFalse(unmodified.has(SkillcastingComponentTypes.PROJECTILE_PIERCE), "firebolt should not set pierce");

        addModifier(helper, zombie, AttributeRegistry.SPELL_PROJECTILE_SPEED, modifier("speed", 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        addModifier(helper, zombie, AttributeRegistry.SPELL_RANGE, modifier("range_base", 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        CastContext percentOnly = fireboltContext(caster, firebolt);
        helper.assertTrue(Math.abs(percentOnly.getOrNull(SkillcastingComponentTypes.PROJECTILE_SPEED) - 2.1f) < 1e-5, "speed 1.75 with +20% should be 2.1");
        helper.assertFalse(percentOnly.has(SkillcastingComponentTypes.CAST_RANGE), "percent alone should not create a range");

        addModifier(helper, zombie, AttributeRegistry.SPELL_RANGE, modifier("range_flat", 1, AttributeModifier.Operation.ADD_VALUE));
        addModifier(helper, zombie, AttributeRegistry.SPELL_RANGE, modifier("range_total", 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        addModifier(helper, zombie, AttributeRegistry.SPELL_PIERCING, modifier("pierce", 2, AttributeModifier.Operation.ADD_VALUE));
        CastContext withFlat = fireboltContext(caster, firebolt);
        helper.assertTrue(Math.abs(withFlat.getOrNull(SkillcastingComponentTypes.CAST_RANGE) - 3f) < 1e-5, "range (0 + 1) * 1.5 * 2 should be 3");
        helper.assertTrue(withFlat.getOrNull(SkillcastingComponentTypes.PROJECTILE_PIERCE) == 2, "pierce 0 + 2 should be 2");

        attribute(helper, zombie, AttributeRegistry.SPELL_PIERCING).removeModifier(IronsSpellbooks.id("test_pierce"));
        CastContext flatRemoved = fireboltContext(caster, firebolt);
        helper.assertFalse(flatRemoved.has(SkillcastingComponentTypes.PROJECTILE_PIERCE), "a removed flat modifier should not create pierce");
        helper.succeed();
    }

    private static CastContext fireboltContext(CasterRef caster, Holder<AbstractSkill> firebolt) {
        return SkillcastingManager.buildCastContext(caster, firebolt, 1, CastSource.of("test", EquipmentSlot.MAINHAND));
    }

    private static AttributeModifier modifier(String name, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(IronsSpellbooks.id("test_" + name), amount, operation);
    }

    private static void addModifier(GameTestHelper helper, LivingEntity entity, DeferredHolder<Attribute, Attribute> attribute, AttributeModifier modifier) {
        attribute(helper, entity, attribute).addTransientModifier(modifier);
    }

    private static AttributeInstance attribute(GameTestHelper helper, LivingEntity entity, DeferredHolder<Attribute, Attribute> attribute) {
        Holder<Attribute> holder = helper.getLevel().registryAccess().registryOrThrow(Registries.ATTRIBUTE).getHolderOrThrow(attribute.getKey());
        return entity.getAttribute(holder);
    }

    private static void assertRecastFinishCooldownOnce(GameTestHelper helper, BiConsumer<CasterRef, Holder<AbstractSkill>> finishRecast) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 1, 1, 1);
        CasterRef caster = CasterRef.entity(zombie);
        Holder<AbstractSkill> skill = SkillRegistry.holder(SpellRegistry.FLAMING_BARRAGE_SPELL.get());
        RecastManager recasts = caster.skillcastingData().recasts();

        AtomicInteger cooldowns = new AtomicInteger();
        List<Boolean> recastPresentOnPost = new ArrayList<>();
        Consumer<BuildCooldownEvent> onCooldown = event -> {
            if (event.context().asEntityCaster() == zombie) {
                cooldowns.incrementAndGet();
            }
        };
        Consumer<SkillRecastEvent.Finish> onRecastPost = event -> {
            if (event.getCastContext().asEntityCaster() == zombie) {
                recastPresentOnPost.add(recasts.hasRecast(skill));
            }
        };
        NeoForge.EVENT_BUS.addListener(BuildCooldownEvent.class, onCooldown);
        NeoForge.EVENT_BUS.addListener(SkillRecastEvent.Finish.class, onRecastPost);
        try {
            finishRecast.accept(caster, skill);
        } finally {
            NeoForge.EVENT_BUS.unregister(onCooldown);
            NeoForge.EVENT_BUS.unregister(onRecastPost);
        }

        helper.assertFalse(recasts.hasRecast(skill), "recast still active");
        helper.assertTrue(cooldowns.get() == 1, "expected one cooldown, got " + cooldowns.get());
        helper.assertTrue(recastPresentOnPost.equals(List.of(false)), "Recast.Post saw hasRecast " + recastPresentOnPost);
        helper.succeed();
    }

    private static void startRecast(CasterRef caster, Holder<AbstractSkill> skill) {
        CastContext context = recastableContext(caster, skill);
        caster.skillcastingData().recasts().addRecast(new RecastInstance(context.getOrNull(SkillcastingComponentTypes.RECAST_CONFIG), context));
    }

    private static CastContext recastableContext(CasterRef caster, Holder<AbstractSkill> skill) {
        CastContext context = SkillcastingManager.buildCastContext(caster, skill, 1, CastSource.of("test", EquipmentSlot.MAINHAND));
        if (context.has(SkillcastingComponentTypes.IGNORE_COOLDOWN) || context.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, 0) <= 0
                || !context.has(SkillcastingComponentTypes.RECAST_CONFIG)) {
            throw new IllegalStateException("test spell must be recastable with a cooldown");
        }
        return context;
    }
}
