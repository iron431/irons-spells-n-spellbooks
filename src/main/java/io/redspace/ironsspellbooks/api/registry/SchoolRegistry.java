package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.schools.*;
import io.redspace.ironsspellbooks.api.spells.AutoSchoolConfig;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@AutoSchoolConfig
public class SchoolRegistry {
    public static final ResourceKey<Registry<SchoolType>> SCHOOL_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(IronsSpellbooks.MODID, "schools"));
    private static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SCHOOL_REGISTRY_KEY, IronsSpellbooks.MODID);
    public static final Supplier<IForgeRegistry<SchoolType>> REGISTRY = SCHOOLS.makeRegistry(() -> new RegistryBuilder<SchoolType>().disableSaving().disableOverrides());

    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }

    /**
     * Helper for our mod.
     */
    private static RegistryObject<SchoolType> registerSchool(Supplier<SchoolType> schoolType) {
        return SCHOOLS.register(schoolType.get().getId().getPath(), schoolType);
    }

    public static SchoolType getSchool(ResourceLocation resourceLocation) {
        return REGISTRY.get().getValue(resourceLocation);
    }

    public static final RegistryObject<SchoolType> FIRE = registerSchool(SchoolFire::new);
    public static final RegistryObject<SchoolType> ICE = registerSchool(SchoolIce::new);
    public static final RegistryObject<SchoolType> LIGHTNING = registerSchool(SchoolLightning::new);
    public static final RegistryObject<SchoolType> HOLY = registerSchool(SchoolHoly::new);
    public static final RegistryObject<SchoolType> ENDER = registerSchool(SchoolEnder::new);
    public static final RegistryObject<SchoolType> BLOOD = registerSchool(SchoolBlood::new);
    public static final RegistryObject<SchoolType> EVOCATION = registerSchool(SchoolEvocation::new);
    public static final RegistryObject<SchoolType> NATURE = registerSchool(SchoolNature::new);

    public static final SchoolType SCHOOL_FIRE = new SchoolFire();
    public static final SchoolType SCHOOL_ICE = new SchoolIce();
    public static final SchoolType SCHOOL_LIGHTNING = new SchoolLightning();
    public static final SchoolType SCHOOL_HOLY = new SchoolHoly();
    public static final SchoolType SCHOOL_ENDER = new SchoolEnder();
    public static final SchoolType SCHOOL_BLOOD = new SchoolBlood();
    public static final SchoolType SCHOOL_EVOCATION = new SchoolEvocation();
    public static final SchoolType SCHOOL_NATURE = new SchoolNature();

    @Nullable
    public static SchoolType getSchoolFromFocus(ItemStack focusStack) {
        for (SchoolType school : REGISTRY.get().getValues()) {
            if (school.isFocus(focusStack)) {
                return school;
            }
        }
        return null;
    }
}
