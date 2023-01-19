package com.example.testmod.gui.scroll_forge;

import com.example.testmod.TestMod;
import com.example.testmod.gui.scroll_forge.network.PacketSpellListSelection;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.spells.SpellRarity;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.ModTags;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ScrollForgeScreen extends AbstractContainerScreen<ScrollForgeMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/scroll_forge.png");
    private static final int SPELL_LIST_X = 89;
    private static final int SPELL_LIST_Y = 15;
    public static final ResourceLocation RUNIC_FONT = new ResourceLocation("illageralt");

    private List<SpellCardInfo> availableSpells;
    private ItemStack lastFocusItem = ItemStack.EMPTY;

    private int selectedSpellIndex;
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
        resetList();
        super.onClose();
    }

    private void resetList() {
        selectedSpellIndex = -1;
        scrollOffset = 0;

        for (SpellCardInfo s : availableSpells)
            removeWidget(s.button);
        availableSpells.clear();

        Messages.sendToServer(new PacketSpellListSelection(this.menu.blockEntity.getBlockPos(), 0));
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

        if (lastFocusItem != menu.getFocusSlot().getItem()) {
            generateSpellList();
            lastFocusItem = menu.getFocusSlot().getItem();
        }
        renderSpellList(poseStack, partialTick, mouseX, mouseY);
        //TestMod.LOGGER.debug("{}", this.menu.getFocusSlot().getItem().getItem().toString());

    }

    private void renderSpellList(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        //TODO: get real ink rarity
        SpellRarity inkRarity = SpellRarity.COMMON;
        for (int i = 0; i < availableSpells.size(); i++) {
            SpellCardInfo spellCard = availableSpells.get(i);

            if (i - scrollOffset >= 0 && i - scrollOffset < 3) {
                spellCard.button.active = true;//spellCard.s;
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
        TestMod.LOGGER.debug("SCROLLING: {}", direction);
        int length = availableSpells.size();
        int newScroll = scrollOffset - (int) direction;
        if (newScroll <= length - 3 && newScroll >= 0) {
            scrollOffset -= direction;
            return true;
        } else {
            return false;
        }
    }

    private List<SpellCardInfo> generateSpellList() {
        this.resetList();

        ItemStack focusStack = menu.getFocusSlot().getItem();
        TestMod.LOGGER.info("ScrollForgeMenu.generateSpellSlots.focus: {}", focusStack.getItem());
        if (!focusStack.isEmpty() && focusStack.is(ModTags.SCHOOL_FOCUS)) {
            SchoolType school = SchoolType.getSchoolFromItem(focusStack);
            TestMod.LOGGER.info("ScrollForgeMenu.generateSpellSlots.school: {}", school.toString());
            var spells = SpellType.getSpellsFromSchool(school);
            for (int i = 0; i < spells.length; i++) {
                int temp_index = i;
                availableSpells.add(new SpellCardInfo(spells[i], i + 1, i, this.addWidget(new Button((int) 0, (int) 0, 108, 19, Component.translatable(i + ""), (p_169820_) -> this.setSelectedIndex(temp_index)))));
            }
            TestMod.LOGGER.info("ScrollForgeMenu.generateSpellSlots.spells: {}", ArrayUtils.toString(availableSpells));

        }
        return availableSpells;
    }

    private void setSelectedIndex(int index) {
        selectedSpellIndex = index;
        Messages.sendToServer(new PacketSpellListSelection(this.menu.blockEntity.getBlockPos(), availableSpells.get(index).spell.getValue()));

        TestMod.LOGGER.debug("ScrollForgeScreen: setting selected spell: {}", availableSpells.get(index).getDisplayName());
    }

    public int getSelectedSpellIndex() {
        return selectedSpellIndex;
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
            this.rarity = SpellRarity.getSpellRarity(AbstractSpell.getSpell(spell, spellLevel));
        }

        void draw(ScrollForgeScreen screen, PoseStack poseStack, int x, int y, int mouseX, int mouseY) {
            setTexture(TEXTURE);
            if (this.button.active) {
                if (index == screen.getSelectedSpellIndex())//mouseX >= x && mouseY >= y && mouseX < x + 108 && mouseY < y + 19)
                    screen.blit(poseStack, x, y, 0, 204, 108, 19);
                else
                    screen.blit(poseStack, x, y, 0, 166, 108, 19);
                font.draw(poseStack, getDisplayName(), x + 2, y + 2, 0xFFFFFF);

            } else {
                screen.blit(poseStack, x, y, 0, 185, 108, 19);
                font.draw(poseStack, getDisplayName().withStyle(Style.EMPTY.withFont(RUNIC_FONT)), x + 2, y + 2, 0xFFFFFF);
            }
            if (mouseX >= x && mouseY >= y && mouseX < x + 108 && mouseY < y + 19) {
                screen.renderTooltip(poseStack, getHoverText(), mouseX, mouseY);
            }
        }

        MutableComponent getHoverText() {
            return this.button.active ? getDisplayName() : Component.translatable("ink_rarity_error");
        }

        MutableComponent getDisplayName() {
            return spell.getDisplayName();
        }
    }
}
