package io.redspace.ironsspellbooks.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ILecternPlaceable {

    List<Component> getPages(ItemStack stack);
}
