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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class LPC extends JavaPlugin implements Listener {

  private LuckPerms luckPerms;

  @Override
  public void onEnable() {
    this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);

    saveDefaultConfig();
    getServer().getPluginManager().registerEvents(this, this);
  }

  // ================================================================================
  // LPC-Command
  // ================================================================================

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

  // ================================================================================
  // Formatter
  // ================================================================================

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent event) {
    var player = event.getPlayer();

    var metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
    var group = metaData.getPrimaryGroup();

    String format;

    if ((format = getConfig().getString("group-formats." + group)) == null)
      format = getConfig().getString("chat-format");

    if (format == null)
      return;

    var message = enableColors(event.getMessage(), player.hasPermission("lpc.colorcodes"), player.hasPermission("lpc.rgbcodes"));

    event.setMessage(message);

    format = replaceVariables(format, variableName -> {
      String value;

      switch (variableName) {
        case "message":
          return message;

        case "prefix":
          value = metaData.getPrefix();
          break;

        case "suffix":
          value = metaData.getSuffix();
          break;

        case "prefixes":
          value = String.join("", metaData.getPrefixes().values());
          break;

        case "suffixes":
          value = String.join("", metaData.getSuffixes().values());
          break;

        case "world":
          value = player.getWorld().getName();
          break;

        case "name":
          value = player.getName();
          break;

        case "displayname":
          value = player.getDisplayName();
          break;

        case "username-color":
          value = metaData.getMetaValue("username-color");
          break;

        case "message-color":
          value = metaData.getMetaValue("message-color");
          break;

        default:
          return null;
      }

      if (value == null)
        return "";

      return value;
    });

    if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
      format = PlaceholderAPI.setPlaceholders(player, format);

    format = enableColors(format, true, true);

    // NOTE: In contrast to the prior implementation, we do not escape % - that's up to the user, as to provide more flexibility.
    event.setFormat(format);
  }

  // ================================================================================
  // Algorithms (see test-cases)
  // ================================================================================

  public static String replaceVariables(String input, Function<String, @Nullable String> valueLookup) {
    var result = new StringBuilder(input.length());

    int charIndex;
    int nextPriorSubstringBegin = 0;

    for (charIndex = 0; charIndex < input.length(); ++charIndex) {
      var currentChar = input.charAt(charIndex);

      if (currentChar != '{')
        continue;

      var closingIndex = input.indexOf('}', charIndex + 1);

      if (closingIndex < 0)
        continue;

      var variableName = input.substring(charIndex + 1, closingIndex);
      var variableValue = valueLookup.apply(variableName);

      if (variableValue == null)
        continue;

      result.append(input, nextPriorSubstringBegin, charIndex);
      nextPriorSubstringBegin = closingIndex + 1;

      result.append(variableValue);

      charIndex = closingIndex;
    }

    if (nextPriorSubstringBegin < charIndex)
      result.append(input, nextPriorSubstringBegin, input.length());

    return result.toString();
  }

  private static boolean isColorChar(char c) {
    return (c >= 'a' && c <= 'f') || (c >= '0' && c <= '9') || (c >= 'k' && c <= 'o') || c == 'r';
  }

  private static boolean isHexChar(char c) {
    return (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || (c >= '0' && c <= '9');
  }

  public static String enableColors(String input, boolean allowVanilla, boolean allowHex) {
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

        if (isHexChar(r1) && isHexChar(r2) && isHexChar(g1) && isHexChar(g2) && isHexChar(b1) && isHexChar(b2)) {
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