package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class ChronicleItem extends ReadableLoreItem {


    private List<Component> chronicleCache;
    private LocalDate lastCachedDate;

    public ChronicleItem(Properties pProperties) {
        super(IronsSpellbooks.id("textures/entity/lectern/archevoker_logbook.png"), pProperties);
    }

    @Override
    public List<Component> getPages(ItemStack stack) {
        if (chronicleCache == null  || (lastCachedDate != null && lastCachedDate.isBefore(LocalDate.now().minusDays(1)))) {
            chronicleCache = new ArrayList<>();
            try {
                var url = new URI("https://iron.wiki/img/chronicle_data.txt").toURL();
                try (BufferedReader reader = new BufferedReader(/*new StringReader(ChronicleData.exampleData)*/new InputStreamReader(url.openStream()))) {
                    List<MutableComponent> loyalSouls = new ArrayList<>();
                    List<MutableComponent> chroniclers = new ArrayList<>();
                    List<MutableComponent> lostSouls = new ArrayList<>();
                    String s = reader.readLine();
                    try {
                        if (!s.startsWith("format")) {
                            throw new RuntimeException();
                        }
                        int formatVersion = Integer.parseInt(s.split(" ")[1]);
                        if (formatVersion != 0) {
                            // no format delineation yet. maybe even never.
                            throw new RuntimeException();
                        }
                        String date = reader.readLine();
                        lastCachedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        int entry = 0;
                        //parse data
                        while ((s = reader.readLine()) != null) {
                            entry++;
                            String[] split = s.split(" ", 3);
                            if (split.length < 3) {
                                IronsSpellbooks.LOGGER.error("Malformatted patreon data on entry {}, skipping", entry);
                                continue;
                            }
                            int bookCategory = Integer.parseInt(split[0]);
                            int activeTier = Integer.parseInt(split[1]);
                            String name = split[2];
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
                                    chroniclers.add(component);
                                    break;
                                case 2:
                                    loyalSouls.add(component);
                                    break;
                            }
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
                        createChapterPages(pages, chroniclers);

                        MutableComponent lostPage = Component.translatable("item.irons_spellbooks.chronicle.chapter", 3).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(false)).append(
                                Component.translatable("item.irons_spellbooks.chronicle.chapter_3").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withUnderlined(true))
                        );
                        lostPage.append("\n\n");
                        pages.push(lostPage);
                        createChapterPages(pages, lostSouls);

                        chronicleCache.addAll(pages);
//                    pages.forEach(comp -> IronsSpellbooks.LOGGER.debug(comp.getString()));

                    } catch (RuntimeException e) {
                        throw new RuntimeException("Failed to parse format version on patreon data. Entire file treated as invalid!");
                    }

                    reader.close();
                } catch (IOException ex) {
                }
            }catch (Exception e){}

        }
        return chronicleCache;
    }

    private void createChapterPages(Stack<MutableComponent> pages, List<MutableComponent> entries) {
        int linecount = 3; // assume each chapter starts with title (2 lines + empty line)
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

