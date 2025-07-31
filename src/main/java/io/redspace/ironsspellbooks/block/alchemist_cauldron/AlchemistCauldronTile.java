package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AlchemistCauldronTile extends BlockEntity implements WorldlyContainer {
    public class AlchemistCauldronFluidHandler implements IFluidHandler {
        public class CallbackFluidTank extends FluidTank {
            public CallbackFluidTank(int capacity) {
                super(capacity);
            }

            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                AlchemistCauldronFluidHandler.this.onContentsChanged();
            }
        }

        /**
         * Can hold up to 4 fluids at once, and each tank should have enough capacity for 4 bottles of the same fluid.
         * If multiple fluids are present, each one gets exactly 1 tank.
         * Held capacity of all tanks combined should never exceed 1000mb
         */
        //fixme: need stack structure, not array/list structure
        IFluidTank[] tanks = new IFluidTank[]{new CallbackFluidTank(1000), new CallbackFluidTank(1000), new CallbackFluidTank(1000), new CallbackFluidTank(1000)};

        @Override
        public int getTanks() {
            return tanks.length;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank < 0 || tank > tanks.length ? FluidStack.EMPTY :
                    (tanks[tank].getFluidAmount() == 0 ? FluidStack.EMPTY : tanks[tank].getFluid());
        }

        @Override
        public int getTankCapacity(int tank) {
            return 1000;
        }

        public int fluidAmount() {
            return fluids().stream().mapToInt(FluidStack::getAmount).sum();
        }

        public boolean canFit(int fluidAmount) {
            return fluidAmount + this.fluidAmount() <= 1000;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank >= 0 && tank <= tanks.length && tanks[tank].isFluidValid(stack);
        }

        public boolean isTankCompatible(IFluidTank tank, FluidStack stack) {
            return tank.isFluidValid(stack) && FluidStack.isSameFluidSameComponents(tank.getFluid(), stack);
        }

        public void onContentsChanged() {
            AlchemistCauldronTile.this.setChanged();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.is(ModTags.CAULDRON_FLUID_DISALLOW)) {
                return 0;
            }
            int resourceLocation = -1;
            int emptyLocation = -1;
            int remainingCapacity = 1000 - fluidAmount();
            if (remainingCapacity == 0) {
                return 0;
            }
            // first, see if we already contain this fluid (we want to merge)
            // otherwise, keep track of the first empty spot where it can do
            for (int i = 0; i < tanks.length; i++) {
                if (isTankCompatible(tanks[i], resource)) {
                    resourceLocation = i;
                    break;
                } else if (emptyLocation == -1 && tanks[i].getFluid().isEmpty()) {
                    emptyLocation = i;
                }
            }
            // capped fluid input
            var copy = resource.copyWithAmount(Math.min(remainingCapacity, resource.getAmount()));
            // insert if applicable
            if (resourceLocation >= 0) {
                return tanks[resourceLocation].fill(copy, action);
            } else if (emptyLocation >= 0) {
                return tanks[emptyLocation].fill(copy, action);
            }

            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            for (int i = 0; i < tanks.length; i++) {
                var tank = tanks[i];
                if (isTankCompatible(tank, resource)) {
                    var result = tank.drain(resource, action);
                    // allow empty tanks to "bubble" to the top
                    for (int j = i; j < tanks.length - 1; j++) {
                        for (int k = j + 1; k < tanks.length; k++) {
                            if (tanks[j].getFluid().isEmpty() && !tanks[k].getFluid().isEmpty()) {
                                var tmp = tanks[j];
                                tanks[j] = tanks[k];
                                tanks[k] = tmp;
                            } else {
                                break;
                            }
                        }
                    }
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            // iterate backwards to prioritize draining the topmost layers
            for (int i = tanks.length - 1; i >= 0; i--) {
                var tank = tanks[i];
                if (!tank.getFluid().isEmpty()) {
                    return tank.drain(maxDrain, action);
                }
            }
            return FluidStack.EMPTY;
        }

        public boolean contains(FluidStack stack, int minAmount) {
            for (IFluidTank tank : tanks) {
                if (isTankCompatible(tank, stack)) {
                    return tank.getFluidAmount() >= minAmount;
                }
            }
            return false;
        }

        public boolean contains(Holder<Fluid> fluid, int minAmount) {
            for (IFluidTank tank : tanks) {
                if (tank.getFluid().is(fluid)) {
                    return tank.getFluidAmount() >= minAmount;
                }
            }
            return false;
        }

        public boolean contains(TagKey<Fluid> fluid, int minAmount) {
            for (IFluidTank tank : tanks) {
                if (tank.getFluid().is(fluid)) {
                    return tank.getFluidAmount() >= minAmount;
                }
            }
            return false;
        }

        public List<FluidStack> fluids() {
            return Arrays.stream(tanks).map(IFluidTank::getFluid).filter(f -> !f.isEmpty()).toList();
        }

        public void clear() {
            for (IFluidTank tank : tanks) {
                tank.drain(tank.getCapacity(), FluidAction.EXECUTE);
            }
        }

        public void save(String name, CompoundTag tag, HolderLookup.Provider access) {
            ListTag fluids = new ListTag();
            for (IFluidTank tank : tanks) {
                if (!tank.getFluid().isEmpty()) {
                    fluids.add(tank.getFluid().save(access));
                }
            }
            tag.put(name, fluids);
        }

        public void load(String name, CompoundTag tag, HolderLookup.Provider access) {
            if (tag.contains(name, 9)) {
                ListTag fluids = tag.getList(name, 10);
                int i = 0;
                try {
                    for (Tag l : fluids) {
                        FluidStack stack = FluidStack.parseOptional(access, (CompoundTag) l);
                        tanks[i++].fill(stack, FluidAction.EXECUTE);
                    }
                } catch (Exception e) {
                    IronsSpellbooks.LOGGER.error("Alchemist Cauldron Handler Failed to load fluid, skipping: {}", e.getMessage());
                }
            }
        }
    }

    public static int INPUT_SIZE = 4;
    //    public static final Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> INTERACTIONS = AlchemistCauldronTile.newInteractionMap();
    public final NonNullList<ItemStack> inputItems = NonNullList.withSize(INPUT_SIZE, ItemStack.EMPTY);
    private final int[] cooktimes = new int[INPUT_SIZE];
    boolean capDirty;
    public IFluidHandler fluidCapability;
    public AlchemistCauldronFluidHandler fluidInventory;

    public void refreshCapabilities() {
        this.fluidCapability = fluidInventory;
        this.invalidateCapabilities();
        capDirty = false;
    }

    public AlchemistCauldronTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.ALCHEMIST_CAULDRON_TILE.get(), pWorldPosition, pBlockState);
        fluidInventory = new AlchemistCauldronFluidHandler();
        capDirty = false;
    }

    /************************************************************
     Logic
     ***********************************************************/
    public static void serverTick(Level level, BlockPos pos, BlockState blockState, AlchemistCauldronTile cauldronTile) {
        if (cauldronTile.capDirty) {
            cauldronTile.refreshCapabilities();
        }
        for (int i = 0; i < cauldronTile.inputItems.size(); i++) {
            ItemStack itemStack = cauldronTile.inputItems.get(i);
            if (itemStack.isEmpty() || !cauldronTile.isBoiling(blockState))
                cauldronTile.cooktimes[i] = 0;
            else {
                cauldronTile.cooktimes[i]++;
            }
            if (cauldronTile.cooktimes[i] > 100) {
                cauldronTile.tryMeltInput(itemStack);
                cauldronTile.cooktimes[i] = 0;
            }
        }
        var random = Utils.random;
        if (cauldronTile.isBoiling(blockState)) {
            float waterLevel = Mth.lerp(cauldronTile.getFluidAmount() / 1000f, .25f, .9f);
            MagicManager.spawnParticles(level, ParticleTypes.BUBBLE_POP, pos.getX() + Mth.randomBetween(random, .2f, .8f), pos.getY() + waterLevel, pos.getZ() + Mth.randomBetween(random, .2f, .8f), 1, 0, 0, 0, 0, false);
        }

    }

    public ItemStack tryExecuteRecipeInteractions(Level level, ItemStack itemStack) {
        SingleRecipeInput fillRecipeInput = new SingleRecipeInput(itemStack);
        var recipeManager = level.getRecipeManager();
        var fillRecipe = recipeManager.getRecipeFor(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get(), fillRecipeInput, level).map(RecipeHolder::value);
        if (fillRecipe.isEmpty() && itemStack.has(DataComponents.POTION_CONTENTS)) {
            // dynamic potion handling
            FluidStack fluid;
            if (itemStack.get(DataComponents.POTION_CONTENTS).is(Potions.WATER)) {
                fluid = new FluidStack(Fluids.WATER, 250);
            } else {
                fluid = PotionFluid.from(itemStack);
            }
            fillRecipe = Optional.of(new FillAlchemistCauldronRecipe(Ingredient.of(itemStack), new ItemStack(Items.GLASS_BOTTLE), fluid, true, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_EMPTY)));
        }
        if (fillRecipe.isPresent()) {
            var recipe = fillRecipe.get();
            var amountThatCanFit = fluidInventory.fill(recipe.result(), IFluidHandler.FluidAction.SIMULATE);
            if ((!recipe.mustFitAll() || amountThatCanFit == recipe.result().getAmount()) && amountThatCanFit != 0) {
                fluidInventory.fill(recipe.result(), IFluidHandler.FluidAction.EXECUTE);
                this.setChanged();
                level.playSound(null, this.getBlockPos(), recipe.fillSound().value(), SoundSource.BLOCKS);
                return recipe.assemble(fillRecipeInput, level.registryAccess());
            }
        }

        FluidStack topFluid = fluidInventory.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        EmptyAlchemistCauldronRecipe.Input emptyRecipeInput = new EmptyAlchemistCauldronRecipe.Input(itemStack, topFluid);
        var emptyRecipe = recipeManager.getRecipeFor(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get(), emptyRecipeInput, level).map(RecipeHolder::value);
        if (emptyRecipe.isEmpty() && itemStack.is(Items.GLASS_BOTTLE)) {
            // dynamic potion handling

            var potionStack = PotionFluid.from(topFluid);
            if (!potionStack.isEmpty()) {
                emptyRecipe = Optional.of(new EmptyAlchemistCauldronRecipe(Ingredient.EMPTY, potionStack, topFluid.copyWithAmount(250), BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_FILL)));
            }
        }
        if (emptyRecipe.isPresent()) {
            var recipe = emptyRecipe.get();
            fluidInventory.drain(recipe.fluid(), IFluidHandler.FluidAction.EXECUTE);
            level.playSound(null, this.getBlockPos(), recipe.emptySound().value(), SoundSource.BLOCKS);
            this.setChanged();
            return recipe.assemble(emptyRecipeInput, level.registryAccess());
        }
        return ItemStack.EMPTY;
    }

    public ItemInteractionResult handleUse(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
//        if (level instanceof ServerLevel serverLevel) {
        ItemStack recipeResult = tryExecuteRecipeInteractions(level, itemStack);
        if (!recipeResult.isEmpty()) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand), player, recipeResult));
            return ItemInteractionResult.SUCCESS;
        }
        // item inputting
        if (isValidInput(itemStack)) {
            if (!level.isClientSide) {
                for (int i = 0; i < inputItems.size(); i++) {
                    var stack = inputItems.get(i);
                    if (stack.isEmpty()) {
                        var input = player.getAbilities().instabuild ? itemStack.copy() : itemStack.split(1);
                        input.setCount(1);
                        inputItems.set(i, input);
                        player.setItemInHand(hand, itemStack);
                        this.setChanged();
                        break;
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        // item taking
        //fixme: players cannot trigger block interactions while crouching
        else if ((itemStack.isEmpty() || player.isCrouching()) && hand.equals(InteractionHand.MAIN_HAND)) {
            for (ItemStack item : inputItems) {
                if (!item.isEmpty()) {
                    if (!level.isClientSide) {
                        var take = item.split(1);
                        if (player.getItemInHand(hand).isEmpty()) {
                            player.setItemInHand(hand, take);
                        } else {
                            if (!player.getInventory().add(take)) {
                                player.drop(take, false);
                            }
                        }
                        this.setChanged();
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
//        }
        //fixme: consume or pass?
        return ItemInteractionResult.CONSUME;
    }

    public void tryMeltInput(ItemStack itemStack) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        /** shouldMelt is whether the input should be consumed*/
        boolean shouldMelt = false;
        /** success is whether the process yields a result*/
        boolean success = true;
        Optional<ItemStack> byproduct = Optional.empty();
        if (itemStack.is(ItemRegistry.SCROLL.get()) && fluidInventory.contains(Tags.Fluids.WATER, 250)) {
            if (Utils.random.nextFloat() < ServerConfigs.SCROLL_RECYCLE_CHANCE.get()) {
                fluidInventory.drain(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
                fluidInventory.fill(new FluidStack(getInkFromScroll(itemStack).fluid(), 250), IFluidHandler.FluidAction.EXECUTE);
            } else {
                success = false;
            }
            shouldMelt = true;
        }
        if (!shouldMelt) {
            for (FluidStack fluid : fluidInventory.fluids()) {
                BrewAlchemistCauldronRecipe.Input input = new BrewAlchemistCauldronRecipe.Input(fluid, itemStack);
                var brewRecipeOpt = serverLevel.getRecipeManager().getRecipeFor(RecipeRegistry.ALCHEMIST_CAULDRON_BREW_TYPE.get(), input, serverLevel).map(RecipeHolder::value);
                if (brewRecipeOpt.isPresent()) {
                    var recipe = brewRecipeOpt.get();
                    int totalNewFluid = recipe.results().stream().mapToInt(FluidStack::getAmount).sum();
                    if (fluidInventory.canFit(totalNewFluid - recipe.fluidIn().getAmount()) && fluidInventory.contains(recipe.fluidIn(), recipe.fluidIn().getAmount())) {
                        shouldMelt = true; // marks reagent item for consumption
                        fluidInventory.drain(recipe.fluidIn(), IFluidHandler.FluidAction.EXECUTE);
                        recipe.results().forEach(result ->
                                fluidInventory.fill(result, IFluidHandler.FluidAction.EXECUTE)
                        );
                        byproduct = recipe.byproduct();
                    }
                }
            }
        }
        if (!shouldMelt && isBrewable(itemStack)) {
            for (FluidStack fluid : fluidInventory.fluids()) {
                ItemStack potionGhostStack = PotionFluid.from(fluid);
                if (potionGhostStack.isEmpty()) {
                    continue;
                }
                if ((serverLevel.potionBrewing().hasPotionMix(potionGhostStack, itemStack) || level.potionBrewing().hasContainerMix(potionGhostStack, itemStack))) {
                    var potionResult = serverLevel.potionBrewing().mix(itemStack, potionGhostStack); // yes, the order switched
                    FluidStack fluidResult = PotionFluid.from(potionResult).copyWithAmount(fluid.getAmount()); // take fluid from stack, and allow the brew to convert as much base as there was
                    fluidInventory.drain(fluid, IFluidHandler.FluidAction.EXECUTE);
                    fluidInventory.fill(fluidResult, IFluidHandler.FluidAction.EXECUTE);
                    shouldMelt = true; // marks reagent item for consumption
                }
            }
        }
        if (shouldMelt) {
            itemStack.shrink(1);
            if (byproduct.isPresent()) {
                for (int i = 0; i < inputItems.size(); i++) {
                    var stack = inputItems.get(i);
                    if (stack.isEmpty()) {
                        var input = byproduct.get().split(1);
                        inputItems.set(i, input);
                        break;
                    }
                }
                // should be impossible, but no space: drop item
                Vec3 pos = Vec3.upFromBottomCenterOf(this.getBlockPos(), 1);
                Containers.dropItemStack(level, pos.x, pos.y, pos.z, byproduct.get().split(1));
            }
            setChanged();
            if (success) {
                level.playSound(null, this.getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.MASTER, 1, 1);
                level.markAndNotifyBlock(this.getBlockPos(), this.level.getChunkAt(this.getBlockPos()), this.getBlockState(), this.getBlockState(), 1, 1);
            } else {
                level.playSound(null, this.getBlockPos(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.MASTER, 1, 1);
            }
        }
    }

    /************************************************************
     Cauldron Helpers
     ***********************************************************/
    public boolean isValidInput(ItemStack itemStack) {
        return itemStack.is(ItemRegistry.SCROLL.get()) || isBrewable(itemStack) ||
                (this.level != null && level.getRecipeManager().getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_BREW_TYPE.get()).stream().anyMatch(holder -> holder.value().reagent().test(itemStack)));
    }

    public boolean isBrewable(ItemStack itemStack) {
        return ServerConfigs.ALLOW_CAULDRON_BREWING.get() && this.level != null && level.potionBrewing().isIngredient(itemStack);
    }

    public static InkItem getInkFromScroll(ItemStack scrollStack) {
        var spellContainer = ISpellContainer.get(scrollStack);
        var spellData = spellContainer.getSpellAtIndex(0);

        SpellRarity rarity = spellData.getSpell().getRarity(spellData.getLevel());
        return InkItem.getInkForRarity(rarity);
    }

    /************************************************************
     Tile Entity Handling
     ***********************************************************/
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registryAccess) {
        Utils.loadAllItems(tag, this.inputItems, "Items", registryAccess);
        fluidInventory.load("Results", tag, registryAccess);
        super.loadAdditional(tag, registryAccess);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registryAccess) {
        Utils.saveAllItems(tag, this.inputItems, "Items", registryAccess);
        fluidInventory.save("Results", tag, registryAccess);
        super.saveAdditional(tag, registryAccess);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        return packet;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(pkt.getTag(), lookupProvider);
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        this.inputItems.clear();
        this.fluidInventory.clear();
        if (tag != null) {
            loadAdditional(tag, lookupProvider);
        }
    }

    public void drops() {
        SimpleContainer simpleContainer = new SimpleContainer(inputItems.size());
        for (int i = 0; i < inputItems.size(); i++) {
            simpleContainer.setItem(i, inputItems.get(i));
        }
        if (level != null) {
            Containers.dropContents(this.level, this.worldPosition, simpleContainer);
        }
    }

    /************************************************************
     Wordly Container Implementation
     ***********************************************************/
    @Override
    public int[] getSlotsForFace(Direction pSide) {
        //any side has access to all four input items
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        //any non-bottom side can insert a valid item
        return pDirection != Direction.DOWN && isValidInput(pItemStack) && getItem(pIndex).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        // can only withdrawl from botoom
        return pDirection == Direction.DOWN;
    }

    @Override
    public void clearContent() {
        inputItems.clear();
        fluidInventory.clear();
    }

    @Override
    public int getContainerSize() {
        return INPUT_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return inputItems.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return pSlot >= 0 && pSlot <= inputItems.size() ? inputItems.get(pSlot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return ContainerHelper.removeItem(inputItems, pSlot, pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return pSlot >= 0 && pSlot <= inputItems.size() ? inputItems.remove(pSlot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        if (pSlot >= 0 && pSlot <= inputItems.size()) {
            inputItems.set(pSlot, pStack);
        }
    }

    public boolean isBoiling(BlockState blockState) {
        return getFluidAmount() >= 1;
    }

    public int getFluidAmount() {
        return this.fluidInventory.fluidAmount();
    }
}
