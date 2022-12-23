package com.example.testmod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.sql.DriverPropertyInfo;


@Mixin(Player.class)
public class PlayerMixin {
    //public static enum SpinAttackType{
    //    RIPTIDE,
    //    FIRE
    //}
    @Unique
    public int lastSpinAttack = 0;

    @Unique
    public int test(){
        return lastSpinAttack;
    }

}
