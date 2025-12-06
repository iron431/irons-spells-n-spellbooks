package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RaiseDeadSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "raise_dead");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(150)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.summon_count", getSummonCount(spellLevel, caster)));
    }

    public RaiseDeadSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 30;
        this.baseManaCost = 50;

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
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.RAISE_DEAD_START.value());
    }

    public int getSummonCount(int spellLevel, LivingEntity caster) {
        return spellLevel + 2;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (SummonManager.recastFinishedHelper(serverPlayer, recastInstance, recastResult, castDataSerializable)) {
            super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var recasts = playerMagicData.getPlayerRecasts();
        if (!recasts.hasRecastForSpell(this)) {
            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
            int summonTime = 20 * 60 * 10;
            int count = getSummonCount(spellLevel, entity);
            float radius = 1.5f + .185f * count;
            for (int i = 0; i < count; i++) {
                boolean isSkeleton = Utils.random.nextDouble() < .3;
                var equipment = getEquipment(getSpellPower(spellLevel, entity), Utils.random);

                Monster undead = isSkeleton ? new SummonedSkeleton(world, entity, true) : new SummonedZombie(world, entity, true);
                undead.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(undead.getOnPos()), MobSpawnType.MOB_SUMMONED, null);
                equip(undead, equipment);
                var yrot = 6.281f / count * i + entity.getYRot() * Mth.DEG_TO_RAD;
                Vec3 spawn = Utils.moveToRelativeGroundLevel(world, entity.getEyePosition().add(new Vec3(radius * Mth.cos(yrot), 0, radius * Mth.sin(yrot))), 10);
                undead.setPos(spawn.x, spawn.y, spawn.z);
                undead.setYRot(entity.getYRot());
                undead.setOldPosAndRot();
                var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, undead, this.spellId, spellLevel)).getCreature();
                world.addFreshEntity(creature);
                SummonManager.initSummon(entity, creature, summonTime, summonedEntitiesCastData);
            }

            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTime, castSource, summonedEntitiesCastData);
            recasts.addRecast(recastInstance, playerMagicData);
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.RAISE_DEAD_FINISH.get(), entity.getSoundSource(), 2.0f, .9f + Utils.random.nextFloat() * .2f);
        } else {
//todo: sound?
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
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
    }

    private ItemStack[] getEquipment(float power, RandomSource random) {
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};

        int minQuality = 12;
        int maxQuality = getMaxLevel() * spellPowerPerLevel + 15;

        ItemStack[] result = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            float quality = Mth.clamp((power + (random.nextIntBetweenInclusive(-3, 8)) - minQuality) / (maxQuality - minQuality), 0, .95f);
            if (random.nextDouble() < quality * quality) {
                if (quality > .85) {
                    result[i] = new ItemStack(iron[i]);
                } else if (quality > .65) {
                    result[i] = new ItemStack(chain[i]);
                } else if (quality > .15) {
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
