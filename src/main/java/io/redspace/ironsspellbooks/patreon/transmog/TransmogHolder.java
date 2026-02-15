package io.redspace.ironsspellbooks.patreon.transmog;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public record TransmogHolder(ResourceLocation id, PatreonPermissions requiredPermission,
                             Set<EquipmentSlot> supportedSlots, DyeConfig dyeConfig) {
    public record DyeConfig(boolean dyeable, int defaultColor) {
        public static final DyeConfig NONE = new DyeConfig(false, -1);
    }

    public static final Set<EquipmentSlot> ALL_SLOTS = Set.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);

    public TransmogHolder(ResourceLocation id, PatreonPermissions permissions) {
        this(id, permissions, ALL_SLOTS, DyeConfig.NONE);
    }

    public boolean supportsSlot(EquipmentSlot slot) {
        return this.supportedSlots.contains(slot);
    }

    public boolean supportsSlot(ItemStack itemStack) {
        return itemStack.getItem() instanceof Equipable equipable && supportsSlot(equipable.getEquipmentSlot());
    }

    public static final Codec<TransmogHolder> CODEC = ResourceLocation.CODEC.xmap(TransmogManager::get, TransmogHolder::id);


//    public @NotNull GeoArmorRenderer<?> getArmorRenderer() {
//        return memoizedSupplier.get();
//    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TransmogHolder oth && this.id.equals(oth.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String descriptionId() {
        return String.format("transmog.%s.%s", id.getNamespace(), id.getPath());
    }

}
