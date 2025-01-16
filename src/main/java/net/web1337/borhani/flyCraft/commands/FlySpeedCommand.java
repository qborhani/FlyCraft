package net.web1337.borhani.flyCraft.commands;

import net.web1337.borhani.flyCraft.FlyCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlySpeedCommand implements CommandExecutor {
    private final FlyCraft plugin;

    public FlySpeedCommand(FlyCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Check if the player has permission to change speed
            if (!player.hasPermission("flycraft.speed")) {
                player.sendMessage(plugin.getConfigMessage("messages.no-permission"));
                return false;
            }

            // Check if the player provided a speed value
            if (args.length != 1) {
                player.sendMessage(plugin.getConfigMessage("messages.speed-usage"));
                return false;
            }

            try {
                int speed = Integer.parseInt(args[0]);

                // Check if the speed is within the valid range
                if (speed < 1 || speed > 10) {
                    player.sendMessage(plugin.getConfigMessage("messages.speed-invalid-range"));
                    return false;
                }

                // Set the flying speed
                plugin.setFlyingSpeed(player, speed);

            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigMessage("messages.speed-invalid-number"));
            }
        } else {
            sender.sendMessage(plugin.getConfigMessage("messages.player-only"));
        }
        return true;
    }
}
