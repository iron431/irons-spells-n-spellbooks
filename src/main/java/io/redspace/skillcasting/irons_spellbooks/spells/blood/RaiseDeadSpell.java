package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Optional;

public class RaiseDeadSpell extends AbstractSpellSkill {

    private static final int SUMMON_DURATION_TICKS = 20 * 60 * 10;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(150)
            .build();

    public RaiseDeadSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 30;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.summon_count",
                castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_COUNT, 0)));
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.RAISE_DEAD_START).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return Optional.empty();
    }

    @Override
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(2, SUMMON_DURATION_TICKS));
    }

    @Override
    public void onRecastFinished(CastContext castContext, RecastResult result) {
        SummonManager.recastFinishedHelper(castContext, result);
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.SUMMON_COUNT, castContext.getSkillLevel() + 2);
    }

    @Override
    public void onCast(Level world, CastContext castContext) {
        if (castContext.getSkillcastingData().recasts().hasRecast(this)) {
            return;
        }
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity caster = castContext.asEntityCaster();
        SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
        int count = castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_COUNT, 0);
        float radius = 1.5f + 0.185f * count;
        float spellPower = getSpellPower(castContext);
        Vec3 castOrigin = castContext.position(PositionAnchor.CENTER);
        float casterYawRad = castContext.getYRot();
        float casterYawDeg = casterYawRad * Mth.RAD_TO_DEG;
        RandomSource random = world.random;

        for (int i = 0; i < count; i++) {
            boolean isSkeleton = random.nextDouble() < 0.3;
            ItemStack[] equipment = getEquipment(spellPower, random, castContext.getSkillLevel());

            Monster undead = isSkeleton ? new SummonedSkeleton(world, true) : new SummonedZombie(world, true);
            undead.finalizeSpawn(serverLevel, world.getCurrentDifficultyAt(undead.getOnPos()), MobSpawnType.MOB_SUMMONED, null);
            equip(undead, equipment);

            float angle = 6.281f / count * i + casterYawRad;
            Vec3 spawn = Utils.moveToRelativeGroundLevel(world,
                    castOrigin.add(new Vec3(radius * Mth.cos(angle), 0, radius * Mth.sin(angle))), 10);
            spawn = world.clip(new ClipContext(castOrigin, spawn, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation();
            if (!world.noCollision(undead.getBoundingBox().move(spawn))) {
                spawn = Utils.moveToRelativeGroundLevel(world,
                        spawn.add(castOrigin.subtract(spawn).normalize()), 3);
            }
            undead.setPos(spawn.x, spawn.y, spawn.z);
            undead.setYRot(casterYawDeg);
            undead.setOldPosAndRot();

            Entity creature = undead;
            if (caster instanceof LivingEntity living) {
                creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(living, undead, getSkillId(), castContext.getSkillLevel())).getCreature();
            }
            world.addFreshEntity(creature);
            SummonManager.initSummon(caster, creature, SUMMON_DURATION_TICKS, summonedEntitiesCastData);
        }

        castContext.set(SpellcastingComponentTypes.SUMMONED_ENTITY_DATA, summonedEntitiesCastData);
        Vec3 soundPos = castContext.position(PositionAnchor.ORIGIN);
        world.playSound(null, soundPos.x, soundPos.y, soundPos.z, SoundRegistry.RAISE_DEAD_FINISH.get(),
                SoundSource.HOSTILE, 2.0f, 0.9f + random.nextFloat() * 0.2f);
    }

    private void equip(Mob mob, ItemStack[] equipment) {
        mob.setItemSlot(EquipmentSlot.FEET, equipment[0]);
        mob.setItemSlot(EquipmentSlot.LEGS, equipment[1]);
        mob.setItemSlot(EquipmentSlot.CHEST, equipment[2]);
        mob.setItemSlot(EquipmentSlot.HEAD, equipment[3]);
        mob.setDropChance(EquipmentSlot.FEET, 0.0F);
        mob.setDropChance(EquipmentSlot.LEGS, 0.0F);
        mob.setDropChance(EquipmentSlot.CHEST, 0.0F);
        mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    private ItemStack[] getEquipment(float power, RandomSource random, int skillLevel) {
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};

        int minQuality = 12;
        int maxQuality = (int) (6 * spellPowerPerLevel) + 15;

        ItemStack[] result = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            float quality = Mth.clamp((power + random.nextIntBetweenInclusive(-3, 8) - minQuality) / (maxQuality - minQuality), 0, 0.95f);
            if (random.nextDouble() < quality * quality) {
                if (quality > 0.85) {
                    result[i] = new ItemStack(iron[i]);
                } else if (quality > 0.65) {
                    result[i] = new ItemStack(chain[i]);
                } else if (quality > 0.15) {
                    result[i] = new ItemStack(leather[i]);
                } else {
                    result[i] = ItemStack.EMPTY;
                }
            } else {
                result[i] = ItemStack.EMPTY;
            }
        }
        return result;
    }
}
