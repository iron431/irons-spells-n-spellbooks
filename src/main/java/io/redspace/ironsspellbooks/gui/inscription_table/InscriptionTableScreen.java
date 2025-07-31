package io.redspace.ironsspellbooks.gui.inscription_table;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.player.ClientRenderCache;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;

public class InscriptionTableScreen extends AbstractContainerScreen<InscriptionTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/inscription_table.png");
    //button locations
    private static final int INSCRIBE_BUTTON_X = 43;
    private static final int INSCRIBE_BUTTON_Y = 35;
    private static final int EXTRACT_BUTTON_X = 188;
    private static final int EXTRACT_BUTTON_Y = 137;
    //slot indexes (vanilla inventory has 36 slots)
    private static final int SPELLBOOK_SLOT = 36 + 0;
    private static final int SCROLL_SLOT = 36 + 1;
    private static final int EXTRACTION_SLOT = 36 + 2;
    //locations to draw spell icons
    private static final int SPELL_BG_X = 67;
    private static final int SPELL_BG_Y = 15;
    private static final int SPELL_BG_WIDTH = 95;
    private static final int SPELL_BG_HEIGHT = 57;

    private static final int LORE_PAGE_X = 176;
    private static final int LORE_PAGE_WIDTH = 80;
    private boolean isDirty;
    protected Button inscribeButton;
    //protected Button extractButton;
    private ItemStack lastSpellBookItem = ItemStack.EMPTY;
    protected ArrayList<SpellSlotInfo> spellSlots;
    private int selectedSpellIndex = -1;
    private int inscriptionErrorCode = 0;

    public InscriptionTableScreen(InscriptionTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 166;

    }

    @Override
    protected void init() {
        super.init();
        inscribeButton = this.addWidget(
                Button.builder(CommonComponents.GUI_DONE, (p_169820_) -> this.onInscription()).bounds(0, 0, 14, 14).build()
        );
        //extractButton = this.addWidget(new Button(0, 0, 14, 14, CommonComponents.GUI_DONE, (p_169820_) -> this.removeSpell()));
        spellSlots = new ArrayList<>();
        //Ironsspellbooks.logger.debug("InscriptionTableScreen: init");
        generateSpellSlots();
    }

    @Override
    public void onClose() {
        super.onClose();
        resetSelectedSpell();
    }

    @Override
    public void render(GuiGraphics guiHelper, int mouseX, int mouseY, float delta) {
        try {
            renderBackground(guiHelper);
            super.render(guiHelper, mouseX, mouseY, delta);
            renderTooltip(guiHelper, mouseX, mouseY);
        } catch (Exception ignore) {
            onClose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        //setTexture(TEXTURE);

        guiHelper.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);


        inscribeButton.active = isValidInscription() && inscriptionErrorCode == 0;
        //extractButton.active = isValidExtraction();
        renderButtons(guiHelper, mouseX, mouseY);

        if (menu.slots.get(SPELLBOOK_SLOT).getItem() != lastSpellBookItem) {
            onSpellBookSlotChanged();
            lastSpellBookItem = menu.slots.get(SPELLBOOK_SLOT).getItem();
        }


        renderSpells(guiHelper, mouseX, mouseY);
        renderLorePage(guiHelper, partialTick, mouseX, mouseY);

        //Error Message
        if (menu.slots.get(SPELLBOOK_SLOT).hasItem())
            inscriptionErrorCode = getErrorCode();
        else
            inscriptionErrorCode = 0;

        if (inscriptionErrorCode > 0) {
            //X over arrow
            guiHelper.blit(TEXTURE, leftPos + 35, topPos + 51, 0, 213, 28, 22);
            if (isHovering(leftPos + 35, topPos + 51, 28, 22, mouseX, mouseY)) {
                guiHelper.renderTooltip(font, getErrorMessage(inscriptionErrorCode), mouseX, mouseY);
            }
        }
    }

    private int getErrorCode() {


        if (menu.getSpellBookSlot().getItem().getItem() instanceof SpellBook spellbook && menu.getScrollSlot().getItem().getItem() instanceof Scroll scroll) {
            var scrollContainer = ISpellContainer.get(menu.getScrollSlot().getItem());
            var spellSlot = scrollContainer.getSpellAtIndex(0);
            if (spellbook.getRarity().compareRarity(spellSlot.getSpell().getRarity(spellSlot.getLevel())) < 0)
                return 1;
        }

        return 0;
    }

    private Component getErrorMessage(int code) {
        if (code == 1)
            return Component.translatable("ui.irons_spellbooks.inscription_table_rarity_error");
        else
            return Component.empty();
    }

    private void renderSpells(GuiGraphics guiHelper, int mouseX, int mouseY) {
        if (isDirty) {
            generateSpellSlots();
        }
        Vec2 center = new Vec2(SPELL_BG_X + leftPos + SPELL_BG_WIDTH / 2, SPELL_BG_Y + topPos + SPELL_BG_HEIGHT / 2);

        for (int i = 0; i < spellSlots.size(); i++) {
            var spellSlot = spellSlots.get(i).button;
            var pos = spellSlots.get(i).relativePosition.add(center);
            spellSlot.setX((int) pos.x);
            spellSlot.setY((int) pos.y);
            renderSpellSlot(guiHelper, pos, mouseX, mouseY, i, spellSlots.get(i));
            //spellSlot.render(poseStack,mouseX,mouseY,1f);
        }

    }

    private void renderButtons(GuiGraphics guiHelper, int mouseX, int mouseY) {
        //
        //  Rendering inscription Button
        //
        inscribeButton.setX(leftPos + INSCRIBE_BUTTON_X);
        inscribeButton.setY(topPos + INSCRIBE_BUTTON_Y);
        if (inscribeButton.active) {
            if (isHovering(inscribeButton.getX(), inscribeButton.getY(), 14, 14, mouseX, mouseY)) {
                //highlighted
                guiHelper.blit(TEXTURE, inscribeButton.getX(), inscribeButton.getY(), 28, 185, 14, 14);
            } else {
                //regular
                guiHelper.blit(TEXTURE, inscribeButton.getX(), inscribeButton.getY(), 14, 185, 14, 14);
            }
        } else {
            //disabled
            guiHelper.blit(TEXTURE, inscribeButton.getX(), inscribeButton.getY(), 0, 185, 14, 14);
        }

    }

    private void renderSpellSlot(GuiGraphics guiHelper, Vec2 pos, int mouseX, int mouseY, int index, SpellSlotInfo slot) {
        //setTexture(TEXTURE);
        boolean hovering = isHovering((int) pos.x, (int) pos.y, 19, 19, mouseX, mouseY);
        int iconToDraw = hovering ? 38 : slot.hasSpell() ? 19 : 0;
        guiHelper.blit(TEXTURE, (int) pos.x, (int) pos.y, iconToDraw, 166, 19, 19);
        if (slot.hasSpell()) {
            drawSpellIcon(guiHelper, pos, slot);
            if (hovering && !slot.spellData.canRemove())
                guiHelper.blit(TEXTURE, (int) pos.x, (int) pos.y, 76, 166, 19, 19);
        }
        if (index == selectedSpellIndex)
            guiHelper.blit(TEXTURE, (int) pos.x, (int) pos.y, 57, 166, 19, 19);
    }

    private void drawSpellIcon(GuiGraphics guiHelper, Vec2 pos, SpellSlotInfo slot) {
        //setTexture(slot.containedSpell.getSpellType().getResourceLocation());
        guiHelper.blit(slot.spellData.getSpell().getSpellIconResource(), (int) pos.x + 2, (int) pos.y + 2, 0, 0, 15, 15, 16, 16);
    }

    private void renderLorePage(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        int x = leftPos + LORE_PAGE_X;
        int y = topPos;
        int margin = 2;
        var textColor = Style.EMPTY.withColor(0x322c2a);
        var poseStack = guiHelper.pose();
        //
        // Title
        //
        boolean spellSelected = selectedSpellIndex >= 0 && selectedSpellIndex < spellSlots.size() && spellSlots.get(selectedSpellIndex).hasSpell();
        var title = selectedSpellIndex < 0 ? Component.translatable("ui.irons_spellbooks.no_selection") : spellSelected ? spellSlots.get(selectedSpellIndex).spellData.getSpell().getDisplayName(Minecraft.getInstance().player) : Component.translatable("ui.irons_spellbooks.empty_slot");
        //font.drawWordWrap(title.withStyle(ChatFormatting.UNDERLINE).withStyle(textColor), titleX, titleY, LORE_PAGE_WIDTH, 0xFFFFFF);

        var titleLines = font.split(title.withStyle(ChatFormatting.UNDERLINE).withStyle(textColor), LORE_PAGE_WIDTH);
        int titleY = topPos + 10;

        for (FormattedCharSequence line : titleLines) {
            int titleWidth = font.width(line);
            int titleX = x + (LORE_PAGE_WIDTH - titleWidth) / 2;
            guiHelper.drawString(font, line, titleX, titleY, 0xFFFFFF, false);

            //show description if hovering
            if (spellSelected && isHovering(titleX, titleY, titleWidth, font.lineHeight, mouseX, mouseY)) {
                guiHelper.renderTooltip(font, TooltipsUtils.createSpellDescriptionTooltip(spellSlots.get(selectedSpellIndex).spellData.getSpell(), font), mouseX, mouseY);
            }

            //increment y for next line
            titleY += font.lineHeight;
        }
        var titleHeight = font.wordWrapHeight(title.withStyle(ChatFormatting.UNDERLINE).withStyle(textColor), LORE_PAGE_WIDTH);
        int descLine = /*y + titleHeight + font.lineHeight*/titleY + 4;

        if (selectedSpellIndex < 0 || selectedSpellIndex >= spellSlots.size() || !spellSlots.get(selectedSpellIndex).hasSpell()) {
            return;
        }
        //good orange color: 0xe2701b
        //okay green color: 0x30bf30
        //good mana color:0x448fff)
        //var colorLevel = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
        var colorMana = Style.EMPTY.withColor(0x0044a9);
        var colorCast = Style.EMPTY.withColor(0x115511);
        var colorCooldown = Style.EMPTY.withColor(0x115511);
        var spell = spellSlots.get(selectedSpellIndex).spellData.getSpell();
        var spellLevel = spellSlots.get(selectedSpellIndex).spellData.getLevel();
        float textScale = 1f;
        float reverseScale = 1 / textScale;

        Component school = spell.getSchoolType().getDisplayName();
        poseStack.scale(textScale, textScale, textScale);


        //
        //  School
        //
        drawTextWithShadow(font, guiHelper, school, x + (LORE_PAGE_WIDTH - font.width(school.getString())) / 2, descLine, 0xFFFFFF, 1);
        descLine += font.lineHeight * textScale;

        //
        // Level
        //
        var levelText = Component.translatable("ui.irons_spellbooks.level", spellLevel).withStyle(textColor);
        guiHelper.drawString(font, levelText, x + (LORE_PAGE_WIDTH - font.width(levelText.getString())) / 2, descLine, 0xFFFFFF, false);
        descLine += font.lineHeight * textScale * 2;

        //
        // Mana
        //
        descLine += drawStatText(font, guiHelper, x + margin, descLine, "ui.irons_spellbooks.mana_cost", textColor, Component.translatable(spell.getManaCost(spellLevel) + ""), colorMana, textScale);

        //
        // Cast Time
        //

        descLine += drawText(font, guiHelper, TooltipsUtils.getCastTimeComponent(spell.getCastType(), Utils.timeFromTicks(spell.getEffectiveCastTime(spellLevel, null), 1)), x + margin, descLine, textColor.getColor().getValue(), textScale);

        //
        // Cooldown
        //
        descLine += drawStatText(font, guiHelper, x + margin, descLine, "ui.irons_spellbooks.cooldown", textColor, Component.translatable(Utils.timeFromTicks(spell.getSpellCooldown(), 1)), colorCooldown, textScale);


        //
        //  Unique Info
        //
        for (MutableComponent component : spell.getUniqueInfo(spellLevel, null)) {
            descLine += drawText(font, guiHelper, component, x + margin, descLine, textColor.getColor().getValue(), 1);
        }


        poseStack.scale(reverseScale, reverseScale, reverseScale);
    }

    private void drawTextWithShadow(Font font, GuiGraphics guiHelper, Component text, int x, int y, int color, float scale) {
        x /= scale;
        y /= scale;
        guiHelper.drawString(font, text, x, y, color);
    }

    private int drawText(Font font, GuiGraphics guiHelper, Component text, int x, int y, int color, float scale) {
        x /= scale;
        y /= scale;
        guiHelper.drawWordWrap(font, text, x, y, LORE_PAGE_WIDTH, color);
        return font.wordWrapHeight(text, LORE_PAGE_WIDTH);

    }

    private int drawStatText(Font font, GuiGraphics guiHelper, int x, int y, String translationKey, Style textStyle, MutableComponent stat, Style statStyle, float scale) {
//        x /= scale;
//        y /= scale;
        return drawText(font, guiHelper, Component.translatable(translationKey, stat.withStyle(statStyle)).withStyle(textStyle), x, y, 0xFFFFFF, scale);
    }

    private void generateSpellSlots() {
        /*
         Reset Per-Book info
         */
        for (SpellSlotInfo s : spellSlots)
            removeWidget(s.button);
        spellSlots.clear();
        if (!isSpellBookSlotted())
            return;

        var spellBookSlot = menu.slots.get(SPELLBOOK_SLOT);
        var spellBookItemStack = spellBookSlot.getItem();

        var spellBookContainer = ISpellContainer.get(spellBookItemStack);

        var storedSpells = spellBookContainer.getAllSpells();
        int spellCount = spellBookContainer.getMaxSpellCount();
        if (spellCount > 15) {
            spellCount = 15;
        }
        if (spellCount <= 0) {
            return;
        }
        /*
         Calculate and save spell slot positions on the screen
         */
        int boxSize = 19;
        int[] rowCounts = ClientRenderCache.getRowCounts(spellCount);
        int[] row1 = new int[rowCounts[0]];
        int[] row2 = new int[rowCounts[1]];
        int[] row3 = new int[rowCounts[2]];

        int[] rowWidth = {
                boxSize * row1.length,
                boxSize * row2.length,
                boxSize * row3.length
        };
        int[] rowHeight = {
                row1.length > 0 ? boxSize : 0,
                row2.length > 0 ? boxSize : 0,
                row3.length > 0 ? boxSize : 0
        };

        int overallHeight = rowHeight[0] + rowHeight[1] + rowHeight[2];


        int[][] display = {row1, row2, row3};
        int index = 0;
        for (int row = 0; row < display.length; row++) {
            for (int column = 0; column < display[row].length; column++) {
                int offset = -rowWidth[row] / 2;
                Vec2 location = new Vec2(offset + column * boxSize, (row) * boxSize - (overallHeight / 2));
                location.add(-9);
                int temp_index = index;
                spellSlots.add(new SpellSlotInfo(storedSpells[index],
                        location,
                        this.addWidget(
                                Button.builder(Component.translatable(temp_index + ""), (p_169820_) -> this.setSelectedIndex(temp_index)).pos((int) location.x, (int) location.y).size(boxSize, boxSize).build()
                        )
                ));
                index++;
            }
        }
        /*
         Unflag as Dirty
         */
        isDirty = false;
    }

    private void onSpellBookSlotChanged() {
        isDirty = true;
        var spellBookStack = menu.slots.get(SPELLBOOK_SLOT).getItem();
        if (spellBookStack.getItem() instanceof SpellBook) {
            var spellBookContainer = ISpellContainer.get(spellBookStack);
            if (spellBookContainer.getMaxSpellCount() <= selectedSpellIndex) {
                resetSelectedSpell();
            }
        } else {
            resetSelectedSpell();
        }
    }

    private void onInscription() {
        //
        //  Called when inscription button clicked
        //

        if (menu.getSpellBookSlot().getItem().getItem() instanceof SpellBook spellBook && menu.getScrollSlot().getItem().getItem() instanceof Scroll scroll) {

            //  Is the spell book bricked?
            if (spellSlots.isEmpty())
                return;

            var scrollContainer = ISpellContainer.get(menu.getScrollSlot().getItem());
            var scrollSlot = scrollContainer.getSpellAtIndex(0);

            //  Is the spellbook a high enough rarity?
            if (spellBook.getRarity().compareRarity(scrollSlot.getSpell().getRarity(scrollSlot.getLevel())) < 0)
                return;

            //  Quick inscribe
            if (selectedSpellIndex < 0 || spellSlots.get(selectedSpellIndex).hasSpell()) {
                for (int i = selectedSpellIndex + 1; i < spellSlots.size(); i++) {
                    if (!spellSlots.get(i).hasSpell()) {
                        setSelectedIndex(i);
                        break;
                    }
                }
            }

            //sanitize
            setSelectedIndex(Mth.clamp(selectedSpellIndex, 0, spellSlots.size() - 1));

            //  Is this slot already taken?
            if (spellSlots.get(selectedSpellIndex).hasSpell()) {
                return;
            }

            //
            //  Good to inscribe
            //

            isDirty = true;
//            Messages.sendToServer(new ServerboundInscribeSpell(menu.blockEntity.getBlockPos(), selectedSpellIndex));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -1);
        }


    }

    private void setSelectedIndex(int index) {
        selectedSpellIndex = index;
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);

