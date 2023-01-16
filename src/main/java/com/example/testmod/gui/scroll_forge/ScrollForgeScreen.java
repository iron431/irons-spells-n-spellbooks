package com.example.testmod.gui.scroll_forge;

import com.example.testmod.TestMod;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.ModTags;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ScrollForgeScreen extends AbstractContainerScreen<ScrollForgeMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/scroll_forge.png");
    private static final int SPELL_LIST_X = 89;
    private static final int SPELL_LIST_Y = 15;

    private List<SpellType> availableSpells;
    private ItemStack lastFocusItem = ItemStack.EMPTY;

    private int selectedSpell;
    private int scrollOffset;

    public ScrollForgeScreen(ScrollForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 218;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        availableSpells = new ArrayList<>();
        super.init();
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
        renderSpellList(poseStack,partialTick,mouseX,mouseY);
        TestMod.LOGGER.debug("{}", this.menu.getFocusSlot().getItem().getItem().toString());

    }

    private void renderSpellList(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        for (int i = 0; i < availableSpells.size(); i++) {
            //if (i < menu.getAvailableSpells().length){
            int x = leftPos + SPELL_LIST_X;
            int y = topPos + SPELL_LIST_Y + i * 19;
            this.blit(poseStack, x, y, 0, 166, 108, 19);
            font.draw(poseStack, availableSpells.get(i).getDisplayName().getString(), x, y, 0xFFFFFF);
            //}

        }
    }

    private List<SpellType> generateSpellList() {
        availableSpells.clear();
        selectedSpell = -1;
        ItemStack focusStack = menu.getFocusSlot().getItem();
        TestMod.LOGGER.info("ScrollForgeMenu.generateSpellSlots.focus: {}", focusStack.getItem().toString());
        if (!focusStack.isEmpty() && focusStack.is(ModTags.SCHOOL_FOCUS)) {
            //TestMod.LOGGER.info("ScrollForgeMenu.generateSpellSlots.focus tag success");

            SchoolType school = SchoolType.getSchoolFromItem(focusStack);
            TestMod.LOGGER.info("ScrollForgeMenu.generateSpellSlots.school: {}", school.toString());
            for (SpellType spellType : SpellType.getSpellsFromSchool(school)) {
                availableSpells.add(spellType);
            }
            TestMod.LOGGER.info("ScrollForgeMenu.generateSpellSlots.spells: {}", ArrayUtils.toString(availableSpells));

        }
        return availableSpells;
    }

    private void setTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }
}
