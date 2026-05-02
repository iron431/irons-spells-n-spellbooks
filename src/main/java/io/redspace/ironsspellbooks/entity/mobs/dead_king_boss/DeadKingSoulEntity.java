package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.NBT;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DeadKingSoulEntity extends Entity {
    private static final double MOVE_PER_TICK = 0.22;

    private Vec3 respawnPos = Vec3.ZERO;

    public DeadKingSoulEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public DeadKingSoulEntity(Level level, Vec3 start, Vec3 respawnPos) {
        this(EntityRegistry.DEAD_KING_SOUL.get(), level);
        this.setPos(start);
        this.respawnPos = respawnPos;
    }

    public void setRespawnPos(Vec3 respawnPos) {
        this.respawnPos = respawnPos;
    }

    public Vec3 getRespawnPos() {
        return this.respawnPos;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 vec, @NotNull InteractionHand hand) {
        if (player.level instanceof ServerLevel serverLevel) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.is(ItemRegistry.WAYWARD_COMPASS)) {
                if (!player.hasInfiniteMaterials()) {
                    Vec3 particlePos = player.getEyePosition().add(player.getForward().scale(0.6)).subtract(0, 0.3, 0);
                    MagicManager.spawnParticles(serverLevel, new ItemParticleOption(ParticleTypes.ITEM, itemStack), particlePos.x, particlePos.y, particlePos.z, 9, .15, .15, .15, 0.08, false);
                    itemStack.shrink(1);
                    player.setItemInHand(hand, itemStack);
                }
                DeadKingCorpseEntity deadKingCorpseEntity = new DeadKingCorpseEntity(EntityRegistry.DEAD_KING_CORPSE.get(), serverLevel);
                // todo: particles, sound
                deadKingCorpseEntity.moveTo(this.position());
                float f = player.getYRot() + 180;
                // set the y rot dammit
                deadKingCorpseEntity.setYRot(f);
                deadKingCorpseEntity.yRotO = f;
                deadKingCorpseEntity.yHeadRot = f;
                deadKingCorpseEntity.yHeadRotO = f;
                deadKingCorpseEntity.yBodyRot = f;
                deadKingCorpseEntity.yBodyRotO = f;
                deadKingCorpseEntity.trigger();
                serverLevel.addFreshEntity(deadKingCorpseEntity);
                this.discard();
            }
        }
        return super.interactAt(player, vec, hand);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            double halfW = getBbWidth() * 0.5;
            for (int i = 0; i < 4; i++) {
                double ox = (random.nextDouble() - 0.5) * halfW * 2;
                double oz = (random.nextDouble() - 0.5) * halfW * 2;
                double py = getY() + random.nextDouble() * getBbHeight();
                level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        true,
                        getX() + ox,
                        py,
                        getZ() + oz,
                        (random.nextDouble() - 0.5) * 0.02,
                        random.nextDouble() * 0.03,
                        (random.nextDouble() - 0.5) * 0.02
                );
            }
        } else {
            Vec3 pos = position();
            Vec3 to = respawnPos.subtract(pos);
            double len = to.length();
            if (len > .1) {
                Vec3 step = to.scale(1.0 / len).scale(Math.min(MOVE_PER_TICK, len));
                setPos(pos.add(step));
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("RespawnPos", Tag.TAG_COMPOUND)) {
            this.respawnPos = NBT.readVec3(compound.getCompound("RespawnPos"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("RespawnPos", NBT.writeVec3Pos(respawnPos));
    }
}
