package org.polyfrost.oneclient.example.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.polyfrost.oneclient.api.OneClient;
import org.polyfrost.oneclient.api.OneClientApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code /restrictions <mods|huds> [ids...]} replaces one list and keeps the other, {@code /restrictions clear}
 * lifts everything and {@code /restrictions} shows what is restricted
 */
final class RestrictionsCommand implements TabExecutor {
    private static final List<String> SUBCOMMANDS = Arrays.asList("mods", "huds", "clear");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OneClientApi api = OneClient.getApi();
        String sub = args.length == 0 ? "" : args[0].toLowerCase();
        List<String> ids = args.length > 1 ? Arrays.asList(args).subList(1, args.length) : Collections.emptyList();
        try {
            switch (sub) {
                case "mods":
                    api.setRestrictions(ids, api.getRestrictedHuds());
                    break;
                case "huds":
                    api.setRestrictions(api.getRestrictedMods(), ids);
                    break;
                case "clear":
                    api.setRestrictions(Collections.emptyList(), Collections.emptyList());
                    break;
                case "":
                    break;
                default:
                    sender.sendMessage("Usage: /" + label + " [mods|huds] [ids...] or /" + label + " clear");
                    return true;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Rejected: " + e.getMessage());
            return true;
        }
        sender.sendMessage("Restricted mods: " + api.getRestrictedMods());
        sender.sendMessage("Restricted HUDs: " + api.getRestrictedHuds());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return SUBCOMMANDS.stream().filter(it -> it.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
    }
}
