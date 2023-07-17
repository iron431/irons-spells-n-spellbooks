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
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
        setSelectedSpell(SpellType.NONE_SPELL);
        resetList();
        super.onClose();
    }

    private void resetList() {
        if (!(!menu.getInkSlot().getItem().isEmpty() && (menu.getInkSlot().getItem().getItem() instanceof InkItem inkItem && inkItem.getRarity().compareRarity(ServerConfigs.getSpellConfig(selectedSpell).minRarity()) >= 0)))
            setSelectedSpell(SpellType.NONE_SPELL);
        //TODO: reorder setting old focus to test if we actually need to reset the spell... or just give ink its own path since we dont even need to regenerate the list anyways
        //TODO: update: what the fuck does that mean
        scrollOffset = 0;

        for (SpellCardInfo s : availableSpells)
            removeWidget(s.button);
        availableSpells.clear();

        //Messages.sendToServer(new ServerboundScrollForgeSelectSpell(this.menu.blockEntity.getBlockPos(), selectedSpell.getValue()));
    }

    @Override
    public void render(GuiGraphics guiHelper, int mouseX, int mouseY, float delta) {
        renderBackground(guiHelper);
        super.render(guiHelper, mouseX, mouseY, delta);
        renderTooltip(guiHelper, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        //setTexture(TEXTURE);

        guiHelper.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

//        if (lastFocusItem != menu.getFocusSlot().getItem()) {
//            generateSpellList();
//            lastFocusItem = menu.getFocusSlot().getItem();
//        }
        if (menuSlotsChanged())
            generateSpellList();
        renderSpellList(guiHelper, partialTick, mouseX, mouseY);
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

    private void renderSpellList(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        ItemStack inkStack = menu.getInkSlot().getItem();

        SpellRarity inkRarity = getRarityFromInk(inkStack.getItem());

        availableSpells.sort((a, b) -> ServerConfigs.getSpellConfig(a.spell).minRarity().compareRarity(ServerConfigs.getSpellConfig(b.spell).minRarity()));

        List<FormattedCharSequence> additionalTooltip = null;
        for (int i = 0; i < availableSpells.size(); i++) {
            SpellCardInfo spellCard = availableSpells.get(i);

            if (i - scrollOffset >= 0 && i - scrollOffset < 3) {
                spellCard.button.active = inkRarity != null && spellCard.spell.getMinRarity() <= inkRarity.getValue();
                int x = leftPos + SPELL_LIST_X;
                int y = topPos + SPELL_LIST_Y + (i - scrollOffset) * 19;
                spellCard.button.setX(x);
                spellCard.button.setY(y);
                spellCard.draw(this, guiHelper, x, y, mouseX, mouseY);
                if (additionalTooltip == null)
                    additionalTooltip = spellCard.getTooltip(x, y, mouseX, mouseY);
            } else {
                spellCard.button.active = false;
            }
        }
        if (additionalTooltip != null) {
            guiHelper.renderTooltip(font, additionalTooltip, mouseX, mouseY);
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
            for (int i = 0; i < spells.size(); i++) {
                //int id = spells[i].getValue();
                int tempIndex = i;
                //IronsSpellbooks.LOGGER.debug("ScrollForgeScreen.generateSpellList: {} isEnabled: {}", spells[i], spells[i].isEnabled());
                if (spells.get(i).isEnabled())
                    availableSpells.add(new SpellCardInfo(spells.get(i), i + 1, i, this.addWidget(
                            new Button.Builder(spells.get(i).getDisplayName(), (b) -> this.setSelectedSpell(spells.get(tempIndex))).pos(0, 0).size(108, 19).build()
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

//    private void setTexture(ResourceLocation texture) {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, texture);
//    }

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

        void draw(ScrollForgeScreen screen, GuiGraphics guiHelper, int x, int y, int mouseX, int mouseY) {
            if (this.button.active) {
                if (spell == screen.getSelectedSpell())//mouseX >= x && mouseY >= y && mouseX < x + 108 && mouseY < y + 19)
                    guiHelper.blit(TEXTURE, x, y, 0, 204, 108, 19);
                else
                    guiHelper.blit(TEXTURE, x, y, 0, 166, 108, 19);

            } else {
                guiHelper.blit(TEXTURE, x, y, 0, 185, 108, 19);
                //font.drawWordWrap(, x + 2, y + 2, maxWidth, 0xFFFFFF);
            }
            //setTexture(this.button.active ? spell.getResourceLocation() : SpellType.NONE_SPELL.getResourceLocation());
            guiHelper.blit(this.button.active ? spell.getResourceLocation() : SpellType.NONE_SPELL.getResourceLocation(), x + 108 - 18, y + 1, 0, 0, 16, 16, 16, 16);

            int maxWidth = 108 - 20;
            var text = trimText(font, getDisplayName().withStyle(this.button.active ? Style.EMPTY : Style.EMPTY.withFont(RUNIC_FONT)), maxWidth);
            int textX = x + 2;
            int textY = y + 3;
            guiHelper.drawWordWrap(font, text, textX, textY, maxWidth, 0xFFFFFF);

            //button.render(poseStack,mouseX,mouseY,1);
        }

        @Nullable
        List<FormattedCharSequence> getTooltip(int x, int y, int mouseX, int mouseY) {
            var text = getDisplayName();
            int textX = x + 2;
            int textY = y + 3;
            if (mouseX >= textX && mouseY >= textY && mouseX < textX + font.width(text) && mouseY < textY + font.lineHeight) {
                return getHoverText();
            } else {
                return null;
            }

        }

        List<FormattedCharSequence> getHoverText() {
            if (!this.button.active) {
                return List.of(FormattedCharSequence.forward(Component.translatable("ui.irons_spellbooks.ink_rarity_error").getString(), Style.EMPTY));
            } else {
                return TooltipsUtils.createSpellDescriptionTooltip(this.spell, font);
            }
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
