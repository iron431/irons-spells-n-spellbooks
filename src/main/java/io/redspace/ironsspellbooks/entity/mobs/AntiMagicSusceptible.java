package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.api.magic.MagicData;

public interface AntiMagicSusceptible {

    void onAntiMagic(MagicData playerMagicData);

}
