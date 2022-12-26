package com.example.testmod.gui;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.mana.network.PacketCastSpell;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.WimpySpellBook;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Logger;

@Mod.EventBusSubscriber(Dist.CLIENT)
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
    protected ArrayList<Button> spellSlotButtons;
    protected ArrayList<Vec2> spellSlotRelativeLocations;
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
        spellSlotButtons = new ArrayList<Button>();
        spellSlotRelativeLocations = new ArrayList<Vec2>();
        selectedSpellIndex = -1;
        generateSpellSlots(temp_spell_count);
        isDirty=false;
    }

    private void generateSpellSlots(int incomingSpellCount) {
        for (Button b : spellSlotButtons)
            removeWidget(b);
        spellSlotButtons.clear();
        spellSlotRelativeLocations.clear();

        if (incomingSpellCount > 15) {
            incomingSpellCount = 15;
        }
        if (incomingSpellCount <= 0) {
            return;
        }
        //renderSpellSlot(poseStack,center,mouseX,mouseY);
        int boxSize = 19;
        //SpellType[] TEMP_SPELL_LIST = {SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL,SpellType.FIREBALL_SPELL};
        int[] row1 = new int[LAYOUT[incomingSpellCount - 1][0]];
        int[] row2 = new int[LAYOUT[incomingSpellCount - 1][1]];
        int[] row3 = new int[LAYOUT[incomingSpellCount - 1][2]];


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
                spellSlotButtons.add(this.addWidget(new Button((int) location.x, (int) location.y, boxSize, boxSize, new TextComponent(temp_index+""), (p_169820_) -> this.setSelectedIndex(temp_index))));
                spellSlotRelativeLocations.add(location);
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

        renderSpells(poseStack, mouseX, mouseY);

    }

    protected void renderSpells(PoseStack poseStack, int mouseX, int mouseY) {
        if(isDirty){
            generateSpellSlots(temp_spell_count);
            isDirty=false;
        }
        Vec2 center = new Vec2(SPELL_BG_X + leftPos + SPELL_BG_WIDTH / 2, SPELL_BG_Y + topPos + SPELL_BG_HEIGHT / 2);

        for (int i = 0; i < spellSlotButtons.size(); i++){
            var spellSlot = spellSlotButtons.get(i);
            var pos = spellSlotRelativeLocations.get(i).add(center);
            spellSlot.x = (int)pos.x;
            spellSlot.y = (int)pos.y;
            renderSpellSlot(poseStack,pos,mouseX,mouseY,i);
            //spellSlot.render(poseStack,mouseX,mouseY,1f);
        }

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

    protected void renderSpellSlot(PoseStack poseStack, Vec2 pos, int mouseX, int mouseY,int index) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, (int) pos.x, (int) pos.y, isHovering((int) pos.x, (int) pos.y, 19, 19, mouseX, mouseY) ? 38 : 0, 166, 19, 19);
        if(index==selectedSpellIndex)
            this.blit(poseStack, (int) pos.x, (int) pos.y, 57, 166, 19, 19);

    }

    protected void renderButtons(PoseStack poseStack, int mouseX, int mouseY) {
        int buttonX = leftPos + INSCRIBE_BUTTON_X;
        int buttonY = topPos + INSCRIBE_BUTTON_Y;
        inscribeButton.x = buttonX;
        inscribeButton.y = buttonY;


        //
        //  Rendering inscription Button
        //
        if (inscribeButton.active) {
            if (hoveringInscribeButton(mouseX, mouseY)) {
                //highlighted
                this.blit(poseStack, buttonX, buttonY, 28, 185, 14, 14);
            } else {
                //regular
                this.blit(poseStack, buttonX, buttonY, 14, 185, 14, 14);
            }
        } else {
            //disabled
            this.blit(poseStack, buttonX, buttonY, 0, 185, 14, 14);
        }

        inscribeButton.renderButton(poseStack, mouseX, mouseY, 1f);
    }

    public void onInscription() {
        //called when inscription button clicked
        TestMod.LOGGER.info("Inscribe!");
        //inscribeButton.active = false;
    }

    public void setSelectedIndex(int index) {
        selectedSpellIndex = index;
    }

    private boolean isValidInscription() {
        //other checks eventually go here (enough space, high enough rarity, etc)
        return hasSpellBookSlotted() && hasScrollSlotted();
    }

    private boolean hasSpellBookSlotted() {
        //switch to forge tags
        return menu.slots.get(SPELLBOOK_SLOT).hasItem() && menu.slots.get(SPELLBOOK_SLOT).getItem().getItem() instanceof WimpySpellBook;
    }

    private boolean hasScrollSlotted() {
        //switch to forge tags
        return menu.slots.get(SCROLL_SLOT).hasItem() && menu.slots.get(SCROLL_SLOT).getItem().getItem() instanceof Scroll;
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

    //    private class SpellInscriptionHandler implements Button.OnPress {
//
//        @Override
//        public void onPress(Button p_93751_) {
//            TestMod.LOGGER.info("Inscribe");
//        }
//    }
    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent e) {
        if (e.getKey() == (int) 'T' && e.getAction() == 1) {
            temp_spell_count--;
            isDirty = true;
        }
        if (e.getKey() == (int) 'Y' && e.getAction() == 1) {
            temp_spell_count++;
            isDirty = true;
        }
        if (e.getKey() == (int) 'L' && e.getAction() == 1) {
            TestMod.LOGGER.info(temp_log);
        }

    }
}