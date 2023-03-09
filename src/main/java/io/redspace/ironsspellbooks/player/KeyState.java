package io.redspace.ironsspellbooks.player;

import net.minecraft.client.KeyMapping;

public class KeyState {
    private boolean isHeld;
    private final KeyMapping key;
    private int heldTicks;

    public KeyState(KeyMapping key) {
        this.key = key;
    }

    public boolean wasPressed() {
        return !isHeld && key.isDown();
    }

    public boolean wasReleased() {
        return isHeld && !key.isDown();
    }

    public boolean wasHeldMoreThan(int ticks) {
        return heldTicks >= ticks;
    }

    public boolean isHeld() {
        return isHeld;
    }

    public void update() {
        if (key.isDown()) {
            heldTicks++;
            isHeld = true;
        } else {
            heldTicks = 0;
            isHeld = false;
        }
    }
}
