package io.redspace.skillcasting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class ExtendedKeyMapping extends KeyMapping {
    public ExtendedKeyMapping(String description, net.neoforged.neoforge.client.settings.IKeyConflictContext keyConflictContext, InputConstants.Type inputType, int keyCode, String category) {
        super(description, keyConflictContext, inputType.getOrCreate(keyCode), category);
    }

    private boolean canBeConsumed;
    private boolean wasDown;

    @Override
    public void setDown(boolean value) {
        super.setDown(value);
        if (value && !wasDown) {
            canBeConsumed = true;
        }
        wasDown = value;
    }

    /**
     * @return true once per keypress (regardless of hold duration), upon which it is consumed
     */
    public boolean consume() {
        while (consumeClick()) {
        }
        if (canBeConsumed) {
            canBeConsumed = false;
            return true;
        }
        return false;
    }

    @Override
    @Deprecated
    public boolean consumeClick() {
        return super.consumeClick();
    }
}
