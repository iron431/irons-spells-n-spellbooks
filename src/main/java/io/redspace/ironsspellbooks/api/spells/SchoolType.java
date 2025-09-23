package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class SchoolType {
    final ResourceLocation id;
    final TagKey<Item> focus;
    final Component displayName;
    final Style displayStyle;
    final Supplier<Attribute> powerAttribute;
    final Supplier<Attribute> resistanceAttribute;
    final Supplier<SoundEvent> defaultCastSound;
    final ResourceKey<DamageType> damageType;
    final boolean requiresLearning;
    final boolean allowLooting;


    /**
     * Legacy 1.20.1 constructor. Switch to forwards-compatible constructor.
     */
    @Deprecated(forRemoval = true)
    public SchoolType(ResourceLocation id, TagKey<Item> focus, Component displayName, LazyOptional<Attribute> powerAttribute, LazyOptional<Attribute> resistanceAttribute, LazyOptional<SoundEvent> defaultCastSound, ResourceKey<DamageType> damageType) {
        this.id = id;
        this.focus = focus;
        this.displayName = displayName;
        this.displayStyle = displayName.getStyle();

        //fixme: this is awful. remove asap
        this.powerAttribute = () -> powerAttribute.orElse(AttributeRegistry.SPELL_POWER.get());
        this.resistanceAttribute = () -> resistanceAttribute.orElse(AttributeRegistry.SPELL_RESIST.get());
        this.defaultCastSound = () -> defaultCastSound.orElse(SoundEvents.EVOKER_CAST_SPELL);

        this.damageType = damageType;
        this.requiresLearning = false;
        this.allowLooting = true;
    }

    public SchoolType(ResourceLocation id, TagKey<Item> focus, Component displayName, Supplier<Attribute> powerAttribute, Supplier<Attribute> resistanceAttribute, Supplier<SoundEvent> defaultCastSound, ResourceKey<DamageType> damageType, boolean requiresLearning, boolean allowLooting) {
        this.id = id;
        this.focus = focus;
        this.displayName = displayName;
        this.displayStyle = displayName.getStyle();
        this.powerAttribute = powerAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.defaultCastSound = defaultCastSound;
        this.damageType = damageType;
        this.requiresLearning = requiresLearning;
        this.allowLooting = allowLooting;
    }

    public SchoolType(ResourceLocation id, TagKey<Item> focus, Component displayName, Supplier<Attribute> powerAttribute, Supplier<Attribute> resistanceAttribute, Supplier<SoundEvent> defaultCastSound, ResourceKey<DamageType> damageType) {
        this(id, focus, displayName, powerAttribute, resistanceAttribute, defaultCastSound, damageType, false, true);
    }

    /**
     * @return Returns raw resistance attribute value of the entity.
     */
    public double getResistanceFor(LivingEntity livingEntity) {
        return livingEntity.getAttributes().hasAttribute(resistanceAttribute.get()) ? livingEntity.getAttributeValue(resistanceAttribute.get()) : 1;
    }

    /**
     * @return Returns raw power attribute value of the entity.
     */
    public double getPowerFor(LivingEntity livingEntity) {
        return livingEntity.getAttributes().hasAttribute(powerAttribute.get()) ? livingEntity.getAttributeValue(powerAttribute.get()) : 1;

    }

    public SoundEvent getCastSound() {
        return defaultCastSound.get();
    }

    public ResourceKey<DamageType> getDamageType() {
        return damageType;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public boolean isFocus(ItemStack itemStack) {
        return itemStack.is(focus);
    }

    public TagKey<Item> getFocus() {
        return focus;
    }

    public Vector3f getTargetingColor() {
        return Utils.deconstructRGB(this.displayStyle.getColor().getValue());
    }
}
