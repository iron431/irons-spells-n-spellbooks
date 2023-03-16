package io.redspace.ironsspellbooks.capabilities.scroll;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class ScrollDataEvents {
    public static final String RESOURCE_NAME = "scrollData";
    public static final ResourceLocation IMBUED_SPELL = new ResourceLocation(IronsSpellbooks.MODID, "imbued_spell");

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ScrollData.class);
    }

    public static void onAttachCapabilitiesItemStack(AttachCapabilitiesEvent<ItemStack> event) {
//        var stack = event.getObject();
//        if (stack.isEmpty())
//            return;
//
//        if (event.getObject().getItem() instanceof SwordItem && !event.getCapabilities().containsKey(IMBUED_SPELL)) {
//            event.addCapability(IMBUED_SPELL, new ScrollDataProvider());
//        }
    }

}