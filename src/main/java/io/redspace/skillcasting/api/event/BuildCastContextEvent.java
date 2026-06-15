package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.api.cast.CastContext;
import net.neoforged.bus.api.Event;

public abstract class BuildCastContextEvent extends Event {
    private final CastContext context;

    public BuildCastContextEvent(CastContext context) {
        this.context = context;
    }

    public CastContext context() {
        return context;
    }

    public static class Level extends BuildCastContextEvent {
        private final int baseLevel;
        private int level;

        public Level(CastContext context, int baseLevel) {
            super(context);
            this.baseLevel = baseLevel;
            this.level = baseLevel;
        }

        public int baseLevel() {
            return baseLevel;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    public static class Post extends BuildCastContextEvent {
        public Post(CastContext context) {
            super(context);
        }
    }
}
