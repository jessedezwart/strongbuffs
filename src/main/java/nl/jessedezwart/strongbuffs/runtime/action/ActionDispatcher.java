package nl.jessedezwart.strongbuffs.runtime.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayManager;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Top-level facade that the rule engine calls to activate, update, deactivate, or fire actions.
 *
 * <p>Owns the {@link RuntimeOverlay} lifecycle (add/remove from RuneLite's overlay manager) and
 * delegates per-action-type work to {@link RuntimeActionHandlerRegistry}.</p>
 */
@Singleton
public class ActionDispatcher
{
	private final OverlayManager overlayManager;
	private final RuntimeOverlay runtimeOverlay;
	private final RuntimeActionHandlerRegistry runtimeActionHandlerRegistry;

	@Inject
	public ActionDispatcher(OverlayManager overlayManager, RuntimeOverlay runtimeOverlay,
		RuntimeActionHandlerRegistry runtimeActionHandlerRegistry)
	{
		this.overlayManager = overlayManager;
		this.runtimeOverlay = runtimeOverlay;
		this.runtimeActionHandlerRegistry = runtimeActionHandlerRegistry;
	}

	/**
	 * Registers the overlay and initializes all action handlers. Called once on plugin start.
	 */
	public void startUp()
	{
		overlayManager.add(runtimeOverlay);
		runtimeActionHandlerRegistry.startUp();
		clearAll();
	}

	/**
	 * Tears down all active actions and removes the overlay. Called once on plugin stop.
	 */
	public void shutDown()
	{
		clearAll();
		overlayManager.remove(runtimeOverlay);
		runtimeActionHandlerRegistry.shutDown();
	}

	/**
	 * Starts a persistent action for a rule whose conditions just became true.
	 */
	public void activatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		runtimeActionHandlerRegistry.activatePersistent(rule, runtimeState);
	}

	/**
	 * Refreshes a persistent action that is already active (e.g. to update a displayed value).
	 */
	public void updatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		runtimeActionHandlerRegistry.updatePersistent(rule, runtimeState);
	}

	/**
	 * Stops a persistent action for a rule whose conditions are no longer true.
	 */
	public void deactivatePersistent(CompiledRule rule)
	{
		runtimeActionHandlerRegistry.deactivatePersistent(rule);
	}

	/**
	 * Fires a one-shot action (e.g. a sound or brief flash) for a transient rule trigger.
	 */
	public void fireTransient(CompiledRule rule, RuntimeState runtimeState)
	{
		runtimeActionHandlerRegistry.fireTransient(rule, runtimeState);
	}

	/**
	 * Removes all active actions across every handler. Used on startup, shutdown, and rule recompilation.
	 */
	public void clearAll()
	{
		runtimeActionHandlerRegistry.clear();
	}
}
