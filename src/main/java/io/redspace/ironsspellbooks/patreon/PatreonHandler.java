package io.redspace.ironsspellbooks.patreon;

import com.google.gson.*;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class PatreonHandler {
    record Profile(String username, UUID uuid, PatreonPermissions permissions) {
        static final Profile NULL = new Profile("", null, PatreonPermissions.None);
    }

    record ChronicleEntry(String displayName, int pledge, int bookCategory) {
    }

    private static final long DEFAULT_RETRY_SECONDS = 60 * 5;
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(25);

    private static PatreonHandler instance;

    private final URI endpoint;
    //    private final Path cacheFile;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    private final ConcurrentHashMap<String, Profile> usernameToProfile;
    private final ConcurrentHashMap<UUID, Profile> uuidToProfile;
    private final List<ChronicleEntry> chronicleEntries;
    private final ScheduledExecutorService scheduler;

    private volatile ScheduledFuture<?> scheduledTask;

    private PatreonHandler(URI endpoint) {
        this.uuidToProfile = new ConcurrentHashMap<>();
        this.usernameToProfile = new ConcurrentHashMap<>();
        this.chronicleEntries = new ArrayList<>();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "iron431-patreon-service");
            t.setDaemon(true);
            return t;
        });
        this.endpoint = endpoint;
        this.httpClient = HttpClient.newHttpClient();
    }

    public static void initialize() {
        if (instance != null) return;
        try {
            URI endpoint = new URI("https://dev.patreon.redspace.io/api/game-data");
            instance = new PatreonHandler(endpoint);
            instance.scheduleNext(0);
        } catch (Exception ignored) {
        }
    }

    public static void shutdown() {
        if(instance == null) return;
        instance.interrupt();
        instance = null;
    }

    private void interrupt() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        scheduler.shutdownNow();
    }

    private void scheduleNext(long delaySeconds) {
        scheduledTask = scheduler.schedule(
                this::refreshInternal,
                delaySeconds,
                TimeUnit.SECONDS
        );
    }

    private void refreshInternal() {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(this::handleResponse)
                .exceptionally(ex -> {
                    IronsSpellbooks.LOGGER.error("Failed to handle Patreon Data response: {}", ex.getMessage());
                    scheduleNext(DEFAULT_RETRY_SECONDS);
                    return null;
                });
    }

    private void handleResponse(HttpResponse<String> httpResponse) {
        //todo: validate headers and code and stuff
        String httpBody = httpResponse.body();
        JsonObject response = JsonParser.parseString(httpBody).getAsJsonObject();
        if (!response.has("data")) {
            throw new RuntimeException("Data not present in payload");
        }
        JsonArray data = response.getAsJsonArray("data");
        synchronized (chronicleEntries) {
            chronicleEntries.clear();
            uuidToProfile.clear();
            usernameToProfile.clear();
            for (JsonElement elem : data) {
                if (!elem.isJsonObject()) continue;
                JsonObject userEntry = elem.getAsJsonObject();
                try {
                    int pledgeTier = userEntry.get("pledgeTier").getAsInt();
                    int bookCategory = userEntry.get("bookCategory").getAsInt();
                    String displayName = userEntry.get("displayName").getAsString();
                    chronicleEntries.add(new ChronicleEntry(displayName, pledgeTier, bookCategory));
                    if (userEntry.has("uuid")) {
                        UUID uuid = uuidFromUndashed(userEntry.get("uuid").getAsString());
                        String username;
                        if (userEntry.has("mcUsername")) {
                            username = userEntry.get("mcUsername").getAsString();
                        } else {
                            username = ""; // todo: fetch from uuid
                        }
                        Profile profile = new Profile(username, uuid, PatreonPermissions.values()[pledgeTier]);
                        if (uuidToProfile.contains(uuid)) {
                            Profile conflict = uuidToProfile.get(uuid);
                            profile = new Profile(username, uuid, PatreonPermissions.values()[Math.max(profile.permissions.ordinal(), conflict.permissions.ordinal())]);
                        }
                        uuidToProfile.put(uuid, profile);
                        usernameToProfile.put(username, profile);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        //todo: TTL, custom delay, etc
        scheduleNext(DEFAULT_RETRY_SECONDS);
    }

    public static UUID uuidFromUndashed(String s) {
        if (s == null || s.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string");
        }

        String dashed = s.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5"
        );

        return UUID.fromString(dashed);
    }

    public static PatreonPermissions getPatreonPermissions(@NotNull Player player) {
        return getPatreonPermissions(player.getUUID());
    }

    public static PatreonPermissions getPatreonPermissions(UUID playerUUID) {
        return instance.uuidToProfile.getOrDefault(playerUUID, Profile.NULL).permissions();
    }

    public static PatreonPermissions getPatreonPermissionsByUsername(String username) {
        return instance.usernameToProfile.getOrDefault(username, Profile.NULL).permissions();
    }

    public static @Nullable UUID profileFromUsername(String username) {
        return instance.usernameToProfile.getOrDefault(username, Profile.NULL).uuid();
    }
}
