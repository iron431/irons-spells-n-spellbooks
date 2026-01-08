package io.redspace.ironsspellbooks.patreon.transmog;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public class TransmogClientHandler {

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

    public static boolean disableOuterLayer(Player player, EquipmentSlot equipmentSlot) {
        var stack = player.getInventory().getArmor(equipmentSlot.getIndex());
        if (stack.isEmpty()) {
            return false;
        }
        HumanoidModel<?> renderer;
        if (hideForTransmog(player, stack)) {
            renderer = stack.get(ComponentRegistry.TRANSMOG).getArmorRenderer();
        } else {
            renderer = GeoRenderProvider.of(stack).getGeoArmorRenderer(player, stack, equipmentSlot, null);
        }
        if (renderer instanceof GenericCustomArmorRenderer<?> armor) {
            return (armor.hideHat && equipmentSlot == EquipmentSlot.HEAD || armor.hideJacket && equipmentSlot != EquipmentSlot.HEAD);
        }
        return false;
    }

    public static boolean canUseTransmog(Player player, ItemStack stack) {
        PatreonPermissions permission;
        permission = PatreonPermissions.None;
        //todo: implement PatreonHandler:
        //    permission = PatreonHandler.getTransmogPermissions(player);
        //    if (permission == TransmogPermissions.None) {
        //        return false;
        //    }
        TransmogHolder transmogHolder = stack.get(ComponentRegistry.TRANSMOG);
        if (transmogHolder == null || !permission.canUse(transmogHolder)) {
            return false;
        }
        return true;
    }
}
