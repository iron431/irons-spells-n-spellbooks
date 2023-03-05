package com.example.testmod.setup;

import com.example.testmod.TestMod;
import com.example.testmod.config.ServerConfigs;
import com.example.testmod.entity.mobs.SummonedSkeleton;
import com.example.testmod.entity.mobs.SummonedVex;
import com.example.testmod.entity.mobs.SummonedZombie;
import com.example.testmod.entity.mobs.debug_wizard.DebugWizard;
import com.example.testmod.entity.mobs.frozen_humanoid.FrozenHumanoid;
import com.example.testmod.entity.mobs.horse.SpectralSteed;
import com.example.testmod.entity.mobs.necromancer.NecromancerEntity;
import com.example.testmod.entity.mobs.undead_king_boss.UndeadKingBoss;
import com.example.testmod.entity.mobs.wizards.pyromancer.PyromancerEntity;
import com.example.testmod.entity.wisp.WispEntity;
import com.example.testmod.registries.EntityRegistry;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonSetup {

    @SubscribeEvent()
    public static void onModConfigLoadingEvent(ModConfigEvent.Loading event) {
        TestMod.LOGGER.debug("onModConfigLoadingEvent");
        ServerConfigs.cacheConfigs();
        //SpellRarity.rarityTest();
    }

    @SubscribeEvent()
    public static void onModConfigReloadingEvent(ModConfigEvent.Reloading event) {
        TestMod.LOGGER.debug("onModConfigReloadingEvent");
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.DEBUG_WIZARD.get(), DebugWizard.prepareAttributes().build());
        event.put(EntityRegistry.PYROMANCER.get(), PyromancerEntity.prepareAttributes().build());
        event.put(EntityRegistry.NECROMANCER.get(), NecromancerEntity.prepareAttributes().build());
        event.put(EntityRegistry.SPECTRAL_STEED.get(), SpectralSteed.prepareAttributes().build());
        event.put(EntityRegistry.WISP.get(), WispEntity.prepareAttributes().build());
        event.put(EntityRegistry.SUMMONED_VEX.get(), SummonedVex.createAttributes().build());
        event.put(EntityRegistry.SUMMONED_ZOMBIE.get(), SummonedZombie.createAttributes().build());
        event.put(EntityRegistry.SUMMONED_SKELETON.get(), SummonedSkeleton.createAttributes().build());
        event.put(EntityRegistry.FROZEN_HUMANOID.get(), FrozenHumanoid.prepareAttributes().build());
        event.put(EntityRegistry.SUMMONED_POLAR_BEAR.get(), PolarBear.createAttributes().build());
        event.put(EntityRegistry.UNDEAD_KING.get(), UndeadKingBoss.prepareAttributes().build());


    }

    @SubscribeEvent
    public static void spawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(EntityRegistry.NECROMANCER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        //Set the player construct callback. It can be a lambda function.
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                new ResourceLocation(TestMod.MODID, "animation"),
                42,
                CommonSetup::registerPlayerAnimation);
    }

    //This method will set your mods animation into the library.
    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        //This will be invoked for every new player
        return new ModifierLayer<>();
    }

}
