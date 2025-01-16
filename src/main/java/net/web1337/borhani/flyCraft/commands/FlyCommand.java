package net.web1337.borhani.flyCraft.commands;

import net.web1337.borhani.flyCraft.FlyCraft;
import org.bukkit.ChatColor;
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

        if (!player.hasPermission("flycraft.use")) {
            player.sendMessage(plugin.getConfigMessage("messages.no-permission"));
            return true;
        }

        // Check for cooldown
        if (plugin.getFlyCooldowns().containsKey(player.getUniqueId())) {
            int timeLeft = plugin.getFlyCooldowns().get(player.getUniqueId());
            player.sendMessage(plugin.getConfigMessage("messages.cooldown").replace("%time%", String.valueOf(timeLeft)));
            return true;
        }

        // Call the toggleFlight method to handle toggling
        plugin.toggleFlight(player);
        return true;
    }
}
