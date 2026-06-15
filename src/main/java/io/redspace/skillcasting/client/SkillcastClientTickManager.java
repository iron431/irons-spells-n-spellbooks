package io.redspace.skillcasting.client;

import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

public class SkillcastClientTickManager {
    private record Wrapped(CasterRef casterRef, ClientSkillTicker ticker) {
    }

    private static final List<Wrapped> TICKING = new ArrayList<>();

    @SubscribeEvent
    public static void tick(ClientTickEvent.Pre event) {
        if (TICKING.isEmpty()) {
            return;
        }
        List<Wrapped> toRemove = new ArrayList<>();
        for (Wrapped wrapped : TICKING) {
            CasterRef casterRef = wrapped.casterRef;
            ClientSkillTicker ticker = wrapped.ticker;
            if (!casterRef.isValid()) {
                toRemove.add(wrapped);
                continue;
            }
            SkillcastingData data = casterRef.skillcastingData();
            ActiveCast activeCast = data.getActiveCast();
            if (activeCast == null) {
                toRemove.add(wrapped);
                continue;
            }
            ticker.tick(casterRef, data, activeCast);
        }
        TICKING.removeAll(toRemove);
    }

    public static void track(CasterRef casterRef, ClientSkillTicker ticker) {
        TICKING.add(new Wrapped(casterRef, ticker));
    }
}
