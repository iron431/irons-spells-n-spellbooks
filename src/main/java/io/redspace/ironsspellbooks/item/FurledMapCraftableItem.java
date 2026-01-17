package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.List;

@EventBusSubscriber
public class FurledMapCraftableItem extends FurledMapItem {
    final boolean ancient;
    final FurledMapData mapData;

    public FurledMapCraftableItem(boolean ancient, FurledMapData mapData) {
        this.ancient = ancient;
        this.mapData = mapData;
    }

    @Override
    public String getDescriptionId() {
        return ancient ? ItemRegistry.ANCIENT_FURLED_MAP.get().getDescriptionId() : ItemRegistry.FURLED_MAP.get().getDescriptionId();
    }


    @SubscribeEvent
    public static void setMapData(ModifyDefaultComponentsEvent event) {
        // fixme: this is obviously way easier to do by simply using item properties component builder, but in order to preserve backwards compatibility, we do this instead
        // todo: rework constructors on api break
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof FurledMapCraftableItem map) {
                map.modifyDefaultComponentsFrom(DataComponentPatch.builder().set(ComponentRegistry.FURLED_MAP_COMPONENT.get(), map.mapData).build());
                map.mapData.descriptionOverride().ifPresent(desc -> map.modifyDefaultComponentsFrom(DataComponentPatch.builder().set(DataComponents.LORE, new ItemLore(List.of(desc))).build()));
            }
        }
    }
}
