package net.citizensnpcs.questers.listeners;

import java.util.Map;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.SettingsManager;
import net.citizensnpcs.questers.QuestManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.google.common.collect.Maps;

public class QuesterBlockListen extends BlockListener {
	private static final Map<Location, Player> placed = Maps.newHashMap();

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Player placer = placed.get(event.getBlock().getLocation());
		if (placer != null && placer.equals(event.getPlayer())) {
			return;
		}
		QuestManager.incrementQuest(event.getPlayer(), event);
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		placed.put(event.getBlock().getLocation(), event.getPlayer());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Citizens.plugin,
				new RemoveBlockTask(event.getBlock()),
				SettingsManager.getInt("BlockTrackingRemoveDelay"));
		QuestManager.incrementQuest(event.getPlayer(), event);
	}

	private static class RemoveBlockTask implements Runnable {
		private final Location location;

		RemoveBlockTask(Block block) {
			this.location = block.getLocation();
		}

		@Override
		public void run() {
			synchronized (placed) {
				placed.remove(location);
			}
		}
	}
}