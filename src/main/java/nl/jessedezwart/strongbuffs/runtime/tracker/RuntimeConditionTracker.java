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
import nl.jessedezwart.strongbuffs.runtime.condition.RuleConditionRequirementCollector;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeTrackingPlan;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeTrackingPlanner;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.GroundItemStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.InventoryStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.LocationStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.SkillStateUpdater;
import nl.jessedezwart.strongbuffs.runtime.tracker.updater.VarStateUpdater;

/**
 * Event-driven tracker that maintains the cached runtime state used for rule
 * evaluation.
 *
 * <p>
 * The tracker owns RuneLite API reads and client-thread coordination. It
 * refreshes only the state slices requested by the current compiled rule set
 * and notifies listeners with coarse runtime triggers describing what changed.
 * </p>
 */
@Singleton
@Slf4j
public class RuntimeConditionTracker
{
	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;
	private final RuleConditionRequirementCollector requirementCollector;
	private final RuntimeTrackingPlanner runtimeRequirementPlanner;
	private final SkillStateUpdater skillStateUpdater;
	private final VarStateUpdater varStateUpdater;
	private final InventoryStateUpdater inventoryStateUpdater;
	private final GroundItemStateUpdater groundItemStateUpdater;
	private final LocationStateUpdater locationStateUpdater;

	@Getter
	private final RuntimeState runtimeState = new RuntimeState();
	private final List<RuntimeStateListener> listeners = new CopyOnWriteArrayList<>();

	private RuntimeStateWatchlist requirements = RuntimeStateWatchlist.empty();
	private RuntimeTrackingPlan requirementPlan = RuntimeTrackingPlan.empty();
	private boolean started;
	private boolean gameTickListenerRegistered;
	private boolean statListenerRegistered;
	private boolean varbitListenerRegistered;
	private boolean itemContainerListenerRegistered;
	private boolean groundItemListenerRegistered;

	private final GameTickListener gameTickListener = new GameTickListener();
	private final StatChangedListener statChangedListener = new StatChangedListener();
	private final VarbitChangedListener varbitChangedListener = new VarbitChangedListener();
	private final ItemContainerChangedListener itemContainerChangedListener = new ItemContainerChangedListener();
	private final GroundItemListener groundItemListener = new GroundItemListener();

