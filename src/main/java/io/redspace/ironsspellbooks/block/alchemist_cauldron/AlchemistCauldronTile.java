package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
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
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

import static io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronBlock.LEVEL;
import static io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronBlock.MAX_LEVELS;

public class AlchemistCauldronTile extends BlockEntity implements WorldlyContainer {
    public Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> interactions = AlchemistCauldronTile.newInteractionMap();
    //basically the input container
    public final NonNullList<ItemStack> inputItems = NonNullList.withSize(MAX_LEVELS, ItemStack.EMPTY);
    //basically the output container
    public final NonNullList<ItemStack> resultItems = NonNullList.withSize(MAX_LEVELS, ItemStack.EMPTY);
    private final int[] cooktimes = new int[MAX_LEVELS];

    public AlchemistCauldronTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.ALCHEMIST_CAULDRON_TILE.get(), pWorldPosition, pBlockState);
    }

    /************************************************************
     Logic
     ***********************************************************/
    public static void serverTick(Level level, BlockPos pos, BlockState blockState, AlchemistCauldronTile cauldronTile) {
        for (int i = 0; i < cauldronTile.inputItems.size(); i++) {
            ItemStack itemStack = cauldronTile.inputItems.get(i);
            if (itemStack.isEmpty() || !AlchemistCauldronBlock.isBoiling(blockState))
                cauldronTile.cooktimes[i] = 0;
            else {
                cauldronTile.cooktimes[i]++;
            }
            if (cauldronTile.cooktimes[i] > 100) {
                cauldronTile.meltComponent(itemStack);
                cauldronTile.cooktimes[i] = 0;
            }
        }
        var random = level.getRandom();
        if (AlchemistCauldronBlock.isBoiling(blockState)) {
            MagicManager.spawnParticles(level, ParticleTypes.BUBBLE_POP, pos.getX() + Mth.randomBetween(random, .2f, .8f), pos.getY() + AlchemistCauldronRenderer.getWaterOffest(blockState), pos.getZ() + Mth.randomBetween(random, .2f, .8f), 1, 0, 0, 0, 0, false);
        }
    }

    public InteractionResult handleUse(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        int currentLevel = blockState.getValue(LEVEL);
        var cauldronInteractionResult = interactions.get(itemStack.getItem()).interact(blockState, level, pos, currentLevel, itemStack);
        if (cauldronInteractionResult != null) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, cauldronInteractionResult));
            this.setChanged();
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (isValidInput(itemStack)) {
            if (!level.isClientSide && appendItem(inputItems, itemStack)) {
                if (!player.getAbilities().instabuild)
                    itemStack.shrink(1);
                this.setChanged();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (itemStack.isEmpty() && hand.equals(InteractionHand.MAIN_HAND)) {
            var item = grabItem(inputItems);
            if (!item.isEmpty()) {
                if (!level.isClientSide) {
                    player.setItemInHand(hand, item);
                    this.setChanged();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public void meltComponent(ItemStack itemStack) {
        //This is only called Server Side
        if (level == null)
            return;
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.meltComponent: {}", itemStack.getDisplayName().getString());
        boolean shouldMelt = false;
        SoundEvent successSound = SoundEvents.BREWING_STAND_BREW;
        if ( itemStack.is(ItemRegistry.SCROLL.get()) && !isFull(resultItems)) {
            if (level.random.nextFloat() < ServerConfigs.SCROLL_RECYCLE_CHANCE.get()) {
                ItemStack result = new ItemStack(getInkFromScroll(itemStack));
                appendItem(resultItems, result);
            } else {
                //failure sound
                successSound = SoundEvents.GENERIC_EXTINGUISH_FIRE;
            }
            shouldMelt = true;

        } else if (BrewingRecipeRegistry.isValidIngredient(itemStack)) {
            for (int i = 0; i < resultItems.size(); i++) {
                ItemStack potentialPotion = resultItems.get(i);
                ItemStack output = BrewingRecipeRegistry.getOutput(potentialPotion.isEmpty() ? PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER) : potentialPotion, itemStack);
                if (!output.isEmpty()) {
                    resultItems.set(i, output);
                    shouldMelt = true;
                }
                IronsSpellbooks.LOGGER.debug("{} + {} = {} ({})", potentialPotion.getDisplayName().getString(), itemStack.getDisplayName().getString(), output.getDisplayName().getString(), shouldMelt);
            }
        }
        if (shouldMelt) {
            itemStack.shrink(1);
            setChanged();
            level.playSound(null, this.getBlockPos(), successSound, SoundSource.MASTER, 1, 1);
            level.markAndNotifyBlock(this.getBlockPos(), this.level.getChunkAt(this.getBlockPos()), this.getBlockState(), this.getBlockState(), 1, 1);
        }
    }

    /************************************************************
     Cauldron Helpers
     ***********************************************************/
    public boolean isValidInput(ItemStack itemStack) {
        return itemStack.is(ItemRegistry.SCROLL.get()) || BrewingRecipeRegistry.isValidIngredient(itemStack);
    }

    public int getItemWaterColor(ItemStack itemStack) {
        if (this.getLevel() == null)
            return 0;
        if (itemStack.is(ItemRegistry.INK_COMMON.get()))
            return 0x222222;
        if (itemStack.is(ItemRegistry.INK_UNCOMMON.get()))
            return 0x124300;
        if (itemStack.is(ItemRegistry.INK_RARE.get()))
            return 0x0f3844;
        if (itemStack.is(ItemRegistry.INK_EPIC.get()))
            return 0xa52ea0;
        if (itemStack.is(ItemRegistry.INK_LEGENDARY.get()))
            return 0xfcaf1c;
        if (itemStack.is(ItemRegistry.BLOOD_VIAL.get()))
            return 0x5b0716;
        if (PotionUtils.getPotion(itemStack) != Potions.EMPTY)
            return PotionUtils.getColor(itemStack);
        return BiomeColors.getAverageWaterColor(this.getLevel(), this.getBlockPos());
    }

    public int getAverageWaterColor() {
        float f = 0.0F;
        float f1 = 0.0F;
        float f2 = 0.0F;

        for (ItemStack itemStack : resultItems) {
            int k = getItemWaterColor(itemStack);
            f += (float) ((k >> 16 & 255)) / 255.0F;
            f1 += (float) ((k >> 8 & 255)) / 255.0F;
            f2 += (float) ((k >> 0 & 255)) / 255.0F;
        }

        f = f / (float) 4 * 255.0F;
        f1 = f1 / (float) 4 * 255.0F;
        f2 = f2 / (float) 4 * 255.0F;
        return (int) f << 16 | (int) f1 << 8 | (int) f2;
    }

    public Item getInkFromScroll(ItemStack scrollStack) {
        if (scrollStack.getItem() instanceof Scroll scroll) {
            var spellData = SpellData.getSpellData(scrollStack);
            SpellRarity rarity = spellData.getSpell().getRarity();
            return switch (rarity) {
                case COMMON -> ItemRegistry.INK_COMMON.get();
                case UNCOMMON -> ItemRegistry.INK_UNCOMMON.get();
                case RARE -> ItemRegistry.INK_RARE.get();
                case EPIC -> ItemRegistry.INK_EPIC.get();
                case LEGENDARY -> ItemRegistry.INK_LEGENDARY.get();
                default -> ItemRegistry.INK_COMMON.get();
            };
        } else
            return null;
    }

    public static boolean appendItem(NonNullList<ItemStack> container, ItemStack newItem) {
        for (int i = 0; i < container.size(); i++) {
            if (container.get(i).isEmpty()) {
                var newItemCopy = newItem.copy();
                newItemCopy.setCount(1);
                container.set(i, newItemCopy);
                IronsSpellbooks.LOGGER.debug("{}", container.toString());
                return true;
            }
        }
        return false;
    }

    public static ItemStack grabItem(NonNullList<ItemStack> container) {
        for (int i = container.size() - 1; i >= 0; i--) {
            ItemStack item = container.get(i);
            if (!item.isEmpty()) {
                var newItem = item.copy();
                newItem.setCount(1);
                item.shrink(1);
                IronsSpellbooks.LOGGER.debug("{}", container.toString());
                return newItem;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isEmpty(NonNullList<ItemStack> container) {
        for (ItemStack itemStack : container)
            if (!itemStack.isEmpty())
                return false;
        return true;
    }

    public static boolean isFull(NonNullList<ItemStack> container) {
        for (ItemStack itemStack : container)
            if (itemStack.isEmpty())
                return false;
        return true;
    }


    /************************************************************
     Tile Entity Handling
     ***********************************************************/
    @Override
    public void setChanged() {
        super.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }


    @Override
    public void load(CompoundTag tag) {
        Utils.loadAllItems(tag, this.inputItems, "Items");
        Utils.loadAllItems(tag, this.resultItems, "Results");
        super.load(tag);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        Utils.saveAllItems(tag, this.inputItems, "Items");
        Utils.saveAllItems(tag, this.resultItems, "Results");
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
        var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        return packet;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        //irons_spellbooks.LOGGER.debug("onDataPacket: pkt.getTag:{}", pkt.getTag());
        handleUpdateTag(pkt.getTag());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        //this only gets run client side
        //why do we have to clear it? isnt that what load should do? missing namespace = ignroe?? i wouldnt fucking know
        this.inputItems.clear();
        this.resultItems.clear();
        if (tag != null) {
            load(tag);
        }
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.handleUpdateTag: tag:{}", tag);
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.handleUpdateTag: items:{}", inputItems);
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.handleUpdateTag: results:{}", resultItems);

    }

    public void drops() {
        SimpleContainer simpleContainer = new SimpleContainer(inputItems.size());
        for (int i = 0; i < inputItems.size(); i++) {
            simpleContainer.setItem(i, inputItems.get(i));
        }
        Containers.dropContents(this.level, this.worldPosition, simpleContainer);
    }

    /************************************************************
     Interaction Map
     ***********************************************************/
    static Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> newInteractionMap() {
        var map = Util.make(new Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction>(), (o2o) -> {
            o2o.defaultReturnValue((blockState, level, blockPos, i, itemStack) -> null);
        });

        map.put(Items.WATER_BUCKET, (blockState, level, pos, currentLevel, itemstack) -> {
            if (currentLevel < MAX_LEVELS) {
                return createFilledResult(level, blockState, pos, MAX_LEVELS, new ItemStack(Items.BUCKET), SoundEvents.BUCKET_EMPTY);
            } else {
                return null;
            }
        });
        map.put(Items.BUCKET, (blockState, level, pos, currentLevel, itemstack) -> {
            if (level.getBlockEntity(pos) instanceof AlchemistCauldronTile tile) {
                if (isEmpty(tile.resultItems) && currentLevel == MAX_LEVELS) {
                    return createFilledResult(level, blockState, pos, 0, new ItemStack(Items.WATER_BUCKET), SoundEvents.BUCKET_FILL);
                }
            }
            return null;
        });
        map.put(Items.GLASS_BOTTLE, (blockState, level, pos, currentLevel, itemstack) -> {
            if (currentLevel > 0 && level.getBlockEntity(pos) instanceof AlchemistCauldronTile tile) {
                var storedItems = tile.resultItems;
                if (isEmpty(storedItems)) {
                    //No items means we only hold water, so we should create a water bottle and decrement level
                    return createFilledResult(level, blockState, pos, currentLevel - 1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), SoundEvents.BOTTLE_FILL);
                } else {
                    //If we have an item ready, pop it but don't change the level
                    return createFilledResult(level, blockState, pos, currentLevel, grabItem(storedItems), SoundEvents.BOTTLE_FILL_DRAGONBREATH);
                }

            }
            return null;
        });
        map.put(Items.POTION, (blockState, level, pos, currentLevel, itemstack) -> {
            if (currentLevel < MAX_LEVELS && PotionUtils.getPotion(itemstack) == Potions.WATER) {
                return createFilledResult(level, blockState, pos, currentLevel + 1, new ItemStack(Items.GLASS_BOTTLE), SoundEvents.BOTTLE_EMPTY);
            }
            return null;
        });


        return map;
    }

    private static ItemStack createFilledResult(Level level, BlockState blockState, BlockPos blockPos, int newLevel, ItemStack resultItem, SoundEvent soundEvent) {
        level.setBlock(blockPos, blockState.setValue(LEVEL, newLevel), 3);
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        return resultItem;
    }

    /************************************************************
     Wordly Container Implementation
     ***********************************************************/
    @Override
    public int[] getSlotsForFace(Direction pSide) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return pItemStack.is(ItemRegistry.SCROLL.get());
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return false;
    }

    @Override
    public void clearContent() {
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.clearContents");
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.isEmpty");
        return isEmpty(inputItems);
    }

    @Override
    public ItemStack getItem(int pSlot) {
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.getItem ({})", pSlot);
        return pSlot >= 0 && pSlot <= inputItems.size() ? inputItems.get(pSlot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.removeItem ({}, #{})", pSlot, pAmount);
        //stack size is always one inside the cauldron, so we should be able to ignore amount
        return pSlot >= 0 && pSlot <= inputItems.size() ? inputItems.remove(pSlot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.removeItemNoUpdate ({})", pSlot);
        return pSlot >= 0 && pSlot <= inputItems.size() ? inputItems.remove(pSlot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        IronsSpellbooks.LOGGER.debug("AlchemistCauldronTile.setItem ({}, {})", pSlot, pStack);
        if (pSlot >= 0 && pSlot <= inputItems.size()) {
            if (inputItems.get(pSlot).isEmpty())
                inputItems.set(pSlot, pStack);
            else
                appendItem(inputItems, pStack);
        }
    }
}
