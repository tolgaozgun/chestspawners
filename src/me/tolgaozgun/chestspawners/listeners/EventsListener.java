package me.tolgaozgun.chestspawners.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import me.tolgaozgun.chestspawners.ChestMain;
import me.tolgaozgun.chestspawners.util.RuntimeChests;

public class EventsListener implements Listener {
	private ItemStack i;

	private ChestMain plugin = ChestMain.getPlugin(ChestMain.class);
	private ArrayList<Location> breakLocations = new ArrayList<Location>();

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlockPlaced();
		Location l = b.getLocation();
		World w = b.getWorld();
		if (b.getType() == Material.CHEST) {
			Chest c = (Chest) b.getState();
			String str = c.getInventory().getTitle();
			if (plugin.getConfig().getConfigurationSection("chests") != null) {
				for (String abc : plugin.getConfig().getConfigurationSection("chests").getKeys(false)) {
					String s1 = plugin.getConfig().getString("chests." + abc + ".display-name");
					{
						String s2 = ChatColor.translateAlternateColorCodes('&', s1);
						if (s2.equals(str)) {
							String perm = plugin.getConfig().getString("chests." + abc + ".permission");
							if (p.hasPermission(perm) || p.isOp()) {
								if (plugin.getConfig().getBoolean("settings.disable-doublechest")) {
									double x = l.getX();
									double y = l.getY();
									double z = l.getZ();
									Location l1 = new Location(w, x + 1, y, z);
									Location l2 = new Location(w, x - 1, y, z);
									Location l3 = new Location(w, x, y, z - 1);
									Location l4 = new Location(w, x, y, z + 1);
									Block b1 = w.getBlockAt(l1);
									Block b2 = w.getBlockAt(l2);
									Block b3 = w.getBlockAt(l3);
									Block b4 = w.getBlockAt(l4);
									if (b1.getType().equals(Material.CHEST) || b2.getType().equals(Material.CHEST)
											|| b3.getType().equals(Material.CHEST)
											|| b4.getType().equals(Material.CHEST)) {
										e.setCancelled(true);
										p.sendMessage(
												plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
														'&', plugin.getLocaleConfig().getString("CANNOT_DOUBLECHEST")));
										return;
									}
								}

								int delay = plugin.getConfig().getInt("chests." + abc + ".delay");
								int expire = plugin.getConfig().getInt("chests." + abc + ".expire");
								String itemdrop = plugin.getConfig().getString("chests." + abc + ".item-drop");
								if (itemdrop.equals("lapis")) {
									Dye dye = new Dye();
									dye.setColor(DyeColor.BLUE);
									i = dye.toItemStack(1);
								} else {
									i = new ItemStack(Material.matchMaterial(itemdrop));
								}
								UUID uuid = p.getUniqueId();
								new RuntimeChests().chestSetup(c, i, abc, delay, expire, uuid);
								String spawnerready = ChatColor.translateAlternateColorCodes('&',
										plugin.getLocaleConfig().getString("SPAWNER_READY"));
								ChatColor color = ChatColor.getByChar(spawnerready.substring(1, 2));
								if (color instanceof ChatColor) {
									p.sendMessage(
											plugin.pltag + ChatColor.RESET + spawnerready.replace("%s", s2 + color));
								} else {
									p.sendMessage(plugin.pltag + ChatColor.RESET
											+ spawnerready.replace("%s", s2 + ChatColor.RESET));
								}
							} else {
								p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
										'&', plugin.getLocaleConfig().getString("NO_PERM")));
								e.setCancelled(true);
							}
						}

					}
				}
			}
		}
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void invMoveItem(InventoryClickEvent e) {
		if (e.getClickedInventory() == null) {
			return;
		}
		if (plugin.getConfig().getBoolean("settings.disable-move")) {
			Inventory inv = e.getClickedInventory();
			Player p = (Player) e.getWhoClicked();
			InventoryView inv2 = p.getOpenInventory();
			if (inv2.getType().equals(InventoryType.CHEST)) {
				for (String abc : plugin.getConfig().getConfigurationSection("chests").getKeys(false)) {
					String s1 = plugin.getConfig().getString("chests." + abc + ".display-name");
					String s2 = ChatColor.translateAlternateColorCodes('&', s1);
					if (s2.equals(inv2.getTitle())) {
						if (inv.getTitle().equals(p.getInventory().getTitle())) {
							e.setCancelled(true);
							p.updateInventory();
							String cannotstore = plugin.getLocaleConfig().getString("CANNOT_STORE");
							String sendingstr = ChatColor.translateAlternateColorCodes('&', cannotstore);
							p.sendMessage(plugin.pltag + ChatColor.RESET + sendingstr);
						}
					}
				}

			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
			Block b = e.getBlock();
			if (b.getType() == Material.CHEST) {
				Chest c = (Chest) b.getState();
				String str = c.getInventory().getTitle();
				if (plugin.getConfig().getConfigurationSection("chests") != null) {
					for (String abc : plugin.getConfig().getConfigurationSection("chests").getKeys(false)) {
						String s1 = plugin.getConfig().getString("chests." + abc + ".display-name");
						String s2 = ChatColor.translateAlternateColorCodes('&', s1);
						if (s2.equals(str)) {
							String perm = plugin.getConfig().getString("chests." + abc + ".permission");
							if (p.hasPermission(perm) || p.isOp()) {
								List<String> str2 = plugin.getConfig().getStringList("chests." + abc + ".lore");
								if (p.isSneaking()) {
									if (plugin.getConfig().getBoolean("settings.shift-left-break")) {
										if (b.getType() == Material.CHEST) {
											ItemStack item1 = new ItemStack(Material.CHEST);
											ItemMeta meta1 = item1.getItemMeta();
											meta1.setLore(str2);
											meta1.setDisplayName(s2);
											item1.setItemMeta(meta1);
											p.getInventory().addItem(item1);
											p.updateInventory();
											String brokenspawner = plugin.getLocaleConfig().getString("BROKEN_SPAWNER");
											ChatColor color = ChatColor.getByChar(brokenspawner.substring(1, 2));
											if (color instanceof ChatColor) {
												p.sendMessage(plugin.pltag + ChatColor.RESET
														+ ChatColor.translateAlternateColorCodes('&',
																brokenspawner.replace("%s", s1 + color)));
											} else {
												p.sendMessage(plugin.pltag + ChatColor.RESET
														+ ChatColor.translateAlternateColorCodes('&',
																brokenspawner.replace("%s", s1 + ChatColor.RESET)));
											}
											b.setType(Material.AIR);
											int x = c.getLocation().getBlockX();
											int y = c.getLocation().getBlockY();
											int z = c.getLocation().getBlockZ();
											String w = c.getWorld().getName();
											String stringful = x + "/" + y + "/" + z + "/" + w;
											plugin.hmap.remove(stringful);
											plugin.expmap.remove(stringful);
											return;
										}

									}
								}
								if (plugin.getConfig().getBoolean("settings.disable-break")) {
									e.setCancelled(true);
									p.sendMessage(
											plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&',
													plugin.getLocaleConfig().getString("UNABLE_TO_BREAK")));
									return;
								} else {
									if (e.getBlock().getDrops().size() > 0) {
										breakLocations.add(e.getBlock().getLocation());
									}
									ItemStack item1 = new ItemStack(Material.CHEST);
									ItemMeta meta1 = item1.getItemMeta();
									meta1.setLore(str2);
									meta1.setDisplayName(s2);
									item1.setItemMeta(meta1);
									p.getInventory().addItem(item1);
									p.updateInventory();
									String brokenspawner = plugin.getLocaleConfig().getString("BROKEN_SPAWNER");
									ChatColor color = ChatColor.getByChar(brokenspawner.substring(1, 2));
									if (color instanceof ChatColor) {
										p.sendMessage(
												plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
														'&', brokenspawner.replace("%s", s1 + color)));
									} else {
										p.sendMessage(
												plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
														'&', brokenspawner.replace("%s", s1 + ChatColor.RESET)));
									}
									b.setType(Material.AIR);
									int x = c.getLocation().getBlockX();
									int y = c.getLocation().getBlockY();
									int z = c.getLocation().getBlockZ();
									String w = c.getWorld().getName();
									String stringful = x + "/" + y + "/" + z + "/" + w;
									plugin.hmap.remove(stringful);
									// }
								}
								
								return;

							} else {
								p.sendMessage(plugin.pltag + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
										'&', plugin.getLocaleConfig().getString("NO_PERM")));
								e.setCancelled(true);
							}
						}
					}
				}
			}
		}
	

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent e) {
		Location l = e.getEntity().getLocation().getBlock().getLocation();
		ItemStack item = e.getEntity().getItemStack();
		ItemMeta meta = item.getItemMeta();
		if (breakLocations.contains(l) && !meta.hasDisplayName() && item.getType().equals(Material.CHEST)) {
			e.setCancelled(true);
		}
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			breakLocations.remove(l);
		}, 1L);
	}

}
