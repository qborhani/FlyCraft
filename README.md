# FlyCraft v1.4 - Official Documentation

## Overview
FlyCraft is a lightweight and powerful flight management plugin for Minecraft servers. It allows players to toggle flight mode, adjust flight speed, and integrate with Discord for remote management.

---

## 1. Core Features
- **Toggle Flight Mode** – Enable or disable flight for players
- **Configurable Flight Duration** – Set limits on flight time
- **Customizable Cooldown System** – Prevents excessive flight toggling
- **Adjustable Fly Speed** – Speed levels from 1 (slowest) to 10 (fastest)
- **Permission-Based Access** – Control who can fly and manage flight settings
- **Welcome Messages** – Display a message to new players
- **Full Discord Integration** – Manage flights via Discord commands

---

## 2. Commands
### 2.1 In-Game Commands
| Command | Description | Permission | Aliases |
|---------|------------|------------|---------|
| `/fly [player]` | Toggle flying mode | `flycraft.use` | `flymode` |
| `/flyspeed <1-10>` | Adjust flying speed | `flycraft.use` | N/A |
| `/flycraftreload` | Reload the plugin configuration | `flycraft.reload` | `fcr`, `flyreload` |

**Note:**
- Using `/fly [player]` requires `flycraft.others` permission.
- Fly speed is adjustable from **1 (slowest) to 10 (fastest)**.

### 2.2 Discord Commands
| Command | Description | Requirements |
|---------|------------|-------------|
| `!startfly <player>` | Enables flight for the specified player | Requires configured Discord role |
| `!stopfly <player>` | Disables flight for the specified player | Requires configured Discord role |
| `!flyspeed <player> <1-10>` | Sets the player’s flight speed | Requires configured Discord role |

---

## 3. Permissions
| Permission | Description | Default |
|------------|------------|---------|
| `flycraft.use` | Allows toggling flight mode | `true` |
| `flycraft.reload` | Allows reloading the configuration | `op` |
| `flycraft.others` | Allows toggling flight for others | `op` |

---

## 4. Configuration (`config.yml`)
### 4.1 Flight Settings
```yaml
flight-duration: 300  # Duration of flight in seconds (default: 300)
flight-cooldown: 60   # Cooldown time between flights (default: 60)
welcome-message-enabled: true  # Enable welcome messages
```
### 4.2 Discord Integration
```yaml
discord:
  enabled: true
  channel-id: "YOUR_CHANNEL_ID"
  bot-token: "YOUR_BOT_TOKEN"
  status: ONLINE  # Options: ONLINE, IDLE, DO_NOT_DISTURB, INVISIBLE
  role-required: true  # Enforce role-based access
  embed-colors:
    flight-start: "#3498db"
    flight-end: "#e74c3c"
    speed-change: "#f1c40f"
```
### 4.3 Bot Activity Settings
```yaml
bot-activity:
  type: PLAYING  # Options: PLAYING, WATCHING, LISTENING, COMPETING
  message: "Flying on %server_name% with %online_players%/%max_players% online!"
```

---

## 5. Discord Features
### 5.1 Notifications
- Flight start/stop alerts
- Speed change updates
- Total flight time tracking
- Player avatars included in embeds

### 5.2 Message Formatting
- Supports **custom emojis**
- Displays **player names** and **server info**
- Tracks **flight duration**

---

## 6. Time Formatting
Automatically formats time periods for clarity:
- `25 seconds`
- `1 minute 30 seconds`
- `2 hours 15 minutes 45 seconds`

---

## 7. Dependencies
### Required
- **Minecraft Server:** Spigot/Paper **1.13+**
- **JDA (Discord API):** `5.0.0-beta.13`

### Optional (For Enhanced Features)
- **PlaceholderAPI** – Dynamic placeholders
- **Vault** – Economy and permissions support
- **DiscordSRV** – Alternative Discord integration

---

## 8. Technical Features
- **Async Discord Messaging** – Improves performance
- **Safe Reload Handling** – Prevents crashes during reloads
- **Automatic Cooldown Management** – Ensures fair gameplay
- **Flight Time Tracking** – Monitors player flight duration
- **Speed Conversion Handling** – Converts between Minecraft speed units
- **Error Logging & Handling** – Logs issues for troubleshooting
- **Event-Based Architecture** – Efficient and modular

---

## 9. Messages
All messages are customizable in `config.yml`:
- Plugin enable/disable messages
- Permission error messages
- Flight status updates
- Speed change notifications
- Welcome messages
- Discord alerts
- Admin notifications

---

## 10. Safety Features
- **Permission Checks** – Ensures players have the correct permissions
- **Input Validation** – Prevents invalid command arguments
- **Error Handling** – Captures and logs issues gracefully
- **Safe Discord Reconnection** – Prevents bot disconnection issues
- **Cooldown Enforcement** – Prevents spam abuse
- **Flight Duration Limits** – Ensures balanced gameplay
- **Speed Range Limits (1-10)** – Prevents unintended behavior

---

## 11. Development Information
- **API Version:** 1.13
- **Java Version:** 21
- **Build System:** Maven-based
- **Open Source:** Yes
- **Extendable Architecture:** Designed for future expansions

---

## Installation Guide
1. **Download FlyCraft v1.4** from the official repository.
2. **Place the .jar file** in your server’s `/plugins/` folder.
3. **Restart your server** to generate configuration files.
4. **Edit `config.yml`** to customize settings.
5. **(Optional) Configure Discord Bot** for remote flight control.
6. **Reload the plugin** using `/flycraftreload` or restart the server.

---

## Support
For help, feature requests, or bug reports:
- **GitHub Issues** – [[Link to Repository]](https://github.com/qborhani/FlyCraft)
- **Discord Support** – [[Invite Link]](https://discord.gg/5KWpEux3KN)

---

## License
License
FlyCraft is an open-source project licensed under the GNU General Public License v3.0 (GPL-3.0). You are free to use, modify, and distribute it, provided that:

- Proper credit is given to the original author (Borhani).
- Any modifications or redistributed versions are also licensed under GPL-3.0.
- The original license is included in all copies or substantial portions of the project.

For full license details, see: [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.html).

---

## Credits
Developed by **Borhani**.

---

