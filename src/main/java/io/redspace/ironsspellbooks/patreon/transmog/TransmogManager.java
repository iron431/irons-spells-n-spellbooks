package io.redspace.ironsspellbooks.patreon.transmog;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.armor.GenericArmorModel;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.util.MemoizedSupplier;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

public class TransmogManager {
    private static final HashMap<ResourceLocation, TransmogHolder> TRANSMOGS;

    private static void register(ResourceLocation id, PatreonPermissions permissions, Supplier<GeoArmorRenderer<?>> supplier) {
        TRANSMOGS.put(id, new TransmogHolder(id, permissions, new MemoizedSupplier<>(supplier)));
    }

    static {
        TRANSMOGS = new HashMap<>();
//        register(IronsSpellbooks.id("priest"), TransmogPermissions.None, () -> new PriestArmorRenderer(new PriestArmorModel()));
        register(IronsSpellbooks.id("rogue"), PatreonPermissions.None, () -> new GenericCustomArmorRenderer<>(new GenericArmorModel<>(
                IronsSpellbooks.MODID, "transmog/rogue"
        )).hideHat().hideJacket());
        register(IronsSpellbooks.id("rogue_2"), PatreonPermissions.Wizard, () -> new GenericCustomArmorRenderer<>(new GenericArmorModel<>(
                IronsSpellbooks.MODID, "transmog/rogue"
        )).hideHat().hideJacket());
    }

    public static @Nullable TransmogHolder get(ResourceLocation id) {
        return TRANSMOGS.get(id);
    }

    public static Collection<TransmogHolder> getAllTransmogs(){
        return TRANSMOGS.values();
    }
}
