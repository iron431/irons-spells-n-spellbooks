package com.example.testmod.entity.mobs;

import com.example.testmod.capabilities.magic.PlayerMagicData;

public interface MagicEntity {

    default boolean isMagicEntity(){
        return true;
    }

    public void onAntiMagic(PlayerMagicData playerMagicData);

}
