package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class RaiseDeadSpell extends AbstractSpell {
    public RaiseDeadSpell() {
        this(1);
    }

    public RaiseDeadSpell(int level) {
        super(SpellType.RAISE_DEAD_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 50;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.summon_count", this.level));
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        int summonTime = 20 * 60 * 3;
        for (int i = 0; i < this.level; i++) {
            boolean isSkeleton = world.random.nextDouble() < .3;
            var equipment = getEquipment(getSpellPower(entity), world.getRandom());
            if (isSkeleton) {
                SummonedSkeleton skeleton = new SummonedSkeleton(world, entity);
                skeleton.setPos(entity.getEyePosition().add(new Vec3(1, 1, 1).yRot(i * 25)));
                skeleton.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(skeleton.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
                skeleton.addEffect(new MobEffectInstance(MobEffectRegistry.RAISE_DEAD_TIMER.get(), summonTime, 0, false, false, false));
                //skeleton.setCustomName(Component.translatable("irons_spellbooks.entity.summoned_entity", entity.getName(), skeleton.getName()));
                //skeleton.setCustomNameVisible(false);
                equip(skeleton, equipment);

                world.addFreshEntity(skeleton);
            } else {
                SummonedZombie zombie = new SummonedZombie(world, entity);
                zombie.setPos(entity.getEyePosition().add(new Vec3(1, 1, 1).yRot(i * 25)));
                zombie.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(zombie.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
                zombie.addEffect(new MobEffectInstance(MobEffectRegistry.RAISE_DEAD_TIMER.get(), summonTime, 0, false, false, false));
                //zombie.setCustomName(Component.translatable("entity.irons_spellbooks.summoned_entity", entity.getName(), zombie.getName()));
                //zombie.setCustomNameVisible(false);
                equip(zombie, equipment);

                world.addFreshEntity(zombie);
            }

        }
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.RAISE_DEAD_TIMER.get(), summonTime, 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
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
        int maxQuality = SpellType.RAISE_DEAD_SPELL.getMaxLevel() * spellPowerPerLevel + 15;

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
