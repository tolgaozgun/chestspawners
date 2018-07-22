package me.tolgaozgun.chestspawners;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import me.tolgaozgun.chestspawners.commands.MainCommand;
import me.tolgaozgun.chestspawners.listeners.EventsListener;
import me.tolgaozgun.chestspawners.util.RuntimeChests;
import me.tolgaozgun.chestspawners.util.RuntimeConfigs;
import me.tolgaozgun.chestspawners.util.Updater;
import me.tolgaozgun.chestspawners.util.Updater.ReleaseType;

public class ChestMain extends JavaPlugin {
	public HashMap<String, String> hmap = new HashMap<String, String>();
	public HashMap<String, Integer> expmap = new HashMap<String, Integer>();
	public MultiverseCore mcore;
	private File configFile, chestsFile, localeFile;
	private FileConfiguration config, chests, locale;
	private boolean reloading = false;
	public String pltag = null;
	private boolean useHolographicDisplays, bool1;
	private RuntimeConfigs configruntime = new RuntimeConfigs();

	public FileConfiguration getConfig() {
		return this.config;
	}

	public FileConfiguration getChestsConfig() {
		return this.chests;
	}

	public FileConfiguration getLocaleConfig() {
		return this.locale;
	}

	public void onEnable() {
		loadPluginTag();
		getServer().getPluginManager().registerEvents(new EventsListener(), this);
		getServer().getPluginCommand("chestspawners").setExecutor(new MainCommand());
		// mcore = getMultiverseCore();

		if (!reloading) {
			reloading = true;
			createFiles();
			getLocale();
			if (config.getBoolean("settings.auto-updates")) {
				Updater updater = new Updater(this, 297121, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
				boolean update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
				ReleaseType type = updater.getLatestType();
				String name = updater.getLatestName();
				if (update) {
					getServer().getConsoleSender().sendMessage(
							pltag + ChatColor.RED + "Update for version " + type + " " + name + " available.");
					getServer().getConsoleSender().sendMessage(
							pltag + ChatColor.RED + "Use console command, csp update to update the plugin.");
				} else {
					getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN + "Your plugin is up to date!");
				}
			} else {
				getServer().getConsoleSender()
						.sendMessage(pltag + ChatColor.YELLOW + "Update check is disabled from config, skipping...");
			}

			if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
				if (getConfig().getBoolean("settings.holograms")) {
					useHolographicDisplays = true;
					getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN
							+ "HolographicDisplays is found and enabled via config. ChestSpawners will now use holograms.");
				} else {
					getServer().getConsoleSender().sendMessage(pltag + ChatColor.RED
							+ "HolographicDisplays is found but disabled via config. Holograms are disabled.");
					useHolographicDisplays = false;
				}
			} else {
				getServer().getConsoleSender()
						.sendMessage(pltag + ChatColor.YELLOW + "HolographicDisplays is not found, continuing...");
				useHolographicDisplays = false;
			}
			if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
				getServer().getConsoleSender()
						.sendMessage(pltag + ChatColor.GREEN + "Multiverse-Core loaded successfully.");
				mcore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
				;
			} else {
				getServer().getConsoleSender()
						.sendMessage(pltag + ChatColor.YELLOW + "Multiverse-Core is not found, continuing...");
			}
			getChests();
			runSaveScheduler();
			reloading = false;
		}

	}

	public void runSaveScheduler() {
		configruntime.saveRunnable();
	}
	
	public File getTheFile() {
		return this.getFile();
	}

	/*
	 * public MultiverseCore getMultiverseCore() { Plugin plugin =
	 * Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core"); if
	 * (plugin == null || !(plugin instanceof MultiverseCore)) {
	 * Bukkit.getServer().getConsoleSender().sendMessage(pltag + ChatColor.RED +
	 * "Multiverse-Core is not found."); return null; } else } return
	 * (MultiverseCore) plugin;
	 * 
	 * }
	 */
	public boolean getHolograms() {
		return useHolographicDisplays;
	}

	public void loadPluginTag() {
		if (locale != null) {
			pltag = ChatColor.translateAlternateColorCodes('&', locale.getString("PL_TAG"));
		} else {
			pltag = ChatColor.translateAlternateColorCodes('&', "&4ChestSpawners > ");
		}
	}

	public void getChests() {
		hmap.clear();
		expmap.clear();
		if (chests.getConfigurationSection("chests") != null) {
			for (String str : chests.getConfigurationSection("chests").getKeys(false)) {
				int exp = chests.getInt("chests." + str + ".expire");
				int x = chests.getInt("chests." + str + ".x");
				int y = chests.getInt("chests." + str + ".y");
				int z = chests.getInt("chests." + str + ".z");

				String w = chests.getString("chests." + str + ".world").trim();
				String type = chests.getString("chests." + str + ".type");
				String uuid = chests.getString("chests." + str + ".owner");
				String stringa1 = x + "/" + y + "/" + z + "/" + w + "/" + uuid;
				/*if (exp == 0) {
					 removeConfig(stringa1);
					continue;
				}*/
				Block b = null;
				if (Bukkit.getServer().getWorlds().get(0).getName() == w) {
					b = Bukkit.getServer().getWorlds().get(0).getBlockAt(x, y, z);
				} else if (Bukkit.getServer().getWorld(w) != null) {
					b = Bukkit.getServer().getWorld(w).getBlockAt(x, y, z);
				} else if (mcore.getMVWorldManager().isMVWorld(w)) {
					MultiverseWorld wor = mcore.getMVWorldManager().getMVWorld(w);
					MVWorld worl = (MVWorld) wor;
					b = worl.getCBWorld().getBlockAt(x, y, z);
				} else {
					getServer().getConsoleSender().sendMessage(pltag + ChatColor.DARK_PURPLE + "Big error!" + w + "a");
					continue;
				}
				if (b.getType() == Material.CHEST) {
					Chest c = (Chest) b.getState();
					String ne = config.getString("chests." + type + ".display-name");

					if (!c.getInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', ne))) {
						chests.set("chests." + str, null);
					} else {
						hmap.put(stringa1, type);
						expmap.put(stringa1, exp);
					}
				} else {
					chests.set("chests." + str, null);
				}
			}

			loadChests();
		}
	}

	private void loadChests() {
		for (String str : hmap.keySet()) {
			int x = Integer.parseInt(str.split("/")[0]);
			int y = Integer.parseInt(str.split("/")[1]);
			int z = Integer.parseInt(str.split("/")[2]);
			String w = str.split("/")[3];
			String uuid = str.split("/")[4];
			String type = hmap.get(str);
			int exp = expmap.get(str);
			Block b = null;

			if (Bukkit.getServer().getWorld(w) != null) {
				b = Bukkit.getServer().getWorld(w).getBlockAt(x, y, z);
			} else if (mcore.getMVWorldManager().getMVWorld(w) != null) {
				MultiverseWorld wor2 = mcore.getMVWorldManager().getMVWorld(w);
				World world = wor2.getCBWorld();
				b = world.getBlockAt(x, y, z);
			} else {
				getServer().getConsoleSender().sendMessage(pltag + ChatColor.DARK_PURPLE + "Big error!" + w + "a");
			}
			Chunk chunk = b.getChunk();
			if (!chunk.isLoaded()) {
				chunk.load();
			}
			if (b.getType() == Material.CHEST) {
				Chest c = (Chest) b.getState();
				int d = getConfig().getInt("chests." + type + ".delay");
				String itemdrop = getConfig().getString("chests." + type + ".item-drop");
				UUID uuids = UUID.fromString(uuid);
				ItemStack i;
				if (itemdrop.equals("lapis")) {
					Dye dye = new Dye();
					dye.setColor(DyeColor.BLUE);
					i = dye.toItemStack(1);
				} else {
					i = new ItemStack(Material.matchMaterial(itemdrop));
				}
				new RuntimeChests().chestSetup(c, i, type, d, exp, uuids);
			}
		}
	}

	private void createFiles() {
		configFile = new File(getDataFolder(), "config.yml");
		chestsFile = new File(getDataFolder(), "chests.yml");

		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			saveResource("config.yml", true);
		}
		if (!chestsFile.exists()) {
			chestsFile.getParentFile().mkdirs();
			saveResource("chests.yml", true);
		}

		config = new YamlConfiguration();
		chests = new YamlConfiguration();
		try {
			config.load(configFile);
			chests.load(chestsFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void getLocale() {
		String selected;
		String lang = getConfig().getString("settings.locale");
		switch (lang) {
		case "tr":
			localeFile = new File(getDataFolder(), "locale.yml");
			locale = new YamlConfiguration();
			try {
				Reader defLocaleStream = new InputStreamReader(this.getResource("locale_tr.yml"),
						Charset.forName("UTF-8"));
				if (defLocaleStream != null) {
					YamlConfiguration defLocale = YamlConfiguration.loadConfiguration(defLocaleStream);
					locale.setDefaults(defLocale);
					defLocale.save(localeFile);
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			selected = locale.getString("LOADED_LANG");
			this.getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN + selected);
			break;
		case "en":
			localeFile = new File(getDataFolder(), "locale.yml");
			locale = new YamlConfiguration();
			try {
				Reader defLocaleStream = new InputStreamReader(this.getResource("locale.yml"), "UTF-8");
				if (defLocaleStream != null) {
					YamlConfiguration defLocale = YamlConfiguration.loadConfiguration(defLocaleStream);
					locale.setDefaults(defLocale);
					defLocale.save(localeFile);
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			selected = locale.getString("LOADED_LANG");
			this.getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN + selected);
			break;
		case "de":
			localeFile = new File(getDataFolder(), "locale.yml");
			locale = new YamlConfiguration();
			try {
				Reader defLocaleStream = new InputStreamReader(this.getResource("locale_de.yml"), "Cp1252");
				if (defLocaleStream != null) {
					YamlConfiguration defLocale = YamlConfiguration.loadConfiguration(defLocaleStream);
					locale.setDefaults(defLocale);
					defLocale.save(localeFile);
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			selected = locale.getString("LOADED_LANG");
			this.getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN + selected);
			break;
		case "is":
			localeFile = new File(getDataFolder(), "locale.yml");
			locale = new YamlConfiguration();
			try {
				Reader defLocaleStream = new InputStreamReader(this.getResource("locale_is.yml"), "Cp1252");
				if (defLocaleStream != null) {
					YamlConfiguration defLocale = YamlConfiguration.loadConfiguration(defLocaleStream);
					locale.setDefaults(defLocale);
					defLocale.save(localeFile);
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			selected = locale.getString("LOADED_LANG");
			this.getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN + selected);
			break;
		case "ru":
			localeFile = new File(getDataFolder(), "locale.yml");
			locale = new YamlConfiguration();
			try {
				Reader defLocaleStream = new InputStreamReader(this.getResource("locale_ru.yml"), "UTF-8");
				if (defLocaleStream != null) {
					YamlConfiguration defLocale = YamlConfiguration.loadConfiguration(defLocaleStream);
					locale.setDefaults(defLocale);
					defLocale.save(localeFile);
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			selected = locale.getString("LOADED_LANG");
			this.getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN + selected);
			break;
		case "nl":
			localeFile = new File(getDataFolder(), "locale.yml");
			locale = new YamlConfiguration();
			try {
				Reader defLocaleStream = new InputStreamReader(this.getResource("locale_nl.yml"), "Cp1252");
				if (defLocaleStream != null) {
					YamlConfiguration defLocale = YamlConfiguration.loadConfiguration(defLocaleStream);
					locale.setDefaults(defLocale);
					defLocale.save(localeFile);
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			selected = locale.getString("LOADED_LANG");
			this.getServer().getConsoleSender().sendMessage(pltag + ChatColor.GREEN + selected);
			break;
		default:
			Bukkit.getServer().getConsoleSender()
					.sendMessage(pltag + ChatColor.GREEN + "No selected language found, selecting English.");
			localeFile = new File(getDataFolder(), "locale.yml");
			locale = new YamlConfiguration();
			try {
				Reader defLocaleStream = new InputStreamReader(this.getResource("locale.yml"), "Cp1252");
				if (defLocaleStream != null) {
					YamlConfiguration defLocale = YamlConfiguration.loadConfiguration(defLocaleStream);
					locale.setDefaults(defLocale);
					defLocale.save(localeFile);
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public void onDisable() {
		getServer().getScheduler().cancelAllTasks();
		saveChests();
		deleteHolograms();
	}

	public void deleteHolograms() {
		if (useHolographicDisplays) {
			if (HologramsAPI.getHolograms(this) != null) {
				for (Hologram holog : HologramsAPI.getHolograms(this)) {
					holog.delete();
				}
			}
		}
	}

	public void deleteHologram(Location where) {
		for (Hologram holo : HologramsAPI.getHolograms(this)) {
			if (where.equals(holo.getLocation())) {
				holo.delete();
			}
		}
	}

	public void removeConfig(String str) {
		int x = Integer.parseInt(str.split("/")[0]);
		int y = Integer.parseInt(str.split("/")[1]);
		int z = Integer.parseInt(str.split("/")[2]);
		String w = str.split("/")[3];
		if (chests.getConfigurationSection("chests").getKeys(false) != null) {
			for (String iterator : chests.getConfigurationSection("chests").getKeys(false)) {
				if (chests.getInt("chests." + iterator + ".x") == x && chests.getInt("chests." + iterator + ".y") == y
						&& chests.getInt("chests." + iterator + ".z") == z
						&& chests.getString("chests." + iterator + ".world").equals(w)) {
					chests.set("chests." + iterator, null);

				}
			}
		}
	}

	public void saveChests() {
		bool1 = false;

		if (!chests.contains("chests")) {
			chests.createSection("chests");
		}

		for (String str : hmap.keySet()) {
			String type = hmap.get(str);
			int exp = expmap.get(str);
			int x = Integer.parseInt(str.split("/")[0]);
			int y = Integer.parseInt(str.split("/")[1]);
			int z = Integer.parseInt(str.split("/")[2]);
			String w = str.split("/")[3];
			String uuids = str.split("/")[4];
			int size = chests.getConfigurationSection("chests").getKeys(false).size();
			for (String iterator : chests.getConfigurationSection("chests").getKeys(false)) {
				if (chests.getInt("chests." + iterator + ".x") == x && chests.getInt("chests." + iterator + ".y") == y
						&& chests.getInt("chests." + iterator + ".z") == z
						&& chests.getString("chests." + iterator + ".world").equals(w)
						&& chests.getString("chests." + iterator + ".type").equals(type)) {
					chests.set("chests." + iterator + ".expire", exp);
					chests.set("chests." + iterator + ".owner", uuids);
					bool1 = true;
				}
			}
			if (!bool1) {
				chests.set("chests." + size + ".x", x);
				chests.set("chests." + size + ".y", y);
				chests.set("chests." + size + ".z", z);
				chests.set("chests." + size + ".world", w);
				chests.set("chests." + size + ".type", type);
				chests.set("chests." + size + ".expire", exp);
				chests.set("chests." + size + ".owner", uuids);
			} else {
				bool1 = false;
			}
		}

		try {
			chests.save(chestsFile);
			for (Player p : getServer().getOnlinePlayers()) {
				String savesuccess = locale.getString("SAVE_SUCCESS");
				String lastsuccess = ChatColor.translateAlternateColorCodes('&', savesuccess);
				p.sendMessage(pltag + lastsuccess);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reloadConfigs() {
		config = YamlConfiguration.loadConfiguration(configFile);
	}

}
