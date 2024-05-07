package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum ExtendedWeaponTiers implements Tier {
    KEEPER_FLAMBERGE(1000, 4, () -> Ingredient.of(Items.NETHERITE_SCRAP)),
    DREADSWORD(1061, 14, () -> Ingredient.of(ItemRegistry.ARCANE_INGOT.get())),
    METAL_MAGEHUNTER(1561, 12, () -> Ingredient.of(ItemRegistry.ARCANE_INGOT.get())),
    CRYSTAL_MAGEHUNTER(1561, 12, () -> Ingredient.of(Items.DIAMOND)),
    TRUTHSEEKER(2031, 10, () -> Ingredient.of(ItemRegistry.ARCANE_INGOT.get())),
    CLAYMORE(1000, 8, () -> Ingredient.of(Items.IRON_INGOT)),
    AMETHYST(1561, 16, () -> Ingredient.of(Items.AMETHYST_SHARD)),
    ;

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    private ExtendedWeaponTiers(int pLevel, int pUses, int pEnchantmentValue, Supplier<Ingredient> pRepairIngredient) {
        this.level = pLevel;
        this.uses = pUses;
        this.speed = 0;
        this.damage = 0;
        this.enchantmentValue = pEnchantmentValue;
        this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
    }

    private ExtendedWeaponTiers(int pUses, int pEnchantmentValue, Supplier<Ingredient> pRepairIngredient) {
        this(0, pUses, pEnchantmentValue, pRepairIngredient);
    }

    public int getUses() {
        return this.uses;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getAttackDamageBonus() {
        return this.damage;
    }

    public int getLevel() {
        return this.level;
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }


}
