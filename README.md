# FlyCraft Plugin Installation Guide

## Installation Steps:
1. **Download the FlyCraft Plugin**
   - Download the latest version of the **FlyCraft** plugin from a trusted source.
   
2. **Place the Plugin in the Server Folder**
   - Save the downloaded `.jar` file and place it in the `Plugins` folder of your Minecraft server.

3. **Restart or Reload Your Server**
   - Restart the server or run `/reload` to ensure that the plugin loads correctly.

4. **Start Using the Plugin**
   - The plugin is now active! You can start using all the features right away.

---

## Features:
- **Lightweight**: Designed with performance in mind, FlyCraft is a highly optimized plugin.
- **Customizable Messages**: Every message in the plugin can be customized through the `config.yml` file, allowing for full flexibility in communication with players.
- **Permissions System**: All commands are permission-based to provide better control over who can use what features.
- **Toggle Flight Mode**: Enables players to toggle their own flight mode or allow admins to toggle it for them.
- **Flight Speed Control**: Players can adjust their flight speed with `/flyspeed <speed>`, providing a more personalized experience.
- **Flight Duration**: Set a specific time limit for how long players can fly before they are grounded again.
- **Cooldowns**: Flight mode comes with a configurable cooldown period to prevent spamming and excessive flying.
- **Flight Restrictions**: Players without the proper permissions cannot toggle flight mode or use other restricted features.

---

## Supported Versions:
- Fully compatible with Minecraft versions **1.13 - 1.21**.
  
---

## Permissions:
- **`flycraft.use`**: Allows the player to toggle flight mode (`/fly`).
- **`flycraft.reload`**: Allows the player to reload the plugin's configuration file (`/fly reload`).
- **`flycraft.speed`**: Allows the player to change their flight speed (`/flyspeed`).

---

## Commands:
- **`/fly`**: Toggle flight mode for yourself. Flying will be enabled if you're not already flying, and disabled if you're currently flying.
- **`/fly reload`**: Reload the `config.yml` file without restarting the server. This applies any changes made to the config.
- **`/flyspeed <speed>`**: Adjusts the flight speed. Accepts values ranging from `1` (slowest) to `10` (fastest).
- **`/fly <player>`**: Admin command to toggle flight mode for another player (requires `flycraft.use` permission).

---

## Configuration:
The plugin’s behavior, messages, and flight duration/cooldowns can all be configured in the `config.yml` file located in the `plugins/FlyCraft` folder. Common configurations include:
- **Flight Duration**: Set the maximum time players can fly.
- **Cooldown Time**: Set the cooldown period between flight mode activations.
- **Messages**: Customize the messages displayed to players when they toggle flight, set their speed, or when their flight expires.
- **Permissions**: Modify the default permissions for various commands.

---

## TO-DO List:
- **Suggestions Welcome**: We are always looking to improve the plugin. Feel free to suggest new features or improvements.
- **Customizable Flight Modes**: Allow players to choose between different flight modes (e.g., creative-like flight or slow floating).
- **Custom Sound Effects**: Add sounds for flight toggling.
  
---

### Support:
If you encounter any issues or need assistance with configuring the plugin, feel free to reach out to the support team on the plugin’s discussion page or contact us directly.
