package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraftforge.registries.RegistryObject;

public class SchoolRegistryHolder {

    RegistryObject<SchoolType> registrySchool;

    public SchoolRegistryHolder(RegistryObject<SchoolType> registrySchool) {
        this.registrySchool = registrySchool;
    }

    public SchoolType get() {
        return registrySchool.get();
    }
}
