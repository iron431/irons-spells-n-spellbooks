package io.redspace.ironsspellbooks.entity.mobs.wizards;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * collection of common implementations and helpers for wizards with trades
 */
public interface IMerchantWizard extends Merchant {

    default void serializeMerchant(CompoundTag pCompound, @Nullable MerchantOffers offers, long lastRestockGameTime, int numberOfRestocksToday) {
        if (offers != null && !offers.isEmpty()) {
            pCompound.put("Offers", offers.createTag());
        }
        pCompound.putLong("LastRestock", lastRestockGameTime);
        pCompound.putInt("RestocksToday", numberOfRestocksToday);
    }

    default void deserializeMerchant(CompoundTag pCompound, Consumer<MerchantOffers> setOffers) {
        if (pCompound.contains("Offers", 10)) {
            setOffers.accept(new MerchantOffers(pCompound.getCompound("Offers")));
        }
        setLastRestockGameTime(pCompound.getLong("LastRestock"));
        setRestocksToday(pCompound.getInt("RestocksToday"));
    }

    default boolean isTrading() {
        return getTradingPlayer() != null;
    }

    default boolean needsToRestock() {
        for (MerchantOffer merchantoffer : this.getOffers()) {
            if (merchantoffer.needsRestock()) {
                return true;
            }
        }
        return false;
    }

    default boolean allowedToRestock() {
        return getRestocksToday() == 0 && merchantLevel().getGameTime() > getLastRestockGameTime() + 2400L;
    }

    default boolean shouldRestock() {
        /*
        Game time is persistent, Day Time not.
        Day time does not reset to zero every day, but commands and such often reset it otherwise
        Therefore, day time is not consistent enough to base our trades off of.
        (one day is 24,000 ticks)
         */
        long timeToNextRestock = getLastRestockGameTime() + 12000L;
        long currentGameTime = merchantLevel().getGameTime();
        //If total game time has exceeded one half day, we can restock.
        boolean hasDayElapsed = currentGameTime > timeToNextRestock;

        long currentDayTime = merchantLevel().getDayTime();
        if (getLastRestockCheckDayTime() > 0L) {
            long lastRestockDay = getLastRestockCheckDayTime() / 24000L;
            long currentDay = currentDayTime / 24000L;
            //Or, if day time is accurate (we arent in the future), and a whole day has passed, we can also restock.
            hasDayElapsed |= currentDay > lastRestockDay;
        } else {
            //Make our day time accurate again
            setLastRestockCheckDayTime(currentDayTime);
        }

        if (hasDayElapsed) {
            //update times
            setLastRestockCheckDayTime(currentDayTime);
            setRestocksToday(0);
        }
        boolean shouldRestock = this.needsToRestock() && allowedToRestock();
        if (shouldRestock) {
            setLastRestockGameTime(currentGameTime);
        }
        return shouldRestock;
    }

    default void restock() {
        for (MerchantOffer offer : getOffers()) {
            offer.updateDemand();
            offer.resetUses();
        }
        this.setRestocksToday(getRestocksToday() + 1);
    }

    int getRestocksToday();

    void setRestocksToday(int restocks);

    long getLastRestockGameTime();

    void setLastRestockGameTime(long time);

    long getLastRestockCheckDayTime();

    void setLastRestockCheckDayTime(long time);

    Level merchantLevel();

    @Override
    default int getVillagerXp() {
        return 0;
    }

    @Override
    default void overrideXp(int pXp) {

    }

    @Override
    default boolean showProgressBar() {
        return false;
    }

    @Override
    default boolean isClientSide() {
        return merchantLevel().isClientSide();
    }
}
