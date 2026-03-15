package nl.jessedezwart.strongbuffs.runtime.tracker;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirementCollector;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.GroundItemStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.InventoryStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.LocationStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.SkillStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.VarStateUpdater;

@Singleton
@Slf4j
public class RuntimeConditionTracker
{
	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;
	private final RuntimeConditionRequirementCollector requirementCollector;
	private final SkillStateUpdater skillStateUpdater;
	private final VarStateUpdater varStateUpdater;
	private final InventoryStateUpdater inventoryStateUpdater;
	private final GroundItemStateUpdater groundItemStateUpdater;
	private final LocationStateUpdater locationStateUpdater;

	@Getter
	private final RuntimeState runtimeState = new RuntimeState();
	private final List<RuntimeStateListener> listeners = new CopyOnWriteArrayList<>();

	private RuntimeConditionRequirements requirements = RuntimeConditionRequirements.empty();
	private boolean started;

	@Inject
	public RuntimeConditionTracker(Client client, ClientThread clientThread, EventBus eventBus,
		RuntimeConditionRequirementCollector requirementCollector, SkillStateUpdater skillStateUpdater,
		VarStateUpdater varStateUpdater, InventoryStateUpdater inventoryStateUpdater,
		GroundItemStateUpdater groundItemStateUpdater, LocationStateUpdater locationStateUpdater)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;
		this.requirementCollector = requirementCollector;
		this.skillStateUpdater = skillStateUpdater;
		this.varStateUpdater = varStateUpdater;
		this.inventoryStateUpdater = inventoryStateUpdater;
		this.groundItemStateUpdater = groundItemStateUpdater;
		this.locationStateUpdater = locationStateUpdater;
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
		notifyListeners(EnumSet.of(RuntimeTrigger.CLEAR));
	}

	public void setRules(List<RuleDefinition> rules)
	{
		setRequirements(requirementCollector.fromRules(rules));
	}

	public void setRequirements(RuntimeConditionRequirements requirements)
	{
		this.requirements = requirements == null ? RuntimeConditionRequirements.empty() : requirements;
		runtimeState.clear();
		notifyListeners(EnumSet.of(RuntimeTrigger.CLEAR));
		refreshTrackedStateAsync();
	}

	public void addListener(RuntimeStateListener listener)
	{
		if (listener != null)
		{
			listeners.add(listener);
		}
	}

	public void removeListener(RuntimeStateListener listener)
	{
		listeners.remove(listener);
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
		notifyListeners(EnumSet.of(RuntimeTrigger.CLEAR));
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isLoggedIn() || !requirements.needsGameTick())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		notifyListeners(locationStateUpdater.onGameTick(runtimeState, requirements, client));
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		notifyListeners(skillStateUpdater.onStatChanged(runtimeState, requirements, event));
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		notifyListeners(varStateUpdater.onVarbitChanged(runtimeState, requirements, client, event));
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		notifyListeners(inventoryStateUpdater.onItemContainerChanged(runtimeState, requirements, event));
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if (requirements.hasGroundItemTracking())
		{
			runtimeState.getSkills().setCurrentTick(client.getTickCount());
			if (groundItemStateUpdater.onItemSpawned(runtimeState, requirements, event))
			{
				notifyListeners(EnumSet.of(RuntimeTrigger.GROUND_ITEMS));
			}
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		if (requirements.hasGroundItemTracking())
		{
			runtimeState.getSkills().setCurrentTick(client.getTickCount());
			if (groundItemStateUpdater.onItemDespawned(runtimeState, requirements, event))
			{
				notifyListeners(EnumSet.of(RuntimeTrigger.GROUND_ITEMS));
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
		skillStateUpdater.refresh(runtimeState, requirements, client);
		varStateUpdater.refresh(runtimeState, requirements, client);
		inventoryStateUpdater.refresh(runtimeState, requirements, client);
		groundItemStateUpdater.refresh(runtimeState, requirements, client);
		locationStateUpdater.refresh(runtimeState, requirements, client);

		log.debug("Runtime tracker refreshed");
		notifyListeners(EnumSet.of(RuntimeTrigger.FULL_REFRESH));
	}

	private void notifyListeners(Set<RuntimeTrigger> triggers)
	{
		if (triggers == null || triggers.isEmpty())
		{
			return;
		}

		Set<RuntimeTrigger> immutableTriggers = Collections.unmodifiableSet(EnumSet.copyOf(triggers));

		for (RuntimeStateListener listener : listeners)
		{
			listener.onRuntimeStateChanged(immutableTriggers, runtimeState);
		}
	}

	private boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}
}
