package io.redspace.ironsspellbooks.api.events.summon;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CasterRef;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Summon lifecycle events, posted on the game event bus by {@link SummonManager}.
 * <p>
 * The owner is exposed as a {@link CasterRef}
 * {@link #getOwnerEntity()} is a convenience for the common entity-owner case. {@link #getCastContext()} is only
 * present for summons produced by a skill cast (see {@link SummonManager#summon})
 */
public abstract class SummonEvent extends Event {
    protected Entity summon;
    private final @Nullable CasterRef owner;
    private final @Nullable CastContext castContext;

    protected SummonEvent(Entity summon, @Nullable CasterRef owner, @Nullable CastContext castContext) {
        this.summon = summon;
        this.owner = owner;
        this.castContext = castContext;
    }

    public Entity getSummon() {
        return summon;
    }

    public @Nullable CasterRef getOwner() {
        return owner;
    }

    /**
     * @return the owner when it is an entity, otherwise null
     */
    public @Nullable Entity getOwnerEntity() {
        return owner != null && owner.get() instanceof Entity entity ? entity : null;
    }

    public @Nullable CastContext getCastContext() {
        return castContext;
    }

    /**
     * Fired before the summon is added to the level. Cancelling skips the spawn entirely.
     * The summoned entity may be substituted via {@link #setSummon(Entity)}; the replacement is what gets spawned,
     * owned and tracked.
     */
    public static class Pre extends SummonEvent implements ICancellableEvent {
        public Pre(Entity summon, @Nullable CasterRef owner, @Nullable CastContext castContext) {
            super(summon, owner, castContext);
        }

        public void setSummon(Entity summon) {
            this.summon = summon;
        }
    }

    /**
     * Fired after the summon has been added to the level, assigned its owner and duration, and (for casts) attached to the cast context.
     */
    public static class Post extends SummonEvent {
        public Post(Entity summon, @Nullable CasterRef owner, @Nullable CastContext castContext) {
            super(summon, owner, castContext);
        }
    }

    /**
     * Fired after {@link SummonManager#setOwner} has updated the ownership maps. Fires for every ownership assignment,
     * including ones not produced by a cast. Not cancellable.
     */
    public static class OwnerSet extends SummonEvent {
        public OwnerSet(Entity summon, CasterRef owner) {
            super(summon, owner, null);
        }
    }

    /**
     * Fired immediately before a summon is dismissed through {@link SummonManager#unsummon}. Not cancellable.
     */
    public static class Unsummon extends SummonEvent {
        public enum Reason {
            /**
             * Summon duration ran out
             */
            EXPIRED,
            /**
             * Owner dismissed the summon
             */
            DISMISSED,
            /**
             * Owner changed dimension and the summon cannot follow
             */
            OWNER_CHANGED_DIMENSION,
            /**
             * Summon was hit by an anti-magic effect
             */
            ANTI_MAGIC,
            OTHER
        }

        private final Reason reason;

        public Unsummon(Entity summon, @Nullable CasterRef owner, Reason reason) {
            super(summon, owner, null);
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }
    }
}
