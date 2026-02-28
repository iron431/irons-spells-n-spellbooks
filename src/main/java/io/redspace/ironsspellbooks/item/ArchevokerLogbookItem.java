package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ArchevokerLogbookItem extends ReadableLoreItem implements IBackwardsCompatDefaultNbtItem {
    //    public static WrittenBookContent TRANSLATED_CONTENTS = new WrittenBookContent(Filterable.passThrough(""), "Archevoker", 0, List.of(
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").append("2:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.1"))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.2")),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").append("14:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.1"))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.2")),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").append("31:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.1"))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.2")),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").append("73:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.1"))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.2"))
//    ), true);
//    public static WrittenBookContent UNTRANSLATED_CONTENTS = new WrittenBookContent(Filterable.passThrough(""), "Archevoker", 0, List.of(
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("2:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt")))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("14:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt")))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("31:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt")))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("73:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
//            Filterable.passThrough(Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))
//            ), true);
    public static List<Component> TRANSLATED_CONTENTS = List.of(
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").append("2:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.1"))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.2")),
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").append("14:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.1"))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.2")),
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").append("31:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.1"))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.2")),
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").append("73:\n\n").append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.1"))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.2"))
    );
    public static List<Component> UNTRANSLATED_CONTENTS = List.of(
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("2:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_1.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt")))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("14:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_2.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt")))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("31:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_3.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt")))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.header").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))).append(Component.literal("73:\n\n").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))).append(Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.1").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))),
            (Component.translatable("item.irons_spellbooks.archevoker_log.entry_4.2").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"))))
    );

    private final boolean translated;

    public ArchevokerLogbookItem(boolean translated, Properties pProperties) {
        super(IronsSpellbooks.id("textures/entity/lectern/archevoker_logbook.png"), pProperties);
        this.translated = translated;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (translated) {
            pTooltipComponents.add(Component.translatable("tooltip.irons_spellbooks.translated").withStyle(ChatFormatting.YELLOW));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.irons_spellbooks.untranslated").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void setupItem(ItemStack stack) {
        ListTag listtag = new ListTag();
        var pages = translated ? TRANSLATED_CONTENTS : UNTRANSLATED_CONTENTS;
        pages.stream().map(component -> StringTag.valueOf(Component.Serializer.toJson(component))).forEach(listtag::add);
        stack.addTagElement("pages", listtag);
        stack.addTagElement("author", StringTag.valueOf("Archevoker"));
        stack.addTagElement("title", StringTag.valueOf("Archevoker Logbook"));
    }
}

