package org.polyfrost.oneclient.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.polyfrost.oneclient.api.OneClientPlayer;

import java.util.Collection;

final class OneClientCommand extends Command {
    private final OneClientBungeePlugin plugin;

    OneClientCommand(OneClientBungeePlugin plugin) {
        super("oneclient", "oneclient.admin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String sub = args.length == 0 ? "list" : args[0].toLowerCase();
        switch (sub) {
            case "list":
                Collection<OneClientPlayer> players = plugin.server().getPlayers();
                send(sender, ChatColor.AQUA + "OneClient players online: " + ChatColor.WHITE + players.size());
                for (OneClientPlayer player : players) {
                    send(sender, ChatColor.GRAY + " - " + ChatColor.WHITE + player.getName()
                        + ChatColor.GRAY + " (PolyPlus " + player.getClientVersion() + ", " + player.getMinecraftVersion() + ")");
                }
                break;
            case "reload":
                plugin.reload();
                send(sender, ChatColor.AQUA + "OneClient server API config reloaded.");
                break;
            default:
                send(sender, ChatColor.RED + "Usage: /oneclient [list|reload]");
                break;
        }
    }

    private static void send(CommandSender sender, String message) {
        sender.sendMessage(TextComponent.fromLegacy(message));
    }
}
