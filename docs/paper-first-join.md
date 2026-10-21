# Detecting first joins on OneClient with Paper 1.21.11

Listen for `OneClientPlayerJoinEvent` and keep a flag in the player's `PersistentDataContainer`. No flag means it's their first OneClient join.

The event fires once per session, when the client's hello packet arrives on the `oneclient:api` channel, shortly after Bukkit's `PlayerJoinEvent`. The API forgets players when they leave, so remembering them is up to your plugin.

## Setup

Install the `OneClientServerAPI-Bukkit` jar on the server (Spigot 1.8.8+, Paper, or Folia). Your plugin compiles against the `api` artifact and gets the implementation from that jar at runtime.

`build.gradle.kts`:

```kotlin
plugins {
    java
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.polyfrost.org/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.polyfrost.oneclient:api:0.1.0")
}
```

Keep the API `compileOnly`. A shaded copy of `OneClient` never gets registered, so `getApi()` throws `IllegalStateException`.

`plugin.yml`:

```yaml
name: FirstJoin
version: '1.0.0'
main: com.example.firstjoin.FirstJoinPlugin
api-version: '1.21'
depend: [OneClientServerAPI]
```

`depend` makes Paper enable OneClientServerAPI first, so `OneClient.getApi()` works in your `onEnable`.

## Detecting OneClient players

A player counts as a OneClient player once their client sends the hello packet. Until then `isOneClient` returns `false`, so don't check it in `PlayerJoinEvent`; use the API's join event.

| Call | Returns |
| --- | --- |
| `OneClient.getApi()` | The `OneClientApi`; throws if OneClientServerAPI isn't enabled |
| `OneClient.isOneClient(uuid)` | `true` if the player has sent a hello this session; `false` if the API isn't loaded |
| `api.getPlayer(uuid)` | `Optional<OneClientPlayer>` for a connected OneClient player |
| `api.getPlayers()` | Every OneClient player online right now |
| `api.getEventBus().subscribe(type, listener)` | A `Subscription`; call `unsubscribe()` in `onDisable` |
| `OneClientPlayerJoinEvent` / `OneClientPlayerQuitEvent` | Posted on hello and on Bukkit quit; `getPlayer()` is the `OneClientPlayer` |

`OneClientPlayer` has `getUniqueId()`, `getName()`, `getClientVersion()`, `getMinecraftVersion()`, `getProtocolVersion()` and `getJoinedAtMillis()`.

On Paper the join event is posted from Bukkit's plugin message callback, so it runs on the main thread and you can use the `Player` directly.

## Tracking first joins

Store a byte under a `NamespacedKey` in the player's `PersistentDataContainer`. Paper saves it in the player's data file, so it survives restarts with no database.

1. On `OneClientPlayerJoinEvent`, look up the Bukkit `Player` by UUID. Skip them if it's `null` (they already left).
2. If the key is already set, they've joined on OneClient before.
3. Otherwise, set the key and run your first-join code.

This fires on a player's first OneClient join, even if they've played the server on vanilla before. To catch only players new to the server, also check `player.hasPlayedBefore()`. Read it in `PlayerJoinEvent` and keep it until the hello arrives, since the server may write the data file in between. The example below does this.

## Example

```java
public final class FirstJoinPlugin extends JavaPlugin implements Listener {
    private final Set<UUID> newToServer = ConcurrentHashMap.newKeySet();
    private NamespacedKey seenKey;
    private EventBus.Subscription subscription;

    @Override
    public void onEnable() {
        seenKey = new NamespacedKey(this, "oneclient_seen");
        getServer().getPluginManager().registerEvents(this, this);
        subscription = OneClient.getApi().getEventBus()
            .subscribe(OneClientPlayerJoinEvent.class, this::onOneClientJoin);
    }

    @Override
    public void onDisable() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            newToServer.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        newToServer.remove(event.getPlayer().getUniqueId());
    }

    private void onOneClientJoin(OneClientPlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Player player = getServer().getPlayer(uuid);
        if (player == null) {
            return;
        }

        PersistentDataContainer data = player.getPersistentDataContainer();
        if (data.has(seenKey, PersistentDataType.BYTE)) {
            return;
        }
        data.set(seenKey, PersistentDataType.BYTE, (byte) 1);

        boolean brandNew = newToServer.remove(uuid);
        player.sendMessage(brandNew
            ? "Welcome to the server, and thanks for using OneClient!"
            : "First time here on OneClient. Welcome!");
        getLogger().info(player.getName() + " joined on OneClient "
            + event.getPlayer().getClientVersion() + " for the first time");
    }
}
```

If you only care about the first OneClient join, delete the `newToServer` set and both Bukkit handlers.

## Testing

1. Put `OneClientServerAPI-Bukkit-0.1.0.jar` and your plugin in `plugins/`, then start Paper 1.21.11.
2. Join on OneClient. You should get the welcome message and appear in `/oneclient list` (needs `oneclient.admin`).
3. Rejoin. No message this time.
4. To reset, stop the server and delete `world/playerdata/<your uuid>.dat`, or rename the key.

## Caveats

- Vanilla clients never fire the event. Put anything every player needs in `PlayerJoinEvent`.
- The flag lives in the main world's player data. Wiping `playerdata` resets everyone.