	@Inject
	public RuntimeConditionTracker(Client client, ClientThread clientThread, EventBus eventBus,
			RuleConditionRequirementCollector requirementCollector, RuntimeTrackingPlanner runtimeRequirementPlanner,
			SkillStateUpdater skillStateUpdater, VarStateUpdater varStateUpdater,
			InventoryStateUpdater inventoryStateUpdater, GroundItemStateUpdater groundItemStateUpdater,
			LocationStateUpdater locationStateUpdater)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;
		this.requirementCollector = requirementCollector;
		this.runtimeRequirementPlanner = runtimeRequirementPlanner;
		this.skillStateUpdater = skillStateUpdater;
		this.varStateUpdater = varStateUpdater;
		this.inventoryStateUpdater = inventoryStateUpdater;
		this.groundItemStateUpdater = groundItemStateUpdater;
		this.locationStateUpdater = locationStateUpdater;
	}

	/**
	 * Starts subscribing to RuneLite events and performs an initial tracked-state
	 * refresh.
	 */
	public void startUp()
	{
		if (started)
		{
			return;
		}

		started = true;
		eventBus.register(this);
		syncDynamicListeners();
		refreshTrackedStateAsync();
	}

	/**
	 * Stops event subscriptions, clears cached state, and notifies listeners to
	 * reset.
	 */
	public void shutDown()
	{
		if (!started)
		{
			return;
		}

		unregisterDynamicListeners();
		eventBus.unregister(this);
		started = false;
		runtimeState.clear();
		notifyListeners(EnumSet.of(RuntimeTrigger.CLEAR));
	}

	/**
	 * Recomputes requirements from persisted rules and republishes them to the
	 * tracker.
	 */
	public void setRules(List<RuleDefinition> rules)
	{
		setRequirements(requirementCollector.fromRules(rules));
	}

	/**
	 * Replaces the active watchlist and refreshes cached state to match it.
	 */
	public void setRequirements(RuntimeStateWatchlist requirements)
	{
		setRequirementPlan(runtimeRequirementPlanner.plan(requirements));
	}

	/**
	 * Replaces the active runtime policy and refreshes cached state to match it.
	 */
	public void setRequirementPlan(RuntimeTrackingPlan requirementPlan)
	{
		this.requirementPlan = requirementPlan == null ? RuntimeTrackingPlan.empty() : requirementPlan;
		this.requirements = this.requirementPlan.getRequirements();
		syncDynamicListeners();
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

	private void onGameTick(GameTick event)
	{
		if (!isLoggedIn() || !requirementPlan.requiresSubscription(RuntimeSubscription.GAME_TICK))
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		notifyListeners(locationStateUpdater.onGameTick(runtimeState, requirements, client));
	}

	private void onStatChanged(StatChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		notifyListeners(skillStateUpdater.onStatChanged(runtimeState, requirements, event));
	}

	private void onVarbitChanged(VarbitChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		notifyListeners(varStateUpdater.onVarbitChanged(runtimeState, requirements, client, event));
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!isLoggedIn())
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		EnumSet<RuntimeTrigger> triggers = inventoryStateUpdater.onItemContainerChanged(runtimeState, requirements,
				event);

		if (requirements.tracksInventoryValue() || requirements.tracksBankValue())
		{
			triggers.addAll(inventoryStateUpdater.onItemContainerValueChanged(runtimeState, requirements, event));
		}

		notifyListeners(triggers);
	}

	private void onItemSpawned(ItemSpawned event)
	{
		if (!requirementPlan.requiresSubscription(RuntimeSubscription.GROUND_ITEM))
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		if (groundItemStateUpdater.onItemSpawned(runtimeState, requirements, event))
		{
			notifyListeners(EnumSet.of(RuntimeTrigger.GROUND_ITEMS));
		}
	}

	private void onItemDespawned(ItemDespawned event)
	{
		if (!requirementPlan.requiresSubscription(RuntimeSubscription.GROUND_ITEM))
		{
			return;
		}

		runtimeState.getSkills().setCurrentTick(client.getTickCount());
		if (groundItemStateUpdater.onItemDespawned(runtimeState, requirements, event))
		{
			notifyListeners(EnumSet.of(RuntimeTrigger.GROUND_ITEMS));
		}
	}

	private void syncDynamicListeners()
	{
		if (!started)
		{
			return;
		}

		gameTickListenerRegistered = syncListener(gameTickListener, gameTickListenerRegistered,
				requirementPlan.requiresSubscription(RuntimeSubscription.GAME_TICK));
		statListenerRegistered = syncListener(statChangedListener, statListenerRegistered,
				requirementPlan.requiresSubscription(RuntimeSubscription.STAT_CHANGED));
		varbitListenerRegistered = syncListener(varbitChangedListener, varbitListenerRegistered,
				requirementPlan.requiresSubscription(RuntimeSubscription.VARBIT_CHANGED));
		itemContainerListenerRegistered = syncListener(itemContainerChangedListener, itemContainerListenerRegistered,
				requirementPlan.requiresSubscription(RuntimeSubscription.ITEM_CONTAINER_CHANGED));
		groundItemListenerRegistered = syncListener(groundItemListener, groundItemListenerRegistered,
				requirementPlan.requiresSubscription(RuntimeSubscription.GROUND_ITEM));
	}

	private void unregisterDynamicListeners()
	{
		gameTickListenerRegistered = syncListener(gameTickListener, gameTickListenerRegistered, false);
		statListenerRegistered = syncListener(statChangedListener, statListenerRegistered, false);
		varbitListenerRegistered = syncListener(varbitChangedListener, varbitListenerRegistered, false);
		itemContainerListenerRegistered = syncListener(itemContainerChangedListener, itemContainerListenerRegistered,
				false);
		groundItemListenerRegistered = syncListener(groundItemListener, groundItemListenerRegistered, false);
	}

	private boolean syncListener(Object listener, boolean registered, boolean shouldBeRegistered)
	{
		if (shouldBeRegistered && !registered)
		{
			eventBus.register(listener);
			return true;
		}

		if (!shouldBeRegistered && registered)
		{
			eventBus.unregister(listener);
			return false;
		}

		return registered;
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
		inventoryStateUpdater.refreshValues(runtimeState, requirements, client);
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

	private final class GameTickListener
	{
		@Subscribe
		public void onGameTick(GameTick event)
		{
			RuntimeConditionTracker.this.onGameTick(event);
		}
	}

	private final class StatChangedListener
	{
		@Subscribe
		public void onStatChanged(StatChanged event)
		{
			RuntimeConditionTracker.this.onStatChanged(event);
		}
	}

	private final class VarbitChangedListener
	{
		@Subscribe
		public void onVarbitChanged(VarbitChanged event)
		{
			RuntimeConditionTracker.this.onVarbitChanged(event);
		}
	}

	private final class ItemContainerChangedListener
	{
		@Subscribe
		public void onItemContainerChanged(ItemContainerChanged event)
		{
			RuntimeConditionTracker.this.onItemContainerChanged(event);
		}
	}

	private final class GroundItemListener
	{
		@Subscribe
		public void onItemSpawned(ItemSpawned event)
		{
			RuntimeConditionTracker.this.onItemSpawned(event);
		}

		@Subscribe
		public void onItemDespawned(ItemDespawned event)
		{
			RuntimeConditionTracker.this.onItemDespawned(event);
		}
	}
}
