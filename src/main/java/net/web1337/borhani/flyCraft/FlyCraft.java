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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.web1337.borhani.flyCraft.discord.DiscordCommandHandler;

public class FlyCraft extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private final HashMap<UUID, Integer> flyingPlayers = new HashMap<>();
    private final HashMap<UUID, Integer> flyCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> flightStartTimes = new HashMap<>();
    private boolean discordEnabled;
    private String discordChannelId;
    private String discordToken;
    private JDA jda;

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

        // Load Discord config first
        loadDiscordConfig();
        // Then setup Discord integration
        setupDiscord();

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
        if (jda != null) {
            jda.shutdown();
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
            
            // Calculate total flight time before removing from maps
            long totalFlightTime = 0;
            if (flightStartTimes.containsKey(player.getUniqueId())) {
                totalFlightTime = (System.currentTimeMillis() - flightStartTimes.get(player.getUniqueId())) / 1000;
                flightStartTimes.remove(player.getUniqueId());
            }
            
            // Send Discord message with actual flight time
            if (discordEnabled) {
                sendFlightMessage(player, false, player.getFlySpeed() * 10, totalFlightTime);
            }
            
            flyingPlayers.remove(player.getUniqueId());
            player.sendMessage(getConfigMessage("messages.flight-disabled"));

            int cooldownTime = config.getInt("flight-cooldown", 60);
            flyCooldowns.put(player.getUniqueId(), cooldownTime);
        } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            int flyTime = config.getInt("flight-duration", 300);
            flyingPlayers.put(player.getUniqueId(), flyTime);
            // Record flight start time
            flightStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
            
            if (discordEnabled) {
                sendFlightMessage(player, true, player.getFlySpeed() * 10, 0);
            }
            
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

    private void setupDiscord() {
        if (discordEnabled) {
            getLogger().info("Initializing Discord integration...");
            try {
                jda = JDABuilder.createDefault(discordToken)
                    .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS
                    )
                    .setAutoReconnect(true)
                    .build();
                
                try {
                    // Wait for JDA to be ready (max 10 seconds)
                    jda.awaitReady();
                    
                    // Verify channel exists
                    TextChannel channel = jda.getTextChannelById(discordChannelId);
                    if (channel != null) {
                        getLogger().info("Successfully connected to Discord channel: " + channel.getName());
                        
                        // Send startup message instead of reload message
                        channel.sendMessage("✅ FlyCraft Discord integration started!").queue();

                        // Add command handler
                        if (getConfig().getBoolean("discord.commands.enabled", true)) {
                            jda.addEventListener(new DiscordCommandHandler(this));
                        }

                        // Set bot status
                        setupBotStatus();
                    } else {
                        getLogger().warning("Could not find Discord channel with ID: " + discordChannelId);
                        discordEnabled = false;
                    }
                } catch (InterruptedException e) {
                    getLogger().warning("Timeout while waiting for Discord connection!");
                    discordEnabled = false;
                }
            } catch (Exception e) {
                getLogger().warning("Failed to initialize Discord: " + e.getMessage());
                e.printStackTrace();
                discordEnabled = false;
            }
        }
    }

    private void loadDiscordConfig() {
        discordEnabled = getConfig().getBoolean("discord.enabled", false);
        discordChannelId = getConfig().getString("discord.channel-id", "");
        discordToken = getConfig().getString("discord.bot-token", "");
        
        getLogger().info("Discord Integration Settings:");
        getLogger().info("- Enabled: " + discordEnabled);
        getLogger().info("- Channel ID: " + discordChannelId);
        
        if (discordEnabled) {
            if (discordToken.isEmpty() || discordToken.equals("YOUR_BOT_TOKEN_HERE")) {
                getLogger().warning("Discord bot token not set! Discord integration will be disabled.");
                discordEnabled = false;
            } else if (discordChannelId.isEmpty()) {
                getLogger().warning("Discord channel ID is not set! Discord integration will be disabled.");
                discordEnabled = false;
            } else if (!discordChannelId.matches("\\d+")) {
                getLogger().warning("Invalid Discord channel ID! Must be a numeric ID.");
                discordEnabled = false;
            }
        }
    }

    public void sendFlightMessage(Player player, boolean isFlying, double speed, long totalFlightTime) {
        if (!discordEnabled || jda == null) return;
        
        try {
            TextChannel channel = jda.getTextChannelById(discordChannelId);
            if (channel == null) return;

            String messageKey = isFlying ? "flight-start" : "flight-end";
            String messageFormat = getConfig().getString("discord.message-format." + messageKey, "");
            
            // Format the flight time nicely
            String formattedTime = formatFlightTime(totalFlightTime);
            
            messageFormat = messageFormat
                .replace("%player%", player.getName())
                .replace("%speed%", String.format("%.1f", speed))
                .replace("%duration%", String.valueOf(config.getInt("flight-duration")))
                .replace("%time%", formattedTime)
                .replace("%server_name%", Bukkit.getServer().getName());
            
            if (getConfig().getBoolean("discord.embed.enabled", true)) {
                sendEmbed(channel, player, isFlying, speed, messageFormat, totalFlightTime);
            } else {
                channel.sendMessage(messageFormat).queue();
            }
        } catch (Exception e) {
            getLogger().warning("Failed to send Discord message: " + e.getMessage());
        }
    }
    
    private void sendEmbed(TextChannel channel, Player player, boolean isFlying, double speed, String messageFormat, long totalFlightTime) {
        try {
            String colorHex = getConfig().getString("discord.embed.color." + 
                (isFlying ? "start" : "end"), 
                isFlying ? "#3498db" : "#e74c3c");
                
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(isFlying ? "✈️ Flight Started" : "🛬 Flight Ended")
                .setColor(Integer.parseInt(colorHex.replace("#", ""), 16))
                .setDescription(messageFormat)
                .addField("Player", player.getName(), true)
                .addField("Speed", String.format("%.1fx", speed), true);

            // Add flight time field for flight end
            if (!isFlying && totalFlightTime > 0) {
                embed.addField("Total Flight Time", formatFlightTime(totalFlightTime), false);
            }
                
            embed.setTimestamp(Instant.now());
            
            if (getConfig().getBoolean("discord.embed.thumbnail.enabled", true)) {
                embed.setThumbnail(getConfig().getString("discord.embed.thumbnail.url", "")
                    .replace("%player%", player.getName()));
            }
                
            String footerText = getConfig().getString("discord.embed.footer.text", "")
                .replace("%server_name%", Bukkit.getServer().getName());
            if (!footerText.isEmpty()) {
                embed.setFooter(footerText);
            }
                
            channel.sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            getLogger().warning("Failed to send Discord embed: " + e.getMessage());
        }
    }

    // Add this helper method to format flight time
    private String formatFlightTime(long seconds) {
        if (seconds < 60) {
            return seconds + (seconds == 1 ? " second" : " seconds");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return String.format("%d %s%s%d %s", 
                minutes, 
                (minutes == 1 ? "minute" : "minutes"),
                remainingSeconds > 0 ? " " : "",
                remainingSeconds,
                remainingSeconds == 1 ? "second" : "seconds"
            ).trim();
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;
            return String.format("%d %s%s%d %s%s%d %s",
                hours,
                (hours == 1 ? "hour" : "hours"),
                minutes > 0 || remainingSeconds > 0 ? " " : "",
                minutes,
                (minutes == 1 ? "minute" : "minutes"),
                remainingSeconds > 0 ? " " : "",
                remainingSeconds,
                remainingSeconds == 1 ? "second" : "seconds"
            ).trim();
        }
    }

    public void sendSpeedChangeMessage(Player player, double speed) {
        if (!discordEnabled || jda == null) return;
        
        try {
            TextChannel channel = jda.getTextChannelById(discordChannelId);
            if (channel == null) return;

            String messageFormat = getConfig().getString("discord.message-format.speed-change", "🔄 **%player%** changed flight speed to **%speed%x**");
            
            messageFormat = messageFormat
                .replace("%player%", player.getName())
                .replace("%speed%", String.format("%.1f", speed))
                .replace("%server_name%", Bukkit.getServer().getName());
            
            if (getConfig().getBoolean("discord.embed.enabled", true)) {
                String colorHex = getConfig().getString("discord.embed.color.speed", "#f1c40f"); // Yellow color for speed changes
                
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔄 Flight Speed Changed")
                    .setColor(Integer.parseInt(colorHex.replace("#", ""), 16))
                    .setDescription(messageFormat)
                    .addField("Player", player.getName(), true)
                    .addField("New Speed", String.format("%.1fx", speed), true)
                    .setTimestamp(Instant.now());
                    
                if (getConfig().getBoolean("discord.embed.thumbnail.enabled", true)) {
                    embed.setThumbnail(getConfig().getString("discord.embed.thumbnail.url", "")
                        .replace("%player%", player.getName()));
                }
                    
                String footerText = getConfig().getString("discord.embed.footer.text", "")
                    .replace("%server_name%", Bukkit.getServer().getName());
                if (!footerText.isEmpty()) {
                    embed.setFooter(footerText);
                }
                    
                channel.sendMessageEmbeds(embed.build()).queue();
            } else {
                channel.sendMessage(messageFormat).queue();
            }
        } catch (Exception e) {
            getLogger().warning("Failed to send Discord speed change message: " + e.getMessage());
        }
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    private void setupBotStatus() {
        if (jda == null) return;

        // Set online status
        String statusStr = getConfig().getString("discord.bot-settings.status", "ONLINE");
        OnlineStatus status;
        try {
            status = OnlineStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            status = OnlineStatus.ONLINE;
        }
        jda.getPresence().setStatus(status);

        // Set activity
        String activityType = getConfig().getString("discord.bot-settings.activity.type", "PLAYING");
        String activityMessage = getConfig().getString("discord.bot-settings.activity.message", "with %online_players% players")
            .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
            .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
            .replace("%server_name%", Bukkit.getServer().getName());

        Activity activity;
        try {
            Activity.ActivityType type = Activity.ActivityType.valueOf(activityType);
            activity = Activity.of(type, activityMessage);
            jda.getPresence().setActivity(activity);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid activity type: " + activityType);
        }
    }
}
