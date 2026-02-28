package io.redspace.ironsspellbooks.player;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;

public class ExtendedKeyMapping extends KeyMapping {
    public ExtendedKeyMapping(String description, IKeyConflictContext keyConflictContext, final InputConstants.Type inputType, final int keyCode, String category) {
        super(description, keyConflictContext, inputType.getOrCreate(keyCode), category);
    }

    /**
     * Stores the rising edge of pressing this key map
     */
    private boolean canBeConsumed;
    private boolean wasDown;

    @Override
    public void setDown(boolean value) {
        // Pass functionality to super
        super.setDown(value);
        // If we are pressing the key for the first time, capture the rising edge into canBeConsumed
        if (value && !wasDown) {
            canBeConsumed = true;
        }
        // Record the current down status
        wasDown = value;
    }

    /**
     * @return true once per keypress (regardless of how long it is held), upon which it is consumed
     */
    public boolean consume() {
        // Consume all keypresses available
        while(consumeClick());
        // If this is the first time we are consuming, return true and reset
        if (canBeConsumed) {
            canBeConsumed = false;
            return true;
        }
        // Otherwise for any continuous input, return false
        return false;
    }

    /**
     * Used {@link ExtendedKeyMapping#consume()} for intended functionality
     */
    @Override
    @Deprecated
    public boolean consumeClick() {
        return super.consumeClick();
    }
}
