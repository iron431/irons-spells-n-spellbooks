package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface ILecternPlaceable {
    public static final ModelResourceLocation OPEN_BOOK_MODEL = ModelResourceLocation.standalone(IronsSpellbooks.id("item/template_open_spell_book_model"));
    List<Component> getPages(ItemStack stack);

    default Optional<ResourceLocation> simpleTextureOverride(ItemStack stack){
        return Optional.empty();
    }
}
