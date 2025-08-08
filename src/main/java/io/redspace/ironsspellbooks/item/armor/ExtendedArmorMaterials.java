package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public enum ExtendedArmorMaterials implements IronsExtendedArmorMaterial {

    TARNISHED("tarnished", 25, makeArmorMap(0, 0, 0, 0), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 150, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.MANA_REGEN.get(), new AttributeModifier("Mana Regen", .25, AttributeModifier.Operation.MULTIPLY_TOTAL),
            Attributes.ATTACK_DAMAGE, new AttributeModifier("minus damage", -.15, AttributeModifier.Operation.MULTIPLY_TOTAL)
    )),
    DEV("dev", 25, makeArmorMap(0, 0, 0, 20), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 10000, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier("Mana Regen", .75, AttributeModifier.Operation.MULTIPLY_TOTAL),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Mana Regen", 1.00, AttributeModifier.Operation.MULTIPLY_TOTAL)
    )),
    WANDERING_MAGICIAN("wandering_magician", 10, makeArmorMap(2, 5, 6, 2), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 25, AttributeModifier.Operation.ADDITION)
    )),
    PUMPKIN("pumpkin", 33, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.HAY_BLOCK), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 75, AttributeModifier.Operation.ADDITION)
    )),
    PYROMANCER("pyromancer", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.FIRE_SPELL_POWER.get(), new AttributeModifier("Fire Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    ARCHEVOKER("archevoker", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.EVOCATION_SPELL_POWER.get(), new AttributeModifier("Evocation Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    CULTIST("cultist", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.BLOOD_SPELL_POWER.get(), new AttributeModifier("Blood Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    PRIEST("priest", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.HOLY_SPELL_POWER.get(), new AttributeModifier("Holy Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    CRYOMANCER("cryomancer", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.ICE_SPELL_POWER.get(), new AttributeModifier("Ice Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    SHADOWWALKER("shadowwalker", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier("Ender Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    PLAGUED("plagued", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.NATURE_SPELL_POWER.get(), new AttributeModifier("Nature Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    ELECTROMANCER("electromancer", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.LIGHTNING_SPELL_POWER.get(), new AttributeModifier("Lightning Power", .10, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    NETHERITE_BATTLEMAGE("netherite", 38, schoolArmorMap(), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.0F, () -> Ingredient.of(Tags.Items.INGOTS_NETHERITE), Map.of(
            AttributeRegistry.MAX_MANA.get(), new AttributeModifier("Max Mana", 125, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier("Base Power", .05, AttributeModifier.Operation.MULTIPLY_BASE)
    ));

    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;
    private final Map<Attribute, AttributeModifier> additionalAttributes;

    private ExtendedArmorMaterials(String pName, int pDurabilityMultiplier, EnumMap<ArmorItem.Type, Integer> protectionMap, int pEnchantmentValue, SoundEvent pSound, float pToughness, float pKnockbackResistance, Supplier<Ingredient> pRepairIngredient, Map<Attribute, AttributeModifier> additionalAttributes) {
        this.name = pName;
        this.durabilityMultiplier = pDurabilityMultiplier;
        this.protectionFunctionForType = protectionMap;
        this.enchantmentValue = pEnchantmentValue;
        this.sound = pSound;
        this.toughness = pToughness;
        this.knockbackResistance = pKnockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
        this.additionalAttributes = additionalAttributes;
    }

    static public EnumMap<ArmorItem.Type, Integer> makeArmorMap(int helmet, int chestplate, int leggings, int boots) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266655_) -> {
            p_266655_.put(ArmorItem.Type.BOOTS, boots);
            p_266655_.put(ArmorItem.Type.LEGGINGS, leggings);
            p_266655_.put(ArmorItem.Type.CHESTPLATE, chestplate);
            p_266655_.put(ArmorItem.Type.HELMET, helmet);
        });
    }

    static public EnumMap<ArmorItem.Type, Integer> schoolArmorMap() {
        return makeArmorMap(3, 8, 6, 3);
    }

    public int getDurabilityForSlot(EquipmentSlot pSlot) {
        return HEALTH_PER_SLOT[pSlot.getIndex()] * this.durabilityMultiplier;
    }

    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266653_) -> {
        p_266653_.put(ArmorItem.Type.BOOTS, 13);
        p_266653_.put(ArmorItem.Type.LEGGINGS, 15);
        p_266653_.put(ArmorItem.Type.CHESTPLATE, 16);
        p_266653_.put(ArmorItem.Type.HELMET, 11);
    });

    public int getDurabilityForType(ArmorItem.Type p_266745_) {
        return HEALTH_FUNCTION_FOR_TYPE.get(p_266745_) * this.durabilityMultiplier;
    }

    public int getDefenseForType(ArmorItem.Type p_266752_) {
        return this.protectionFunctionForType.get(p_266752_);
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
