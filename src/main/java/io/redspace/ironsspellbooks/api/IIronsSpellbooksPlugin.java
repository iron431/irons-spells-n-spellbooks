package io.redspace.ironsspellbooks.api;

import io.redspace.ironsspellbooks.api.registration.ISpellRegistration;
import net.minecraft.resources.ResourceLocation;

/**
 * The main class to implement to create an ISS plugin.
 * IIronsSpellbooksPlugin must have the {@link IronsSpellbooksPlugin} annotation to get loaded by ISS.
 */
public interface IIronsSpellbooksPlugin {
    /**
     * The unique ID for this mod plugin.
     * The namespace should be your mod's modId.
     */
    ResourceLocation getPluginId();

    /**
     * Register the spells added by your plugin.
     */
    default void registerSpells(ISpellRegistration registration) {

    }

    /**
     * Called when all ISS plugins spell loading is completed
     */
    default void onAllSpellsRegistered() {

    }
}
