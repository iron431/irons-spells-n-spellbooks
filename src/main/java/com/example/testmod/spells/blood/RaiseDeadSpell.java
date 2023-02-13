package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.config.ServerConfigs;
import com.example.testmod.entity.mobs.SummonedSkeleton;
import com.example.testmod.entity.mobs.SummonedZombie;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
        this.cooldown = 300;
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
                //skeleton.setCustomName(Component.translatable("testmod.entity.summoned_entity", entity.getName(), skeleton.getName()));
                //skeleton.setCustomNameVisible(false);
                skeleton.setItemSlot(EquipmentSlot.FEET, equipment[0]);
                skeleton.setItemSlot(EquipmentSlot.LEGS, equipment[1]);
                skeleton.setItemSlot(EquipmentSlot.CHEST, equipment[2]);
                skeleton.setItemSlot(EquipmentSlot.HEAD, equipment[3]);
                world.addFreshEntity(skeleton);
            } else {
                SummonedZombie zombie = new SummonedZombie(world, entity);
                zombie.setPos(entity.getEyePosition().add(new Vec3(1, 1, 1).yRot(i * 25)));
                zombie.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(zombie.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
                zombie.addEffect(new MobEffectInstance(MobEffectRegistry.RAISE_DEAD_TIMER.get(), summonTime, 0, false, false, false));
                //zombie.setCustomName(Component.translatable("entity.testmod.summoned_entity", entity.getName(), zombie.getName()));
                //zombie.setCustomNameVisible(false);
                zombie.setItemSlot(EquipmentSlot.FEET, equipment[0]);
                zombie.setItemSlot(EquipmentSlot.LEGS, equipment[1]);
                zombie.setItemSlot(EquipmentSlot.CHEST, equipment[2]);
                zombie.setItemSlot(EquipmentSlot.HEAD, equipment[3]);
                world.addFreshEntity(zombie);
            }

        }
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.RAISE_DEAD_TIMER.get(), summonTime, 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }

    private ItemStack[] getEquipment(float power, RandomSource random) {
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};

        int minQuality = 12;
        int maxQuality = ServerConfigs.getSpellConfig(SpellType.RAISE_DEAD_SPELL).MAX_LEVEL * spellPowerPerLevel + 15;

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
