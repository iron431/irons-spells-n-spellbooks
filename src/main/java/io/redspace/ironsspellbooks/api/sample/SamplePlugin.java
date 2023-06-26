package io.redspace.ironsspellbooks.api.sample;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.IIronsSpellbooksPlugin;
import io.redspace.ironsspellbooks.api.IronsSpellbooksPlugin;
import io.redspace.ironsspellbooks.api.registration.ISpellRegistration;
import net.minecraft.resources.ResourceLocation;

import java.lang.annotation.Annotation;

@IronsSpellbooksPlugin
public class SamplePlugin implements IIronsSpellbooksPlugin {

    @Override
    public ResourceLocation getPluginId() {
        IronsSpellbooks.LOGGER.debug("Hello from sample plugin");
        return new ResourceLocation("irons_test_plugin_mod_name", "irons_test_plugin_name");
    }

    @Override
    public void registerSpells(ISpellRegistration registration) {
        IIronsSpellbooksPlugin.super.registerSpells(registration);
    }

    @Override
    public void onAllSpellsRegistered() {
        IIronsSpellbooksPlugin.super.onAllSpellsRegistered();
    }
}
