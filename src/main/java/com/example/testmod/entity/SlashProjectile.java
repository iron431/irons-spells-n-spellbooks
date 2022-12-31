package com.example.testmod.entity;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class SlashProjectile extends Projectile/* implements ItemSupplier*/ {

    public SlashProjectile(EntityType<? extends Projectile> p_36833_, Level p_36834_) {
        super(p_36833_, p_36834_);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket(){
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }
}
