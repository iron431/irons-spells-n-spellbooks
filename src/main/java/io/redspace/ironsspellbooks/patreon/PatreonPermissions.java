package io.redspace.ironsspellbooks.patreon;

import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;

public enum PatreonPermissions {
    None("tooltip.irons_spellbooks.patreon.tier.none"),
    Acolyte("tooltip.irons_spellbooks.patreon.tier.acolyte"),
    Wizard("tooltip.irons_spellbooks.patreon.tier.wizard"),
    AncientMagician("tooltip.irons_spellbooks.patreon.tier.ancient_magician");

    final String descriptionId;

    PatreonPermissions(String descriptionId) {
        this.descriptionId = descriptionId;
    }

    public String getDescriptionId() {
        return descriptionId;
    }

    /**
     * @param holder Transmog attempting to be used
     * @return Whether this permission level meets or surpasses the required permissions of the transmog
     */
    public boolean canUse(TransmogHolder holder) {
        return this.compareTo(holder.requiredPermission()) >= 0;
    }

    public boolean supportsStatues() {
        //todo: implement
        return this == AncientMagician || true;
    }
}
