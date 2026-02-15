package io.redspace.ironsspellbooks.patreon.transmog;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.armor.GenericArmorModel;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.util.MemoizedSupplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.HashMap;
import java.util.Objects;

public class TransmogClientHandler {
    private static final HashMap<ResourceLocation, MemoizedSupplier<GeoArmorRenderer<?>>> TRANSMOGS;

    static {
        TRANSMOGS = new HashMap<>();
        TRANSMOGS.put(IronsSpellbooks.id("rogue"), new MemoizedSupplier<>(() -> new GenericCustomArmorRenderer<>(new GenericArmorModel<>(
                IronsSpellbooks.MODID, "transmog/rogue"
        )).hideHat().hideJacket()));

        TRANSMOGS.put(IronsSpellbooks.id("rogue_2"), new MemoizedSupplier<>(() -> new GenericCustomArmorRenderer<>(new GenericArmorModel<>(
                IronsSpellbooks.id(String.format("geo/%s_armor.geo.json", "transmog/rogue")),
                IronsSpellbooks.id(String.format("textures/models/armor/%s.png", "transmog/rogue_two"))
        )).hideHat().hideJacket()));

        TRANSMOGS.put(IronsSpellbooks.id("sorcerer"), new MemoizedSupplier<>(() -> new GenericCustomArmorRenderer<>(new GenericArmorModel<>(
                IronsSpellbooks.MODID, "transmog/sorcerer"
        )).dyeable()));
    }

    private static boolean isTransmogRenderActive;

    public static boolean isTransmogRenderActive() {
        return RenderSystem.isOnRenderThread() && isTransmogRenderActive;
    }

    public static void setTransmogRenderActive(boolean transmogRenderActive) {
        isTransmogRenderActive = transmogRenderActive;
    }

    public static boolean hideForTransmog(Player player, ItemStack stack) {
        return isTransmogRenderActive() && canUseTransmog(player, stack);
    }

    public static GeoArmorRenderer<?> renderer(TransmogHolder transmogHolder) {
        return Objects.requireNonNull(TRANSMOGS.get(transmogHolder.id()), "No Renderer Registered for transmog: " + transmogHolder.id()).get();
    }

    public static boolean shouldDisableOuterLayer(Player player, EquipmentSlot equipmentSlot) {
        var stack = player.getInventory().getArmor(equipmentSlot.getIndex());
        if (stack.isEmpty()) {
            return false;
        }
        HumanoidModel<?> renderer;
        if (hideForTransmog(player, stack)) {
            renderer = renderer(TransmogItemData.get(stack).transmog());
        } else {
            renderer = GeoRenderProvider.of(stack).getGeoArmorRenderer(player, stack, equipmentSlot, null);
        }
        if (renderer instanceof GenericCustomArmorRenderer<?> armor) {
            return (armor.hideHat && equipmentSlot == EquipmentSlot.HEAD || armor.hideJacket && equipmentSlot != EquipmentSlot.HEAD);
        }
        return false;
    }

    public static boolean canUseTransmog(Player player, ItemStack stack) {
        if (stack.isEmpty() || !TransmogItemData.has(stack)) {
            return false;
        }
        if (((ITransmogPreview) player).irons_spellbooks$isTransmogPreview()) {
            return true;
        }
        PatreonPermissions permission = PatreonHandler.getPatreonPermissions(player);
//        if (permission == PatreonPermissions.None) {
//            return false;
//        }
        TransmogHolder transmogHolder = TransmogItemData.get(stack).transmog();
        return transmogHolder != null && permission.canUse(transmogHolder);
    }
}
