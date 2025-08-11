package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.ActualMagicData;

public interface AntiMagicSusceptible {

    void onAntiMagic(ActualMagicData playerMagicData);

}
