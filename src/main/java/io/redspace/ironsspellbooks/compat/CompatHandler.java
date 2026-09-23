package io.redspace.ironsspellbooks.compat;

import net.neoforged.fml.ModList;

import java.util.Map;

public class CompatHandler {
    private static final Map<String, Runnable> MOD_MAP = Map.of(
            // todo(compat): Apotheosis handler not ported to 1.21
//            "apotheosis", ApotheosisHandler::init
    );

    public static void init() {
        MOD_MAP.forEach((modid, supplier) -> {
            if (ModList.get().isLoaded(modid)) {
                supplier.run();
            }
        });
    }
}
