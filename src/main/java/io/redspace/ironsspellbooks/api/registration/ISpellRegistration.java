package io.redspace.ironsspellbooks.api.registration;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.registries.RegistryObject;

public interface ISpellRegistration {
    RegistryObject<AbstractSpell> registerSpell(AbstractSpell spell);
}
