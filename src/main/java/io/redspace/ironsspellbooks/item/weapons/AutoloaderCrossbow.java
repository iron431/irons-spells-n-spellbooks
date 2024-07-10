package io.redspace.ironsspellbooks.item.weapons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
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

import java.util.List;

public class AutoloaderCrossbow extends CrossbowItem {
    //NBT flag used for if we are currently in the process of loading a new projectile
    public static final String LOADING = "is_loading";
    //NBT flag used for keeping track of the server tick when we will be finished loading
    public static final String LOADING_TIMESTAMP = "load_timestamp";

    public AutoloaderCrossbow(Properties pProperties) {
        super(pProperties);
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pHand) {
        ItemStack itemstack = player.getItemInHand(pHand);
        //If we are charged, shoot.
        if (isCharged(itemstack)) {
            performShooting(pLevel, player, pHand, itemstack, getShootingPower(itemstack), 1.0F);
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
                if (i > (entity instanceof LivingEntity livingEntity ? getChargeDuration(itemStack,livingEntity) : 1.25f * 20 * 3)) {
                    setLoading(itemStack, false);
//                    if (entity instanceof LivingEntity livingEntity && !isCharged(itemStack) && tryLoadProjectiles(livingEntity, itemStack)) {
//                        setCharged(itemStack, true);
//                    }
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

    public static int getChargeDuration(ItemStack pCrossbowStack, LivingEntity entity) {
        return CrossbowItem.getChargeDuration(pCrossbowStack, entity) * 3;
    }

    public static boolean isLoading(ItemStack pCrossbowStack) {
        return pCrossbowStack.has(ComponentRegistry.CROSSBOW_LOAD_STATE) && pCrossbowStack.get(ComponentRegistry.CROSSBOW_LOAD_STATE).isLoading();
    }

    public static void setLoading(ItemStack pCrossbowStack, boolean isLoading) {
        pCrossbowStack.set(ComponentRegistry.CROSSBOW_LOAD_STATE, pCrossbowStack.getOrDefault(ComponentRegistry.CROSSBOW_LOAD_STATE, new LoadStateComponent(false, 0)).setLoading(isLoading));
    }

    public static int getLoadingTicks(ItemStack pCrossbowStack) {
        return pCrossbowStack.has(ComponentRegistry.CROSSBOW_LOAD_STATE) ? pCrossbowStack.get(ComponentRegistry.CROSSBOW_LOAD_STATE).loadTimestamp() : 0;

    }

    public static void setLoadingTicks(ItemStack pCrossbowStack, int timestamp) {
        pCrossbowStack.set(ComponentRegistry.CROSSBOW_LOAD_STATE, pCrossbowStack.getOrDefault(ComponentRegistry.CROSSBOW_LOAD_STATE, new LoadStateComponent(false, 0)).setTimestamp(timestamp));
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> pTooltip, TooltipFlag pFlag) {
        TooltipsUtils.addShiftTooltip(pTooltip, List.of(
                Component.translatable("item.irons_spellbooks.autoloader_crossbow.desc").withStyle(ChatFormatting.YELLOW)));
        super.appendHoverText(pStack, context, pTooltip, pFlag);
    }

    public record LoadStateComponent(boolean isLoading, int loadTimestamp) {
        public static final Codec<LoadStateComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.BOOL.optionalFieldOf(LOADING, false).forGetter(LoadStateComponent::isLoading),
                Codec.INT.optionalFieldOf(LOADING_TIMESTAMP, 0).forGetter(LoadStateComponent::loadTimestamp)
        ).apply(builder, LoadStateComponent::new));

        public LoadStateComponent setLoading(boolean loading) {
            return new LoadStateComponent(loading, this.loadTimestamp);
        }

        public LoadStateComponent setTimestamp(int timestamp) {
            return new LoadStateComponent(this.isLoading, timestamp);
        }
    }
}
