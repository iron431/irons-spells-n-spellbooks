package io.redspace.ironsspellbooks.entity.mobs;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CatacombsZombie extends Zombie {
    //This is a wrapper class that in reality creates a vanilla zombie, but with some cool stuff thrown on top
    public CatacombsZombie(EntityType<? extends Zombie> pEntityType, Level pLevel) {
        super(EntityType.ZOMBIE, pLevel);
        if (this.random.nextFloat() < .2f) {
            switch (this.random.nextIntBetweenInclusive(1, 4)) {

                case 1 -> addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE));
                case 2 -> addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 1));
                case 3 -> addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
                case 4 -> addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE));
            }

        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(random, pDifficulty);
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};

        float power = random.nextFloat();
        ItemStack[] equipment = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            if (random.nextFloat() > .60f) {
                equipment[i] = ItemStack.EMPTY;
            } else {
                float stray = (random.nextFloat() - .5f) / 3;
                if (power + stray > .85)
                    equipment[i] = new ItemStack(iron[i]);
                else if (power + stray > .45)
                    equipment[i] = new ItemStack(chain[i]);
                else
                    equipment[i] = new ItemStack(leather[i]);
            }

        }
        setItemSlot(EquipmentSlot.FEET, equipment[0]);
        setItemSlot(EquipmentSlot.LEGS, equipment[1]);
        setItemSlot(EquipmentSlot.CHEST, equipment[2]);
        setItemSlot(EquipmentSlot.HEAD, equipment[3]);
        if (random.nextFloat() < .01f)
            setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CYAN_BANNER));

        setDropChance(EquipmentSlot.FEET, 0.0F);
        setDropChance(EquipmentSlot.LEGS, 0.0F);
        setDropChance(EquipmentSlot.CHEST, 0.0F);
        setDropChance(EquipmentSlot.HEAD, 0.0F);
    }
}
