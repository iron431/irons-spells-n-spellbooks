package com.example.testmod.registries;

import com.example.testmod.TestMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    public static RegistryObject<SoundEvent> FORCE_IMPACT = registerSoundEvent("force_impact");
    public static RegistryObject<SoundEvent> ICE_IMPACT = registerSoundEvent("ice_impact");

    public static RegistryObject<SoundEvent> MAGIC_SPELL_REVERSE_3 = registerSoundEvent("magic_spell_reverse_3");
    public static RegistryObject<SoundEvent> ARIAL_SUMMONING_5_CUSTOM_1 = registerSoundEvent("arial_summoning_5_custom_1");
    public static RegistryObject<SoundEvent> DARK_MAGIC_BUFF_03_CUSTOM_1 = registerSoundEvent("dark_magic_buff_03_custom_1");
    public static RegistryObject<SoundEvent> DARK_SPELL_02 = registerSoundEvent("dark_spell_02");
    public static RegistryObject<SoundEvent> LIGHTNING_WOOSH_01 = registerSoundEvent("lightning_woosh_01");

    public static RegistryObject<SoundEvent> HEARTSTOP_CAST = registerSoundEvent("heartstop_cast");
    public static RegistryObject<SoundEvent> LIGHTNING_LANCE_CAST = registerSoundEvent("lightning_lance_cast");
    public static RegistryObject<SoundEvent> MAGIC_ARROW_RELEASE = registerSoundEvent("magic_arrow_release");
    public static RegistryObject<SoundEvent> MAGIC_ARROW_CHARGE = registerSoundEvent("magic_arrow_charge");
    public static RegistryObject<SoundEvent> FROST_STEP = registerSoundEvent("frost_step");
    public static RegistryObject<SoundEvent> ABYSSAL_TELEPORT = registerSoundEvent("abyssal_teleport");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(TestMod.MODID, name)));
    }
}