//        Messages.sendToServer(new ServerboundInscriptionTableSelectSpell(this.menu.blockEntity.getBlockPos(), selectedSpellIndex));
    }

    private void resetSelectedSpell() {
        setSelectedIndex(-1);
    }

    private boolean isValidInscription() {
        return isSpellBookSlotted() && isScrollSlotted();
    }

    private boolean isValidExtraction() {
        return selectedSpellIndex >= 0 && spellSlots.get(selectedSpellIndex).hasSpell() && !menu.slots.get(EXTRACTION_SLOT).hasItem();
    }

    private boolean isSpellBookSlotted() {
        return menu.slots.get(SPELLBOOK_SLOT).getItem().getItem() instanceof SpellBook;
    }

    private boolean isScrollSlotted() {
        //is "hasItem" necessary? at what point does null break this?
        return menu.slots.get(SCROLL_SLOT).hasItem() && menu.slots.get(SCROLL_SLOT).getItem().getItem() instanceof Scroll;
    }

    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private final int[][] LAYOUT = ClientRenderCache.SPELL_LAYOUT;

    private static class SpellSlotInfo {
        public SpellData spellData;
        public Vec2 relativePosition;
        public Button button;

        SpellSlotInfo(SpellData spellData, Vec2 relativePosition, Button button) {
            this.spellData = spellData;
            this.relativePosition = relativePosition;
            this.button = button;
        }

        public boolean hasSpell() {
            return spellData != null && !spellData.equals(SpellData.EMPTY);
        }
    }

}