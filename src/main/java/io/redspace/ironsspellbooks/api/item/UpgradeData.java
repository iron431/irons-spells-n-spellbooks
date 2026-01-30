package io.redspace.ironsspellbooks.api.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import io.redspace.ironsspellbooks.api.backwards_compat.UpgradeTypeCache;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;


public record UpgradeData(Map<Holder<UpgradeOrbType>, Integer> upgrades, String upgradedSlot) {
    public static final String NBT = "irons_spellbooks:upgrade_data";
    public static final String LEGACY_NBT = "ISBUpgrades";
    public static final String UPGRADE_TYPE = "id";
    public static final String SLOT = "slot";
    public static final String COUNT = "count";
    public static final String UPGRADES = "upgrades";
    public static final UpgradeData NONE = new UpgradeData(ImmutableMap.of(), EquipmentSlot.MAINHAND.getName());

    @Deprecated(forRemoval = true)
    private static final Codec<ObjectObjectImmutablePair<String, Integer>> ELEMENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.STRING.fieldOf(UPGRADE_TYPE).forGetter(Pair::left),
                    Codec.INT.fieldOf(COUNT).forGetter(Pair::right))
            .apply(builder, ObjectObjectImmutablePair::new));


    /**
     * We can't access the data registry without level data, but 1.20.1 has embedded static access. So we made the janky static registry cache, which this codec looks-up
     */
    public static final Codec<Holder<UpgradeOrbType>> I_LOVE_ONE_POINT_TWENTY = ResourceKey.codec(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY).xmap(UpgradeTypeCache.CACHE::get, holder -> holder.unwrapKey().get());

    /**
     * Manually parses nbt used in pre 3.4.0 items
     */
    public static final Codec<UpgradeData> LEGACY_CODEC = Codec.of(
            Encoder.error("Legacy codec should never write!"),
            new Decoder<>() {
                @Override
                public <T> DataResult<com.mojang.datafixers.util.Pair<UpgradeData, T>> decode(DynamicOps<T> ops, T input) {
                    try {
                        ListTag inputTag = (ListTag) input;
                        Map<Holder<UpgradeOrbType>, Integer> map = new HashMap<>();
                        String upgradedSlot = null;
                        for (Tag tag : inputTag) {
                            if (tag instanceof CompoundTag compoundTag) {
                                if (upgradedSlot == null) {
                                    upgradedSlot = compoundTag.getString("slot");
                                }
                                var upgradeId = ResourceLocation.parse(compoundTag.getString("id"));
                                Holder<UpgradeOrbType> holder = UpgradeTypeCache.CACHE.get(ResourceKey.create(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY, upgradeId));
                                int count = compoundTag.getInt("upgrades");
                                map.put(holder, count);
                            }
                        }
                        return DataResult.success(com.mojang.datafixers.util.Pair.of(new UpgradeData(map, upgradedSlot), input));
                    } catch (Exception e) {
                        return DataResult.error(e::getMessage);
                    }
                }
            }
    );

    public static final Codec<UpgradeData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf(SLOT).forGetter(UpgradeData::getUpgradedSlot),
            Codec.unboundedMap(I_LOVE_ONE_POINT_TWENTY, Codec.INT).fieldOf(UPGRADES).forGetter(UpgradeData::upgrades)
    ).apply(builder, (slot, list) -> new UpgradeData(list, slot)));


    public static UpgradeData getUpgradeData(ItemStack itemStack) {
        return CodecHelper.getOrElseWithLegacy(itemStack, NBT, CODEC, NONE, LEGACY_NBT, LEGACY_CODEC);
    }

    public static boolean hasUpgradeData(ItemStack stack) {
        return CodecHelper.hasWithLegacy(stack, NBT, LEGACY_NBT);
    }

    public static void removeUpgradeData(ItemStack itemstack) {
        itemstack.getOrCreateTag().remove(NBT);
    }

    public UpgradeData addUpgrade(ItemStack stack, Holder<UpgradeOrbType> upgradeType, String slot) {
        if (this == NONE) {
            ImmutableMap.Builder<Holder<UpgradeOrbType>, Integer> map = ImmutableMap.builder();
            map.put(upgradeType, 1);
            var upgrade = new UpgradeData(map.build(), slot);
            UpgradeData.set(stack, upgrade);
            return upgrade;
        } else {
            ImmutableMap.Builder<Holder<UpgradeOrbType>, Integer> map = ImmutableMap.builder();
            if (this.upgrades.containsKey(upgradeType)) {
                map.put(upgradeType, this.upgrades.get(upgradeType) + 1);
                map.putAll(this.upgrades.entrySet().stream().filter(entry -> entry.getKey() != upgradeType).toList());
            } else {
                map.put(upgradeType, 1);
                map.putAll(this.upgrades);
            }
            var upgrade = new UpgradeData(map.build(), this.upgradedSlot);
            UpgradeData.set(stack, upgrade);
            return upgrade;
        }
    }

    public static void set(ItemStack stack, UpgradeData data) {
        CodecHelper.set(stack, NBT, CODEC, data);
    }

    public int getTotalUpgrades() {
        int count = 0;
        for (ImmutableMap.Entry<Holder<UpgradeOrbType>, Integer> upgradeInstance : this.upgrades.entrySet()) {
            count += upgradeInstance.getValue();
        }
        return count;
    }

    public String getUpgradedSlot() {
        return this.upgradedSlot;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof UpgradeData upgradeData && this.upgradedSlot.equals(upgradeData.upgradedSlot) && this.upgrades.equals(upgradeData.upgrades));
    }

    @Override
    public int hashCode() {
        return this.upgradedSlot.hashCode() * 31 + this.upgrades.hashCode();
    }
}
