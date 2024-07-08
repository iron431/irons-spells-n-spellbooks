package io.redspace.ironsspellbooks.item.weapons;

public interface IronsWeaponTier {
    float getAttackDamageBonus();

    float getSpeed();

    AttributeContainer[] getAdditionalAttributes();
}
