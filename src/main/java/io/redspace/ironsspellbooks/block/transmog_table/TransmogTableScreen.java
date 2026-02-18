package io.redspace.ironsspellbooks.block.transmog_table;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.mixin.PlayerAccessor;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.patreon.transmog.ITransmogPreview;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogItemData;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
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

    private static final int TRANSMOG_WINDOW_X = 118;
    private static final int TRANSMOG_WINDOW_Y = 6;
    private static final int TRANSMOG_WINDOW_WIDTH = 72;
    private static final int TRANSMOG_WINDOW_HEIGHT = 108;
    private static final int TRANSMOG_OPTION_HEIGHT = 36;
    private static final int TRANSMOG_OPTION_WIDTH = 24;

    private static final int PREVIEW_WINDOW_X = 60;
    private static final int PREVIEW_WINDOW_Y = 24;
    private static final int PREVIEW_WINDOW_WIDTH = 52;
    private static final int PREVIEW_WINDOW_HEIGHT = 84;

    private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0, Mth.PI);

    private List<TransmogOption> transmogOptions = new ArrayList<>();
    private Button transmogForgeButton;
    private LivingEntity armorStandPreview;
    private LivingEntity playerPreview;
    private int scrollOffset;
    private boolean isScrollbarHeld;

    private int dyeColor = -1;

    @Nullable ColorPickerScreen popupScreen;

    protected static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            IronsSpellbooks.id("gui/sprites/transmog_table/transmog_button_selected"),
            IronsSpellbooks.id("gui/sprites/transmog_table/transmog_button_disabled"),
            IronsSpellbooks.id("gui/sprites/transmog_table/transmog_button_highlighted")
    );

    public TransmogTableScreen(TransmogTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        initPreviewEntities();
        this.menu.armorSlotsChangedCallback = this::onArmorSlotsChanged;
        this.menu.transmogSelectionChangedCallback = this::onSelectedTransmogChanged;
        this.imageWidth = 204;
        this.imageHeight = 200;
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        super.renderSlot(guiGraphics, slot);
        if (slot instanceof TransmogArmorSlot armorSlot) {
            var itemstack = armorSlot.getItem();
            if (itemstack.isEmpty() && slot.isActive()) {
                guiGraphics.blitSprite(armorSlot.getEmptyIcon(), slot.x, slot.y, 16, 16);
            }
        }
    }

    private void openColorPicker() {
        this.popupScreen = new ColorPickerScreen(this::closeColorPicker, this::pickColor, dyeColor);
        this.popupScreen.init(this.minecraft, this.width, this.height);
        this.popupScreen.init();
    }

    private void closeColorPicker() {
        this.popupScreen = null;
    }

    private void pickColor(int color) {
        this.dyeColor = color;
    }

    @Override
    protected void init() {
        super.init();
        if (Minecraft.getInstance().player == null) {
            return;
        }
        PatreonPermissions permissions = PatreonHandler.getPatreonPermissions(Minecraft.getInstance().player);
        this.transmogForgeButton = Button.builder(Component.empty(), button -> {
            int id = menu.packTransmogRequest(dyeColor);
            if (menu.clickMenuButton(Minecraft.getInstance().player, id)) {
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(31 + leftPos, topPos + 77, 20, 20).build(
                b -> new Button(b) {
                    @Override
                    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        ResourceLocation sprite = IronsSpellbooks.id("transmog_table/transmog_button");
                        guiGraphics.blitSprite(BUTTON_SPRITES.get(active, isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                    }

                    @Override
                    public void playDownSound(SoundManager handler) {
                        handler.play(SimpleSoundInstance.forUI(SoundRegistry.TRANSMOG_TABLE_FORGE.get(), (float) (.9 + Math.random() * .25), .75f));
                    }
                }
        );
        this.transmogOptions = new ArrayList<>();
        for (int i = 0; i < this.menu.transmogActions.size(); i++) {
            var action = this.menu.transmogActions.get(i);
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
        if (popupScreen != null) {
            popupScreen.init();
        }
        openColorPicker();
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
        var key = PlayerAccessor.getDataPlayerModeCustomisation();
        this.playerPreview.getEntityData().set(key, Minecraft.getInstance().player.getEntityData().get(key));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        for (TransmogOption option : transmogOptions) {
            if (option.visible) {
                resetArmorstandPreview();
                option.setupArmorPreview(Minecraft.getInstance().player, armorStandPreview);
                option.renderArmorPreview(guiGraphics, mouseX, mouseY, partialTick, armorStandPreview, null);
            }
        }
        transmogForgeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = leftPos + PREVIEW_WINDOW_X;
        int y = topPos + PREVIEW_WINDOW_Y;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x, y, x + PREVIEW_WINDOW_WIDTH, y + PREVIEW_WINDOW_HEIGHT, (int) (30 * PREVIEW_WINDOW_WIDTH / 45f), 0.0625F * 3, mouseX, mouseY, playerPreview);
        if (popupScreen != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 500);
            popupScreen.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.pose().popPose();

        }
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
        guiHelper.blitSprite(IronsSpellbooks.id("transmog_table/scroller"), getScrollBarX(), getScrollBarY(), 6, 27);
    }

    private int getScrollBarX() {
        return leftPos + TRANSMOG_WINDOW_X + TRANSMOG_WINDOW_WIDTH + 2;
    }

    private int getScrollBarY() {
        return topPos + TRANSMOG_WINDOW_Y + (int) ((scrollOffset / (float) getMaxScroll()) * (TRANSMOG_WINDOW_HEIGHT - 27));
    }

    public void updateTransmogButtonStatus() {
        this.transmogForgeButton.active = menu.transmogSlot.hasItem() &&
                menu.getSelectedTransmogAction() != null && menu.getSelectedTransmogAction().canPerform(menu.transmogSlot.getItem(), PatreonHandler.getPatreonPermissions(Minecraft.getInstance().player));
    }

    public void onSelectedTransmogChanged() {
        this.dyeColor = -1;
        var transmog = menu.getSelectedTransmogAction();
        if (transmog != null && transmog.holder() != null) {
            this.dyeColor = transmog.holder().dyeConfig().defaultColor();
        }
        updateTransmogButtonStatus();
        setupPlayerPreview();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popupScreen != null && popupScreen.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (transmogForgeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (TransmogOption option : transmogOptions) {
            if (option.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (mouseX >= getScrollBarX() && mouseX < getScrollBarX() + 6 && mouseY >= getScrollBarY() && mouseY < getScrollBarY() + 27) {
            isScrollbarHeld = true;
            return true;
        } else {
            isScrollbarHeld = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        int maxScroll = getMaxScroll();
        int newScroll = Math.clamp(scrollOffset - (int) pScrollY, 0, maxScroll);
        if (newScroll != scrollOffset) {
            setScrollOffset(newScroll);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (popupScreen != null && popupScreen.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true;
        }
        int max = getMaxScroll();
        if (this.isScrollbarHeld) {
            int scrollZoneMin = topPos + TRANSMOG_WINDOW_Y;
            int scrollZoneMax = scrollZoneMin + TRANSMOG_WINDOW_HEIGHT;
            var scrollOffs = ((float) pMouseY - (float) scrollZoneMin - 7.5F) / ((float) (scrollZoneMax - scrollZoneMin) - 15.0F);
            scrollOffs = Mth.clamp(scrollOffs, 0.0F, 1.0F);
            int i = Math.max((int) ((double) (scrollOffs * (float) max) + 0.5D), 0);
            if (i != this.scrollOffset) {
                setScrollOffset(i);
            }
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (popupScreen != null && popupScreen.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (popupScreen != null && popupScreen.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
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
                    TransmogItemData.remove(transmogPreview);
                } else if (selectedTransmog.holder().supportsSlot(equipable.getEquipmentSlot())) {
                    TransmogItemData.set(transmogPreview, new TransmogItemData(selectedTransmog.holder(), dyeColor));
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

        private static ItemStack createStack(Item item, TransmogHolder holder) {
            var stack = new ItemStack(item);
            TransmogItemData.set(stack, new TransmogItemData(holder));
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
            boolean selected = menu.selectedTransmogIndex == this.index;
            ResourceLocation frameSprite = IronsSpellbooks.id("transmog_table/transmog_option");
            if (selected) {
                frameSprite = frameSprite.withSuffix("_selected");
            } else {
                if (!unlocked) {
                    frameSprite = frameSprite.withSuffix("_disabled");
                }
                if (hovered) {
                    frameSprite = frameSprite.withSuffix("_highlighted");
                }
            }
            if (!frameSprite.getPath().endsWith("option")) {
                frameSprite = frameSprite.withPrefix("gui/sprites/");
            }
            guiGraphics.blitSprite(frameSprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            if (!unlocked) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.blitSprite(IronsSpellbooks.id("transmog_table/lock"), this.getX() + this.getWidth() / 2 - 5, this.getY() + this.getHeight() / 2 - 7, 10, 14);
                guiGraphics.pose().popPose();
            } else if (action.remove()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.setColor(1f, 1f, 1f, 0.5f);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                guiGraphics.blitSprite(IronsSpellbooks.id("transmog_table/remove_transmog_overlay"), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                guiGraphics.setColor(1f, 1f, 1f, 1f);
                guiGraphics.pose().popPose();
            }
        }

        protected void setupArmorPreview(Player player, LivingEntity armorStand) {
            // setup armor items for rendering of each transmog option button
            for (EquipmentSlot slot : TransmogTableMenu.HUMANOID_ARMOR_SLOTS) {
                ItemStack previewStack;
                if (this.action.remove()) {
                    // if we are the "clear transmog" button, then copy the player's active armor
                    previewStack = player.getItemBySlot(slot).copy();
                    if (menu.transmogSlot.getItem().getItem() instanceof Equipable equipable && equipable.getEquipmentSlot() == slot) {
                        // if the "active item" in the current transmog slot fits into this armor slot, then use it instead
                        previewStack = menu.transmogSlot.getItem().copy();
                    }
                    TransmogItemData.remove(previewStack);
                } else {
                    // set to cached item with the transmog applied
                    previewStack = this.action.holder().supportsSlot(slot) ? previewItems[slot.getIndex()] : ItemStack.EMPTY;
                }
                armorStand.setItemSlot(slot, previewStack);
            }
        }

        protected void renderArmorPreview(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, LivingEntity armorStand, @Nullable EquipmentSlot equipmentSlot) {
            float scale = this.getWidth() / 16f * 9.5f;
            //fixme: the scissor is messing with the text of the tooltip...
//            guiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
            guiGraphics.pose().pushPose();

            InventoryScreen.renderEntityInInventory(guiGraphics, this.getX() + this.getWidth() / 2f, this.getY() + this.getHeight() / 2f, scale,
                    new Vector3f(0f, 0.97f, 0f),
                    ARMOR_STAND_ANGLE, null, armorStand);
            guiGraphics.pose().popPose();
            render(guiGraphics, mouseX, mouseY, partialTick);
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
                list.add(Component.translatable("tooltip.irons_spellbooks.transmog_option.requirement", Component.translatable(holder.requiredPermission().getDescriptionId()).withStyle(ChatFormatting.GOLD)).withStyle(canUse ? ChatFormatting.GREEN : ChatFormatting.RED));
                list.add(Component.empty());
                MutableComponent supportedSlotsList;
                if (action.holder().supportedSlots() == TransmogHolder.ALL_SLOTS) {
                    supportedSlotsList = Component.translatable("tooltip.irons_spellbooks.transmog_option.supported_slots.all");
                } else {
                    supportedSlotsList = Component.empty();
                    var slots = action.holder().supportedSlots();
                    var s = slots.size();
                    int i = 0;
                    for (EquipmentSlot slot : slots) {
                        supportedSlotsList.append(Component.translatable(String.format("tooltip.irons_spellbooks.transmog_option.supported_slots.%s", slot.getName())));
                        if (++i < s) {
                            supportedSlotsList.append(", ");
                        }
                    }
                }
                list.add(Component.translatable("tooltip.irons_spellbooks.transmog_option.supported_slots", supportedSlotsList).withStyle(ChatFormatting.GRAY));

            }
            return list;
        }

    }
}