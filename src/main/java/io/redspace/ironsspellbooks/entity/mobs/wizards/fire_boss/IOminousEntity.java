package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

/**
 * Interface marks entities that interact with the Bad Omen/Trial Omen ominous system
 * <br>
 * Entities marked as such will automatically trigger Trial Omen on spawn, and automatically have given entrypoints triggered
 */
public interface IOminousEntity {
    /**
     * Server-Side. Setup any ominous state tracking and play any ominous spawn effects.
     */
    void onOminousTrigger();

    boolean isOminous();

    default float ominousTriggerRange() {
        return 24;
    }
}
