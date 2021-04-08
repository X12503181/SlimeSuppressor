package info.bcrc.mc.slimesuppressor;

import java.util.Random;
import java.util.Arrays;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public final class SlimeSuppressor extends JavaPlugin implements Listener {
  private String version = this.getDescription().getVersion();
  private float spawnChance = 0;
  private boolean spawnEnabled;
  private FileConfiguration config;
  
  private void savePluginConfig() {
    config.addDefault("slime-spawning-enabled", false);
    config.addDefault("slime-spawn-chance", "0.0");
    
    config.options().copyDefaults(true);
    saveDefaultConfig();
    saveConfig();
  }
  
  private void loadPluginConfig() {
    spawnChance = (float) config.getDouble("slime-spawn-chance");
    spawnEnabled = config.getBoolean("slime-spawning-enabled");
  }


  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    config = getConfig();
    savePluginConfig();
    loadPluginConfig();
    getLogger().info("SlimeSuppressor v" + version + " enabled successfully!");
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("slime")) {
      if (args.length == 0) {
        sender.sendMessage(ChatColor.AQUA + "Slime spawning (" + Float.toString(spawnChance) + " chance) is " + (spawnEnabled ? "enabled" : "disabled"));
      } else {
        if (args[0].equalsIgnoreCase("reload")) {
          reloadConfig();
          savePluginConfig();
          config = getConfig();
          loadPluginConfig();
        } else {
          if (args[0].equalsIgnoreCase("enable")) {
            spawnEnabled = true;
            sender.sendMessage(ChatColor.GREEN + "Slime spawning enabled");
          } else if (args[0].equalsIgnoreCase("disable")) {
            spawnEnabled = false;
            sender.sendMessage(ChatColor.GREEN + "Slime spawning disabled");
          } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length > 1) {
              try {
                spawnChance = Float.parseFloat(args[1]);
              } catch (NumberFormatException e) {
                spawnChance = 0;
                sender.sendMessage(ChatColor.RED + "Error in conversion " + e.getMessage());
                sender.sendMessage(ChatColor.YELLOW + "Defaulted to 0.");
                throw e;
              };
              config.set("slime-spawn-chance", spawnChance);
              sender.sendMessage(ChatColor.GREEN + "Slime spawning chance set to " + Float.toString(spawnChance));
            } else {
              sender.sendMessage("Usage: /slime set <float>");
            };
          } else {
            return false;
          };
          config.set("slime-spawning-enabled", spawnEnabled);
          saveConfig();
        };
      };
      return true;
    };
    return false;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (command.getName().equalsIgnoreCase("slime") && args.length <= 1) {
      return Arrays.asList("disable", "enable", "set", "reload");
    };
    return null;
  }

  @EventHandler
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    if (event.getEntityType().getKey().getKey().equalsIgnoreCase("slime") && event.getEntity().getLocation().getY() <= 40 && event.getSpawnReason() == SpawnReason.NATURAL) {
      if (spawnEnabled && (new Random()).nextFloat() < spawnChance) {
        return;
      };
      event.setCancelled(true);
    };
  }
}
