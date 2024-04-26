package io.redspace.ironsspellbooks.api.config;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class DefaultConfig {
    public SpellRarity minRarity;
    public ResourceLocation schoolResource;
    public int maxLevel = -1;
    public boolean enabled = true;
    public double cooldownInSeconds = -1;
    public boolean allowCrafting = true;

    public DefaultConfig(Consumer<DefaultConfig> intialize) throws RuntimeException {
        intialize.accept(this);
        build();
    }

    public DefaultConfig() {
    }

    public DefaultConfig setMaxLevel(int i) {
        this.maxLevel = i;
        return this;
    }

    public DefaultConfig setDeprecated(boolean deprecated) {
        this.enabled = !deprecated;
        return this;
    }

    public DefaultConfig setMinRarity(SpellRarity i) {
        this.minRarity = i;
        return this;
    }

    public DefaultConfig setCooldownSeconds(double i) {
        this.cooldownInSeconds = i;
        return this;
    }

    public DefaultConfig setSchoolResource(ResourceLocation schoolResource) {
        this.schoolResource = schoolResource;
        return this;
    }

    public DefaultConfig setAllowCrafting(boolean allowCrafting) {
        this.allowCrafting = allowCrafting;
        return this;
    }

    public DefaultConfig build() throws RuntimeException {
        if (!this.validate())
            throw new RuntimeException("You didn't define all config attributes!");

        return this;
    }

    private boolean validate() {
        return minRarity != null && maxLevel >= 0 && schoolResource != null && cooldownInSeconds >= 0;
    }
}

