package com.example.testmod.util;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.scroll.ScrollData;
import com.example.testmod.capabilities.scroll.ScrollDataProvider;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.spells.SpellType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
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

    public static LazyOptional<ScrollData> getScrollDataProvider(ItemStack stack) {
        return stack.getCapability(ScrollDataProvider.SCROLL_DATA);
    }

    public static ScrollData getScrollData(ItemStack stack) {
        return stack.getCapability(ScrollDataProvider.SCROLL_DATA).resolve().get();
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

    public static ItemStack getImbuedSwordInHand(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SwordItem) {
            var spell = Utils.getScrollData(stack).getSpell();
            if (spell.getSpellType() != SpellType.NONE_SPELL)
                return stack;

        }
        return null;
    }

    public static ItemStack getHeldImbuedSword(Player player) {
        ItemStack mainhand = getImbuedSwordInHand(player, InteractionHand.MAIN_HAND);
        if (mainhand != null) {
            return mainhand;

        } else {
            ItemStack offhand = getImbuedSwordInHand(player, InteractionHand.OFF_HAND);
            return offhand;
        }
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

    public static HitResult checkEntityIntersecting(Entity entity, Vec3 start, Vec3 end) {
        Vec3 hitPos = null;
        if (entity.isMultipartEntity()) {
            for (PartEntity p : entity.getParts()) {
                var hit = p.getBoundingBox().clip(start, end).orElse(null);
                if (hit != null) {
                    hitPos = hit;
                    break;
                }
            }
        } else {
            hitPos = entity.getBoundingBox().clip(start, end).orElse(null);
        }
        if (hitPos != null)
            return new EntityHitResult(entity, hitPos);
        else
            return BlockHitResult.miss(end, Direction.UP, new BlockPos(end));

    }

    public static HitResult raycastForEntity(Level level, Entity originEntity, float distance, boolean checkForBlocks) {
        Vec3 start = originEntity.getEyePosition();
        Vec3 end = originEntity.getLookAngle().normalize().scale(distance).add(start);

        return raycastForEntity(level, originEntity, start, end, checkForBlocks);
    }

    public static HitResult raycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks) {

        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, null);
    }

    public static HitResult raycastForEntityOfClass(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, Class<? extends Entity> c) {

        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, c);
    }

    private static HitResult internalRaycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, @Nullable Class<? extends Entity> c) {
        AABB range = originEntity.getBoundingBox().expandTowards(end.subtract(start));

        if (checkForBlocks) {
            BlockHitResult blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity));
            end = blockHitResult.getLocation();
        }

        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = c == null ? level.getEntities(originEntity, range) : level.getEntitiesOfClass(c, range);
        for (Entity target : entities) {
            HitResult hit = checkEntityIntersecting(target, start, end);
            if (hit.getType() != HitResult.Type.MISS)
                hits.add(hit);
        }

        if (hits.size() > 0) {
            hits.sort((o1, o2) -> (int) (o1.getLocation().distanceToSqr(start) - o2.getLocation().distanceToSqr(start)));
            return hits.get(0);
        } else {
            return BlockHitResult.miss(end, Direction.UP, new BlockPos(end));
        }
    }

}
