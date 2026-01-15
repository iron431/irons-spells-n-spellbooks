package io.redspace.ironsspellbooks.block.transmog_table;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.patreon.statue.Color;
import io.redspace.ironsspellbooks.patreon.transmog.ITransmogPreview;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TransmogTableScreen extends AbstractContainerScreen<TransmogTableMenu> {
    public static final GameProfile TRANSMOG_PREVIEW = new GameProfile(UUID.fromString("db3ebb97-ab61-484d-ba69-001dc920a330"), "[Transmog Preview]");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/transmog_table.png");

    private static final int TRANSMOG_WINDOW_X = 116;
    private static final int TRANSMOG_WINDOW_Y = 8;
    private static final int TRANSMOG_WINDOW_WIDTH = 72;
    private static final int TRANSMOG_WINDOW_HEIGHT = 72;
    //    private static final int OPTIONS_WIDGET_SIZE = 24;
    private static final int TRANSMOG_OPTION_HEIGHT = 72 / 2;
    private static final int TRANSMOG_OPTION_WIDTH = 24;

    private static final int PREVIEW_WINDOW_X = 59;
    private static final int PREVIEW_WINDOW_Y = 10;
    private static final int PREVIEW_WINDOW_WIDTH = 50;
    private static final int PREVIEW_WINDOW_HEIGHT = 70;

    private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0, Mth.PI);

    private List<TransmogOption> transmogOptions = new ArrayList<>();
    private Button transmogForgeButton;
    private LivingEntity armorStandPreview;
    private LivingEntity playerPreview;
    private int scrollOffset;
    private boolean isScrollbarHeld;

    public TransmogTableScreen(TransmogTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        initPreviewEntities();
        this.menu.armorSlotsChangedCallback = this::onArmorSlotsChanged;
        this.menu.transmogSelectionChangedCallback = this::onSelectedTransmogChanged;
        this.imageWidth = 204;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        if (Minecraft.getInstance().player == null) {
            return;
        }
        PatreonPermissions permissions = PatreonHandler.getPatreonPermissions(Minecraft.getInstance().player);
        this.transmogForgeButton = Button.builder(Component.empty(), button -> {
            if (menu.clickMenuButton(Minecraft.getInstance().player, -99)) {
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, -99);
            }
        }).bounds(menu.transmogSlot.x + leftPos, menu.transmogSlot.y + topPos + 24, 16, 16).build();
        this.transmogOptions = new ArrayList<>();
        for (int i = 0; i < this.menu.transmogActions.size(); i++) {
            var action = this.menu.transmogActions.get(i);
//            int x = leftPos + TRANSMOG_WINDOW_X + i * TRANSMOG_OPTION_WIDTH;
//            int y = topPos + TRANSMOG_WINDOW_Y;
            int optionsPerRow = TRANSMOG_WINDOW_WIDTH / TRANSMOG_OPTION_WIDTH;
            int x = leftPos + TRANSMOG_WINDOW_X + (i % optionsPerRow) * TRANSMOG_OPTION_WIDTH;
            int y = topPos + TRANSMOG_WINDOW_Y + (i / optionsPerRow) * TRANSMOG_OPTION_HEIGHT;
            transmogOptions.add(new TransmogOption(Button.builder(Component.empty(), button -> {
                if (menu.clickMenuButton(Minecraft.getInstance().player, ((TransmogOption) button).index)) {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, ((TransmogOption) button).index);
                }
            }).bounds(x, y, TRANSMOG_OPTION_WIDTH, TRANSMOG_OPTION_HEIGHT), action, i, action.remove() || permissions.canUse(action.holder())));
        }
        onSelectedTransmogChanged();
        setScrollOffset(scrollOffset);
    }

    protected void initPreviewEntities() {
        this.armorStandPreview = new RemotePlayer(Minecraft.getInstance().level, TRANSMOG_PREVIEW);
        this.armorStandPreview.yBodyRot = 210.0F;
        this.armorStandPreview.yHeadRot = this.armorStandPreview.yBodyRot;
        this.armorStandPreview.yHeadRotO = this.armorStandPreview.yBodyRot;
        this.armorStandPreview.setInvisible(true);
        ((ITransmogPreview) this.armorStandPreview).irons_spellbooks$setTransmogPreview(true);


        // we want to reuse the main player, but still have "ghosting" abilities that don't actually affect the player entity. so we make a copy.
        this.playerPreview = new RemotePlayer(Minecraft.getInstance().level, Minecraft.getInstance().getGameProfile());
        ((ITransmogPreview) this.playerPreview).irons_spellbooks$setTransmogPreview(true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        EquipmentSlot previewSlot = null;
        if (!menu.transmogContainer.isEmpty() && menu.transmogContainer.getItem(0).getItem() instanceof Equipable equipable && equipable.getEquipmentSlot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            previewSlot = equipable.getEquipmentSlot();
        }
        resetArmorstandPreview();
        for (TransmogOption option : transmogOptions) {
            if (option.visible) {
                option.render(guiGraphics, mouseX, mouseY, partialTick);
                option.renderArmorPreview(guiGraphics, mouseX, mouseY, partialTick, armorStandPreview, previewSlot);
            }
        }
        transmogForgeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        int width = PREVIEW_WINDOW_WIDTH;
        int height = PREVIEW_WINDOW_HEIGHT;
        int x = leftPos + PREVIEW_WINDOW_X;//leftPos - width + 22;
        int y = topPos + PREVIEW_WINDOW_Y;//topPos;
        Color background = new Color(-267386864);
        Color borderTop = new Color(1347420415);
        Color borderBottom = new Color(1344798847);
        int alpha = 0xCCFFFFFF;
//        guiGraphics.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y + 4, width - 3, height - 3, 0, background.packedARGB() & alpha, background.packedARGB() & alpha, borderTop.packedARGB() & alpha, borderBottom.packedARGB() & alpha));
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x, y, x + width, y + height, (int) (30 * PREVIEW_WINDOW_WIDTH/50f), 0.0625F, mouseX, mouseY, playerPreview);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        for (TransmogOption button : this.transmogOptions) {
            if (button.isHovered() && button.isActive()) {
                guiGraphics.renderTooltip(this.font, button.getTooltip(Minecraft.getInstance().player), Optional.empty(), x, y);
                return;
            }
        }
        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        guiHelper.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        guiHelper.blitSprite(IronsSpellbooks.id("transmog_table/scroller"), leftPos + TRANSMOG_WINDOW_X + TRANSMOG_WINDOW_WIDTH + 2, topPos + TRANSMOG_WINDOW_Y + (int) ((scrollOffset / (float) getMaxScroll()) * (TRANSMOG_WINDOW_HEIGHT - 27)), 6, 27);
    }

    public void updateTransmogButtonStatus() {
        this.transmogForgeButton.active = menu.transmogSlot.hasItem() &&
                menu.getSelectedTransmogAction() != null && menu.getSelectedTransmogAction().canPerform(PatreonHandler.getPatreonPermissions(Minecraft.getInstance().player));
    }

    public void onSelectedTransmogChanged() {
        setupPlayerPreview();
        updateTransmogButtonStatus();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (transmogForgeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (TransmogOption option : transmogOptions) {
            if (option.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
//        int maxScroll = Math.max(0, transmogOptions.size() - 2); // can fit 3 rows without scrolling
//        int newScroll = Math.clamp(scrollOffset - (int) pScrollY, 0, maxScroll);
//        if (newScroll != scrollOffset) {
//            setScrollOffset(newScroll);
//            return true;
//        } else {
//            return false;
//        }
        int maxScroll = getMaxScroll();
        int newScroll = Math.clamp(scrollOffset - (int) pScrollY, 0, maxScroll);
        if (newScroll != scrollOffset) {
            setScrollOffset(newScroll);
            return true;
        } else {
            return false;
        }
    }

    private int getMaxScroll() {
        int optionsPerRow = TRANSMOG_WINDOW_WIDTH / TRANSMOG_OPTION_WIDTH;
        int rowsRequired = (int) Math.ceil(transmogOptions.size() / (double) optionsPerRow);
        return Math.max(0, rowsRequired - TRANSMOG_WINDOW_HEIGHT / TRANSMOG_OPTION_HEIGHT); // can fit 2 rows without scrolling
    }

    private void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
        int optionsPerRow = TRANSMOG_WINDOW_WIDTH / TRANSMOG_OPTION_WIDTH;
        int minIndex = scrollOffset * optionsPerRow;
        int optionsPerColumn = TRANSMOG_WINDOW_HEIGHT / TRANSMOG_OPTION_HEIGHT;
        int maxIndex = minIndex + optionsPerRow * optionsPerColumn;
        for (int i = 0; i < transmogOptions.size(); i++) {
            TransmogOption option = transmogOptions.get(i);
            option.setY(option.originalY - scrollOffset * TRANSMOG_OPTION_HEIGHT);
//            option.setX(option.originalX - scrollOffset * TRANSMOG_OPTION_WIDTH);
            if (i < minIndex || i >= maxIndex) {
                option.active = false;
                option.visible = false;
            } else {
                option.active = true;
                option.visible = true;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        return;
    }

    public void resetArmorstandPreview() {
        armorStandPreview.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        armorStandPreview.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        armorStandPreview.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        armorStandPreview.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
    }

    public void onArmorSlotsChanged() {
        setupPlayerPreview();
        updateTransmogButtonStatus();
    }

    public void setupPlayerPreview() {
        Player actualPlayer = Minecraft.getInstance().player;
        playerPreview.setItemSlot(EquipmentSlot.HEAD, actualPlayer.getItemBySlot(EquipmentSlot.HEAD));
        playerPreview.setItemSlot(EquipmentSlot.CHEST, actualPlayer.getItemBySlot(EquipmentSlot.CHEST));
        playerPreview.setItemSlot(EquipmentSlot.LEGS, actualPlayer.getItemBySlot(EquipmentSlot.LEGS));
        playerPreview.setItemSlot(EquipmentSlot.FEET, actualPlayer.getItemBySlot(EquipmentSlot.FEET));
        if (!menu.transmogContainer.isEmpty() && menu.transmogContainer.getItem(0).getItem() instanceof Equipable equipable && equipable.getEquipmentSlot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            ItemStack transmogPreview = menu.transmogContainer.getItem(0).copy();
            TransmogTableMenu.TransmogAction selectedTransmog = menu.getSelectedTransmogAction();
            if (selectedTransmog != null) {
                if (selectedTransmog.remove()) {
                    transmogPreview.remove(ComponentRegistry.TRANSMOG);
                } else {
                    transmogPreview.set(ComponentRegistry.TRANSMOG, selectedTransmog.holder());
                }
            }
            playerPreview.setItemSlot(equipable.getEquipmentSlot(), transmogPreview);
        }
    }

    class TransmogOption extends Button {
        final TransmogTableMenu.TransmogAction action;
        final int index;
        final boolean unlocked;
        final ItemStack[] previewItems;
        final int originalY, originalX;

        protected static final WidgetSprites T_SPRITES = new WidgetSprites(
                IronsSpellbooks.id("transmog_table/transmog_option"),
                IronsSpellbooks.id("transmog_table/transmog_option_disabled"),
                IronsSpellbooks.id("transmog_table/transmog_option_highlighted")
        );

        private static ItemStack createStack(Item item, TransmogHolder holder) {
            var stack = new ItemStack(item);
            stack.set(ComponentRegistry.TRANSMOG, holder);
            return stack;
        }

        TransmogOption(Builder builder, TransmogTableMenu.TransmogAction action, int index, boolean unlocked) {
            super(builder);
            this.action = action;
            this.index = index;
            this.unlocked = unlocked;
            this.previewItems = this.action.remove() ? null : new ItemStack[]{
                    createStack(Items.IRON_BOOTS, this.action.holder()),
                    createStack(Items.IRON_LEGGINGS, this.action.holder()),
                    createStack(Items.IRON_CHESTPLATE, this.action.holder()),
                    createStack(Items.IRON_HELMET, this.action.holder())
            };
            this.originalY = this.getY();
            this.originalX = this.getX();
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();
            guiGraphics.blitSprite(T_SPRITES.get(this.active && unlocked || hovered, hovered), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            boolean selected = menu.selectedTransmogIndex == this.index;
            if (selected) {
                guiGraphics.blitSprite(IronsSpellbooks.id("transmog_table/transmog_option_selected_frame"), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
        }

        protected void renderArmorPreview(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, LivingEntity armorStand, @Nullable EquipmentSlot equipmentSlot) {
            if (this.action.remove()) {
                // todo: render custom sprite or something
                return;
            }
            float scale = this.getWidth() / 16f * 12f;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(20 / 2, 24 / 2, 0);
            if (equipmentSlot == null) {
                // render whole armor set
                guiGraphics.pose().translate(0, 40 / 2, 0);
                scale *= 0.95f;
                EquipmentSlot[] armorSlots = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
                for (EquipmentSlot slot : armorSlots) {
                    ItemStack previewStack = previewItems[slot.getIndex()];
                    armorStand.setItemSlot(slot, previewStack);
                }
            } else {
                final float[] offsetForSlot = new float[]{8f, 12f, 22f, 28f/*0.65f, 1.05f, 1.7f, 2.05f*/};
                final float[] scaleForSlot = new float[]{1.35f, 1.2f, 1f, 1.2f};
                ItemStack previewStack = previewItems[equipmentSlot.getIndex()];
                armorStand.setItemSlot(equipmentSlot, previewStack);
                scale *= scaleForSlot[equipmentSlot.getIndex()];
                guiGraphics.pose().translate(12 / scale, offsetForSlot[equipmentSlot.getIndex()] * scale / 16f + 6 / scale, 0);
            }
            InventoryScreen.renderEntityInInventory(guiGraphics, this.getX(), this.getY(), scale,
                    new Vector3f(0, 0f, 0f),
                    ARMOR_STAND_ANGLE, null, armorStand);
            guiGraphics.pose().popPose();
        }

        public List<Component> getTooltip(LocalPlayer player) {
            List<Component> list = new ArrayList<>();
            if (this.action.remove()) {
                list.add(Component.translatable("tooltip.irons_spellbooks.transmog_option.remove"));
            } else {
                var holder = this.action.holder();
                boolean canUse = PatreonHandler.getPatreonPermissions(player).canUse(holder);
                list.add(Component.translatable("tooltip.irons_spellbooks.transmog_option.title").withStyle(ChatFormatting.LIGHT_PURPLE));
                list.add(Component.translatable(holder.descriptionId()).withStyle(ChatFormatting.ITALIC));
                list.add(Component.empty());
                list.add(Component.translatable("tooltip.irons_spellbooks.transmog_option.requirement").withStyle(canUse ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
            return list;
        }
    }
}