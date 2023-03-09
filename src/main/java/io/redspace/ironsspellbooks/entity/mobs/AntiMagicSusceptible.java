package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;

public interface AntiMagicSusceptible {

    public void onAntiMagic(PlayerMagicData playerMagicData);

}
