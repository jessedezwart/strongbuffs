package nl.jessedezwart.strongbuffs.runtime;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import net.runelite.api.gameval.InventoryID;
import nl.jessedezwart.strongbuffs.runtime.state.InventoryRuntimeState;

@Singleton
@Slf4j
public class RuntimeConditionTracker
{
	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;
	private final ItemManager itemManager;

	@Getter
	private final RuntimeState runtimeState = new RuntimeState();

	private RuntimeConditionRequirements requirements = RuntimeConditionRequirements.empty();
	private boolean started;

	@Inject
	public RuntimeConditionTracker(Client client, ClientThread clientThread, EventBus eventBus, ItemManager itemManager)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;
		this.itemManager = itemManager;
	}

	public void startUp()
	{
		if (started)
		{
			return;
		}

		started = true;
		eventBus.register(this);
		refreshTrackedStateAsync();
	}

	public void shutDown()
	{
		if (!started)
		{
			return;
		}

		eventBus.unregister(this);
		started = false;
		runtimeState.clear();
	}

	public void setRules(List<RuleDefinition> rules)
	{
		requirements = RuntimeConditionRequirements.fromRules(rules);
		runtimeState.clear();
		refreshTrackedStateAsync();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			refreshTrackedStateAsync();
			return;
		}

		runtimeState.clear();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isLoggedIn() || !requirements.needsGameTick())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());

		if (requirements.tracksRunEnergy())
		{
			runtimeState.getLocation().setRunEnergyPercent(client.getEnergy() / 100);
		}

		if (requirements.tracksPlayerLocation())
		{
			runtimeState.getLocation().setPlayerLocation(readPlayerLocation());
		}

		if (requirements.tracksPlayerInstance())
		{
			runtimeState.getLocation().setInInstance(client.getTopLevelWorldView().isInstance());
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		Skill skill = event.getSkill();

		if (requirements.tracksHitpoints() && skill == Skill.HITPOINTS)
		{
			runtimeState.getSkills().setHitpoints(event.getBoostedLevel());
		}

		if (requirements.tracksPrayerPoints() && skill == Skill.PRAYER)
		{
			runtimeState.getSkills().setPrayerPoints(event.getBoostedLevel());
		}

		if (requirements.getRealSkills().contains(skill))
		{
			runtimeState.getSkills().setRealSkillLevel(skill, event.getLevel());
		}

		if (requirements.getXpGainSkills().contains(skill))
		{
			runtimeState.getSkills().markXpGain(skill);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		if (requirements.tracksSpecialAttack() && event.getVarpId() == VarPlayerID.SA_ENERGY)
		{
			runtimeState.getVars().setSpecialAttackPercent(client.getVarpValue(VarPlayerID.SA_ENERGY) / 10);
		}

		if (requirements.tracksPoison() && event.getVarpId() == VarPlayerID.POISON)
		{
			runtimeState.getVars().setPoisonState(readPoisonState());
		}

		if (requirements.tracksSlayerTask() && event.getVarpId() == VarPlayerID.SLAYER_COUNT)
		{
			int remaining = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
			runtimeState.getVars().setSlayerTaskRemaining(remaining);
			runtimeState.getVars().setSlayerTaskActive(remaining > 0);
		}

		if (!requirements.getPrayers().isEmpty() && event.getVarbitId() != -1)
		{
			for (Prayer prayer : requirements.getPrayers())
			{
				if (prayer.getVarbit() == event.getVarbitId())
				{
					runtimeState.getVars().setPrayerActive(prayer, isPrayerActive(prayer));
					break;
				}
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		if (requirements.hasInventoryTracking() && event.getContainerId() == InventoryID.INV)
		{
			refreshInventoryState(event.getItemContainer());
		}

		if (requirements.hasEquipmentTracking() && event.getContainerId() == InventoryID.WORN)
		{
			refreshEquipmentState(event.getItemContainer());
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if (requirements.hasGroundItemTracking())
		{
			String itemName = resolveItemName(event.getItem().getId());

			if (requirements.getGroundItems().contains(itemName))
			{
				runtimeState.getGroundItems().incrementNearbyGroundItem(itemName);
			}
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		if (requirements.hasGroundItemTracking())
		{
			String itemName = resolveItemName(event.getItem().getId());

			if (requirements.getGroundItems().contains(itemName))
			{
				runtimeState.getGroundItems().decrementNearbyGroundItem(itemName);
			}
		}
	}

	private void refreshTrackedStateAsync()
	{
		if (!started)
		{
			return;
		}

		clientThread.invokeLater(this::refreshTrackedState);
	}

	private void refreshTrackedState()
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());

		if (requirements.tracksHitpoints())
		{
			runtimeState.getSkills().setHitpoints(client.getBoostedSkillLevel(Skill.HITPOINTS));
		}

		if (requirements.tracksPrayerPoints())
		{
			runtimeState.getSkills().setPrayerPoints(client.getBoostedSkillLevel(Skill.PRAYER));
		}

		for (Skill skill : requirements.getRealSkills())
		{
			runtimeState.getSkills().setRealSkillLevel(skill, client.getRealSkillLevel(skill));
		}

		if (requirements.tracksSpecialAttack())
		{
			runtimeState.getVars().setSpecialAttackPercent(client.getVarpValue(VarPlayerID.SA_ENERGY) / 10);
		}

		if (requirements.tracksRunEnergy())
		{
			runtimeState.getLocation().setRunEnergyPercent(client.getEnergy() / 100);
		}

		if (requirements.tracksPoison())
		{
			runtimeState.getVars().setPoisonState(readPoisonState());
		}

		if (requirements.tracksSlayerTask())
		{
			int remaining = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
			runtimeState.getVars().setSlayerTaskRemaining(remaining);
			runtimeState.getVars().setSlayerTaskActive(remaining > 0);
		}

		if (!requirements.getPrayers().isEmpty())
		{
			for (Prayer prayer : requirements.getPrayers())
			{
				runtimeState.getVars().setPrayerActive(prayer, isPrayerActive(prayer));
			}
		}

		if (requirements.hasInventoryTracking())
		{
			refreshInventoryState(client.getItemContainer(InventoryID.INV));
		}

		if (requirements.hasEquipmentTracking())
		{
			refreshEquipmentState(client.getItemContainer(InventoryID.WORN));
		}

		if (requirements.hasGroundItemTracking())
		{
			refreshGroundItemsState();
		}

		if (requirements.tracksPlayerLocation())
		{
			runtimeState.getLocation().setPlayerLocation(readPlayerLocation());
		}

		if (requirements.tracksPlayerInstance())
		{
			runtimeState.getLocation().setInInstance(client.getTopLevelWorldView().isInstance());
		}

		log.debug("Runtime tracker refreshed");
	}

	private void refreshInventoryState(ItemContainer itemContainer)
	{
		runtimeState.getInventory().clearInventory();

		if (itemContainer == null)
		{
			return;
		}

		for (Item item : itemContainer.getItems())
		{
			if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			String itemName = resolveItemName(item.getId());

			if (!requirements.getInventoryItems().contains(itemName))
			{
				continue;
			}

			int currentCount = runtimeState.getInventory().getInventoryItemCount(itemName);
			runtimeState.getInventory().setInventoryItemCount(itemName, currentCount + item.getQuantity());
		}
	}

	private void refreshEquipmentState(ItemContainer itemContainer)
	{
		List<String> equippedItems = new ArrayList<>();

		if (itemContainer != null)
		{
			for (Item item : itemContainer.getItems())
			{
				if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
				{
					continue;
				}

				String itemName = resolveItemName(item.getId());

				if (requirements.getEquippedItems().contains(itemName))
				{
					equippedItems.add(itemName);
				}
			}
		}

		runtimeState.getInventory().setEquippedItems(equippedItems);
	}

	private void refreshGroundItemsState()
	{
		runtimeState.getGroundItems().clear();

		Tile[][][] tiles = client.getTopLevelWorldView().getScene().getTiles();

		if (tiles == null)
		{
			return;
		}

		for (Tile[][] planeTiles : tiles)
		{
			if (planeTiles == null)
			{
				continue;
			}

			for (Tile[] row : planeTiles)
			{
				if (row == null)
				{
					continue;
				}

				for (Tile tile : row)
				{
					if (tile == null)
					{
						continue;
					}

					for (TileItem item : tile.getGroundItems())
					{
						String itemName = resolveItemName(item.getId());

						if (requirements.getGroundItems().contains(itemName))
						{
							runtimeState.getGroundItems().incrementNearbyGroundItem(itemName);
						}
					}
				}
			}
		}
	}

	private WorldPoint readPlayerLocation()
	{
		return client.getLocalPlayer() == null ? null : client.getLocalPlayer().getWorldLocation();
	}

	private RuntimeState.PoisonState readPoisonState()
	{
		int poisonValue = client.getVarpValue(VarPlayerID.POISON);

		if (poisonValue >= 1000000)
		{
			return RuntimeState.PoisonState.VENOM;
		}

		if (poisonValue > 0)
		{
			return RuntimeState.PoisonState.POISON;
		}

		return RuntimeState.PoisonState.NONE;
	}

	private String resolveItemName(int itemId)
	{
		int canonicalId = itemManager.canonicalize(itemId);
		return InventoryRuntimeState.normalizeName(itemManager.getItemComposition(canonicalId).getName());
	}

	private boolean isPrayerActive(Prayer prayer)
	{
		return client.getVarbitValue(prayer.getVarbit()) > 0;
	}

	private boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}
}
