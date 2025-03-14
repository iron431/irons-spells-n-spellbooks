package io.redspace.ironsspellbooks.item.weapons.pyrium_staff;

import io.redspace.ironsspellbooks.render.ClientStaffItemExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public class PyriumStaffClientExtensions extends ClientStaffItemExtensions {
    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return new PyriumStaffRenderer(Minecraft.getInstance().getItemRenderer(),
                Minecraft.getInstance().getEntityModels());
    }
}
