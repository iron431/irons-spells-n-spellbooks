package io.redspace.ironsspellbooks.item;

import io.redspace.ironspatreonlib.patreon.PatreonData;
import io.redspace.ironspatreonlib.patreon.data.ChronicleEntry;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;
import java.util.*;

public class ChronicleItem extends ReadableLoreItem {

    private static final int BOOK_LOST = 0;
    private static final int BOOK_FAITHFUL = 1;
    private static final int BOOK_LOYAL = 2;

    private static final int PLEDGE_WIZARD = 2;
    private static final int PLEDGE_ANCIENT_MAGICIAN = 3;

    private static final Comparator<MutableComponent> BY_DISPLAY_STRING =
            Comparator.comparing(c -> c.getString().toLowerCase(Locale.ROOT));

    private List<Component> chronicleCache;
    private LocalDate lastCachedDate;

    public ChronicleItem(Properties pProperties) {
        super(IronsSpellbooks.id("textures/entity/lectern/archevoker_logbook.png"), pProperties);
    }

    @Override
    public Optional<ResourceLocation> simpleTextureOverride(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public List<Component> getPages(ItemStack stack) {
        // invalidate cache if the last time it was fetched was over 1 day ago (ie servers)
        if (chronicleCache == null || (lastCachedDate != null && lastCachedDate.isBefore(LocalDate.now().minusDays(1)))) {
            chronicleCache = new ArrayList<>();
            List<MutableComponent> loyalSouls = new ArrayList<>();
            List<MutableComponent> faithfulSouls = new ArrayList<>();
            List<MutableComponent> lostSouls = new ArrayList<>();
            resolveChronicleData(lostSouls, faithfulSouls, loyalSouls);
            // create book structure
            Stack<MutableComponent> pages = new Stack<>();
            MutableComponent loyalPage = Component.translatable("item.irons_spellbooks.chronicle.chapter", 1).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(false)).append(
                    Component.translatable("item.irons_spellbooks.chronicle.chapter_1").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(true))
            );
            loyalPage.append("\n\n");
            pages.push(loyalPage);
            createChapterPages(pages, loyalSouls);

            MutableComponent chroniclersPage = Component.translatable("item.irons_spellbooks.chronicle.chapter", 2).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(false)).append(
                    Component.translatable("item.irons_spellbooks.chronicle.chapter_2").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(true))
            );
            chroniclersPage.append("\n\n");
            pages.push(chroniclersPage);
            createChapterPages(pages, faithfulSouls);

            MutableComponent lostPage = Component.translatable("item.irons_spellbooks.chronicle.chapter", 3).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(false)).append(
                    Component.translatable("item.irons_spellbooks.chronicle.chapter_3").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(true))
            );
            lostPage.append("\n\n");
            pages.push(lostPage);
            createChapterPages(pages, lostSouls);

            chronicleCache.addAll(pages);
        }
        return chronicleCache;
    }

    public void clearCache() {
        this.chronicleCache = null;
    }

    private static Style styleForPledge(int pledge) {
        return switch (pledge) {
            case PLEDGE_WIZARD -> Style.EMPTY.withColor(0xdf7900).withBold(true).withUnderlined(false);
            case PLEDGE_ANCIENT_MAGICIAN ->
                    Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withBold(true).withUnderlined(false);
            default -> Style.EMPTY.withColor(0x9e5500).withBold(false).withUnderlined(false);
        };
    }

    private void resolveChronicleData(List<MutableComponent> lostSouls, List<MutableComponent> faithfulSouls, List<MutableComponent> loyalSouls) {
        List<ChronicleEntry> chronicleEntries = new ArrayList<>(PatreonData.getInstance().getChronicleEntries());
        chronicleEntries.sort(Comparator.comparing(ChronicleEntry::displayName));
        chronicleEntries.sort(Comparator.comparing(ChronicleEntry::pledge).reversed());
        for (ChronicleEntry entry : chronicleEntries) {
            String name = entry.displayName();
            MutableComponent line = Component.literal(name).withStyle(styleForPledge(entry.pledge()));
            switch (entry.bookCategory()) {
                case BOOK_LOST -> lostSouls.add(line);
                case BOOK_FAITHFUL -> faithfulSouls.add(line);
                case BOOK_LOYAL -> loyalSouls.add(line);
                default -> {
                }
            }
        }
        lastCachedDate = LocalDate.now();
    }

    private void createChapterPages(Stack<MutableComponent> pages, List<MutableComponent> entries) {
        int linecount = 3; // assume each chapter starts with title (2 lines + empty line)
        // i don't believe we can measure the text due to font not existing on the server (relevant for lecterns), so use general all-purpose formula instead
        int charWidth = 6; // bolded full-size char is 7
        int bookLimit = 114;
        for (Component component : entries) {
            int estLines = component.getString().length() * charWidth / bookLimit + 1;
            linecount += estLines;
            if (linecount > 14 - 1) {
                MutableComponent nextPage = Component.empty();
                pages.push(nextPage);
                linecount = estLines;
            }
            pages.peek().append(component).append("\n");
        }
    }
}

