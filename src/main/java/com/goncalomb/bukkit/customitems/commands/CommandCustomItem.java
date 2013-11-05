package com.goncalomb.bukkit.customitems.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.goncalomb.bukkit.bkglib.Utils;
import com.goncalomb.bukkit.bkglib.betterplugin.BetterCommand;
import com.goncalomb.bukkit.bkglib.betterplugin.BetterCommandException;
import com.goncalomb.bukkit.bkglib.betterplugin.BetterCommandType;
import com.goncalomb.bukkit.bkglib.betterplugin.Lang;
import com.goncalomb.bukkit.customitems.api.CustomItem;
import com.goncalomb.bukkit.customitems.api.CustomItemManager;

public final class CommandCustomItem extends BetterCommand {
	
	private CustomItemManager _manager;
	
	public CommandCustomItem(CustomItemManager manager) {
		super("customitem", "customitemsapi.customitem");
		_manager = manager;
		setAlises("citem");
		setDescription(Lang._("citems.cmds.citem.description"));
	}
	
	public void giveCustomItem(Player player, String slug, String amount) throws BetterCommandException {
		CustomItem customItem = _manager.getCustomItem(slug);
		int intAmount = (amount == null ? 1 : Utils.parseInt(amount, -1));
		if (customItem == null) {
			throw new BetterCommandException(Lang._("citems.cmds.citem.no-item"));
		} else if (intAmount < 1) {
			throw new BetterCommandException(Lang._("common.invalid-amount"));
		} else {
			ItemStack item = customItem.getItem();
			if (item == null) {
				throw new BetterCommandException(Lang._("citems.cmds.citem.invalid"));
			} else {
				item.setAmount(intAmount);
				if (player.getInventory().addItem(item).size() > 0) {
					throw new BetterCommandException(Lang._("common.inventory-full"));
				} else {
					player.sendMessage(Lang._("citems.cmds.citem.ok"));
				}
			}
		}
	}
	
	@Command(args = "get", type = BetterCommandType.PLAYER_ONLY, minargs = 1, maxargs = 2, usage = "<item> [amount]")
	public boolean getCommand(CommandSender sender, String[] args) throws BetterCommandException {
		giveCustomItem((Player) sender, args[0], (args.length == 2 ? args[1] : null));
		return true;
	}
	
	@Command(args = "give", type = BetterCommandType.DEFAULT, minargs = 2, maxargs = 3, usage = "<player> <item> [amount]")
	public boolean giveCommand(CommandSender sender, String[] args) throws BetterCommandException {
		Player player = Bukkit.getPlayer(args[0]);
		if (player == null) {
			throw new BetterCommandException(Lang._("common.player-not-found"));
		}
		giveCustomItem(player, args[1], (args.length == 3 ? args[2] : null));
		return true;
	}
	
	@Command(args = "list", type = BetterCommandType.DEFAULT)
	public boolean listCommand(CommandSender sender, String[] args) {
		World world = (sender instanceof Player ? ((Player) sender).getWorld() : null);
		for (Plugin plugin : _manager.getOwningPlugins()) {
			StringBuilder sb = new StringBuilder("" + ChatColor.GOLD + ChatColor.ITALIC + plugin.getName() + ":");
			for (CustomItem customItem : _manager.getCustomItems(plugin)) {
				sb.append(" ");
				if (!customItem.isEnabled()) {
					sb.append(ChatColor.RED);
				} else if (world != null && !customItem.isValidWorld(world)) {
					sb.append(ChatColor.YELLOW);
				} else {
					sb.append(ChatColor.WHITE);
				}
				sb.append(customItem.getSlug());
			}
			sender.sendMessage(sb.toString());
		}
		return true;
	}
	
}