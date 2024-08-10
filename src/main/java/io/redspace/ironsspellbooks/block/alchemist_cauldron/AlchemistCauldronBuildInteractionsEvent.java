package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;

public class AlchemistCauldronBuildInteractionsEvent extends Event {
    private final Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> interactionMap;

    public AlchemistCauldronBuildInteractionsEvent(Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> interactionMap) {
        this.interactionMap = interactionMap;
    }

    public void addInteraction(Item item, AlchemistCauldronInteraction interaction) {
        if (!interactionMap.containsKey(item)) {
            interactionMap.put(item, interaction);
        }
    }

    public void addSimpleBottleEmptyInteraction(Item item) {
        if (!interactionMap.containsKey(item)) {
            AlchemistCauldronTile.createBottleEmptyInteraction(interactionMap, () -> item);
        }
    }
}
