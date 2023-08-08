package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

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
//    final PlaceholderDamageType damageType;
    final LazyOptional<Attribute> powerAttribute;
    final LazyOptional<Attribute> resistanceAttribute;
    final LazyOptional<SoundEvent> defaultCastSound;

    public SchoolType(ResourceLocation id, TagKey<Item> focus, Component displayName, LazyOptional<Attribute> powerAttribute, LazyOptional<Attribute> resistanceAttribute, LazyOptional<SoundEvent> defaultCastSound) {
        this.id = id;
        this.focus = focus;
        this.displayName = displayName;
        this.displayStyle = displayName.getStyle();
        this.powerAttribute = powerAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.defaultCastSound = defaultCastSound;
    }

    public double getResistanceFor(LivingEntity livingEntity) {
        var resistanceAttribute = this.resistanceAttribute.orElse(null);
        if (resistanceAttribute != null) {
            return livingEntity.getAttributeValue(resistanceAttribute);
        } else {
            return 1;
        }
    }

    public double getPowerFor(LivingEntity livingEntity) {
        var powerAttribute = this.powerAttribute.orElse(null);
        if (powerAttribute != null) {
            return livingEntity.getAttributeValue(powerAttribute);
        } else {
            return 1;
        }
    }

    public SoundEvent getCastSound() {
        return defaultCastSound.resolve().get();
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

    public static final Component DISPLAY_FIRE = Component.translatable("school.irons_spellbooks.fire").withStyle(ChatFormatting.GOLD);
    public static final Component DISPLAY_ICE = Component.translatable("school.irons_spellbooks.ice").withStyle(Style.EMPTY.withColor(0xd0f9ff));
    public static final Component DISPLAY_LIGHTNING = Component.translatable("school.irons_spellbooks.lightning").withStyle(ChatFormatting.AQUA);
    public static final Component DISPLAY_HOLY = Component.translatable("school.irons_spellbooks.holy").withStyle(Style.EMPTY.withColor(0xfff8d4));
    public static final Component DISPLAY_ENDER = Component.translatable("school.irons_spellbooks.ender").withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final Component DISPLAY_BLOOD = Component.translatable("school.irons_spellbooks.blood").withStyle(ChatFormatting.DARK_RED);
    public static final Component DISPLAY_EVOCATION = Component.translatable("school.irons_spellbooks.evocation").withStyle(ChatFormatting.WHITE);
    public static final Component DISPLAY_VOID = Component.translatable("school.irons_spellbooks.void").withStyle(Style.EMPTY.withColor(0x490059));
    public static final Component DISPLAY_NATURE = Component.translatable("school.irons_spellbooks.nature").withStyle(ChatFormatting.GREEN);
    public static final Component[] DISPLAYS = {DISPLAY_FIRE, DISPLAY_ICE, DISPLAY_LIGHTNING, DISPLAY_HOLY, DISPLAY_ENDER, DISPLAY_BLOOD, DISPLAY_EVOCATION, DISPLAY_VOID, DISPLAY_NATURE};


}
