package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgePatchEvents {

    @SubscribeEvent
    public static void forceEntityDrops(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide)
            return;
        if (entity.getType() == EntityType.STRAY) {
            dropLoot(entity, IronsSpellbooks.id("entities/additional_stray_loot"));
        } else if (entity.getType() == EntityType.HOGLIN) {
            dropLoot(entity, IronsSpellbooks.id("entities/additional_hoglin_loot"));
        } else if (entity.getType() == EntityType.EVOKER) {
            dropLoot(entity, IronsSpellbooks.id("entities/additional_evoker_loot"));
        } else if (entity.getType() == EntityType.BLAZE) {
            dropLoot(entity, IronsSpellbooks.id("entities/additional_blaze_loot"));
        } else if (entity.getType() == EntityType.ENDER_DRAGON) {
            dropLoot(entity, IronsSpellbooks.id("entities/additional_dragon_loot"));
        }
    }

    private static void dropLoot(LivingEntity entity, ResourceLocation resourceLocation) {
        LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel) entity.level())).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.DAMAGE_SOURCE, entity.damageSources().genericKill()).withParameter(LootContextParams.ORIGIN, entity.position());
        LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
        LootTable loottable = entity.level().getServer().getLootData().getLootTable(resourceLocation);
        loottable.getRandomItems(lootparams, entity.getLootTableSeed(), entity::spawnAtLocation);
    }
}
