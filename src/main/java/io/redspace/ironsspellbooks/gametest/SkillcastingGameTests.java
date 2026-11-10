package io.redspace.ironsspellbooks.gametest;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import io.redspace.skillcasting.api.event.BuildCooldownEvent;
import io.redspace.skillcasting.api.event.GatherSkillSelectionEvent;
import io.redspace.skillcasting.api.event.SkillRecastEvent;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.component.CastComponentMap;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.resolver.DirectionResolver;
import io.redspace.skillcasting.data.recast.RecastManager;
import io.redspace.skillcasting.data.selection.SkillSelection;
import io.redspace.skillcasting.data.selection.SkillSelectionManager;
import io.redspace.skillcasting.data.selection.SkillSelectionPriority;
import io.redspace.skillcasting.data.skill.SkillContainer;
import io.redspace.skillcasting.data.skill.SkillData;
import io.redspace.skillcasting.data.skill.SkillSlot;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        forward.sort(GatherSkillSelectionEvent.Source.ORDER);
        reversed.sort(GatherSkillSelectionEvent.Source.ORDER);

        helper.assertTrue(forward.equals(List.of(a, b)), "[b, a] should sort to [a, b]");
        helper.assertTrue(reversed.equals(List.of(a, b)), "[a, b] should stay [a, b]");

        var feet = new GatherSkillSelectionEvent.Source(container, CastSource.of("same", "feet"), SkillSelectionPriority.ARMOR);
        var head = new GatherSkillSelectionEvent.Source(container, CastSource.of("same", "head"), SkillSelectionPriority.ARMOR);
        var feetFirst = new ArrayList<>(List.of(feet, head));
        var headFirst = new ArrayList<>(List.of(head, feet));
        feetFirst.sort(GatherSkillSelectionEvent.Source.ORDER);
        headFirst.sort(GatherSkillSelectionEvent.Source.ORDER);
        helper.assertTrue(feetFirst.equals(List.of(head, feet)) && headFirst.equals(List.of(head, feet)), "head should sort before feet");
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
                SkillcastingManager.cancelCast(caster, CastEndReason.COMPLETED);
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

    @GameTest(template = "empty")
    public static void contextFireAndFreezeTicksReachTarget(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 1, 1, 1);
        CasterRef caster = CasterRef.entity(zombie);
        Holder<AbstractSkill> firebolt = SkillRegistry.holder(SpellRegistry.FIREBOLT_SPELL.get());
        CastContext context = fireboltContext(caster, firebolt);
        context.set(SkillcastingComponentTypes.FIRE_TICKS, 100);
        context.set(SkillcastingComponentTypes.FREEZE_TICKS, 30);
        Zombie target = helper.spawn(EntityType.ZOMBIE, 2, 1, 2);

        DamageSources.applyDamage(target, 1, SpellRegistry.FIREBOLT_SPELL.get().getDamageSource(context, false));

        helper.assertTrue(target.getRemainingFireTicks() == 100, "burn should be 100, was " + target.getRemainingFireTicks());
        helper.assertTrue(target.getTicksFrozen() == 30 * DamageSources.FREEZE_THAW_RATE, "frozen ticks should be 60, was " + target.getTicksFrozen());
        zombie.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unknownIdsDropOnlyTheirEntry(GameTestHelper helper) {
        var registryAccess = helper.getLevel().registryAccess();
        var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

        var container = SkillContainer.fromSerialized(2, true, false, List.of(
                SkillSlot.of(new SkillData(SpellRegistry.FIREBOLT_SPELL.get(), 1), 0),
                SkillSlot.of(new SkillData(SpellRegistry.FIREBALL_SPELL.get(), 1), 1)));
        JsonObject containerJson = SkillContainer.CODEC.encodeStart(ops, container).getOrThrow().getAsJsonObject();
        containerJson.getAsJsonArray("data").get(1).getAsJsonObject().getAsJsonObject("skill").addProperty("skill", "irons_spellbooks:missing");
        var slots = SkillContainer.CODEC.parse(ops, containerJson).getOrThrow().getActiveSkills();
        helper.assertTrue(slots.size() == 1 && slots.get(0).getSkill() == SpellRegistry.FIREBOLT_SPELL.get(), "container should keep only firebolt, kept " + slots);

        var components = new CastComponentMap();
        components.set(SkillcastingComponentTypes.SKILL_LEVEL.get(), 3);
        JsonObject componentsJson = CastComponentMap.COMPONENT_CODEC.encodeStart(ops, components).getOrThrow().getAsJsonObject();
        componentsJson.addProperty("skillcasting:missing", 1);
        componentsJson.addProperty(SkillcastingComponentTypes.CAST_START_ANIMATION.getId().toString(), 1);
        var decodedComponents = CastComponentMap.COMPONENT_CODEC.parse(ops, componentsJson).getOrThrow();
        helper.assertTrue(Integer.valueOf(3).equals(decodedComponents.getOrNull(SkillcastingComponentTypes.SKILL_LEVEL.get())), "skill level should survive unknown and non-persisted components");

        Holder<UpgradeOrbType> fire = registryAccess.registryOrThrow(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY).getHolderOrThrow(UpgradeOrbTypeRegistry.FIRE_SPELL_POWER);
        JsonObject upgradeJson = UpgradeData.REAL_CODEC.encodeStart(ops, new UpgradeData(Map.of(fire, 2), "chest")).getOrThrow().getAsJsonObject();
        upgradeJson.getAsJsonObject("upgrades").addProperty("irons_spellbooks:missing", 1);
        var upgrades = UpgradeData.CODEC.parse(ops, upgradeJson).getOrThrow().upgrades();
        helper.assertTrue(upgrades.size() == 1 && upgrades.get(fire) == 2, "upgrades should keep only fire power, kept " + upgrades);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void teleportFollowsContextDirection(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 1, 2, 1);
        zombie.setNoAi(true);
        zombie.setYRot(180);
        zombie.setYHeadRot(180);
        Vec3 start = zombie.position();
        CasterRef caster = CasterRef.entity(zombie);
        CastContext context = SkillcastingManager.buildCastContext(caster, SkillRegistry.holder(SpellRegistry.TELEPORT_SPELL.get()), 1, CastSource.of("test", EquipmentSlot.MAINHAND));
        context.set(SkillcastingComponentTypes.DIRECTION_RESOLVER, DirectionResolver.fixed(new Vec3(1, 0, 0)));
        context.set(SkillcastingComponentTypes.TELEPORT_RANGE, 4f);
        SkillcastingManager.initiateCast(caster, context);
        helper.runAfterDelay(2, () -> {
            Vec3 moved = zombie.position().subtract(start);
            helper.assertTrue(moved.x > 1 && Math.abs(moved.z) < 0.5, "teleport should follow the context direction (+x), moved " + moved);
            zombie.discard();
            helper.succeed();
        });
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
        try {
            assertRecastFinishCooldownOnce(helper, finishRecast, zombie);
        } finally {
            zombie.discard();
        }
    }

    private static void assertRecastFinishCooldownOnce(GameTestHelper helper, BiConsumer<CasterRef, Holder<AbstractSkill>> finishRecast, Zombie zombie) {
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
