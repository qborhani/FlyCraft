package net.web1337.borhani.flyCraft;

import net.web1337.borhani.flyCraft.commands.FlyCommand;
import net.web1337.borhani.flyCraft.commands.FlySpeedCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyCraft extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private final HashMap<UUID, Integer> flyingPlayers = new HashMap<>();
    private final HashMap<UUID, Integer> flyCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        getServer().getPluginManager().registerEvents(this, this);

        // Register the FlyCommand with the plugin instance
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("flyspeed").setExecutor(new FlySpeedCommand(this));

        // Start the cooldown and duration checker task
        startCooldownChecker();

        // Register the reload command
        getCommand("flycraftreload").setExecutor((sender, command, label, args) -> {
            if (sender.hasPermission("flycraft.reload")) {
                reloadConfig();
                config = getConfig(); // Reassign the config object after reloading
                sender.sendMessage(ChatColor.GREEN + "FlyCraft plugin configuration reloaded!");
                getLogger().info("FlyCraft plugin configuration reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to reload the configuration.");
            }
            return true;
        });

        getLogger().info(getConfigMessage("messages.plugin-enabled"));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isFlying()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
        getLogger().info(getConfigMessage("messages.plugin-disabled"));
    }

    // Make getConfigMessage public so FlyCommand can access it
    public String getConfigMessage(String path) {
        String message = config.getString(path);
        return ChatColor.translateAlternateColorCodes('&', message != null ? message : "Message not found: " + path);
    }

    // Getter for flyCooldowns map
    public HashMap<UUID, Integer> getFlyCooldowns() {
        return flyCooldowns;
    }

    // Toggle flight method
    public void toggleFlight(Player player) {
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            flyingPlayers.remove(player.getUniqueId());
            player.sendMessage(getConfigMessage("messages.flight-disabled"));

            int cooldownTime = config.getInt("flight-cooldown", 60);
            flyCooldowns.put(player.getUniqueId(), cooldownTime);
        } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            int flyTime = config.getInt("flight-duration", 300);
            flyingPlayers.put(player.getUniqueId(), flyTime);
            player.sendMessage(getConfigMessage("messages.flight-enabled").replace("%time%", String.valueOf(flyTime)));
        }
    }

    // Set flying speed
    public void setFlyingSpeed(Player player, int speed) {
        float flySpeed = (float) speed / 10.0f; // Convert to float (range from 0.1 to 1.0)
        player.setFlySpeed(flySpeed);
        player.sendMessage(getConfigMessage("messages.speed-set").replace("%speed%", String.valueOf(speed)));
    }

    private void startCooldownChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Update flying time
                for (UUID uuid : new HashMap<>(flyingPlayers).keySet()) {
                    int timeLeft = flyingPlayers.get(uuid);
                    if (timeLeft <= 0) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            player.sendMessage(getConfigMessage("messages.flight-expired"));
                        }
                        flyingPlayers.remove(uuid);
                    } else {
                        flyingPlayers.put(uuid, timeLeft - 1);
                    }
                }

                // Update cooldowns
                for (UUID uuid : new HashMap<>(flyCooldowns).keySet()) {
                    int timeLeft = flyCooldowns.get(uuid);
                    if (timeLeft <= 0) {
                        flyCooldowns.remove(uuid);
                    } else {
                        flyCooldowns.put(uuid, timeLeft - 1);
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (config.getBoolean("welcome-message-enabled", true)) {
            player.sendMessage(getConfigMessage("messages.welcome"));
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("flycraft.use")) {
            event.setCancelled(true);
            player.sendMessage(getConfigMessage("messages.no-permission"));
        }
    }

    public <K, V> Map<K, V> getFlyingPlayers() {
        return null;
    }
}
