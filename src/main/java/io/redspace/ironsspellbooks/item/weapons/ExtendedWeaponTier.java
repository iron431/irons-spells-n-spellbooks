package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public class ExtendedWeaponTier implements Tier {
    //    KEEPER_FLAMBERGE(1000, 4, () -> Ingredient.of(Items.NETHERITE_SCRAP)),
//    DREADSWORD(1061, 14, () -> Ingredient.of(ItemRegistry.ARCANE_INGOT.get())),
//    METAL_MAGEHUNTER(1561, 12, () -> Ingredient.of(ItemRegistry.ARCANE_INGOT.get())),
//    CRYSTAL_MAGEHUNTER(1561, 12, () -> Ingredient.of(Items.DIAMOND)),
//    TRUTHSEEKER(2031, 10, () -> Ingredient.of(ItemRegistry.ARCANE_INGOT.get())),
//    CLAYMORE(1000, 8, () -> Ingredient.of(Items.IRON_INGOT)),
//    AMETHYST(1561, 16, () -> Ingredient.of(Items.AMETHYST_SHARD)),
//    ;
    public static ExtendedWeaponTier KEEPER_FLAMBERGE = new ExtendedWeaponTier(1000, 10, -2.7f, 4, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, Ingredient.of(Items.NETHERITE_SCRAP), new AttributeHolder(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE));
    public static ExtendedWeaponTier DREADSWORD = new ExtendedWeaponTier(1061, 6, -2.4f, 14, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Ingredient.of(ItemRegistry.ARCANE_INGOT.get()));
    public static ExtendedWeaponTier MISERY = new ExtendedWeaponTier(1061, 7, -2.1f, 14, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Ingredient.of(ItemRegistry.ARCANE_INGOT.get()));
    public static ExtendedWeaponTier METAL_MAGEHUNTER = new ExtendedWeaponTier(1561, 6, -2.4f, 12, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Ingredient.of(ItemRegistry.ARCANE_INGOT.get()), new AttributeHolder(AttributeRegistry.SPELL_RESIST, .15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    public static ExtendedWeaponTier CRYSTAL_MAGEHUNTER = new ExtendedWeaponTier(1561, 6, -2.4f, 12, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Ingredient.of(Items.DIAMOND), new AttributeHolder(AttributeRegistry.SPELL_RESIST, .15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    public static ExtendedWeaponTier SPELLBREAKER = new ExtendedWeaponTier(1561, 9, -2f, 12, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Ingredient.of(Items.DIAMOND), new AttributeHolder(AttributeRegistry.COOLDOWN_REDUCTION, .15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    public static ExtendedWeaponTier TRUTHSEEKER = new ExtendedWeaponTier(2031, 11, -3f, 10, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, Ingredient.of(ItemRegistry.ARCANE_INGOT.get()));
    public static ExtendedWeaponTier CLAYMORE = new ExtendedWeaponTier(1000, 9, -2.7f, 8, BlockTags.INCORRECT_FOR_IRON_TOOL, Ingredient.of(Items.IRON_INGOT));
    public static ExtendedWeaponTier AMETHYST_RAPIER = new ExtendedWeaponTier(1561, 7, -1.5f, 16, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Ingredient.of(Items.AMETHYST_SHARD));

    int uses;
    float damage;
    float speed;
    int enchantmentValue;
    TagKey<Block> incorrectBlocksForDrops;
    Ingredient repairIngredient;
    AttributeHolder[] attributes;

    public ExtendedWeaponTier(int uses, float damage, float speed, int enchantmentValue, TagKey<Block> incorrectBlocksForDrops, Ingredient repairIngredient, AttributeHolder... attributes) {
        this.uses = uses;
        this.damage = damage;
        this.speed = speed;
        this.enchantmentValue = enchantmentValue;
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.repairIngredient = repairIngredient;
        this.attributes = attributes;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient;
    }

    public AttributeHolder[] getAdditionalAttributes() {
        return this.attributes;
    }

    public record AttributeHolder(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
        public AttributeModifier createModifier() {
            return new AttributeModifier(IronsSpellbooks.id(String.format("mainhand_%s_modifier", attribute.getRegisteredName())), value, operation);
        }
    }
}
