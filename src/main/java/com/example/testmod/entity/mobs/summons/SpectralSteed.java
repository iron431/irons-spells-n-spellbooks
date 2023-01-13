package com.example.testmod.entity.mobs.summons;

import com.example.testmod.TestMod;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.util.Utils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpectralSteed extends AbstractHorse {
    public SpectralSteed(EntityType<? extends AbstractHorse> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        //randomizeAttributes(level.random);

    }

    public SpectralSteed(Level pLevel) {
        this(EntityRegistry.SPECTRAL_STEED.get(), pLevel);
        //randomizeAttributes(level.random);

    }

    public SpectralSteed(Level pLevel, LivingEntity owner) {
        this(pLevel);
        setOwnerUUID(owner.getUUID());
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 15)
                .add(Attributes.JUMP_STRENGTH, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    @Override
    protected void followMommy() {
        // Called from server
        Player owner = Utils.getServerPlayer(level, getOwnerUUID());
        if (owner != null && owner.position().distanceToSqr(position()) > 100){
            this.navigation.createPath(owner, 0);

        }
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.isVehicle()) {
            return super.mobInteract(pPlayer, pHand);
        }
        if (pPlayer.getUUID() == getOwnerUUID()) {
            this.doPlayerRide(pPlayer);
        } else {
            this.makeMad();
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    @Override
    protected boolean canParent() {
        return false;
    }

    @Override
    public boolean isSaddled() {
        return true;
    }

    @Override
    public boolean isTamed() {
        return true;
    }

}
