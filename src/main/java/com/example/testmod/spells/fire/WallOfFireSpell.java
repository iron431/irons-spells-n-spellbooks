package com.example.testmod.spells.fire;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.CastData;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.wall_of_fire.WallOfFireCastTracker;
import com.example.testmod.entity.wall_of_fire.WallOfFireEntity;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class WallOfFireSpell extends AbstractSpell {
    public WallOfFireSpell() {
        this(1);
    }

    public WallOfFireSpell(int level) {
        super(SpellType.WALL_OF_FIRE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 0;
        this.castTime = 100;
        this.baseManaCost = 5;
        this.cooldown = 100;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getID() && playerMagicData.getAdditionalCastData() == null) {
            TestMod.LOGGER.debug("WallOfFireSpell: creating new data");
            var fireWallData = new FireWallData(getWallLength());
            playerMagicData.setAdditionalCastData(fireWallData);
            WallOfFireCastTracker wallOfFireCastTracker = new WallOfFireCastTracker(world, entity, (FireWallData) playerMagicData.getAdditionalCastData(), 2);
            wallOfFireCastTracker.setPos(entity.position());
            world.addFreshEntity(wallOfFireCastTracker);
            fireWallData.setCastingEntity(wallOfFireCastTracker);

        }
        TestMod.LOGGER.debug(playerMagicData.toString());
        //if (playerMagicData.getAdditionalCastData() instanceof FireWallData fireWallData)

        super.onCast(world, entity, playerMagicData);
    }

    @Override
    public void onCastServerComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
//        for (Vec3 vec : spawnAnchors) {
//            TestMod.LOGGER.debug(vec.toString());
//
//        }
        super.onCastServerComplete(level, entity, playerMagicData);
        TestMod.LOGGER.debug("WallOfFireSpell.onCastComplete");
        if (playerMagicData.getAdditionalCastData() instanceof FireWallData fireWallData) {
            if (fireWallData.anchors.size() == 1) {
                if (fireWallData.getCastingEntity() instanceof WallOfFireCastTracker wallOfFireCastTracker) {
                    wallOfFireCastTracker.addAnchor(fireWallData, level, entity);
                }
            }

            WallOfFireEntity fireWall = new WallOfFireEntity(level, entity, fireWallData.anchors, getDamage(entity));
            fireWall.setPos(fireWallData.getFirstAnchorSafe());
            level.addFreshEntity(fireWall);
        }
    }

    private float getWallLength() {
        return 7 + level * 2;
    }

    private float getDamage(Entity sourceEntity) {
        return getSpellPower(sourceEntity);
    }

    public class FireWallData implements CastData {
        private Entity castingEntity;
        public List<Vec3> anchors = new ArrayList<>();
        public float maxTotalDistance;
        public float accumulatedDistance;

        FireWallData(float maxTotalDistance) {
            this.maxTotalDistance = maxTotalDistance;
        }

        Vec3 getFirstAnchorSafe() {
            if (anchors.size() > 0)
                return anchors.get(0);
            else
                return Vec3.ZERO;
        }

        public void setCastingEntity(Entity castingEntity) {
            discardCastingEntity();
            this.castingEntity = castingEntity;
        }

        public Entity getCastingEntity() {
            return this.castingEntity;
        }

        public void discardCastingEntity() {
            if (this.castingEntity != null) {
                this.castingEntity.discard();
                this.castingEntity = null;
                //TestMod.LOGGER.debug("PlayerMagicData: discarding cone");
            }
        }

        @Override
        public void reset() {
            discardCastingEntity();
        }
    }
}
