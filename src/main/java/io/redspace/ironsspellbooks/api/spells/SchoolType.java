package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.skillcasting.data.component.ComponentType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class SchoolType {
    private final TagKey<Item> focus;
    private final Component displayName;
    private final Style displayStyle;
    private final Holder<Attribute> powerAttribute;
    private final Holder<Attribute> resistanceAttribute;
    private final Supplier<ComponentType<Float>> powerComponent;
    private final Holder<SoundEvent> defaultCastSound;
    private final ResourceKey<DamageType> damageType;
    private final boolean requiresLearning;
    private final boolean allowLooting;

    public SchoolType(
            TagKey<Item> focus,
            Component displayName,
            Supplier<ComponentType<Float>> powerComponent,
            Holder<Attribute> powerAttribute,
            Holder<Attribute> resistanceAttribute,
            Holder<SoundEvent> defaultCastSound,
            ResourceKey<DamageType> damageType,
            boolean requiresLearning,
            boolean allowLooting) {
        this.focus = focus;
        this.displayName = displayName;
        this.displayStyle = displayName.getStyle();
        this.powerAttribute = powerAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.defaultCastSound = defaultCastSound;
        this.damageType = damageType;
        this.requiresLearning = requiresLearning;
        this.allowLooting = allowLooting;
        this.powerComponent = powerComponent;
    }

    public SchoolType(
            TagKey<Item> focus,
            Component displayName,
            Supplier<ComponentType<Float>> powerComponent,
            Holder<Attribute> powerAttribute,
            Holder<Attribute> resistanceAttribute,
            Holder<SoundEvent> defaultCastSound,
            ResourceKey<DamageType> damageType) {
        this(focus, displayName, powerComponent, powerAttribute, resistanceAttribute, defaultCastSound, damageType, false, true);
    }

    /**
     * @return Returns raw resistance attribute value of the entity.
     */
    public double getResistanceFor(LivingEntity livingEntity) {
        return livingEntity.getAttributes().hasAttribute(resistanceAttribute) ? livingEntity.getAttributeValue(resistanceAttribute) : 1;
    }

    /**
     * @return Returns raw power attribute value of the entity.
     */
    public double getPowerFor(LivingEntity livingEntity) {
        return livingEntity.getAttributes().hasAttribute(powerAttribute) ? livingEntity.getAttributeValue(powerAttribute) : 1;

    }

    public SoundEvent getCastSound() {
        return defaultCastSound.value();
    }

    public ResourceKey<DamageType> getDamageType() {
        return damageType;
    }

    public ResourceLocation getId() {
        return SchoolRegistry.REGISTRY.getKey(this);
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

    public boolean allowLooting() {
        return allowLooting;
    }

    public boolean requiresLearning() {
        return requiresLearning;
    }

    public Supplier<ComponentType<Float>> getPowerComponent() {
        return powerComponent;
    }
}
