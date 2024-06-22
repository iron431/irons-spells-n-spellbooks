package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.api.spells.SchoolType;


public class SchoolRegistryHolder {

    Supplier<SchoolType> registrySchool;

    public SchoolRegistryHolder(Supplier<SchoolType> registrySchool) {
        this.registrySchool = registrySchool;
    }

    public SchoolType get() {
        return registrySchool.get();
    }
}
