package com.example.testmod.player;

import net.minecraft.client.KeyMapping;

public class KeyState {
    boolean isHeld;
    final KeyMapping key;

    public KeyState(KeyMapping key) {
        this.key = key;
    }

    public boolean wasPressed() {
        return !isHeld && key.isDown();
    }

    public boolean wasReleased() {
        return isHeld && !key.isDown();
    }

    public boolean isHeld() {
        return isHeld;
    }

    public void Update() {
        if (key.isDown())
            isHeld = true;
        else
            isHeld = false;
    }
}
