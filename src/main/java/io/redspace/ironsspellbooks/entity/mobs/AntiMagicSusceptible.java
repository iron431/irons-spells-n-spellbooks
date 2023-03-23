package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;

public interface AntiMagicSusceptible {

    void onAntiMagic(PlayerMagicData playerMagicData);

}
