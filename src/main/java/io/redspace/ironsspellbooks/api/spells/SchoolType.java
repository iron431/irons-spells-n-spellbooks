package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.util.Utils;
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

public class SchoolType {
//    FIRE(0),
//    ICE(1),
//    LIGHTNING(2),
//    HOLY(3),
//    ENDER(4),
//    BLOOD(5),
//    EVOCATION(6),
//    //VOID(7),
//    NATURE(8);

    final ResourceLocation id;
    final TagKey<Item> focus;
    final Component displayName;
    final Style displayStyle;
    final Holder<Attribute> powerAttribute;
    final Holder<Attribute> resistanceAttribute;
    final Holder<SoundEvent> defaultCastSound;
    final ResourceKey<DamageType> damageType;

    public SchoolType(ResourceLocation id, TagKey<Item> focus, Component displayName, Holder<Attribute> powerAttribute, Holder<Attribute> resistanceAttribute, Holder<SoundEvent> defaultCastSound, ResourceKey<DamageType> damageType) {
        this.id = id;
        this.focus = focus;
        this.displayName = displayName;
        this.displayStyle = displayName.getStyle();
        this.powerAttribute = powerAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.defaultCastSound = defaultCastSound;
        this.damageType = damageType;
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
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public boolean isFocus(ItemStack itemStack) {
        return itemStack.is(focus);
    }

    public Vector3f getTargetingColor() {
        return Utils.deconstructRGB(this.displayStyle.getColor().getValue());
    }
}
