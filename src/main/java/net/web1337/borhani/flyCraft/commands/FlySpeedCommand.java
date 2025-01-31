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
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigMessage("messages.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("flycraft.use")) {
            player.sendMessage(plugin.getConfigMessage("messages.no-permission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigMessage("messages.speed-usage"));
            return true;
        }

        try {
            int speed = Integer.parseInt(args[0]);
            if (speed < 1 || speed > 10) {
                player.sendMessage(plugin.getConfigMessage("messages.speed-invalid-range"));
                return true;
            }

            plugin.setFlyingSpeed(player, speed);
            
            // Send Discord message about speed change if player is flying
            if (player.isFlying()) {
                plugin.sendSpeedChangeMessage(player, speed);
            }

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigMessage("messages.speed-invalid-number"));
        }

        return true;
    }
}
