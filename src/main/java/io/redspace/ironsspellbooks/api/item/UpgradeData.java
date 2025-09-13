package io.redspace.ironsspellbooks.api.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;


public record UpgradeData(Map<Holder<UpgradeOrbType>, Integer> upgrades, String upgradedSlot) {
    public static final String NBT = "ISBUpgrades";
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


    public static final Codec<UpgradeData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf(SLOT).forGetter(UpgradeData::getUpgradedSlot),
            Codec.unboundedMap(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_CODEC, Codec.INT).fieldOf(UPGRADES).forGetter(UpgradeData::upgrades)
    ).apply(builder, (slot, list) -> new UpgradeData(list, slot)));

//    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeData> STREAM_CODEC = StreamCodec.of(
//            (buf, data) -> {
//                buf.writeUtf(data.upgradedSlot);
//                var entries = data.upgrades.entrySet();
//                buf.writeInt(entries.size());
//                for (Map.Entry<Holder<UpgradeOrbType>, Integer> entry : entries) {
//                    if (entry.getKey().getKey() != null) {
//                        buf.writeResourceLocation(entry.getKey().getKey().location());
//                        buf.writeInt(entry.getValue());
//                    }
//                }
//            },
//            (buf) -> {
//                var registry = UpgradeOrbTypeRegistry.upgradeTypeRegistry(buf.registryAccess());
//                String slot = buf.readUtf();
//                int i = buf.readInt();
//                ImmutableMap.Builder<Holder<UpgradeOrbType>, Integer> upgrades = ImmutableMap.builder();
//                for (int j = 0; j < i; j++) {
//                    var upgradeKey = buf.readResourceLocation();
//                    int c = buf.readInt();
//                    Optional.ofNullable(registry.get(upgradeKey)).ifPresent((upgrade) -> upgrades.put(registry.wrapAsHolder(upgrade), c));
//                }
//                return new UpgradeData(upgrades.build(), slot);
//            }
//    );

    public static UpgradeData getUpgradeData(ItemStack itemStack) {
        return CodecHelper.getOrElse(itemStack, NBT, CODEC, NONE);
    }

    public static boolean hasUpgradeData(ItemStack stack) {
        return CodecHelper.has(stack, NBT);
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
