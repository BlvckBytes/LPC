package me.wikmor.lpc;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class LPC extends JavaPlugin implements Listener {

  private LuckPerms luckPerms;

  @Override
  public void onEnable() {
    // Load an instance of 'LuckPerms' using the services manager.
    this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);

    saveDefaultConfig();
    getServer().getPluginManager().registerEvents(this, this);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 1 && "reload".equals(args[0])) {
      reloadConfig();

      sender.sendMessage("§aLPC has been reloaded.");
      return true;
    }

    return false;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1)
      return List.of("reload");

    return List.of();
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent event) {
    var message = event.getMessage();
    var player = event.getPlayer();

    // Get a LuckPerms cached metadata for the player.
    var metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
    var group = metaData.getPrimaryGroup();

    String format = getConfig().getString(getConfig().getString("group-formats." + group) != null ? "group-formats." + group : "chat-format")
      .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
      .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
      .replace("{prefixes}", metaData.getPrefixes().keySet().stream().map(key -> metaData.getPrefixes().get(key)).collect(Collectors.joining()))
      .replace("{suffixes}", metaData.getSuffixes().keySet().stream().map(key -> metaData.getSuffixes().get(key)).collect(Collectors.joining()))
      .replace("{world}", player.getWorld().getName())
      .replace("{name}", player.getName())
      .replace("{displayname}", player.getDisplayName())
      .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
      .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");

    if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
      format = PlaceholderAPI.setPlaceholders(player, format);

    format = enableColors(format, true, true);
    message = enableColors(message, player.hasPermission("lpc.colorcodes"), player.hasPermission("lpc.rgbcodes"));

    event.setMessage(message);
    event.setFormat(format.replace("{message}", message));
  }

  private static boolean isColorChar(char c) {
    return (c >= 'a' && c <= 'f') || (c >= '0' && c <= '9') || (c >= 'k' && c <= 'o') || c == 'r';
  }

  private static boolean isHexChar(char c) {
    return (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || (c >= '0' && c <= '9');
  }

  private static String enableColors(String input, boolean allowVanilla, boolean allowHex) {
    var inputLength = input.length();
    var result = new StringBuilder(inputLength);

    for (var charIndex = 0; charIndex < inputLength; ++charIndex) {
      var currentChar = input.charAt(charIndex);
      var remainingChars = inputLength - 1 - charIndex;

      if (currentChar != '&' || remainingChars == 0) {
        result.append(currentChar);
        continue;
      }

      var nextChar = input.charAt(++charIndex);

      // Possible hex-sequence of format &#RRGGBB
      if (allowHex && nextChar == '#' && remainingChars >= 6 + 1) {
        var r1 = input.charAt(charIndex + 1);
        var r2 = input.charAt(charIndex + 2);
        var g1 = input.charAt(charIndex + 3);
        var g2 = input.charAt(charIndex + 4);
        var b1 = input.charAt(charIndex + 5);
        var b2 = input.charAt(charIndex + 6);

        if (
          isHexChar(r1) && isHexChar(r2)
            && isHexChar(g1) && isHexChar(g2)
            && isHexChar(b1) && isHexChar(b2)
        ) {
          result
            .append('§').append('x')
            .append('§').append(r1)
            .append('§').append(r2)
            .append('§').append(g1)
            .append('§').append(g2)
            .append('§').append(b1)
            .append('§').append(b2);

          charIndex += 6;
          continue;
        }
      }

      // Vanilla color-sequence
      if (allowVanilla && isColorChar(nextChar)) {
        result.append('§').append(nextChar);
        continue;
      }

      // Wasn't a color-sequence, store as-is
      result.append(currentChar).append(nextChar);
    }

    return result.toString();
  }
}