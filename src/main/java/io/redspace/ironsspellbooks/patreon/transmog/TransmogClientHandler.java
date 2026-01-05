package io.redspace.ironsspellbooks.patreon.transmog;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TransmogClientHandler {

    private static boolean isTransmogRenderActive;

    public static boolean isTransmogRenderActive() {
        return RenderSystem.isOnRenderThread() && isTransmogRenderActive;
    }

    public static void setTransmogRenderActive(boolean transmogRenderActive) {
        isTransmogRenderActive = transmogRenderActive;
    }

    public static Optional<ItemStack> handleTransmogReplacement(Player player, ItemStack stack) {
        if (!isTransmogRenderActive()) {
            return Optional.empty();
        }
        TransmogPermissions permission;
        permission = TransmogPermissions.None;
        //todo: implement PatreonHandler:
        //    permission = PatreonHandler.getTransmogPermissions(player);
        //    if (permission == TransmogPermissions.None) {
        //        return Optional.empty();
        //    }
        TransmogHolder transmogHolder = stack.get(ComponentRegistry.TRANSMOG);
        if (transmogHolder == null || !permission.canUse(transmogHolder)) {
            return Optional.empty();
        }
        return Optional.of(new ItemStack(ItemRegistry.TRANSMOG_GHOST_ITEM, stack.getCount(), stack.getComponentsPatch()));
    }
}
