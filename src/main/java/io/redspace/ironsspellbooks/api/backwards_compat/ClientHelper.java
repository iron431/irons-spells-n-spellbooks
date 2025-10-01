package io.redspace.ironsspellbooks.api.backwards_compat;

import io.redspace.ironsspellbooks.network.OpenHeldBookPacket;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ClientHelper {
    public static void handleOpenBookPacket(OpenHeldBookPacket packet){
        MinecraftInstanceHelper.ifPlayerPresent(player -> {
            ItemStack itemstack = player.getItemInHand(InteractionHand.values()[packet.hand]);
            Minecraft.getInstance().setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemstack)));
        });
    }
}
