package me.tolgaozgun.chestspawners.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.tolgaozgun.chestspawners.ChestMain;
import me.tolgaozgun.chestspawners.util.Updater;
import me.tolgaozgun.chestspawners.util.Updater.ReleaseType;

public class MainCommand implements CommandExecutor {
	private ChestMain plugin = ChestMain.getPlugin(ChestMain.class);
	private String itemstr;
	private Set<String> list;
	private int listsize;
	String givenspawner;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if (sender instanceof Player) {
			try {
				listsize = plugin.getConfig().getConfigurationSection("chests").getKeys(false).size();
				list = plugin.getConfig().getConfigurationSection("chests").getKeys(false);
			} catch (java.lang.NullPointerException e) {
				list = null;
			}
			Player p = (Player) sender;
			if (args.length == 0 || args.length == 1 && args[0].toLowerCase().equals("help")) {
				if (p.hasPermission("chestspawners.command")) {
					p.sendMessage(plugin.pltag + ChatColor.GOLD + "------ " + ChatColor.translateAlternateColorCodes(
							'&', plugin.getLocaleConfig().getString("AVAILABLE_CMDS")) + " ------");
					if (p.hasPermission(plugin.getConfig().getString("permissions.list-permission"))) {
						p.sendMessage(plugin.pltag + ChatColor.GOLD + "/csp list" + ChatColor.GOLD + " - "
								+ ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
										plugin.getLocaleConfig().getString("LIST_DESC")));
					}
					if (p.isOp()) {
						p.sendMessage(plugin.pltag + ChatColor.GOLD + "/csp reload" + ChatColor.GOLD + " - "
								+ ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
										plugin.getLocaleConfig().getString("RELOAD_DESC")));
					}
					p.sendMessage(plugin.pltag + ChatColor.GOLD + "-----------------------------");

					p.sendMessage(plugin.pltag + ChatColor.RESET + "A plugin by Progr4mm3r");
					p.sendMessage(plugin.pltag + ChatColor.RESET + "https://www.tolgaozgun.me");
					return true;
				} else {
					p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
							plugin.getLocaleConfig().getString("NO_PERM")));
					return true;
				}
			} else if (args.length == 1) {
				String msg = args[0].toLowerCase();
				if (msg.equals("list")) {
					if (p.hasPermission(plugin.getConfig().getString("permissions.list-permission")) || p.isOp()) {
						if (listsize > 0) {
							String nvm = "";
							for (String st : list) {
								if (nvm != null) {
									nvm += st + ", ";
								} else {
									nvm = st + ", ";
								}
							}
							String nv = nvm.substring(0, nvm.length() - 2);
							String chestlist = plugin.getLocaleConfig().getString("CHEST_LIST");
							ChatColor color = ChatColor.getByChar(chestlist.substring(1, 2));
							if (color instanceof ChatColor) {
								p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor
										.translateAlternateColorCodes('&', chestlist.replace("%s", nv + color)));
							} else {
								p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
										'&', chestlist.replace("%s", nv + ChatColor.RESET)));
							}
							return true;
						} else {
							p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
									plugin.getLocaleConfig().getString("EMPTY_CONFIG")));
							return true;
						}
					} else {
						p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
								plugin.getLocaleConfig().getString("NO_PERM")));
						return true;
					}
				} else if (msg.equals("reload")) {
					String setting = plugin.getConfig().getString("permissions.reload-permission");
					if (p.hasPermission(setting) || p.isOp()) {
						plugin.getServer().getScheduler().cancelAllTasks();
						plugin.saveChests();
						plugin.deleteHolograms();
						plugin.reloadConfigs();
						plugin.getLocale();
						plugin.getChests();
						plugin.loadPluginTag();
						plugin.runSaveScheduler();
						p.sendMessage(
								plugin.pltag + ChatColor.RESET + plugin.getLocaleConfig().getString("RELOAD_SUCCESS"));

					}
				} else if (listsize > 0 && list.contains(msg)) {
					if (p.hasPermission(plugin.getConfig().getString("chests." + msg + ".permission")) || p.isOp()) {
						String str1 = plugin.getConfig().getString("chests." + msg + ".display-name");
						List<String> str2 = plugin.getConfig().getStringList("chests." + msg + ".lore");
						itemstr = ChatColor.translateAlternateColorCodes('&', str1);
						ItemStack item = new ItemStack(Material.CHEST);
						ItemMeta meta = item.getItemMeta();
						meta.setLore(str2);
						meta.setDisplayName(itemstr);
						item.setItemMeta(meta);
						p.getInventory().addItem(item);
						givenspawner = ChatColor.translateAlternateColorCodes('&',
								plugin.getLocaleConfig().getString("GIVEN_SPAWNER"));
						ChatColor color = ChatColor.getByChar(givenspawner.substring(1, 2));
						if (color instanceof ChatColor) {
							p.sendMessage(plugin.pltag + ChatColor.RESET + givenspawner.replace("%s", itemstr + color));
						} else {
							p.sendMessage(plugin.pltag + ChatColor.RESET
									+ givenspawner.replace("%s", itemstr + ChatColor.RESET));
						}
					} else {
						p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
								plugin.getLocaleConfig().getString("NO_PERM")));
						return true;
					}
				} else if (msg.equals("update")) {
					p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
							plugin.getLocaleConfig().getString("CONSOLE_ONLY")));
					return true;
				}

				else {
					p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
							plugin.getLocaleConfig().getString("INVALID_FORMAT")));
					return true;
				}
			} else {
				if (p.isOp()) {
					p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
							plugin.getLocaleConfig().getString("INVALID_FORMAT")));
					return true;
				} else {
					p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
							plugin.getLocaleConfig().getString("NO_PERM")));
					return true;
				}
			}
		} else {
			if (args.length == 0) {
				sender.sendMessage(plugin.pltag + ChatColor.GOLD + "------ " + ChatColor.translateAlternateColorCodes(
						'&', plugin.getLocaleConfig().getString("AVAILABLE_CMDS")) + " ------");
				sender.sendMessage(plugin.pltag + ChatColor.GOLD + "/csp list" + ChatColor.GOLD + " - "
						+ ChatColor.RESET
						+ ChatColor.translateAlternateColorCodes('&', plugin.getLocaleConfig().getString("LIST_DESC")));

				sender.sendMessage(plugin.pltag + ChatColor.GOLD + "/csp reload" + ChatColor.GOLD + " - "
						+ ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
								plugin.getLocaleConfig().getString("RELOAD_DESC")));

				sender.sendMessage(plugin.pltag + ChatColor.GOLD + "-----------------------------");

				sender.sendMessage(plugin.pltag + ChatColor.RESET + "A plugin by Progr4mm3r");
				sender.sendMessage(plugin.pltag + ChatColor.RESET + "https://www.tolgaozgun.me");
				return true;
			} else if (args.length == 1) {
				String message = args[0].toLowerCase();
				if (message.equals("update")) {

					Updater updater = new Updater(plugin, 297121, plugin.getTheFile(), Updater.UpdateType.DEFAULT,
							false);
					boolean update = updater.getResult() == Updater.UpdateResult.SUCCESS;
					if (update) {
						String name = updater.getLatestName();
						ReleaseType type = updater.getLatestType();
						plugin.getServer().getConsoleSender()
								.sendMessage(plugin.pltag + "Successfully updated to version " + type + " " + name);
					} else {

						plugin.getServer().getConsoleSender().sendMessage(plugin.pltag + "Update unsuccessful.");
					}
				} else {

					sender.sendMessage(plugin.pltag + ChatColor.RED + "Cannot use console for ChestSpawners.");
				}

			} else {

				sender.sendMessage(plugin.pltag + ChatColor.RED + "Cannot use console for ChestSpawners.");
			}
		}
		return true;
	}

}
