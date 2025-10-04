package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.ItemStack;

public class FurledMapCraftableItem extends FurledMapItem implements IBackwardsCompatDefaultNbtItem {
    final FurledMapData mapData;
    final boolean ancient;

    public FurledMapCraftableItem(boolean ancient, FurledMapData mapData) {
        this.ancient = ancient;
        this.mapData = mapData;
    }

    @Override
    public String getDescriptionId() {
        return ancient ? ItemRegistry.ANCIENT_FURLED_MAP.get().getDescriptionId() : ItemRegistry.FURLED_MAP.get().getDescriptionId();
    }

    @Override
    public void setupItem(ItemStack stack) {
        FurledMapData.set(stack, mapData);
        mapData.descriptionOverride().ifPresent(desc -> FurledMapData.setLoreHelper(stack, desc));
    }
}
