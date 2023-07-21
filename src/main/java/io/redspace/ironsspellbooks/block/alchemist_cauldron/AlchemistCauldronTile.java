package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

import java.util.Stack;

import static io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronBlock.MAX_LEVELS;

public class AlchemistCauldronTile extends BlockEntity {
    private final NonNullList<ItemStack> floatingItems = NonNullList.withSize(MAX_LEVELS, ItemStack.EMPTY);
    private final Stack<ItemStack> storedItems = new Stack<>();
    public AlchemistCauldronTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.ALCHEMIST_CAULDRON_TILE.get(), pWorldPosition, pBlockState);
    }

    public boolean addItem(ItemStack newItem) {
        //TODO: figure out how this shit is actually going to work
        if (getLevel() == null || !getLevel().getBlockState(getBlockPos()).hasProperty(AlchemistCauldronBlock.LEVEL))
            return false;
        int level = Math.min(MAX_LEVELS, getLevel().getBlockState(getBlockPos()).getValue(AlchemistCauldronBlock.LEVEL));
        for (int i = 0; i < level; i++) {
            if (floatingItems.get(i).isEmpty()) {
                floatingItems.set(i, newItem);
                setChanged();
                return true;
            }
        }
        return false;
    }

    public int getItemWaterColor(ItemStack itemStack) {
        if (this.getLevel() == null)
            return 0;
        //TODO: ink/potion colors
        return BiomeColors.getAverageWaterColor(this.getLevel(), this.getBlockPos());
    }

//    public Stack<ItemStack> getStoredItems(){
//        return storedItems;
//    }

    public int getAverageWaterColor() {
        //TODO: figure out how this shit is actually going to work
        return getItemWaterColor(null);
    }

    @Override
    public void load(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, this.floatingItems);
        super.load(tag);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, this.floatingItems);
        super.saveAdditional(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        //var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        CompoundTag nbt = getUpdateTag();
        return ClientboundBlockEntityDataPacket.create(this, (block) -> nbt);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        //irons_spellbooks.LOGGER.debug("onDataPacket: pkt.getTag:{}", pkt.getTag());
        handleUpdateTag(pkt.getTag());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        //irons_spellbooks.LOGGER.debug("handleUpdateTag: tag:{}", tag);
        if (tag != null) {
            load(tag);
        }
    }

    static Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> newInteractionMap() {
        var map = Util.make(new Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction>(), (p_175646_) -> {
            p_175646_.defaultReturnValue((p_175739_, p_175740_, p_175741_, p_175742_, p_175743_, p_175744_) -> {
                return InteractionResult.PASS;
            });
        });

        map.put(Items.WATER_BUCKET, (blockState, level, pos, player, hand, currentLevel) -> {
            if (currentLevel < MAX_LEVELS) {
                createFilledResult(player, hand, level, blockState, pos, MAX_LEVELS, new ItemStack(Items.BUCKET), SoundEvents.BUCKET_EMPTY);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        });
        map.put(Items.GLASS_BOTTLE, (blockState, level, pos, player, hand, currentLevel) -> {
            if (currentLevel > 0) {
                //TODO: safety checks?
                var storedItems = ((AlchemistCauldronTile) level.getBlockEntity(pos)).storedItems;
                if (storedItems.empty()) {
                    //No items means we only hold water, so we should create a water bottle and decrement level
                    createFilledResult(player, hand, level, blockState, pos, currentLevel - 1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), SoundEvents.BOTTLE_FILL);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            return InteractionResult.PASS;
        });


        return map;
    }


    private static void createFilledResult(Player player, InteractionHand hand, Level level, BlockState blockState, BlockPos blockPos, int newLevel, ItemStack resultItem, SoundEvent soundEvent) {
        player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand), player, resultItem));
        level.setBlock(blockPos, blockState.setValue(AlchemistCauldronBlock.LEVEL, newLevel), 3);
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
