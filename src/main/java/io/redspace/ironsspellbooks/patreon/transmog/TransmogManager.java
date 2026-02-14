package io.redspace.ironsspellbooks.patreon.transmog;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

public class TransmogManager {
    private static final HashMap<ResourceLocation, TransmogHolder> TRANSMOGS;

    private static void register(ResourceLocation id, Function<ResourceLocation, TransmogHolder> factory) {
        TRANSMOGS.put(id, factory.apply(id));
    }

    private interface TransmogBuilder extends Function<ResourceLocation, TransmogHolder> {
        static Function<ResourceLocation, TransmogHolder> simple(PatreonPermissions permissions) {
            return id -> new TransmogHolder(id, permissions);
        }
    }

    static {
        TRANSMOGS = new HashMap<>();
        register(IronsSpellbooks.id("rogue"), TransmogBuilder.simple(PatreonPermissions.None));
        register(IronsSpellbooks.id("rogue_2"), TransmogBuilder.simple(PatreonPermissions.Wizard));
        register(IronsSpellbooks.id("sorcerer"), id -> new TransmogHolder(id, PatreonPermissions.None, Set.of(EquipmentSlot.CHEST), TransmogHolder.DyeConfig.NONE));
    }

    public static @Nullable TransmogHolder get(ResourceLocation id) {
        return TRANSMOGS.get(id);
    }

    public static Collection<TransmogHolder> getAllTransmogs() {
        return TRANSMOGS.values();
    }
}
