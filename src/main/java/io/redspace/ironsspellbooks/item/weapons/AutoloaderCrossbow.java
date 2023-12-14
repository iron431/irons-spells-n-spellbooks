package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.player.ClientInputEvents;
import io.redspace.ironsspellbooks.player.KeyState;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.tools.Tool;
import java.util.List;

public class AutoloaderCrossbow extends CrossbowItem {
    //NBT flag used for if we are currently in the process of loading a new projectile
    public static final String LOADING = "Loading";
    //NBT flag used for keeping track of the server tick when we will be finished loading
    public static final String LOADING_TIMESTAMP = "LoadingTimestamp";

    public AutoloaderCrossbow(Properties pProperties) {
        super(pProperties);
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pHand) {
        ItemStack itemstack = player.getItemInHand(pHand);
        //If we are charged, shoot.
        if (isCharged(itemstack)) {
            performShooting(pLevel, player, pHand, itemstack, getShootingPower(itemstack), 1.0F);
            setCharged(itemstack, false);
            //If we have an additional ammo item, begin autoloading. If not, play a sound cue. (We do not save or reserve this item, so even though we begin loading, it could still fail)
            if (!player.getProjectile(itemstack).isEmpty()) {
                startLoading(player, itemstack);
            } else {
                player.playSound(SoundEvents.ITEM_BREAK, 0.75f, 1.5f);
            }
            return InteractionResultHolder.consume(itemstack);
        } else if (isLoading(itemstack)) {
            if (player.isCrouching()) {
                setLoadingTicks(itemstack, 0);
                setLoading(itemstack, false);
            }
        } else if (!player.getProjectile(itemstack).isEmpty()) {
            startLoading(player, itemstack);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
        return InteractionResultHolder.pass(itemstack);
    }

    public static void startLoading(Player player, ItemStack itemstack) {
        setLoading(itemstack, true);
        setLoadingTicks(itemstack, 0);
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        handleTicking(itemstack, pLevel, pEntity);
        super.inventoryTick(itemstack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        int i = getLoadingTicks(stack);
        handleTicking(stack, entity.level, entity);
        if (i != getLoadingTicks(stack)) {
            //Force item entity to re-sync to client. a bit of a brute force, but the alternative would be making access transforms on synced entity data's dirty handling. this should be fine.
            ItemStack cloneStack = stack.copy();
            entity.setItem(cloneStack);
        }
        return super.onEntityItemUpdate(stack, entity);
    }

    protected static void handleTicking(ItemStack itemStack, Level level, @NotNull Entity entity) {
        if (!level.isClientSide) {
            if (isLoading(itemStack)) {
                int i = getLoadingTicks(itemStack);
                if (i > getChargeDuration(itemStack)) {
                    setLoading(itemStack, false);
                    if (entity instanceof LivingEntity livingEntity && !isCharged(itemStack) && tryLoadProjectiles(livingEntity, itemStack)) {
                        setCharged(itemStack, true);
                    }
                    SoundSource soundsource = entity instanceof Player ? SoundSource.PLAYERS : SoundSource.BLOCKS;
                    if (isCharged(itemStack)) {
                        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
                    } else {
                        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BREAK, soundsource, 1.0F, 1.5F + 0.2F);
                    }
                }
                i++;
                setLoadingTicks(itemStack, i);
            }
        }
    }

    public static int getChargeDuration(ItemStack pCrossbowStack) {
        return CrossbowItem.getChargeDuration(pCrossbowStack) * 3;
    }

    public static boolean isLoading(ItemStack pCrossbowStack) {
        CompoundTag compoundtag = pCrossbowStack.getTag();
        return compoundtag != null && compoundtag.getBoolean(LOADING);
    }

    public static void setLoading(ItemStack pCrossbowStack, boolean isLoading) {
        pCrossbowStack.getOrCreateTag().putBoolean(LOADING, isLoading);
    }

    public static int getLoadingTicks(ItemStack pCrossbowStack) {
        CompoundTag compoundtag = pCrossbowStack.getTag();
        return compoundtag != null ? compoundtag.getInt(LOADING_TIMESTAMP) : 0;
    }

    public static void setLoadingTicks(ItemStack pCrossbowStack, int timestamp) {
        pCrossbowStack.getOrCreateTag().putInt(LOADING_TIMESTAMP, timestamp);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        TooltipsUtils.addShiftTooltip(pTooltip, List.of(
                Component.translatable("item.irons_spellbooks.autoloader_crossbow.desc").withStyle(ChatFormatting.YELLOW)));
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
