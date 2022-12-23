package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.item.SpellBook;
import com.example.testmod.item.WimpySpellBook;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class SpellBookDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<SpellBookData> SPELL_BOOK_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });

    private SpellBookData spellBookData = null;
    private final LazyOptional<SpellBookData> opt = LazyOptional.of(this::createSpellbookData);

    private final SpellBookTypes spellBookType;

    public SpellBookDataProvider(SpellBookTypes spellBookType) {
        this.spellBookType = spellBookType;
    }

    @Nonnull
    private SpellBookData createSpellbookData() {
        if (spellBookData == null) {
            spellBookData = new SpellBookData();
            switch (spellBookType) {
                case WimpySpellBook -> spellBookData.setSpellSlots(2);
                default -> System.out.println("Unknown SpellBook Type");
            }

        }
        return spellBookData;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == SPELL_BOOK_DATA) {
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createSpellbookData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createSpellbookData().loadNBTData(nbt);
    }
}
