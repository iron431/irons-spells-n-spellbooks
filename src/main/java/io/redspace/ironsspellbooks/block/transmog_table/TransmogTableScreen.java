package io.redspace.ironsspellbooks.block.transmog_table;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransmogTableScreen extends AbstractContainerScreen<TransmogTableMenu> {
    public static final GameProfile TRANSMOG_PREVIEW = new GameProfile(UUID.fromString("db3ebb97-ab61-484d-ba69-001dc920a330"), "[Transmog Preview]");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/transmog_table.png");

    private static final int OPTIONS_X = 82;
    private static final int OPTIONS_Y = 8;
    private static final int OPTIONS_WIDTH = 71;
    private static final int OPTIONS_HEIGHT = 71;
    private static final int OPTIONS_WIDGET_SIZE = 24;
    private LivingEntity armorStandPreview;
    private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0, Mth.PI);

    protected void initArmorstand() {
//        this.armorStandPreview = new ArmorStand(Minecraft.getInstance().level, 0.0, 0.0, 0.0);
//        this.armorStandPreview.setNoBasePlate(true);
        this.armorStandPreview = new RemotePlayer(Minecraft.getInstance().level, TRANSMOG_PREVIEW);
        this.armorStandPreview.yBodyRot = 210.0F;
//        this.armorStandPreview.setXRot(25.0F);
        this.armorStandPreview.yHeadRot = this.armorStandPreview.yBodyRot;
        this.armorStandPreview.yHeadRotO = this.armorStandPreview.yBodyRot;
        this.armorStandPreview.setInvisible(true);
    }

    class TransmogOption extends Button {
        final TransmogHolder holder;
        final int index;
        final boolean locked;
        final ItemStack[] previewItems;

        private static ItemStack createStack(Item item, TransmogHolder holder) {
            var stack = new ItemStack(item);
            stack.set(ComponentRegistry.TRANSMOG, holder);
            return stack;
        }

        TransmogOption(Builder builder, TransmogHolder holder, int index, boolean locked) {
            super(builder);
            this.holder = holder;
            this.index = index;
            this.locked = locked;
            this.previewItems = new ItemStack[]{
                    createStack(Items.IRON_BOOTS, this.holder),
                    createStack(Items.IRON_LEGGINGS, this.holder),
                    createStack(Items.IRON_CHESTPLATE, this.holder),
                    createStack(Items.IRON_HELMET, this.holder)
            };
        }

        protected void renderArmorPreview(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, LivingEntity armorStand, EquipmentSlot equipmentSlot) {
            final float[] offsetForSlot = new float[]{8f, 12f, 22f, 28f/*0.65f, 1.05f, 1.7f, 2.05f*/};
            final float[] scaleForSlot = new float[]{1.35f, 1.2f, 1f, 1.2f};
            ItemStack previewStack = previewItems[equipmentSlot.getIndex()];
            armorStand.setItemSlot(equipmentSlot, previewStack);
            float scale = this.getWidth() / 16f * 12f * scaleForSlot[equipmentSlot.getIndex()];
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, offsetForSlot[equipmentSlot.getIndex()] * scale / 16f, 0);
            InventoryScreen.renderEntityInInventory(guiGraphics, this.getX(), this.getY(), scale,
                    new Vector3f(12 / scale, 6 / scale, 0f),
                    ARMOR_STAND_ANGLE, null, armorStand);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        armorStandPreview.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        armorStandPreview.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        armorStandPreview.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        armorStandPreview.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        for (TransmogOption option : options) {
            option.render(guiGraphics, mouseX, mouseY, partialTick);
            option.renderArmorPreview(guiGraphics, mouseX, mouseY, partialTick, armorStandPreview, EquipmentSlot.CHEST);
        }
    }

    public TransmogTableScreen(TransmogTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        initArmorstand();
    }

    List<TransmogOption> options = new ArrayList<>();

    @Override
    protected void init() {
        super.init();
        if (Minecraft.getInstance().player == null) {
            return;
        }
        PatreonPermissions permissions = PatreonHandler.getPatreonPermissions(Minecraft.getInstance().player);
        options = new ArrayList<>();
        int widgetsPerRow = OPTIONS_WIDTH / OPTIONS_WIDGET_SIZE;
        for (int i = 0; i < this.menu.allTransmogs.size(); i++) {
            var holder = this.menu.allTransmogs.get(i);
            int x = leftPos + OPTIONS_X + (i % widgetsPerRow) * OPTIONS_WIDGET_SIZE;
            int y = topPos + OPTIONS_Y + (i / widgetsPerRow) * OPTIONS_WIDGET_SIZE;
            options.add(new TransmogOption(Button.builder(Component.empty(), button -> {
                if (menu.clickMenuButton(Minecraft.getInstance().player, ((TransmogOption) button).index)) {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, ((TransmogOption) button).index);
                }
            }).bounds(x, y, OPTIONS_WIDGET_SIZE, OPTIONS_WIDGET_SIZE), holder, i, permissions.canUse(holder)));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (TransmogOption option : options) {
            if (option.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    protected void renderBg(GuiGraphics guiHelper, float partialTick, int mouseX, int mouseY) {
        guiHelper.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        return;
    }
}