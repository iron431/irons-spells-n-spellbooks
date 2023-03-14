package io.redspace.ironsspellbooks.gui.scroll_forge;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.gui.scroll_forge.network.ServerboundScrollForgeSelectSpell;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ModTags;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ScrollForgeScreen extends AbstractContainerScreen<ScrollForgeMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/scroll_forge.png");
    private static final int SPELL_LIST_X = 89;
    private static final int SPELL_LIST_Y = 15;
    public static final ResourceLocation RUNIC_FONT = new ResourceLocation("illageralt");

    private List<SpellCardInfo> availableSpells;
    private ItemStack[] oldMenuSlots = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    private SpellType selectedSpell = SpellType.NONE_SPELL;
    private int scrollOffset;

    public ScrollForgeScreen(ScrollForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 218;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        availableSpells = new ArrayList<>();
        generateSpellList();
        super.init();
    }

    @Override
    public void onClose() {
        selectedSpell = SpellType.NONE_SPELL;
        resetList();
        super.onClose();
    }

    private void resetList() {
        if (!(!menu.getInkSlot().getItem().isEmpty() && (menu.getInkSlot().getItem().getItem() instanceof InkItem inkItem && inkItem.getRarity().compareRarity(ServerConfigs.getSpellConfig(selectedSpell).MIN_RARITY) >= 0)))
            selectedSpell = SpellType.NONE_SPELL;
        //TODO: reorder setting old focus to test if we actually need to reset the scroll... or just give ink its own path since we dont even need to regenerate the list anyways
        scrollOffset = 0;

        for (SpellCardInfo s : availableSpells)
            removeWidget(s.button);
        availableSpells.clear();

        Messages.sendToServer(new ServerboundScrollForgeSelectSpell(this.menu.blockEntity.getBlockPos(), selectedSpell.getValue()));
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        renderTooltip(pPoseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        setTexture(TEXTURE);

        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

//        if (lastFocusItem != menu.getFocusSlot().getItem()) {
//            generateSpellList();
//            lastFocusItem = menu.getFocusSlot().getItem();
//        }
        if (menuSlotsChanged())
            generateSpellList();
        renderSpellList(poseStack, partialTick, mouseX, mouseY);
        //irons_spellbooks.LOGGER.debug("{}", this.menu.getFocusSlot().getItem().getItem().toString());

    }

    private boolean menuSlotsChanged() {
        if (menu.getInkSlot().getItem().getItem() != oldMenuSlots[0].getItem() || /*menu.getBlankScrollSlot().getItem().getItem() != oldMenuSlots[1].getItem() || */menu.getFocusSlot().getItem().getItem() != oldMenuSlots[2].getItem()) {
            oldMenuSlots = new ItemStack[]{
                    menu.getInkSlot().getItem(),
                    menu.getBlankScrollSlot().getItem(),
                    menu.getFocusSlot().getItem()
            };
            return true;
        } else
            return false;
    }

    private void renderSpellList(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        ItemStack inkStack = menu.getInkSlot().getItem();

        SpellRarity inkRarity = getRarityFromInk(inkStack.getItem());

        availableSpells.sort((a, b) -> ServerConfigs.getSpellConfig(a.spell).MIN_RARITY.compareRarity(ServerConfigs.getSpellConfig(b.spell).MIN_RARITY));

        for (int i = 0; i < availableSpells.size(); i++) {
            SpellCardInfo spellCard = availableSpells.get(i);

            if (i - scrollOffset >= 0 && i - scrollOffset < 3) {
                spellCard.button.active = inkRarity != null && spellCard.spell.getMinRarity() <= inkRarity.getValue();
                int x = leftPos + SPELL_LIST_X;
                int y = topPos + SPELL_LIST_Y + (i - scrollOffset) * 19;
                spellCard.button.x = x;
                spellCard.button.y = y;
                spellCard.draw(this, poseStack, x, y, mouseX, mouseY);
            } else {
                spellCard.button.active = false;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double direction) {
        int length = availableSpells.size();
        int newScroll = scrollOffset - (int) direction;
        if (newScroll <= length - 3 && newScroll >= 0) {
            scrollOffset -= direction;
            return true;
        } else {
            return false;
        }
    }

    public void generateSpellList() {
        this.resetList();

        ItemStack focusStack = menu.getFocusSlot().getItem();
        IronsSpellbooks.LOGGER.info("ScrollForgeMenu.generateSpellSlots.focus: {}", focusStack.getItem());
        if (!focusStack.isEmpty() && focusStack.is(ModTags.SCHOOL_FOCUS)) {
            SchoolType school = SchoolType.getSchoolFromItem(focusStack);
            //irons_spellbooks.LOGGER.info("ScrollForgeMenu.generateSpellSlots.school: {}", school.toString());
            var spells = SpellType.getSpellsFromSchool(school);
            for (int i = 0; i < spells.length; i++) {
                //int id = spells[i].getValue();
                int tempIndex = i;
                //IronsSpellbooks.LOGGER.debug("ScrollForgeScreen.generateSpellList: {} isEnabled: {}", spells[i], spells[i].isEnabled());
                if (spells[i].isEnabled())
                    availableSpells.add(new SpellCardInfo(spells[i], i + 1, i, this.addWidget(
                            new Button(0, 0, 108, 19,
                                    spells[i].getDisplayName(),
                                    (b) -> this.setSelectedSpell(spells[tempIndex]))
                    )));
            }
        }
    }

    private void setSelectedSpell(SpellType spell) {
        selectedSpell = spell;
        Messages.sendToServer(new ServerboundScrollForgeSelectSpell(this.menu.blockEntity.getBlockPos(), spell.getValue()));

        //irons_spellbooks.LOGGER.debug("ScrollForgeScreen: setting selected spell: {}", availableSpells.get(index).getDisplayName());
    }

    private SpellRarity getRarityFromInk(Item ink) {
        if (ink instanceof InkItem inkItem)
            return inkItem.getRarity();
        else
            return null;

    }

    public SpellType getSelectedSpell() {
        return selectedSpell;
    }

    private void setTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }

    private class SpellCardInfo {
        SpellType spell;
        int spellLevel;
        SpellRarity rarity;
        Button button;
        int index;

        SpellCardInfo(SpellType spell, int spellLevel, int index, Button button) {
            this.spell = spell;
            this.spellLevel = spellLevel;
            this.index = index;
            this.button = button;
            this.rarity = spell.getRarity(spellLevel);
        }

        void draw(ScrollForgeScreen screen, PoseStack poseStack, int x, int y, int mouseX, int mouseY) {
            setTexture(TEXTURE);
            int maxWidth = 108 - 20;
            //var hoverText = new HoverEvent(HoverEvent.Action.SHOW_TEXT, getHoverText());
            var text = trimText(font, getDisplayName().withStyle(this.button.active ? Style.EMPTY : Style.EMPTY.withFont(RUNIC_FONT)), maxWidth);
            if (this.button.active) {
                if (spell == screen.getSelectedSpell())//mouseX >= x && mouseY >= y && mouseX < x + 108 && mouseY < y + 19)
                    screen.blit(poseStack, x, y, 0, 204, 108, 19);
                else
                    screen.blit(poseStack, x, y, 0, 166, 108, 19);

            } else {
                screen.blit(poseStack, x, y, 0, 185, 108, 19);
                //font.drawWordWrap(, x + 2, y + 2, maxWidth, 0xFFFFFF);
            }
            setTexture(this.button.active ? spell.getResourceLocation() : SpellType.NONE_SPELL.getResourceLocation());
            screen.blit(poseStack, x + 108 - 18, y + 1, 0, 0, 16, 16, 16, 16);

            int textX = x + 2;
            int textY = y + 3;
            font.drawWordWrap(text, textX, textY, maxWidth, 0xFFFFFF);

            if (mouseX >= textX && mouseY >= textY && mouseX < textX + font.width(text) && mouseY < textY + font.lineHeight) {
                screen.renderTooltip(poseStack, getHoverText(), mouseX, mouseY);
            }
            //button.render(poseStack,mouseX,mouseY,1);
        }

        MutableComponent getHoverText() {
            return this.button.active ? getDisplayName() : Component.translatable("ui.irons_spellbooks.ink_rarity_error");
        }

        private FormattedText trimText(Font font, Component component, int maxWidth) {
            var text = font.getSplitter().splitLines(component, maxWidth, component.getStyle()).get(0);
            if (text.getString().length() < component.getString().length())
                text = FormattedText.composite(text, FormattedText.of("..."));
            return text;
        }

        MutableComponent getDisplayName() {
            return spell.getDisplayName();
        }
    }
}
