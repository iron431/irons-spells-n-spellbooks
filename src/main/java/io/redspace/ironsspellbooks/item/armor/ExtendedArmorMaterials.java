package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum ExtendedArmorMaterials implements ArmorMaterial {
    //DIAMOND FOR REFERENCE
    DIAMOND("diamond", 33, new int[]{3, 6, 8, 3}, 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> {
        return Ingredient.of(Items.DIAMOND);
    }, new HashMap<>()),
    /////////////////////////////////////////////////////////

    TARNISHED("tarnished", 25, new int[]{0, 0, 0, 0}, 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 150, AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_DAMAGE, new AttributeModifier("minus damage", -.15, AttributeModifier.Operation.MULTIPLY_TOTAL)
    )),
    WANDERING_MAGICIAN("wandering_magician", 10, new int[]{2, 5, 6, 2}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 25, AttributeModifier.Operation.ADDITION)
    )),
    PUMPKIN("pumpkin", 33, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.HAY_BLOCK), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 50, AttributeModifier.Operation.ADDITION)
    )),
    PYROMANCER("pyromancer", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.FIRE_SPELL_POWER.get(), new AttributeModifier("Fire Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    ARCHEVOKER("archevoker", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.EVOCATION_SPELL_POWER.get(), new AttributeModifier("Evocation Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    CULTIST("cultist", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.BLOOD_SPELL_POWER.get(), new AttributeModifier("Blood Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    PRIEST("priest", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.HOLY_SPELL_POWER.get(), new AttributeModifier("Holy Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    CRYOMANCER("cryomancer", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.ICE_SPELL_POWER.get(), new AttributeModifier("Ice Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    SHADOWWALKER("shadowwalker", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier("Ender Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    PLAGUED("plagued", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.POISON_SPELL_POWER.get(), new AttributeModifier("Poison Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    ELECTROMANCER("electromancer", 38, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 100, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.LIGHTNING_SPELL_POWER.get(), new AttributeModifier("Lightning Power", .075, AttributeModifier.Operation.MULTIPLY_BASE)
    ));

    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;
    private final Map<Attribute, AttributeModifier> additionalAttributes;

    private ExtendedArmorMaterials(String pName, int pDurabilityMultiplier, int[] pSlotProtections, int pEnchantmentValue, SoundEvent pSound, float pToughness, float pKnockbackResistance, Supplier<Ingredient> pRepairIngredient, Map<Attribute, AttributeModifier> additionalAttributes) {
        this.name = pName;
        this.durabilityMultiplier = pDurabilityMultiplier;
        this.slotProtections = pSlotProtections;
        this.enchantmentValue = pEnchantmentValue;
        this.sound = pSound;
        this.toughness = pToughness;
        this.knockbackResistance = pKnockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
        this.additionalAttributes = additionalAttributes;
    }

    public int getDurabilityForSlot(EquipmentSlot pSlot) {
        return HEALTH_PER_SLOT[pSlot.getIndex()] * this.durabilityMultiplier;
    }

    public int getDefenseForSlot(EquipmentSlot pSlot) {
        return this.slotProtections[pSlot.getIndex()];
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public SoundEvent getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public Map<Attribute, AttributeModifier> getAdditionalAttributes() {
        return additionalAttributes;
    }

    /**
     * Gets the percentage of knockback resistance provided by armor of the material.
     */
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
