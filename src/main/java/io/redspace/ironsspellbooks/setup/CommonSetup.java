package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.SummonedHorse;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import io.redspace.ironsspellbooks.entity.mobs.SummonedVex;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingBoss;
import io.redspace.ironsspellbooks.entity.mobs.debug_wizard.DebugWizard;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoid;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import io.redspace.ironsspellbooks.entity.mobs.necromancer.NecromancerEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist.ApothecaristEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.archevoker.ArchevokerEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cryomancer.CryomancerEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cultist.CultistEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cursed_armor_stand.CursedArmorStandEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.priest.PriestEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer.PyromancerEntity;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammer;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedClaymoreEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedRapierEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedSwordEntity;
import io.redspace.ironsspellbooks.entity.spells.void_tentacle.VoidTentacle;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {
    @SubscribeEvent
    public static void onModConfigLoadingEvent(ModConfigEvent.Loading event) {
        //IronsSpellbooks.LOGGER.debug("onModConfigLoadingEvent");
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            SpellRegistry.onConfigReload();
            ServerConfigs.onConfigReload();
        } else if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
            ClientConfigs.onConfigReload();
        }
    }

    @SubscribeEvent
    public static void onModConfigReloadingEvent(ModConfigEvent.Reloading event) {
        //IronsSpellbooks.LOGGER.debug("onModConfigReloadingEvent");
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            SpellRegistry.onConfigReload();
            ServerConfigs.onConfigReload();
        } else if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
            ClientConfigs.onConfigReload();
        }
    }

    //todo: forge sucks
//    @SubscribeEvent
//    public static void registerCapabilitiesEvent(/*RegisterCapabilitiesEvent event*/AttachCapabilitiesEvent<BlockEntity> event) {
//        if (event.getObject() instanceof AlchemistCauldronTile) {
//            var noopHandler =  new FluidTank(0);
//            event.addCapability(IronsSpellbooks.id("alchemist_cauldron"), new CapabilityProvider<>(ForgeCapabilities.FLUID_HANDLER, LazyOptional.of(() -> noopHandler)) {
//            });
//        }
////        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BlockRegistry.ALCHEMIST_CAULDRON_TILE.get(),
////                (be, context) -> {
////                    if (be.fluidCapability == null) {
////                        be.refreshCapabilities();
////                    }
////
////                    return be.fluidCapability;
////                });
//    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.DEBUG_WIZARD.get(), DebugWizard.prepareAttributes().build());
        event.put(EntityRegistry.PYROMANCER.get(), PyromancerEntity.prepareAttributes().build());
        event.put(EntityRegistry.NECROMANCER.get(), NecromancerEntity.prepareAttributes().build());
        event.put(EntityRegistry.SPECTRAL_STEED.get(), SummonedHorse.prepareAttributes().build());
        event.put(EntityRegistry.WISP.get(), WispEntity.prepareAttributes().build());
        event.put(EntityRegistry.SPECTRAL_HAMMER.get(), SpectralHammer.prepareAttributes().build());
        event.put(EntityRegistry.SUMMONED_VEX.get(), SummonedVex.createAttributes().build());
        event.put(EntityRegistry.SUMMONED_ZOMBIE.get(), SummonedZombie.createAttributes().build());
        event.put(EntityRegistry.SUMMONED_SKELETON.get(), SummonedSkeleton.createAttributes().build());
        event.put(EntityRegistry.FROZEN_HUMANOID.get(), FrozenHumanoid.prepareAttributes().build());
        event.put(EntityRegistry.SUMMONED_POLAR_BEAR.get(), PolarBear.createAttributes().build());
        event.put(EntityRegistry.DEAD_KING.get(), DeadKingBoss.prepareAttributes().build());
        event.put(EntityRegistry.DEAD_KING_CORPSE.get(), DeadKingBoss.prepareAttributes().build());
        event.put(EntityRegistry.CATACOMBS_ZOMBIE.get(), Zombie.createAttributes().build());
        event.put(EntityRegistry.MAGEHUNTER_VINDICATOR.get(), Vindicator.createAttributes().build());
        event.put(EntityRegistry.ARCHEVOKER.get(), ArchevokerEntity.prepareAttributes().build());
        event.put(EntityRegistry.PRIEST.get(), PriestEntity.prepareAttributes().build());
        event.put(EntityRegistry.KEEPER.get(), KeeperEntity.prepareAttributes().build());
        event.put(EntityRegistry.SCULK_TENTACLE.get(), VoidTentacle.createLivingAttributes().build());
        event.put(EntityRegistry.CRYOMANCER.get(), CryomancerEntity.prepareAttributes().build());
        event.put(EntityRegistry.ROOT.get(), RootEntity.createLivingAttributes().build());
        event.put(EntityRegistry.FIREFLY_SWARM.get(), WispEntity.prepareAttributes().build());
        event.put(EntityRegistry.APOTHECARIST.get(), ApothecaristEntity.prepareAttributes().build());
        event.put(EntityRegistry.CULTIST.get(), CultistEntity.prepareAttributes().build());
        event.put(EntityRegistry.FIRE_BOSS.get(), FireBossEntity.prepareAttributes().build());
        event.put(EntityRegistry.CURSED_ARMOR_STAND.get(), CursedArmorStandEntity.prepareAttributes().build());
        event.put(EntityRegistry.SUMMONED_SWORD.get(), SummonedSwordEntity.prepareAttributes().build());
        event.put(EntityRegistry.SUMMONED_CLAYMORE.get(), SummonedClaymoreEntity.prepareAttributes().build());
        event.put(EntityRegistry.SUMMONED_RAPIER.get(), SummonedRapierEntity.prepareAttributes().build());
        event.put(EntityRegistry.ICE_SPIDER.get(), IceSpiderEntity.prepareAttributes().build());
    }

    @SubscribeEvent
    public static void spawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(EntityRegistry.NECROMANCER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, serverLevelAccessor, spawnType, blockPos, random) -> Utils.checkMonsterSpawnRules(serverLevelAccessor, spawnType, blockPos, random), SpawnPlacementRegisterEvent.Operation.OR);
    }
}
