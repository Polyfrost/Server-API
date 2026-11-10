# OneClient server protocol

Server and client talk over the `oneclient:api` plugin channel. Each message is one packet: a VarInt packet ID followed
by its fields. The current protocol version is 2.

You don't need this page to use the API. `OneClientPlayer` builds these packets for you. This page documents what each
call sends and the limits the client enforces.

## Types

| Type   | Encoding                                                |
|--------|---------------------------------------------------------|
| VarInt | Minecraft-style VarInt, 7 bits per byte, low bits first |
| String | VarInt byte length, then UTF-8 bytes                    |
| Value  | One tag byte, then the value (see below)                |

Value tags:

| Tag | Meaning        | Payload                       |
|-----|----------------|-------------------------------|
| 0   | Reset this key | none                          |
| 1   | Boolean        | 1 byte, 0 or 1                |
| 2   | Integer        | ZigZag-encoded VarInt         |
| 3   | Double         | 8 bytes, IEEE 754, big endian |
| 4   | String         | String, at most 4,096 bytes   |

## Handshake

1. The client sends `Hello` (serverbound `0x00`): protocol version (VarInt), client version (String, max 64 bytes),
   Minecraft version (String, max 64 bytes).
2. The server answers with `Welcome` (clientbound `0x00`): protocol version (VarInt), server API version (String, max 64
   bytes).
3. A version 2 client then sends `Capabilities` (serverbound `0x01`): a VarInt count, then that many keys (String, max
   256 bytes each). Keys are option names the client can apply, plus `notification` if it can show notifications.

The server posts `OneClientPlayerJoinEvent` after step 3. For a version 1 client it posts the event after step 2, with
no capabilities.

## Clientbound packets

| ID     | Packet              | Fields                                                                                                                                                    | API call                  |
|--------|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|
| `0x01` | Set options         | VarInt count (max 1,024), then per entry a key (String, max 256 bytes) and a Value                                                                        | `setOption`, `setOptions` |
| `0x02` | Reset options       | none                                                                                                                                                      | `resetOptions`            |
| `0x03` | Notification        | title JSON (String, max 16,384 bytes), description JSON (String, max 16,384 bytes), duration in ms (VarInt), icon (String, max 256 bytes, empty for none) | `sendNotification`        |
| `0x04` | Reset notifications | none                                                                                                                                                      | `resetNotifications`      |

The server only sends keys the client listed in `Capabilities`, and only sends notifications if it listed
`notification`. It drops anything else and logs it at debug level.

## Limits

The client rejects a whole packet if any field is over its limit, so the server checks the same limits before sending
and throws `IllegalArgumentException` instead.

A whole packet, including its ID, can't be over 32,766 bytes. That's the Bukkit plugin message limit, and the server
applies it on every platform. Fields can be within their own limits and still break it: two 16,384-byte notification
texts already come to 32,777 bytes.

## How the current client handles these

- Options last until the player disconnects or the server resets them.
- Notification text is shown as plain text. The client cuts the title to 256 characters and the description to 2,048.
- The duration is clamped to 1 to 60 seconds. Zero uses the client's default.
- The client keeps at most 16 server notifications and dismisses the oldest when a new one arrives.
- The icon field is read but not displayed.
