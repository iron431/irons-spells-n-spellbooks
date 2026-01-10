package io.redspace.ironsspellbooks.patreon;

import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;

public enum PatreonPermissions {
    None,
    Acolyte,
    Wizard,
    AncientMagician;

    /**
     * @param holder Transmog attempting to be used
     * @return Whether this permission level meets or surpasses the required permissions of the transmog
     */
    public boolean canUse(TransmogHolder holder) {
        return this.compareTo(holder.requiredPermission()) >= 0;
    }
}
