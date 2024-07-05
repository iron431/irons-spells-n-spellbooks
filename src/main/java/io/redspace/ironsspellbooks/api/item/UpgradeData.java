package io.redspace.ironsspellbooks.api.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.redspace.ironsspellbooks.registries.ComponentRegistry.UPGRADE_DATA;

public class UpgradeData {
    public static final String Upgrades = "ISBUpgrades";
    public static final String UPGRADE_TYPE = "id";
    public static final String SLOT = "slot";
    public static final String COUNT = "count";
    public static final String UPGRADES = "upgrades";
    public static final UpgradeData NONE = new UpgradeData(Map.of(), EquipmentSlot.MAINHAND.getName());

    private static final Codec<ObjectObjectImmutablePair<String, Integer>> ELEMENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.STRING.fieldOf(UPGRADE_TYPE).forGetter(Pair::left),
                    Codec.INT.fieldOf(COUNT).forGetter(Pair::right))
            .apply(builder, ObjectObjectImmutablePair::new));

    public static final Codec<UpgradeData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf(SLOT).forGetter(UpgradeData::getUpgradedSlot),
            Codec.list(ELEMENT_CODEC).fieldOf(UPGRADES).forGetter((data) -> data.getUpgrades().entrySet().stream().map(entry -> new ObjectObjectImmutablePair<>(entry.getKey().getId().toString(), entry.getValue())).toList())
    ).apply(builder, (slot, list) -> new UpgradeData(parseCodec(list), slot)));

    public static final StreamCodec<FriendlyByteBuf, UpgradeData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeUtf(data.upgradedSlot);
                var entries = data.upgrades.entrySet();
                buf.writeInt(entries.size());
                for (Map.Entry<UpgradeType, Integer> entry : entries) {
                    buf.writeUtf(entry.getKey().getId().toString());
                    buf.writeInt(entry.getValue());
                }
            },
            (buf) -> {
                String slot = buf.readUtf();
                int i = buf.readInt();
                Map<UpgradeType, Integer> upgrades = new HashMap<>();
                for (int j = 0; j < i; j++) {
                    var upgradeKey = ResourceLocation.parse(buf.readUtf());
                    int c = buf.readInt();
                    UpgradeType.getUpgrade(upgradeKey).ifPresent((upgrade) -> upgrades.put(upgrade, c));
                }
                return new UpgradeData(upgrades, slot);
            }
    );

    private static Map<UpgradeType, Integer> parseCodec(List<ObjectObjectImmutablePair<String, Integer>> data) {
        Map<UpgradeType, Integer> map = new HashMap<>();
        for (Pair<String, Integer> pair : data) {
            var upgradeKey = ResourceLocation.parse(pair.left());
            UpgradeType.getUpgrade(upgradeKey).ifPresent((upgrade) -> map.put(upgrade, pair.right()));
        }
        return map;
    }

    private final Map<UpgradeType, Integer> upgrades;
    private final String upgradedSlot;

    protected UpgradeData(Map<UpgradeType, Integer> upgrades, String slot) {
        this.upgrades = upgrades;
        this.upgradedSlot = slot;
    }

    public static UpgradeData getUpgradeData(ItemStack itemStack) {
        if (!itemStack.has(UPGRADE_DATA))
            return NONE;
        return itemStack.get(UPGRADE_DATA);
    }

//    public static boolean hasUpgradeData(ItemStack itemStack) {
//        return itemStack.getTag() != null && itemStack.getTag().contains(Upgrades);
//    }

//    public static void setUpgradeData(ItemStack itemStack, UpgradeData upgradeData) {
//        if (upgradeData == NONE) {
//            if (UpgradeData.hasUpgradeData(itemStack)) {
//                itemStack.removeTagKey(Upgrades);
//            }
//            return;
//        }
//        ListTag upgrades = new ListTag();
//
//        for (ImmutableMap.Entry<UpgradeType, Integer> upgradeInstance : upgradeData.upgrades.entrySet()) {
//            CompoundTag upgradeTag = new CompoundTag();
//            upgradeTag.putString(UPGRADE_TYPE, upgradeInstance.getKey().getId().toString());
//            upgradeTag.putString(SLOT, upgradeData.upgradedSlot/*.getName()*/);
//            upgradeTag.putInt(COUNT, upgradeInstance.getValue());
//            upgrades.add(upgradeTag);
//        }
//
//        itemStack.addTagElement(Upgrades, upgrades);
//    }

//    public static void removeUpgradeData(ItemStack itemStack) {
//        setUpgradeData(itemStack, NONE);
//    }

    public UpgradeData addUpgrade(ItemStack stack, UpgradeType upgradeType, String slot) {
        if (this == NONE) {
            Map<UpgradeType, Integer> map = new HashMap<>();
            map.put(upgradeType, 1);
            var upgrade = new UpgradeData(map, slot);
            stack.set(UPGRADE_DATA, upgrade);
            return upgrade;
        } else {
            if (this.upgrades.containsKey(upgradeType)) {
                this.upgrades.put(upgradeType, this.upgrades.get(upgradeType) + 1);
            } else {
                this.upgrades.put(upgradeType, 1);
            }
            //FIXME: 1.21: are data components mutable? is this supposed to work this way?
            stack.set(UPGRADE_DATA, this);
            return this;
        }
    }

    public int getTotalUpgrades() {
        int count = 0;
        for (ImmutableMap.Entry<UpgradeType, Integer> upgradeInstance : this.upgrades.entrySet()) {
            count += upgradeInstance.getValue();
        }
        return count;
    }

    public String getUpgradedSlot() {
        return this.upgradedSlot;
    }

    public Map<UpgradeType, Integer> getUpgrades() {
        return this.upgrades;
    }
}
