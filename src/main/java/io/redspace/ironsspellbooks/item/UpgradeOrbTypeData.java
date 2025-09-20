package io.redspace.ironsspellbooks.item;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public record UpgradeOrbTypeData(ResourceKey<UpgradeOrbType> type) {
    private static final String NBT = "irons_spellbooks:upgrade_orb_type";
    private static final Codec<UpgradeOrbTypeData> CODEC = ResourceKey.codec(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY).xmap(UpgradeOrbTypeData::new,UpgradeOrbTypeData::type);
    public static UpgradeOrbTypeData get(ItemStack itemStack) {
        return CodecHelper.get(CODEC, itemStack.getOrCreateTag().get(NBT));
    }

    public static void set(ItemStack stack, UpgradeOrbTypeData data) {
        CodecHelper.set(stack, NBT, CODEC, data);
    }

    public static boolean has(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && CodecHelper.has(itemStack, NBT);
    }
}
