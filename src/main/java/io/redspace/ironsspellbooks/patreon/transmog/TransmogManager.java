package io.redspace.ironsspellbooks.patreon.transmog;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.priest.PriestArmorModel;
import io.redspace.ironsspellbooks.entity.armor.priest.PriestArmorRenderer;
import io.redspace.ironsspellbooks.util.MemoizedSupplier;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Supplier;

public class TransmogManager {
    private static final HashMap<ResourceLocation, TransmogHolder> TRANSMOGS;

    private static void register(ResourceLocation id, TransmogPermissions permissions, Supplier<GeoArmorRenderer<?>> supplier) {
        TRANSMOGS.put(id, new TransmogHolder(id, permissions, new MemoizedSupplier<>(supplier)));
    }

    static {
        TRANSMOGS = new HashMap<>();
        register(IronsSpellbooks.id("priest"), TransmogPermissions.None, () -> new PriestArmorRenderer(new PriestArmorModel()));
        register(IronsSpellbooks.id("rogue"), TransmogPermissions.None, () -> new GenericCustomArmorRenderer<>(new TransmogArmorModel<>(
                IronsSpellbooks.id("geo/transmog/rogue_armor.geo.json"),
                IronsSpellbooks.id("textures/models/armor/transmog/rogue.png")
        )).hideHat().hideJacket());
    }

    public static @Nullable TransmogHolder get(ResourceLocation id) {
        return TRANSMOGS.get(id);
    }
}
