package net.web1337.borhani.flyCraft.commands;

import net.web1337.borhani.flyCraft.FlyCraft;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyTimeCommand implements CommandExecutor {
    private FlyCraft plugin;

    public FlyTimeCommand(FlyCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigMessage("messages.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("flycraft.flytime")) {
            player.sendMessage(plugin.getConfigMessage("messages.no-permission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /flytime <time>");
            return false;
        }

        try {
            int time = Integer.parseInt(args[0]);
            if (time < 10 || time > 600) {
                player.sendMessage(plugin.getConfigMessage("messages.flight-time-invalid"));
                return false;
            }

            if (player.getAllowFlight() && player.isFlying()) {
                plugin.getFlyingPlayers().put(player.getUniqueId(), time);
                player.sendMessage(plugin.getConfigMessage("messages.flight-enabled").replace("%time%", String.valueOf(time)));
            } else {
                player.sendMessage(plugin.getConfigMessage("messages.not-flying"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid time value.");
        }

        return true;
    }
}
