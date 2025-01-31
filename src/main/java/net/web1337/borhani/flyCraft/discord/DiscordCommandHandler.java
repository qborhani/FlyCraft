package net.web1337.borhani.flyCraft.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.web1337.borhani.flyCraft.FlyCraft;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class DiscordCommandHandler extends ListenerAdapter {
    private final FlyCraft plugin;

    public DiscordCommandHandler(FlyCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bots and non-command messages
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        // Check if message is from the configured channel
        if (!event.getChannel().getId().equals(plugin.getDiscordChannelId())) return;

        String prefix = plugin.getConfig().getString("discord.commands.prefix", "!");
        String message = event.getMessage().getContentRaw();
        if (!message.startsWith(prefix)) return;

        // Check if user has permission (via roles)
        List<String> allowedRoles = plugin.getConfig().getStringList("discord.commands.allowed-roles");
        boolean hasPermission = false;
        Member member = event.getMember();
        
        if (member != null) {
            for (Role role : member.getRoles()) {
                if (allowedRoles.contains(role.getId())) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            event.getChannel().sendMessage("❌ You don't have permission to use this command!").queue();
            return;
        }

        // Parse command
        String[] args = message.substring(prefix.length()).split(" ");
        String command = args[0].toLowerCase();

        // Execute command
        switch (command) {
            case "stopfly":
                handleStopFly(event, args);
                break;
            case "startfly":
                handleStartFly(event, args);
                break;
            case "flyspeed":
                handleFlySpeed(event, args);
                break;
        }
    }

    private void handleStopFly(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            event.getChannel().sendMessage("❌ Usage: !stopfly <player>").queue();
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                event.getChannel().sendMessage("❌ Player not found: " + args[1]).queue();
                return;
            }

            if (!target.isFlying()) {
                event.getChannel().sendMessage("❌ Player is not flying: " + args[1]).queue();
                return;
            }

            target.setFlying(false);
            target.setAllowFlight(false);
            // Send message to player
            target.sendMessage(plugin.getConfigMessage("messages.discord-flight-disabled"));
            event.getChannel().sendMessage("✅ Disabled flight for " + target.getName()).queue();
        });
    }

    private void handleStartFly(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            event.getChannel().sendMessage("❌ Usage: !startfly <player>").queue();
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                event.getChannel().sendMessage("❌ Player not found: " + args[1]).queue();
                return;
            }

            // Check if player is already flying
            if (target.isFlying()) {
                event.getChannel().sendMessage("❌ Player is already flying: " + args[1]).queue();
                return;
            }

            // Get flight duration from config
            int flyTime = plugin.getConfig().getInt("flight-duration", 300);
            
            // Call toggleFlight first
            plugin.toggleFlight(target);
            
            // Send additional message about Discord admin
            target.sendMessage(plugin.getConfigMessage("messages.discord-flight-enabled")
                .replace("%time%", String.valueOf(flyTime)));

            event.getChannel().sendMessage("✅ Enabled flight for " + target.getName()).queue();
        });
    }

    private void handleFlySpeed(MessageReceivedEvent event, String[] args) {
        if (args.length != 3) {
            event.getChannel().sendMessage("❌ Usage: !flyspeed <player> <1-10>").queue();
            return;
        }

        try {
            int speed = Integer.parseInt(args[2]);
            if (speed < 1 || speed > 10) {
                event.getChannel().sendMessage("❌ Speed must be between 1 and 10").queue();
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    event.getChannel().sendMessage("❌ Player not found: " + args[1]).queue();
                    return;
                }

                plugin.setFlyingSpeed(target, speed);
                // Send message to player
                target.sendMessage(plugin.getConfigMessage("messages.discord-speed-set")
                    .replace("%speed%", String.valueOf(speed)));
                event.getChannel().sendMessage("✅ Set fly speed to " + speed + " for " + target.getName()).queue();
            });
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ Invalid speed value: " + args[2]).queue();
        }
    }
} 