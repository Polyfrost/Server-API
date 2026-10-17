package org.polyfrost.oneclient.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.polyfrost.oneclient.api.OneClientPlayer;

import java.util.Collection;

final class OneClientCommand implements CommandExecutor {
    private final OneClientBukkitPlugin plugin;

    OneClientCommand(OneClientBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "list" : args[0].toLowerCase();
        switch (sub) {
            case "list":
                Collection<OneClientPlayer> players = plugin.server().getPlayers();
                sender.sendMessage(ChatColor.AQUA + "OneClient players online: " + ChatColor.WHITE + players.size());
                for (OneClientPlayer player : players) {
                    sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + player.getName()
                        + ChatColor.GRAY + " (PolyPlus " + player.getClientVersion() + ", " + player.getMinecraftVersion() + ")");
                }
                return true;
            case "reload":
                plugin.reload();
                sender.sendMessage(ChatColor.AQUA + "OneClient server API config reloaded.");
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [list|reload]");
                return true;
        }
    }
}
