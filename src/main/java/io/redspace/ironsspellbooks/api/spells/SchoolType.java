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

    private SchoolType(Builder builder) {
        this.focus = builder.focus;
        this.displayName = builder.displayName;
        this.displayStyle = builder.displayName.getStyle();
        this.powerAttribute = builder.powerAttribute;
        this.resistanceAttribute = builder.resistanceAttribute;
        this.defaultCastSound = builder.castSound;
        this.damageType = builder.damageType;
        this.requiresLearning = builder.requiresLearning;
        this.allowLooting = builder.allowLooting;
        this.powerComponent = builder.powerComponent;
    }

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder {
        private TagKey<Item> focus;
        private Component displayName;
        private Supplier<ComponentType<Float>> powerComponent;
        private Holder<Attribute> powerAttribute;
        private Holder<Attribute> resistanceAttribute;
        private Holder<SoundEvent> castSound;
        private ResourceKey<DamageType> damageType;
        private boolean requiresLearning = false;
        private boolean allowLooting = true;

        private Builder() {
        }

        public Builder focus(TagKey<Item> focus) {
            this.focus = focus;
            return this;
        }

        public Builder displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder powerComponent(Supplier<ComponentType<Float>> powerComponent) {
            this.powerComponent = powerComponent;
            return this;
        }

        public Builder powerAttribute(Holder<Attribute> powerAttribute) {
            this.powerAttribute = powerAttribute;
            return this;
        }

        public Builder resistanceAttribute(Holder<Attribute> resistanceAttribute) {
            this.resistanceAttribute = resistanceAttribute;
            return this;
        }

        public Builder castSound(Holder<SoundEvent> castSound) {
            this.castSound = castSound;
            return this;
        }

        public Builder damageType(ResourceKey<DamageType> damageType) {
            this.damageType = damageType;
            return this;
        }

        public Builder requiresLearning() {
            this.requiresLearning = true;
            return this;
        }

        public Builder noLooting() {
            this.allowLooting = false;
            return this;
        }

        public SchoolType build() {
            require(focus, "focus");
            require(displayName, "displayName");
            require(powerComponent, "powerComponent");
            require(powerAttribute, "powerAttribute");
            require(resistanceAttribute, "resistanceAttribute");
            require(castSound, "castSound");
            require(damageType, "damageType");
            return new SchoolType(this);
        }

        private static void require(Object value, String field) {
            if (value == null) {
                throw new IllegalStateException("SchoolType.Builder is missing " + field);
            }
        }
    }
}
