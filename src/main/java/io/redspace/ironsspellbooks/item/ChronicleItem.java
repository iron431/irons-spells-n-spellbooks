package io.redspace.ironsspellbooks.item;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

public class ChronicleItem extends ReadableLoreItem {

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
            boolean success = resolveChronicleData(lostSouls, faithfulSouls, loyalSouls);
            if (!success) {
                chronicleCache.add(Component.literal("Failed to fetch Patreon Data :(").withStyle(ChatFormatting.RED));
                return chronicleCache;
            }
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

    private boolean resolveChronicleData(List<MutableComponent> lostSouls, List<MutableComponent> faithfulSouls, List<MutableComponent> loyalSouls) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URI("https://code.redspace.io/data/chronicle_data.json").toURL().openStream()))) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            int format = json.get("format").getAsInt();
            if (format != 1) {
                // todo: create mapping structure when format count surpasses 1
                throw new IllegalStateException("Unsupported data format: " + format);
            }
            lastCachedDate = LocalDate.now();
            int entry = 0;
            JsonArray entries = json.getAsJsonArray("values");
            for (JsonElement e : entries) {
                try {
                    entry++;
                    JsonObject object = e.getAsJsonObject();
                    int bookCategory = object.get("category").getAsInt();
                    int activeTier = object.get("type").getAsInt();
                    String name = object.get("name").getAsString();
                    Style style = switch (activeTier) {
                        case 2 -> Style.EMPTY.withColor(0xdf7900).withBold(true).withUnderlined(false); // Wizard
                        case 3 ->
                                Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withBold(true).withUnderlined(false); // Ancient Magician
                        default -> Style.EMPTY.withColor(0x9e5500).withBold(false).withUnderlined(false); // Acolyte
                    };
                    MutableComponent component = Component.literal(name).withStyle(style);
                    switch (bookCategory) {
                        case 0:
                            lostSouls.add(component);
                            break;
                        case 1:
                            faithfulSouls.add(component);
                            break;
                        case 2:
                            loyalSouls.add(component);
                            break;
                    }
                } catch (Exception exception) {
                    IronsSpellbooks.LOGGER.error("Failed to handle chronicle member entry {}: {}", entry, exception.getMessage());
                }

            }
            reader.close();
        } catch (Exception ex) {
            IronsSpellbooks.LOGGER.error("Failed to handle Chronicle Data: {}", ex.toString());
            return false;
        }
        Comparator<MutableComponent> comparator = Comparator.comparing(c -> c.getString().toLowerCase(Locale.ROOT));
        lostSouls.sort(comparator);
        faithfulSouls.sort(comparator);
        loyalSouls.sort(comparator);
        return true;
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

