/*
 * Copyright (C) 2013 - Gonçalo Baltazar <http://goncalomb.com>
 *
 * This file is part of CustomItemsAPI.
 *
 * CustomItemsAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CustomItemsAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CustomItemsAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.goncalomb.bukkit.customitemsapi.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import com.goncalomb.bukkit.bkglib.Lang;
import com.goncalomb.bukkit.bkglib.bkgcommand.BKgCommand;
import com.goncalomb.bukkit.bkglib.bkgcommand.BKgCommandException;
import com.goncalomb.bukkit.customitemsapi.CustomItemsAPI;
import com.goncalomb.bukkit.customitemsapi.api.CustomItem;
import com.goncalomb.bukkit.customitemsapi.api.CustomItemManager;

public final class CommandCustomItems extends BKgCommand {
	
	public CommandCustomItems() {
		super("customitem", "citem");
	}

	private List<String> getCustomItemNamesList(String prefix) {
		ArrayList<String> names = new ArrayList<String>();
		for (CustomItem citem : CustomItemManager.getCustomItems()) {
			String slug = citem.getSlug();
			if (citem.isEnabled() && StringUtil.startsWithIgnoreCase(slug, prefix)) {
				names.add(slug);
			}
		}
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
		return names;
	}
	
	private void giveCustomItem(Player player, String slug, String amount) throws BKgCommandException {
		int intAmount = (amount == null ? 1 : CommandUtils.parseInt(amount));
		CustomItem customItem = CustomItemManager.getCustomItem(slug);
		if (customItem == null) {
			throw new BKgCommandException(Lang._(CustomItemsAPI.class, "commands.customitem.no-item"));
		} else {
			ItemStack item = customItem.getItem();
			if (item == null) {
				throw new BKgCommandException(Lang._(CustomItemsAPI.class, "commands.customitem.invalid"));
			} else {
				item.setAmount(intAmount);
				CommandUtils.giveItem(player, item);
				player.sendMessage(Lang._(CustomItemsAPI.class, "commands.customitem.ok"));
			}
		}
	}
	
	@Command(args = "get", type = CommandType.PLAYER_ONLY, minargs = 1, maxargs = 2, usage = "<item> [amount]")
	public boolean customitem_get(CommandSender sender, String[] args) throws BKgCommandException {
		giveCustomItem((Player) sender, args[0], (args.length == 2 ? args[1] : null));
		return true;
	}
	
	@TabComplete(args = "get")
	public List<String> customitem_get_Tab(CommandSender sender, String[] args) {
		return (args.length == 1 ? getCustomItemNamesList(args[0]) : null);
	}
	
	@Command(args = "give", type = CommandType.DEFAULT, minargs = 2, maxargs = 3, usage = "<player> <item> [amount]")
	public boolean customitem_give(CommandSender sender, String[] args) throws BKgCommandException {
		Player player = CommandUtils.findPlayer(args[0]);
		giveCustomItem(player, args[1], (args.length == 3 ? args[2] : null));
		return true;
	}
	
	@TabComplete(args = "give")
	public List<String> customitem_give_Tab(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return CommandUtils.playerTabComplete(sender, args[0]);
		} else if (args.length == 2) {
			return getCustomItemNamesList(args[1]);
		}
		return null;
	}
	
	@Command(args = "list", type = CommandType.DEFAULT)
	public boolean customitem_list(CommandSender sender, String[] args) {
		World world = (sender instanceof Player ? ((Player) sender).getWorld() : null);
		for (Plugin plugin : CustomItemManager.getOwningPlugins()) {
			StringBuilder sb = new StringBuilder("" + ChatColor.GOLD + ChatColor.ITALIC + plugin.getName() + ":");
			for (CustomItem customItem : CustomItemManager.getCustomItems(plugin)) {
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
