package io.redspace.ironsspellbooks.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.network.ServerboundLearnSpell;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.eldritch.AbstractEldritchSpell;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class EldritchResearchScreen extends Screen {
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/eldritch_research_window.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;

    int leftPos, topPos;

    public EldritchResearchScreen(Component pTitle) {
        super(pTitle);
    }

    List<AbstractSpell> learnableSpells;
    List<SpellNode> nodes;
    SyncedSpellData playerData;

    protected void init() {
        learnableSpells = SpellRegistry.getSpellsForSchool(SchoolRegistry.ELDRITCH.get());
        if (this.minecraft != null) {
            playerData = ClientMagicData.getSyncedSpellData(minecraft.player);
        }
        this.leftPos = (this.width - WINDOW_WIDTH) / 2;
        this.topPos = (this.height - WINDOW_HEIGHT) / 2;
        nodes = new ArrayList<>();
        float f = 6.282f / learnableSpells.size();
        for (int i = 0; i < learnableSpells.size(); i++) {
            float r = 35;
            int x = leftPos + WINDOW_WIDTH / 2 - 8 + (int) (r * Mth.cos(f * i));
            int y = topPos + WINDOW_HEIGHT / 2 - 8 + (int) (r * Mth.sin(f * i));
            nodes.add(new SpellNode(learnableSpells.get(i), x, y));
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        super.render(poseStack, mouseX, mouseY, pPartialTick);
        this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        setTexture(WINDOW_LOCATION);
        this.blit(poseStack, leftPos, topPos, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        float f = 6.282f / learnableSpells.size();
        for (int i = 0; i < nodes.size(); i++) {
            setTexture(nodes.get(i).spell.getSpellIconResource());
            int x = nodes.get(i).x;
            int y = nodes.get(i).y;
            this.blit(poseStack, x, y, 0, 0, 16, 16, 16, 16);
            if (isHovering(x, y, 16, 16, mouseX, mouseY)) {
                renderTooltip(poseStack, buildTooltip(nodes.get(i).spell, font), mouseX, mouseY);
            }
        }
    }

    private static final Component ALREADY_LEARNED = Component.translatable("ui.irons_spellbooks.research_already_learned").withStyle(ChatFormatting.GREEN);
    private static final Component UNLEARNED = Component.translatable("ui.irons_spellbooks.research_warning").withStyle(ChatFormatting.RED);

    public static List<FormattedCharSequence> buildTooltip(AbstractSpell spell, Font font) {
        boolean learned = spell instanceof AbstractEldritchSpell eldritchSpell && eldritchSpell.isLearned(Minecraft.getInstance().player);
        var name = spell.getDisplayName(null).withStyle(learned ? ChatFormatting.GREEN : ChatFormatting.RED);
        var description = font.split(Component.translatable(String.format("%s.guide", spell.getComponentId())).withStyle(ChatFormatting.GRAY), 180);
        var hoverText = new ArrayList<FormattedCharSequence>();
        hoverText.add(FormattedCharSequence.forward(name.getString(), name.getStyle().withUnderlined(true)));
        hoverText.addAll(description);
        hoverText.add(FormattedCharSequence.EMPTY);
        hoverText.add((learned ? ALREADY_LEARNED : UNLEARNED).getVisualOrderText());
        return hoverText;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (SpellNode node : nodes) {
            if (isHovering(node.x, node.y, 16, 16, (int) pMouseX, (int) pMouseY)) {
                //Minecraft.getInstance().player.sendSystemMessage(node.spell.getDisplayName(minecraft.player));
                Messages.sendToServer(new ServerboundLearnSpell((byte) 1, node.spell.getSpellId()));
                break;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void setTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    record SpellNode(AbstractSpell spell, int x, int y) {
    }
}
