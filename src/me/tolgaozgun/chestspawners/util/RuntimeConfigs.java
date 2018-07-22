package me.tolgaozgun.chestspawners.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.tolgaozgun.chestspawners.ChestMain;
import net.md_5.bungee.api.ChatColor;

public class RuntimeConfigs {
	private int timer;
	private int delay;
	private boolean isautosave;

	public void saveRunnable() {
		ChestMain plugin = ChestMain.getPlugin(ChestMain.class);
		isautosave = plugin.getConfig().getBoolean("settings.autosave");
		if (isautosave) {
			delay = plugin.getConfig().getInt("settings.saveinterval");
			timer = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
				plugin.saveChests();
			}, 0, delay * 20);
		}
	}
}
