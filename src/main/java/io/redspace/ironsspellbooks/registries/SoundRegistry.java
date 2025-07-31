package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, IronsSpellbooks.MODID);

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
    public static RegistryObject<SoundEvent> ABYSSAL_SHROUD = registerSoundEvent("cast.abyssal_shroud");
    public static RegistryObject<SoundEvent> BLOOD_STEP = registerSoundEvent("cast.blood_step");
    public static RegistryObject<SoundEvent> FIRE_BREATH_LOOP = registerSoundEvent("loop.fire_breath");
    public static RegistryObject<SoundEvent> ELECTROCUTE_LOOP = registerSoundEvent("loop.electrocute");
    public static RegistryObject<SoundEvent> CONE_OF_COLD_LOOP = registerSoundEvent("loop.cone_of_cold");
    public static RegistryObject<SoundEvent> CLOUD_OF_REGEN_LOOP = registerSoundEvent("loop.cloud_of_regen");
    public static RegistryObject<SoundEvent> RAISE_DEAD_START = registerSoundEvent("cast.raise_dead.start");
    public static RegistryObject<SoundEvent> RAISE_DEAD_FINISH = registerSoundEvent("cast.raise_dead.finish");
    public static RegistryObject<SoundEvent> VOID_TENTACLES_START = registerSoundEvent("cast.void_tentacles.start");
    public static RegistryObject<SoundEvent> VOID_TENTACLES_FINISH = registerSoundEvent("cast.void_tentacles.finish");
    public static RegistryObject<SoundEvent> VOID_TENTACLES_LEAVE = registerSoundEvent("entity.void_tentacles.retreat");
    public static RegistryObject<SoundEvent> VOID_TENTACLES_AMBIENT = registerSoundEvent("entity.void_tentacles.ambient");
    public static RegistryObject<SoundEvent> ICE_BLOCK_CAST = registerSoundEvent("cast.ice_block");
    public static RegistryObject<SoundEvent> ICE_BLOCK_IMPACT = registerSoundEvent("entity.ice_block.impact");
    public static RegistryObject<SoundEvent> RAY_OF_SIPHONING = registerSoundEvent("loop.ray_of_siphoning");
    public static RegistryObject<SoundEvent> FIREBALL_START = registerSoundEvent("cast.fireball");
    public static RegistryObject<SoundEvent> ACID_ORB_CHARGE = registerSoundEvent("spell.acid_orb.charge");
    public static RegistryObject<SoundEvent> ACID_ORB_CAST = registerSoundEvent("spell.acid_orb.cast");
    public static RegistryObject<SoundEvent> ACID_ORB_IMPACT = registerSoundEvent("entity.acid_orb.impact");
    public static RegistryObject<SoundEvent> POISON_ARROW_CHARGE = registerSoundEvent("spell.poison_arrow.charge");
    public static RegistryObject<SoundEvent> POISON_ARROW_CAST = registerSoundEvent("spell.poison_arrow.cast");
    public static RegistryObject<SoundEvent> POISON_BREATH_LOOP = registerSoundEvent("spell.poison_breath.loop");
    public static RegistryObject<SoundEvent> ROOT_EMERGE = registerSoundEvent("entity.root.emerge");
    public static RegistryObject<SoundEvent> BLACK_HOLE_CHARGE = registerSoundEvent("spell.black_hole.charge");
    public static RegistryObject<SoundEvent> BLACK_HOLE_CAST = registerSoundEvent("spell.black_hole.cast");
    public static RegistryObject<SoundEvent> BLACK_HOLE_LOOP = registerSoundEvent("entity.black_hole.loop");
    public static RegistryObject<SoundEvent> POISON_SPLASH_BEGIN = registerSoundEvent("spell.poison_splash.begin");
    public static RegistryObject<SoundEvent> BLIGHT_BEGIN = registerSoundEvent("spell.blight.begin");
    public static RegistryObject<SoundEvent> SPIDER_ASPECT_CAST = registerSoundEvent("spell.spider_aspect.cast");
    public static RegistryObject<SoundEvent> BLOOD_NEEDLE_IMPACT = registerSoundEvent("entity.blood_needle.impact");
    public static RegistryObject<SoundEvent> FIRE_BOMB_CHARGE = registerSoundEvent("spell.fire_bomb.charge");
    public static RegistryObject<SoundEvent> FIRE_BOMB_CAST = registerSoundEvent("spell.fire_bomb.cast");
    public static RegistryObject<SoundEvent> GUST_CHARGE = registerSoundEvent("spell.gust.charge");
    public static RegistryObject<SoundEvent> GUST_CAST = registerSoundEvent("spell.gust.cast");
    public static RegistryObject<SoundEvent> GUIDING_BOLT_IMPACT = registerSoundEvent("entity.guiding_bolt.impact");
    public static RegistryObject<SoundEvent> GUIDING_BOLT_CAST = registerSoundEvent("spell.guiding_bolt.cast");
    public static RegistryObject<SoundEvent> CHAIN_LIGHTNING_CHAIN = registerSoundEvent("entity.chain_lightning.lightning_chain");
    public static RegistryObject<SoundEvent> DEVOUR_BITE = registerSoundEvent("entity.devour_jaw.bite");
    public static RegistryObject<SoundEvent> KEEPER_SWING = registerSoundEvent("entity.citadel_keeper.swing");
    public static RegistryObject<SoundEvent> KEEPER_STEP = registerSoundEvent("entity.citadel_keeper.step");
    public static RegistryObject<SoundEvent> KEEPER_DEATH = registerSoundEvent("entity.citadel_keeper.death");
    public static RegistryObject<SoundEvent> KEEPER_HURT = registerSoundEvent("entity.citadel_keeper.hurt");
    public static RegistryObject<SoundEvent> KEEPER_SWORD_IMPACT = registerSoundEvent("entity.citadel_keeper.sword_impact");
    public static RegistryObject<SoundEvent> KEEPER_IDLE = registerSoundEvent("entity.citadel_keeper.idle");
    public static RegistryObject<SoundEvent> OAKSKIN_CAST = registerSoundEvent("spell.oakskin.cast");
    public static RegistryObject<SoundEvent> EARTHQUAKE_LOOP = registerSoundEvent("entity.earthquake_aoe.loop");
    public static RegistryObject<SoundEvent> EARTHQUAKE_IMPACT = registerSoundEvent("entity.earthquake_aoe.impact");
    public static RegistryObject<SoundEvent> EARTHQUAKE_CAST = registerSoundEvent("spell.earthquake.cast");
    public static RegistryObject<SoundEvent> FIREFLY_SWARM_IDLE = registerSoundEvent("entity.firefly_swarm.idle");
    public static RegistryObject<SoundEvent> FIREFLY_SWARM_ATTACK = registerSoundEvent("entity.firefly_swarm.attack");
    public static RegistryObject<SoundEvent> FIREFLY_SPELL_PREPARE = registerSoundEvent("spell.firefly_swarm.prepare");
    public static RegistryObject<SoundEvent> RAY_OF_FROST = registerSoundEvent("spell.ray_of_frost.cast");
    public static RegistryObject<SoundEvent> SONIC_BOOM = registerSoundEvent("spell.sonic_boom.cast");
    public static RegistryObject<SoundEvent> DIVINE_SMITE_WINDUP = registerSoundEvent("spell.divine_smite.windup");
    public static RegistryObject<SoundEvent> DIVINE_SMITE_CAST = registerSoundEvent("spell.divine_smite.cast");
    public static RegistryObject<SoundEvent> TELEKINESIS_CAST = registerSoundEvent("spell.telekinesis.cast");
    public static RegistryObject<SoundEvent> TELEKINESIS_LOOP = registerSoundEvent("spell.telekinesis.loop");
    public static RegistryObject<SoundEvent> PLANAR_SIGHT_CAST = registerSoundEvent("spell.planar_sight.cast");
    public static RegistryObject<SoundEvent> HEAT_SURGE_PREPARE = registerSoundEvent("spell.heat_surge.prepare");
    public static RegistryObject<SoundEvent> FROSTWAVE_PREPARE = registerSoundEvent("spell.frostwave.prepare");
    public static RegistryObject<SoundEvent> ARROW_VOLLEY_PREPARE = registerSoundEvent("spell.arrow_volley.prepare");
    public static RegistryObject<SoundEvent> BOW_SHOOT = registerSoundEvent("bow_shoot");
    public static RegistryObject<SoundEvent> RECALL_PREPARE = registerSoundEvent("spell.recall.prepare");
    public static RegistryObject<SoundEvent> ELDRITCH_BLAST = registerSoundEvent("spell.eldritch_blast.cast");
    public static RegistryObject<SoundEvent> FLAMING_STRIKE_UPSWING = registerSoundEvent("spell.flaming_strike.begin");
    public static RegistryObject<SoundEvent> FLAMING_STRIKE_SWING = registerSoundEvent("spell.flaming_strike.cast");
    public static RegistryObject<SoundEvent> SHOCKWAVE_CAST = registerSoundEvent("spell.shockwave.cast");
    public static RegistryObject<SoundEvent> SHOCKWAVE_PREPARE = registerSoundEvent("spell.shockwave.prepare");
    public static RegistryObject<SoundEvent> TRADER_YES = registerSoundEvent("entity.generic.trader.yes");
    public static RegistryObject<SoundEvent> TRADER_NO = registerSoundEvent("entity.generic.trader.no");
    public static RegistryObject<SoundEvent> SCORCH_PREPARE = registerSoundEvent("spell.scorch.prepare");
    public static RegistryObject<SoundEvent> FIERY_EXPLOSION = registerSoundEvent("entity.generic.fiery_explosion");
    public static RegistryObject<SoundEvent> ECHOING_STRIKE = registerSoundEvent("entity.echoing_strike.echoing_strike");
    public static RegistryObject<SoundEvent> SMALL_LIGHTNING_STRIKE = registerSoundEvent("entity.lightning_strike.strike");
    public static RegistryObject<SoundEvent> THUNDERSTORM_PREPARE = registerSoundEvent("spell.thunderstorm.prepare");
    public static RegistryObject<SoundEvent> BLOOD_EXPLOSION = registerSoundEvent("spell.sacrifice.blood_explosion");
    public static RegistryObject<SoundEvent> SUNBEAM_WINDUP = registerSoundEvent("entity.sunbeam.windup");
    public static RegistryObject<SoundEvent> SUNBEAM_IMPACT = registerSoundEvent("entity.sunbeam.impact");
    public static RegistryObject<SoundEvent> CLEANSE_CAST = registerSoundEvent("spell.cleanse.cast");
    public static RegistryObject<SoundEvent> ICE_SPIKE_EMERGE = registerSoundEvent("entity.ice_spike.emerge");
    public static RegistryObject<SoundEvent> FIRE_ARROW_CHARGE = registerSoundEvent("spell.fire_arrow.charge");
    public static RegistryObject<SoundEvent> FIRE_ARROW_CAST = registerSoundEvent("spell.fire_arrow.cast");

    public static RegistryObject<SoundEvent> DEAD_KING_SWING = registerSoundEvent("entity.dead_king.attack_swing");
    public static RegistryObject<SoundEvent> DEAD_KING_SLAM = registerSoundEvent("entity.dead_king.attack_slam");
    public static RegistryObject<SoundEvent> DEAD_KING_HIT = registerSoundEvent("entity.dead_king.attack_hit");
    public static RegistryObject<SoundEvent> DEAD_KING_RESURRECT = registerSoundEvent("entity.dead_king.resurrect");
    public static RegistryObject<SoundEvent> DEAD_KING_SPAWN = registerSoundEvent("entity.dead_king.spawn");
    public static RegistryObject<SoundEvent> DEAD_KING_FAKE_DEATH = registerSoundEvent("entity.dead_king.fake_death");
    public static RegistryObject<SoundEvent> DEAD_KING_DEATH = registerSoundEvent("entity.dead_king.death");
    public static RegistryObject<SoundEvent> DEAD_KING_HURT = registerSoundEvent("entity.dead_king.hurt");
    public static RegistryObject<SoundEvent> DEAD_KING_EXPLODE = registerSoundEvent("entity.dead_king.explode");
    public static RegistryObject<SoundEvent> DEAD_KING_DRUM_LOOP = registerSoundEvent("entity.dead_king.music.drum_loop");
    public static RegistryObject<SoundEvent> DEAD_KING_AMBIENCE = registerSoundEvent("entity.dead_king.ambience");
    public static RegistryObject<SoundEvent> DEAD_KING_MUSIC_INTRO = registerSoundEvent("entity.dead_king.music.intro");
    public static RegistryObject<SoundEvent> DEAD_KING_FIRST_PHASE_MELODY = registerSoundEvent("entity.dead_king.music.first_phase_melody");
    public static RegistryObject<SoundEvent> DEAD_KING_FIRST_PHASE_ACCENT_01 = registerSoundEvent("entity.dead_king.music.first_phase_accent_01");
    public static RegistryObject<SoundEvent> DEAD_KING_SECOND_PHASE_MELODY_ALT = registerSoundEvent("entity.dead_king.music.second_phase_melody_alt");
    public static RegistryObject<SoundEvent> DEAD_KING_SUSPENSE = registerSoundEvent("entity.dead_king.music.suspense");

    public static RegistryObject<SoundEvent> FIRE_CAST = registerSoundEvent("cast.generic.fire");
    public static RegistryObject<SoundEvent> ICE_CAST = registerSoundEvent("cast.generic.ice");
    public static RegistryObject<SoundEvent> LIGHTNING_CAST = registerSoundEvent("cast.generic.lightning");
    public static RegistryObject<SoundEvent> HOLY_CAST = registerSoundEvent("cast.generic.holy");
    public static RegistryObject<SoundEvent> ENDER_CAST = registerSoundEvent("cast.generic.ender");
    public static RegistryObject<SoundEvent> BLOOD_CAST = registerSoundEvent("cast.generic.blood");
    public static RegistryObject<SoundEvent> EVOCATION_CAST = registerSoundEvent("cast.generic.evocation");
    public static RegistryObject<SoundEvent> NATURE_CAST = registerSoundEvent("cast.generic.nature");
    public static RegistryObject<SoundEvent> POISON_CAST = registerSoundEvent("cast.generic.poison");

    public static RegistryObject<SoundEvent> LEARN_ELDRITCH_SPELL = registerSoundEvent("ui.learn_eldritch_spell");
    public static RegistryObject<SoundEvent> UI_TICK = registerSoundEvent("ui.tick");
    public static RegistryObject<SoundEvent> EQUIP_SPELL_BOOK = registerSoundEvent("item.spell_book.equip");



    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, name)));
    }
}
