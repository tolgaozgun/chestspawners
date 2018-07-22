package me.tolgaozgun.chestspawners.util;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import me.tolgaozgun.chestspawners.ChestMain;

public class RuntimeChests implements Listener {
	private ChestMain plugin = ChestMain.getPlugin(ChestMain.class);
	private int ex3 = 0;
	private int timer, hours = 0, minutes = 0, seconds = 0;
	String[] delays;
	private boolean holograms;
	private Hologram hologram;
	private int remaining;
	private String hrs, mins, secs;
	private boolean warned = false;
	private int count = 0;
	private int keyinteger = 0;
	private Location where;

	public void chestSetup(Chest c, ItemStack i, String type, int d, int exp, UUID uuid) {
		holograms = plugin.getHolograms();
		delays = plugin.getConfig().getString("settings.warnings").split(",");
		int x = c.getLocation().getBlockX();
		int y = c.getLocation().getBlockY();
		int z = c.getLocation().getBlockZ();
		String w = c.getWorld().getName();
		String uuids = uuid.toString();
		String str = x + "/" + y + "/" + z + "/" + w + "/" + uuids;
		keyinteger = plugin.getConfig().getInt("chests." + type + ".delay");
		plugin.hmap.put(str, type);
		if (plugin.expmap.get(str) == null) {
			plugin.expmap.put(str, exp);

		}
		if (holograms) {
			where = c.getLocation();
			where.setY(where.getY() + 1.5);
			where.setX(where.getX() + 0.5);
			where.setZ(where.getZ() + 0.5);
			hologram = HologramsAPI.createHologram(plugin, where);
			String newone = plugin.getConfig().getString("chests." + type + ".display-name");
			newone = ChatColor.translateAlternateColorCodes('&', newone);
			hologram.insertTextLine(0, newone);
			if (plugin.expmap.get(str) == -1) {
				remaining = -1;
			} else {
				remaining = plugin.expmap.get(str) + 1;
			}
			if (remaining > 3600) {
				hours = remaining / 3600;
				minutes = remaining % 3600 / 60;
				seconds = remaining % 60;
			} else if (3600 > remaining && remaining > 60) {
				minutes = remaining / 60;
				seconds = remaining % 60;
			} else if (remaining == 0) {
				hours = 0;
				minutes = 0;
				seconds = 0;
				hrs = "00";
				mins = "00";
				secs = "00";
				String expiredtext = plugin.getLocaleConfig().getString("EXPIRED");
				String expiredlast = ChatColor.translateAlternateColorCodes('&', expiredtext);
				try {
					if (!hologram.getLine(1).toString().equals(expiredlast)) {
						hologram.removeLine(1);
						hologram.insertTextLine(1, expiredlast);
					}
				} catch (IndexOutOfBoundsException e) {
					hologram.insertTextLine(1, expiredlast);
				}
				return;
			} else if (remaining < 0) {
				hours = 0;
				minutes = 0;
				seconds = 0;
				hrs = "00";
				mins = "00";
				secs = "00";
				/*
				 * String inftext = plugin.getLocaleConfig().getString("INFINITE"); String
				 * inflast = ChatColor.translateAlternateColorCodes('&', inftext); try { if
				 * (!hologram.getLine(1).toString().equals(inflast)) { hologram.removeLine(1);
				 * hologram.insertTextLine(1, inflast + "b"); } } catch
				 * (IndexOutOfBoundsException e) { hologram.insertTextLine(1, inflast + "b"); }
				 */
			}

			else {
				minutes = 0;
				seconds = remaining;
			}
			if (hours < 10) {
				hrs = "0" + hours;

			} else {
				hrs = hours + "";
			}
			if (minutes < 10) {
				mins = "0" + minutes;
			} else {
				mins = minutes + "";
			}
			if (seconds < 10) {
				secs = "0" + seconds;
			} else {
				secs = seconds + "";
			}
			hologram.insertTextLine(1, hrs + "h:" + mins + "m:" + secs + "s");
		}
		timer = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			try {
				c.getInventory();
			} catch (java.lang.NullPointerException e) {

				if (holograms) {
					hologram.delete();
					plugin.deleteHologram(where);
				}
				plugin.removeConfig(str);
				plugin.expmap.remove(str);
				plugin.hmap.remove(str);
				plugin.getServer().getScheduler().cancelTask(timer);
				return;
			} catch (java.lang.IllegalStateException e) {
				if (holograms) {
					hologram.delete();
					plugin.deleteHologram(where);
				}
				plugin.removeConfig(str);
				plugin.expmap.remove(str);
				plugin.hmap.remove(str);
				plugin.getServer().getScheduler().cancelTask(timer);
				return;

			}
			if (holograms) {
				if(remaining > 0) {
				remaining = remaining - 1;
				}
				if (remaining > 3600) {
					hours = remaining / 3600;
					minutes = remaining % 3600 / 60;
					seconds = remaining % 60;
				} else if (3600 > remaining && remaining > 60) {
					minutes = remaining / 60;
					seconds = remaining % 60;
				} else if (remaining == 0) {
					hours = 0;
					minutes = 0;
					seconds = 0;
					hrs = "00";
					mins = "00";
					secs = "00";
					String expiredtext = plugin.getLocaleConfig().getString("EXPIRED");
					String expiredlast = ChatColor.translateAlternateColorCodes('&', expiredtext);
					try {
						if (!hologram.getLine(1).toString().equals(expiredlast)) {
							hologram.removeLine(1);
							hologram.insertTextLine(1, expiredlast);
						}
					} catch (IndexOutOfBoundsException e) {
						hologram.insertTextLine(1, expiredlast);
					}
					if (!warned) {
						if (plugin.getConfig().getBoolean("settings.expire-warning")) {
							warned = true;
							String remaining = ChatColor.translateAlternateColorCodes('&',
									plugin.getLocaleConfig().getString("CHEST_EXPIRED"));
							String displayname = plugin.getConfig().getString("chests." + type + ".display-name");
							String remaining1 = remaining.replace("%s", displayname + "" + ChatColor.RESET);
							String last1 = ChatColor.translateAlternateColorCodes('&', remaining1);
							if (plugin.getServer().getPlayer(uuid) != null) {
								plugin.getServer().getPlayer(uuid).sendMessage(plugin.pltag + ChatColor.RESET + last1);
							}
						}
					}
				} else if (remaining < 0) {
					hours = 0;
					minutes = 0;
					seconds = 0;
					hrs = "00";
					mins = "00";
					secs = "00";
					String inftext = plugin.getLocaleConfig().getString("INFINITE");
					String inflast = ChatColor.translateAlternateColorCodes('&', inftext);
					try {
						if (!hologram.getLine(1).toString().equals("CraftTextLine [text=" + inflast + "]")) {
							hologram.removeLine(1);
							hologram.insertTextLine(1, inflast);
						}
					} catch (IndexOutOfBoundsException e) {
						hologram.insertTextLine(1, inflast);
					}
				} else {
					minutes = 0;
					seconds = remaining;
				}
				if (hours < 10) {
					hrs = "0" + hours;

				} else {
					hrs = hours + "";
				}
				if (minutes < 10) {
					mins = "0" + minutes;
				} else {
					mins = minutes + "";
				}
				if (seconds < 10) {
					secs = "0" + seconds;
				} else {
					secs = seconds + "";
				}
				if (remaining > 0) {

					hologram.removeLine(1);
					hologram.insertTextLine(1, hrs + "h:" + mins + "m:" + secs + "s");
				}
			}
			/*
			 * if (seconds == 10 || seconds < 10){ seconds = seconds - 1; String sec = "0" +
			 * seconds; hologram.removeLine(1); hologram.insertTextLine(1, minutes + ":" +
			 * sec); }else { seconds = seconds - 1; hologram.removeLine(1);
			 * hologram.insertTextLine(1, minutes + ":" + seconds); }
			 */
			if (plugin.expmap.get(str) != null) {
				ex3 = plugin.expmap.get(str);
			} else {
				plugin.removeConfig(str);
				plugin.expmap.remove(str);
				plugin.hmap.remove(str);
				plugin.getServer().getScheduler().cancelTask(timer);
				return;
			}
			if (ex3 >= 1) {
				for (int i2 = 0; i2 < delays.length; i2++) {
					int i3 = Integer.parseInt(delays[i2]);
					if (ex3 == i3) {
						String remaining = ChatColor.translateAlternateColorCodes('&',
								plugin.getLocaleConfig().getString("SECS_REMAINING"));
						String displayname = plugin.getConfig().getString("chests." + type + ".display-name");
						String remaining1 = remaining.replace("%s1", displayname + "" + ChatColor.RESET);
						String last = remaining1.replace("%s2", ex3 + "" + ChatColor.RESET);
						String last1 = ChatColor.translateAlternateColorCodes('&', last);
						if (plugin.getServer().getPlayer(uuid) != null) {
							plugin.getServer().getPlayer(uuid).sendMessage(plugin.pltag + ChatColor.RESET + last1);
						}
					}
				}
				if (ex3 % keyinteger == 0) {
					c.getInventory().addItem(i);
				}
				int ex2 = ex3 - 1;
				plugin.expmap.put(str, ex2);
			} else if (ex3 == -1) {
				if (count == d) {
					c.getInventory().addItem(i);
					count = 0;
				}

				count++;
			} else {
				// c.getInventory().addItem(i);
				// plugin.removeConfig(str);
				/*
				 * if (plugin.getConfig().getBoolean("settings.expire-warning")) { String
				 * remaining = ChatColor.translateAlternateColorCodes('&',
				 * plugin.getLocaleConfig().getString("CHEST_EXPIRED")); String displayname =
				 * plugin.getConfig().getString("chests." + type + ".display-name"); String
				 * remaining1 = remaining.replace("%s", displayname + "" + ChatColor.RESET);
				 * String last1 = ChatColor.translateAlternateColorCodes('&', remaining1); if
				 * (plugin.getServer().getPlayer(uuid) != null) {
				 * plugin.getServer().getPlayer(uuid).sendMessage(plugin.pltag + ChatColor.RESET
				 * + last1); } } plugin.getServer().getScheduler().cancelTask(timer); return;
				 */
			}
		}, 0, 20);

	}

}
