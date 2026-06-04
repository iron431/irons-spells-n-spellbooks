package io.redspace.ironsspellbooks.api.events;

import io.redspace.ironsspellbooks.player.ModNameCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.Optional;

public class CustomizeScrollModNameEvent extends Event {
    private final Component originalModName;

    private final String modid;

    private Component modName;

    public CustomizeScrollModNameEvent(Component originalModName, String modid) {
        this.originalModName = originalModName;
        this.modName = originalModName;
        this.modid = modid;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    public Component getOriginalModName() {
        return originalModName;
    }

    public Component getModName() {
        return modName;
    }

    public void setModName(Component modName) {
        this.modName = modName;
    }

    public String getModId() {
        return modid;
    }

    /**
     * Helper to post the {@link CustomizeScrollModNameEvent}, with the default mod title and style.
     */
    public static Optional<Component> resolveModLabel(String modid) {
        Optional<String> modname = ModNameCache.getModName(modid);
        if (modname.isPresent()) {
            Component modNameComponent = Component.literal(modname.get()).withStyle(ChatFormatting.DARK_GRAY);
            CustomizeScrollModNameEvent event = new CustomizeScrollModNameEvent(modNameComponent, modid);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                return Optional.of(event.getModName());
            }
        }
        return Optional.empty();
    }
}
