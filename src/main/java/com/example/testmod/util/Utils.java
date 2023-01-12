package com.example.testmod.util;

import com.example.testmod.TestMod;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Utils {
    public static String getStackTraceAsString() {
        var trace = Arrays.stream(Thread.currentThread().getStackTrace());
        StringBuffer sb = new StringBuffer();
        trace.forEach(item -> {
            sb.append(item.toString());
            sb.append("\n");
        });
        return sb.toString();
    }

    public static void spawnInWorld(Level level, BlockPos pos, ItemStack remaining) {
        if (!remaining.isEmpty()) {
            ItemEntity entityitem = new ItemEntity(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, remaining);
            entityitem.setPickUpDelay(40);
            entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0, 1, 0));
            level.addFreshEntity(entityitem);
        }
    }

    public static String timeFromTicks(float ticks, int decimalPlaces) {
        float ticks_to_seconds = 20;
        float seconds_to_minutes = 60;
        String affix = "s";
        float time = ticks / ticks_to_seconds;
        if (time > seconds_to_minutes) {
            time /= seconds_to_minutes;
            affix = "m";
        }
        return stringTruncation(time, decimalPlaces) + affix;
    }

    public static String decimalToPercent(double decimal) {
        return stringTruncation(decimal, 2) + "%";
    }

    public static boolean isPlayerHoldingSpellBook(Player player) {
        return player.getMainHandItem().getItem() instanceof SpellBook || player.getOffhandItem().getItem() instanceof SpellBook;
    }

    public static boolean isPlayerHoldingScroll(Player player) {
        return player.getMainHandItem().getItem() instanceof Scroll || player.getOffhandItem().getItem() instanceof Scroll;
    }

    public static ServerPlayer getServerPlayer(Level level, UUID uuid) {
        return level.getServer().getPlayerList().getPlayer(uuid);
    }

    public static String stringTruncation(double f, int places) {
        int whole = (int) f;
        if (f % 1 == 0) {
            return ("" + whole);
        }
        String s = "" + f;
        int decimalIndex = s.indexOf(".");
        return whole + s.substring(decimalIndex, Math.min(decimalIndex + places + 1, s.length()));
    }

    public static float getAngle(Vec2 a, Vec2 b) {
        return (float) (Math.atan2(b.y - a.y, b.x - a.x)) + 3.141f;// + (a.x > b.x ? Math.PI : 0));
    }

    public static BlockHitResult getTargetOld(Level level, Player player, ClipContext.Fluid clipContext, double reach) {
        float f = player.getXRot();
        float f1 = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3 vec31 = vec3.add((double) f6 * reach, (double) f5 * reach, (double) f7 * reach);
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, clipContext, player));
    }

    public static BlockHitResult getTargetBlock(Level level, LivingEntity entity, ClipContext.Fluid clipContext, double reach) {
        var rotation = entity.getLookAngle().normalize().scale(reach);
        var pos = entity.getEyePosition();
        var dest = rotation.add(pos);
        return level.clip(new ClipContext(pos, dest, ClipContext.Block.COLLIDER, clipContext, entity));
    }

    public static EntityHitResult getEntityIntersecting(Entity entity, Vec3 start, Vec3 end) {

        Vec3 hitPos = entity.getBoundingBox().clip(start, end).orElse(null);
        if (hitPos != null)
            return new EntityHitResult(entity, hitPos);
        else
            return null;

    }

    public static EntityHitResult getTargetEntity(Level level, LivingEntity entity, Vec3 start, Vec3 end) {
        AABB range = entity.getBoundingBox().expandTowards(end.subtract(start));
        TestMod.LOGGER.debug("Utils.getTargetEntity.rangeStart: {}",new Vec3(range.minX,range.minY,range.minZ));
        TestMod.LOGGER.debug("Utils.getTargetEntity.rangeEnd: {}",new Vec3(range.maxX,range.maxY,range.maxZ));


        List<EntityHitResult> hits = new ArrayList<>();
        //TestMod.LOGGER.debug("Utils.getTargetEntity.foundEntityCount: {}",level.getEntities(player, range).size());

        for (Entity target : level.getEntities(entity, range)) {
            EntityHitResult hit = getEntityIntersecting(target, start, end);
            if (hit != null)
                hits.add(hit);
        }
        if (hits.size() > 0) {
            hits.sort((o1, o2) -> (int) (o1.getLocation().distanceToSqr(start) - o2.getLocation().distanceToSqr(start)));
            return hits.get(0);
        } else {
            return null;
        }
    }
}
