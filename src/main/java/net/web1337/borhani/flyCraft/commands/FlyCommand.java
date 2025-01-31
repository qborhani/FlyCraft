package net.web1337.borhani.flyCraft.commands;

import net.web1337.borhani.flyCraft.FlyCraft;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    private final FlyCraft plugin;

    public FlyCommand(FlyCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigMessage("messages.player-only"));
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has permission
        if (!player.hasPermission("flycraft.use")) {
            player.sendMessage(plugin.getConfigMessage("messages.no-permission"));
            return true;
        }

        // Check if the player is on cooldown
        if (plugin.getFlyCooldowns().containsKey(player.getUniqueId())) {
            int timeLeft = plugin.getFlyCooldowns().get(player.getUniqueId());
            player.sendMessage(plugin.getConfigMessage("messages.cooldown")
                    .replace("%time%", String.valueOf(timeLeft)));
            return true;
        }

        // If there are arguments, try to toggle flight for another player
        if (args.length > 0) {
            if (!player.hasPermission("flycraft.others")) {
                player.sendMessage(plugin.getConfigMessage("messages.no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }
            plugin.toggleFlight(target);
        } else {
            plugin.toggleFlight(player);
        }

        return true;
    }
}
