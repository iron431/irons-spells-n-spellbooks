package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public interface PlayerDetector {
    PlayerDetector NO_CREATIVE_PLAYERS = (p_338030_, p_338031_, p_338032_, p_338033_, p_338034_) -> p_338031_.getPlayers(
                p_338030_, p_352883_ -> p_352883_.blockPosition().closerThan(p_338032_, p_338033_) && !p_352883_.isCreative() && !p_352883_.isSpectator()
            )
            .stream()
            .filter(p_350221_ -> !p_338034_ || inLineOfSight(p_338030_, p_338032_.getCenter(), p_350221_.getEyePosition()))
            .map(Entity::getUUID)
            .toList();
    PlayerDetector INCLUDING_CREATIVE_PLAYERS = (p_338015_, p_338016_, p_338017_, p_338018_, p_338019_) -> p_338016_.getPlayers(
                p_338015_, p_352886_ -> p_352886_.blockPosition().closerThan(p_338017_, p_338018_) && !p_352886_.isSpectator()
            )
            .stream()
            .filter(p_350228_ -> !p_338019_ || inLineOfSight(p_338015_, p_338017_.getCenter(), p_350228_.getEyePosition()))
            .map(Entity::getUUID)
            .toList();
    PlayerDetector SHEEP = (p_338002_, p_338003_, p_338004_, p_338005_, p_338006_) -> {
        AABB aabb = new AABB(p_338004_).inflate(p_338005_);
        return p_338003_.getEntities(p_338002_, EntityType.SHEEP, aabb, LivingEntity::isAlive)
            .stream()
            .filter(p_350217_ -> !p_338006_ || inLineOfSight(p_338002_, p_338004_.getCenter(), p_350217_.getEyePosition()))
            .map(Entity::getUUID)
            .toList();
    };

    List<UUID> detect(ServerLevel level, EntitySelector entitySelector, BlockPos pos, double maxDistance, boolean requireLineOfSight);

    private static boolean inLineOfSight(Level level, Vec3 pos, Vec3 targetPos) {
        BlockHitResult blockhitresult = level.clip(
            new ClipContext(targetPos, pos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null)
        );
        return blockhitresult.getBlockPos().equals(BlockPos.containing(pos)) || blockhitresult.getType() == HitResult.Type.MISS;
    }

    public interface EntitySelector {
        EntitySelector SELECT_FROM_LEVEL = new EntitySelector() {
            @Override
            public List<ServerPlayer> getPlayers(ServerLevel p_323695_, Predicate<? super Player> p_324206_) {
                return p_323695_.getPlayers(p_324206_);
            }

            @Override
            public <T extends Entity> List<T> getEntities(
                ServerLevel p_324491_, EntityTypeTest<Entity, T> p_323728_, AABB p_324572_, Predicate<? super T> p_323881_
            ) {
                return p_324491_.getEntities(p_323728_, p_324572_, p_323881_);
            }
        };

        List<? extends Player> getPlayers(ServerLevel level, Predicate<? super Player> predicate);

        <T extends Entity> List<T> getEntities(ServerLevel level, EntityTypeTest<Entity, T> typeTest, AABB boundingBox, Predicate<? super T> predicate);

        static EntitySelector onlySelectPlayer(Player player) {
            return onlySelectPlayers(List.of(player));
        }

        static EntitySelector onlySelectPlayers(final List<Player> players) {
            return new EntitySelector() {
                @Override
                public List<Player> getPlayers(ServerLevel p_323585_, Predicate<? super Player> p_323950_) {
                    return players.stream().filter(p_323950_).toList();
                }

                @Override
                public <T extends Entity> List<T> getEntities(
                    ServerLevel p_324352_, EntityTypeTest<Entity, T> p_323526_, AABB p_324544_, Predicate<? super T> p_323570_
                ) {
                    return players.stream().map(p_323526_::tryCast).filter(Objects::nonNull).filter(p_323570_).toList();
                }
            };
        }
    }
}
