package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class MagehunterVindicator extends Vindicator {
    //This is a wrapper class that in reality creates a vanilla Vindicator, but with the Magehunter sword
    public MagehunterVindicator(EntityType<? extends Vindicator> pEntityType, Level pLevel) {
        super(EntityType.VINDICATOR, pLevel);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(random, pDifficulty);
        ItemStack magehunter = new ItemStack(ItemRegistry.MAGEHUNTER.get());

        magehunter.enchant(Enchantments.SHARPNESS, 5);

        setItemSlot(EquipmentSlot.MAINHAND, magehunter);
    }
}
