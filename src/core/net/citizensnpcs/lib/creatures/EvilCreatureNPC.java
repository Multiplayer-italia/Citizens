package net.citizensnpcs.lib.creatures;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.event.NPCCreateEvent.NPCCreateReason;
import net.citizensnpcs.lib.HumanNPC;
import net.citizensnpcs.lib.NPCManager;
import net.citizensnpcs.permissions.PermissionManager;
import net.citizensnpcs.properties.properties.UtilityProperties;
import net.citizensnpcs.utils.InventoryUtils;
import net.citizensnpcs.utils.MessageUtils;
import net.citizensnpcs.utils.Messaging;
import net.citizensnpcs.utils.StringUtils;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.World;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EvilCreatureNPC extends CreatureNPC {
	private boolean isTame = false;

	public EvilCreatureNPC(MinecraftServer minecraftserver, World world,
			String s, ItemInWorldManager iteminworldmanager) {
		super(minecraftserver, world, s, iteminworldmanager);
	}

	@Override
	public void onSpawn() {
		this.getPlayer()
				.getInventory()
				.setItemInHand(
						new ItemStack(weapons[this.random
								.nextInt(weapons.length)], 1));
		this.health = Settings.getInt("EvilHealth");
		super.onSpawn();
	}

	@Override
	public void doTick() {
		if (isTame)
			return;
		EntityHuman closest = getClosestPlayer(this.range);
		if (!hasTarget() && closest != null) {
			if (!PermissionManager
					.hasPermission((Player) closest.getBukkitEntity(),
							"citizens.evils.immune")) {
				targetClosestPlayer(true, this.range);
			}
		}
		super.doTick();
	}

	@Override
	public void onDeath() {
		ItemStack item = UtilityProperties.getRandomDrop(Settings
				.getString("EvilDrops"));
		if (item == null)
			return;
		this.getEntity().getWorld().dropItemNaturally(this.getLocation(), item);
	}

	@Override
	public CreatureNPCType getType() {
		return CreatureNPCType.EVIL;
	}

	@Override
	public void onRightClick(Player player) {
		if (!PermissionManager.canCreate(player)) {
			Messaging
					.sendError(
							player,
							"You cannot tame this Evil NPC because you have reached the NPC creation limit.");
			return;
		}
		if (player.getItemInHand().getTypeId() != Settings
				.getInt("EvilTameItem"))
			return;
		if (random.nextInt(100) <= Settings.getInt("EvilTameChance")) {
			InventoryUtils.decreaseItemInHand(player);
			isTame = true;
			CreatureTask.despawn(this);
			HumanNPC npc = NPCManager.register(this.getPlayer().getName(),
					player.getLocation(), NPCCreateReason.RESPAWN);
			npc.getNPCData().setOwner(player.getName());
			player.sendMessage(ChatColor.GREEN + "You have tamed "
					+ StringUtils.wrap(this.getPlayer().getName())
					+ "! You can now toggle it to be any type.");
		} else {
			Messaging.send(
					player,
					StringUtils.colourise(Settings.getString("ChatFormat")
							.replace("%name%", this.getPlayer().getName()))
							+ ChatColor.WHITE
							+ MessageUtils.getRandomMessage(Settings
									.getString("EvilFailedTameMessages")));
		}
	}

	@Override
	public void onLeftClick(Player player) {
	}
}