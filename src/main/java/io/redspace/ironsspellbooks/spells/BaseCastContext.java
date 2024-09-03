package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCastContext implements ICastContext {

    private int spellLevel;
    private final CastSource castSource;


    public BaseCastContext(int spellLevel, CastSource castSource) {
        this.spellLevel = spellLevel;
        this.castSource = castSource;
    }

    @Override
    public int getSpellLevel() {
        return spellLevel;
    }

    @Override
    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }

    @Override
    public @NotNull CastSource getCastSource() {
        return castSource;
    }
}
