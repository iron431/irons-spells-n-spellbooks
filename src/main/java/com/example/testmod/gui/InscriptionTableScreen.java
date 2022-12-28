package com.example.testmod.gui;

import com.example.testmod.TestMod;
import com.example.testmod.item.FireBallScroll;
import com.example.testmod.item.WimpySpellBook;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;

public class InscriptionTableScreen extends AbstractContainerScreen<InscriptionTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/inscription_table.png");
    //button locations
    private static final int INSCRIBE_BUTTON_X = 43;
    private static final int INSCRIBE_BUTTON_Y = 35;
    //slot indexes (vanilla inventory has 36 slots)
    private static final int SPELLBOOK_SLOT = 36 + 0;
    private static final int SCROLL_SLOT = 36 + 1;
    private static final int EXTRACTION_SLOT = 36 + 2;
    //location of the space to draw spell iconsd
    private static final int SPELL_BG_X = 67;
    private static final int SPELL_BG_Y = 15;
    private static final int SPELL_BG_WIDTH = 95;
    private static final int SPELL_BG_HEIGHT = 57;

    private static boolean isDirty;
    private static int temp_spell_count = 5;
    protected Button inscribeButton;
    //protected ArrayList<Button> spellSlotButtons;
    //protected ArrayList<Vec2> spellSlotRelativeLocations;
    //private AbstractSpell[] loadedSpells;
    private ItemStack lastSpellBookItem = null;
    protected ArrayList<SpellSlotInfo> spellSlots;
    private int selectedSpellIndex;
    private static String temp_log;

    public InscriptionTableScreen(InscriptionTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 256;
        this.imageHeight = 166;

    }

    @Override
    protected void init() {
        super.init();
        inscribeButton = this.addWidget(new Button(0, 0, 14, 14, CommonComponents.GUI_DONE, (p_169820_) -> this.onInscription()));
        spellSlots = new ArrayList<SpellSlotInfo>();
        //spellSlotButtons = new ArrayList<Button>();
        //spellSlotRelativeLocations = new ArrayList<Vec2>();
        selectedSpellIndex = -1;
        generateSpellSlots();
        isDirty = false;
    }

    private void generateSpellSlots() {
        //loadedSpells = null;
        for (SpellSlotInfo s : spellSlots)
            removeWidget(s.button);
        spellSlots.clear();
        if (!isSpellBookSlotted())
            return;

        var spellData = ((WimpySpellBook) menu.slots.get(SPELLBOOK_SLOT).getItem().getItem()).getSpellBookData();
        var storedSpells = spellData.getInscribedSpells();

        //loadedSpells=spellData.getInscribedSpells();

        int spellCount = spellData.getSpellSlots();
        if (spellCount > 15) {
            spellCount = 15;
        }
        if (spellCount <= 0) {
            return;
        }
        //renderSpellSlot(poseStack,center,mouseX,mouseY);
        int boxSize = 19;
        //SpellType[] TEMP_SPELL_LIST = {SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL};
        int[] row1 = new int[LAYOUT[spellCount - 1][0]];
        int[] row2 = new int[LAYOUT[spellCount - 1][1]];
        int[] row3 = new int[LAYOUT[spellCount - 1][2]];


        //ArrayList<Vec2> locations = new ArrayList<Vec2>();
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
                        this.addWidget(new Button((int) location.x, (int) location.y, boxSize, boxSize, new TextComponent(temp_index + ""), (p_169820_) -> this.setSelectedIndex(temp_index)))));
                //spellSlotButtons.add(this.addWidget(new Button((int) location.x, (int) location.y, boxSize, boxSize, new TextComponent(temp_index+""), (p_169820_) -> this.setSelectedIndex(temp_index))));
                //spellSlotRelativeLocations.add(location);
                index++;
            }
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);


        inscribeButton.active = isValidInscription();
        renderButtons(poseStack, mouseX, mouseY);

        if (menu.slots.get(SPELLBOOK_SLOT).getItem() != lastSpellBookItem) {
            onSpellBookSlotChanged();
            lastSpellBookItem = menu.slots.get(SPELLBOOK_SLOT).getItem();
        }
        renderSpells(poseStack, mouseX, mouseY);

    }

    private void renderSpells(PoseStack poseStack, int mouseX, int mouseY) {
        if (isDirty) {
            generateSpellSlots();
            isDirty = false;
        }
        Vec2 center = new Vec2(SPELL_BG_X + leftPos + SPELL_BG_WIDTH / 2, SPELL_BG_Y + topPos + SPELL_BG_HEIGHT / 2);

        for (int i = 0; i < spellSlots.size(); i++) {
            var spellSlot = spellSlots.get(i).button;
            var pos = spellSlots.get(i).relativePosition.add(center);
            spellSlot.x = (int) pos.x;
            spellSlot.y = (int) pos.y;
            renderSpellSlot(poseStack, pos, mouseX, mouseY, i, spellSlots.get(i).hasSpell());
            //spellSlot.render(poseStack,mouseX,mouseY,1f);
        }

    }


    private void onSpellBookSlotChanged() {
        isDirty = true;
        selectedSpellIndex = -1;
    }

    private void renderSpellSlot(PoseStack poseStack, Vec2 pos, int mouseX, int mouseY, int index, boolean hasSpell) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int iconToDraw = isHovering((int) pos.x, (int) pos.y, 19, 19, mouseX, mouseY) ? 38 : hasSpell ? 19 : 0;
        this.blit(poseStack, (int) pos.x, (int) pos.y, iconToDraw, 166, 19, 19);
        if (index == selectedSpellIndex)
            this.blit(poseStack, (int) pos.x, (int) pos.y, 57, 166, 19, 19);

    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY) {


        //
        //  Rendering inscription Button
        //
        inscribeButton.x = leftPos + INSCRIBE_BUTTON_X;
        inscribeButton.y = topPos + INSCRIBE_BUTTON_Y;
        if (inscribeButton.active) {
            if (hoveringInscribeButton(mouseX, mouseY)) {
                //highlighted
                this.blit(poseStack, inscribeButton.x, inscribeButton.y, 28, 185, 14, 14);
            } else {
                //regular
                this.blit(poseStack, inscribeButton.x, inscribeButton.y, 14, 185, 14, 14);
            }
        } else {
            //disabled
            this.blit(poseStack, inscribeButton.x, inscribeButton.y, 0, 185, 14, 14);
        }

    }

    private void onInscription() {
        //called when inscription button clicked
        TestMod.LOGGER.info("Inscribe!");

        ItemStack spellbookStack = menu.slots.get(SPELLBOOK_SLOT).getItem();
        ItemStack scroll = menu.slots.get(SCROLL_SLOT).getItem();
        var spellBookData = ((WimpySpellBook) spellbookStack.getItem()).getSpellBookData();

        //is this slot already taken?
        if(spellSlots.get(selectedSpellIndex).hasSpell()){
            TestMod.LOGGER.info("No space here");
            return;
        }
        //are we eligible? (rarity, other condidtions, etc)
        if(false)
            return;

        //slot new spell
        var tempSpellFromScrollReplaceMe = new BurningDashSpell();
        TestMod.LOGGER.info(spellbookStack.hashCode()+"");
        spellBookData.addSpell(tempSpellFromScrollReplaceMe,selectedSpellIndex);
        TestMod.LOGGER.info(spellbookStack.hashCode()+"");

        menu.slots.get(SCROLL_SLOT).remove(1);
        isDirty=true;

    }

    private void setSelectedIndex(int index) {
        selectedSpellIndex = index;
    }

    private boolean isValidInscription() {
        //other checks eventually go here (enough space, high enough rarity, etc)
        return selectedSpellIndex >= 0 && isSpellBookSlotted() && isScrollSlotted();
    }

    private boolean isSpellBookSlotted() {
        //switch to forge tags
        return menu.slots.get(SPELLBOOK_SLOT).hasItem() && menu.slots.get(SPELLBOOK_SLOT).getItem().getItem() instanceof WimpySpellBook;
    }

    private boolean isScrollSlotted() {
        //switch to forge tags
        return menu.slots.get(SCROLL_SLOT).hasItem() && menu.slots.get(SCROLL_SLOT).getItem().getItem() instanceof FireBallScroll;
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        renderTooltip(pPoseStack, mouseX, mouseY);
    }

    private boolean hoveringInscribeButton(int mouseX, int mouseY) {
        return isHovering(INSCRIBE_BUTTON_X + leftPos, INSCRIBE_BUTTON_Y + topPos, 14, 14, mouseX, mouseY);
    }

    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private final int[][] LAYOUT = {
            {1, 0, 0}, //1
            {2, 0, 0}, //2
            {2, 1, 0}, //3
            {2, 2, 0}, //4
            {3, 2, 0}, //5
            {3, 3, 0}, //6
            {4, 3, 0}, //7
            {4, 4, 0}, //8
            {3, 3, 3}, //9
            {3, 4, 3}, //10
            {4, 4, 3}, //11
            {4, 4, 4}, //12
            {4, 5, 4}, //13
            {5, 5, 4}, //14
            {5, 5, 5}  //15
    };

    private class SpellSlotInfo {
        public AbstractSpell containedSpell;
        public Vec2 relativePosition;
        public Button button;

        SpellSlotInfo(AbstractSpell containedSpell, Vec2 relativePosition, Button button) {
            this.containedSpell = containedSpell;
            this.relativePosition = relativePosition;
            this.button = button;
        }

        public boolean hasSpell() {
            return containedSpell != null;
        }
    }

}