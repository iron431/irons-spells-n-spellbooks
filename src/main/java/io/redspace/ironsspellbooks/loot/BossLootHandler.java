package io.redspace.ironsspellbooks.loot;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BossLootHandler {
    private static final String PARTICIPANTS_TAG = "boss_loot_participants";
    private static final String PARTICIPANT_ID_TAG = "id";
    private static final String PREPARED_DROPS_TAG = "boss_loot_prepared";
    private static final String PREPARED_DROP_ITEM_TAG = "item";
    private static final String PREPARED_DROP_TARGET_TAG = "target";
    private static final double PLAYER_TARGETING_RANGE_SQR = 128 * 128;

    private final List<UUID> participantIds = new ArrayList<>();
    private final List<PreparedDrop> preparedDrops = new ArrayList<>();

    public void setParticipants(List<UUID> participantIds) {
        this.participantIds.clear();
        this.participantIds.addAll(participantIds);
    }

    public void setParticipantsFromPlayers(List<? extends Player> players) {
        this.participantIds.clear();
        for (Player player : players) {
            this.participantIds.add(player.getUUID());
        }
    }

    public void prepareDrops(Mob mob, ServerLevel serverLevel, DamageSource damageSource, boolean attackedRecently, boolean ominous, @Nullable ServerPlayer lastDamagePlayer) {
        preparedDrops.clear();

        ResourceLocation rootLootTable = mob.getLootTable();
        ResourceLocation perPlayerLootTable = rootLootTable.withSuffix("_per_player");
        ResourceLocation ominousLootTable = rootLootTable.withSuffix("_ominous");
        ResourceLocation perPlayerOminousLootTable = rootLootTable.withSuffix("_per_player_ominous");

        // if not set, loot tables default to empty
        LootTable sharedLoot = serverLevel.getServer().getLootData().getLootTable(rootLootTable);
        LootTable sharedOminousLoot = serverLevel.getServer().getLootData().getLootTable(ominousLootTable);
        LootTable perPlayer = serverLevel.getServer().getLootData().getLootTable(perPlayerLootTable);
        LootTable perPlayerOminous = serverLevel.getServer().getLootData().getLootTable(perPlayerOminousLootTable);

        List<ItemStack> sharedDrops = new ArrayList<>();

        LootParams sharedParams = createLootParams(serverLevel, mob, damageSource, attackedRecently ? lastDamagePlayer : null);
        sharedLoot.getRandomItems(sharedParams, sharedDrops::add);
        if (ominous) {
            sharedOminousLoot.getRandomItems(sharedParams, sharedDrops::add);
        }

        int effectivePerPlayerCount = Math.max(1, participantIds.size());
        Map<UUID, List<ItemStack>> perPlayerDrops = new LinkedHashMap<>();
        for (int i = 0; i < effectivePerPlayerCount; i++) {
            UUID participantId = null;
            ServerPlayer contextPlayer = null;
            if (!participantIds.isEmpty()) {
                participantId = participantIds.get(i);
                Player participantLookup = serverLevel.getPlayerByUUID(participantId);
                if (participantLookup instanceof ServerPlayer serverPlayer) {
                    contextPlayer = serverPlayer;
                }
            }
            LootParams participantParams = createLootParams(serverLevel, mob, damageSource, contextPlayer);

            List<ItemStack> rolledForParticipant = new ArrayList<>();
            perPlayer.getRandomItems(participantParams, rolledForParticipant::add);
            if (ominous) {
                perPlayerOminous.getRandomItems(participantParams, rolledForParticipant::add);
            }

            if (participantId == null) {
                sharedDrops.addAll(rolledForParticipant);
            } else {
                // verbose but allows for other accumulation passes
                perPlayerDrops.computeIfAbsent(participantId, uuid -> new ArrayList<>()).addAll(rolledForParticipant);
            }
        }

        for (ItemStack itemStack : sharedDrops) {
            preparedDrops.add(new PreparedDrop(itemStack, null));
        }
        for (Map.Entry<UUID, List<ItemStack>> entry : perPlayerDrops.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                preparedDrops.add(new PreparedDrop(itemStack, entry.getKey()));
            }
        }
    }

    public void spawnPreparedDrops(Mob mob) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (PreparedDrop preparedDrop : preparedDrops) {
            ItemEntity itemEntity = new ItemEntity(serverLevel, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), preparedDrop.itemStack.copy());
            itemEntity.setPickUpDelay(20);
            if (preparedDrop.targetPlayer != null) {
                Player player = serverLevel.getPlayerByUUID(preparedDrop.targetPlayer);
                if (player != null && player.distanceToSqr(mob) <= PLAYER_TARGETING_RANGE_SQR && Utils.hasLineOfSight(serverLevel, mob, player, false)) {
                    itemEntity.setThrower(player.getUUID());
                    Vec3 targetPos = player.position().add(0, player.getBbHeight() * 0.5, 0);
                    Vec3 launchMotion = computeLaunchVelocity(itemEntity.position(), targetPos);
                    itemEntity.setDeltaMovement(launchMotion);
                }
            }
            serverLevel.addFreshEntity(itemEntity);
        }
        preparedDrops.clear();
    }

    public void save(CompoundTag tag) {
        ListTag participants = new ListTag();
        for (UUID participantId : participantIds) {
            CompoundTag participantTag = new CompoundTag();
            participantTag.putUUID(PARTICIPANT_ID_TAG, participantId);
            participants.add(participantTag);
        }
        tag.put(PARTICIPANTS_TAG, participants);

        ListTag preparedDropTags = new ListTag();
        for (PreparedDrop preparedDrop : preparedDrops) {
            CompoundTag dropTag = new CompoundTag();
            dropTag.put(PREPARED_DROP_ITEM_TAG, preparedDrop.itemStack.save(new CompoundTag()));
            if (preparedDrop.targetPlayer != null) {
                dropTag.putUUID(PREPARED_DROP_TARGET_TAG, preparedDrop.targetPlayer);
            }
            preparedDropTags.add(dropTag);
        }
        tag.put(PREPARED_DROPS_TAG, preparedDropTags);
    }

    public void load(CompoundTag tag) {
        participantIds.clear();
        ListTag participants = tag.getList(PARTICIPANTS_TAG, Tag.TAG_COMPOUND);
        for (Tag participantTagRaw : participants) {
            CompoundTag participantTag = (CompoundTag) participantTagRaw;
            if (participantTag.hasUUID(PARTICIPANT_ID_TAG)) {
                participantIds.add(participantTag.getUUID(PARTICIPANT_ID_TAG));
            }
        }

        preparedDrops.clear();
        ListTag preparedDropTags = tag.getList(PREPARED_DROPS_TAG, Tag.TAG_COMPOUND);
        for (Tag dropTagRaw : preparedDropTags) {
            CompoundTag dropTag = (CompoundTag) dropTagRaw;
            ItemStack stack = ItemStack.of(dropTag.getCompound(PREPARED_DROP_ITEM_TAG));
            if (stack.isEmpty()) {
                continue;
            }
            UUID targetPlayer = dropTag.hasUUID(PREPARED_DROP_TARGET_TAG) ? dropTag.getUUID(PREPARED_DROP_TARGET_TAG) : null;
            preparedDrops.add(new PreparedDrop(stack, targetPlayer));
        }
    }

    private static LootParams createLootParams(ServerLevel serverLevel, Mob mob, DamageSource damageSource, @Nullable ServerPlayer damagePlayer) {
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());

        if (damagePlayer != null) {
            builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, damagePlayer)
                    .withLuck(damagePlayer.getLuck());
        }

        return builder.create(LootContextParamSets.ENTITY);
    }

    private static Vec3 computeLaunchVelocity(Vec3 start, Vec3 target) {
        target = target.add(Utils.getRandomVec3(0.25));
        Vec3 horizontal = target.subtract(start).multiply(1, 0, 1);
        double horizontalSpeed = horizontal.length() / 20f * 0.8f; // 1 second flight, 80% so it lands in front of player instead of on top
        double distance = horizontal.length();
        double ticks = distance / horizontalSpeed;

        // y(t) = -1/2(g)(t^2) + v0*t
        // => v0 = [y1 + 1/2(g)(t1^2)]/t1
        double y1 = target.y - start.y;
        double g = 0.04; // assume default gravity
        double verticalSpeed = (y1 + 0.5 * g * ticks * ticks) / ticks;
        return horizontal.normalize().scale(horizontalSpeed).add(0, verticalSpeed, 0);
    }

    private record PreparedDrop(ItemStack itemStack, @Nullable UUID targetPlayer) {
    }
}
