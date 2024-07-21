package io.redspace.ironsspellbooks.api.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.item.armor.UpgradeType;
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

public record UpgradeData(ImmutableMap<UpgradeType, Integer> upgrades, String upgradedSlot) {
    public static final String Upgrades = "ISBUpgrades";
    public static final String UPGRADE_TYPE = "id";
    public static final String SLOT = "slot";
    public static final String COUNT = "count";
    public static final String UPGRADES = "upgrades";
    public static final UpgradeData NONE = new UpgradeData(ImmutableMap.of(), EquipmentSlot.MAINHAND.getName());

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
                ImmutableMap.Builder<UpgradeType, Integer> upgrades = ImmutableMap.builder();
                for (int j = 0; j < i; j++) {
                    var upgradeKey = ResourceLocation.parse(buf.readUtf());
                    int c = buf.readInt();
                    UpgradeType.getUpgrade(upgradeKey).ifPresent((upgrade) -> upgrades.put(upgrade, c));
                }
                return new UpgradeData(upgrades.build(), slot);
            }
    );

    //TODO: this looks dirty
    private static ImmutableMap<UpgradeType, Integer> parseCodec(List<ObjectObjectImmutablePair<String, Integer>> data) {
        ImmutableMap.Builder<UpgradeType, Integer> map = ImmutableMap.builder();
        for (Pair<String, Integer> pair : data) {
            var upgradeKey = ResourceLocation.parse(pair.left());
            UpgradeType.getUpgrade(upgradeKey).ifPresent((upgrade) -> map.put(upgrade, pair.right()));
        }
        return map.build();
    }

    public static UpgradeData getUpgradeData(ItemStack itemStack) {
        if (!itemStack.has(UPGRADE_DATA))
            return NONE;
        return itemStack.get(UPGRADE_DATA);
    }

    public UpgradeData addUpgrade(ItemStack stack, UpgradeType upgradeType, String slot) {
        if (this == NONE) {
            ImmutableMap.Builder<UpgradeType, Integer> map = ImmutableMap.builder();
            map.put(upgradeType, 1);
            var upgrade = new UpgradeData(map.build(), slot);
            stack.set(UPGRADE_DATA, upgrade);
            return upgrade;
        } else {
            ImmutableMap.Builder<UpgradeType, Integer> map = ImmutableMap.builder();
            map.putAll(this.upgrades);
            if (this.upgrades.containsKey(upgradeType)) {
                map.put(upgradeType, this.upgrades.get(upgradeType) + 1);
            } else {
                map.put(upgradeType, 1);
            }
            var upgrade = new UpgradeData(map.build(), this.upgradedSlot);
            stack.set(UPGRADE_DATA, upgrade);
            return upgrade;
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


    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof UpgradeData upgradeData && this.upgradedSlot.equals(upgradeData.upgradedSlot) && this.upgrades.equals(upgradeData.upgrades));
    }

    @Override
    public int hashCode() {
        return this.upgradedSlot.hashCode() * 31 + this.upgrades.hashCode();
    }
}
