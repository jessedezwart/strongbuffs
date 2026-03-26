package nl.jessedezwart.strongbuffs.runtime.action;

import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Strategy interface for executing one type of action at runtime.
 *
 * <p>Each action definition class (overlay text, screen flash, sound alert, etc.) has a
 * corresponding handler that knows how to start, update, stop, and fire that action.
 * Handlers are registered in {@link RuntimeActionHandlerRegistry}.</p>
 *
 * <p>The persistent/transient split mirrors the rule engine's evaluation model:
 * <ul>
 *   <li><b>Persistent</b> actions stay active as long as the rule's conditions hold true
 *       (activated once, updated each tick, deactivated when conditions fail).</li>
 *   <li><b>Transient</b> actions fire once per trigger and expire on their own.</li>
 * </ul>
 *
 * @param <T> the concrete {@link ActionDefinition} subclass this handler supports
 */
public interface RuntimeActionHandler<T extends ActionDefinition>
{
	/** Returns the action definition class this handler supports. */
	Class<T> getActionType();

	/** Called once when the plugin starts. Override to allocate resources. */
	default void startUp()
	{
	}

	/** Called once when the plugin stops. Override to release resources. */
	default void shutDown()
	{
	}

	/** Removes all active state managed by this handler. */
	default void clear()
	{
	}

	/** Starts a persistent action for a rule whose conditions just became true. */
	void activatePersistent(CompiledRule rule, T action, RuntimeState runtimeState);

	/** Refreshes an already-active persistent action (e.g. to update a displayed value). */
	void updatePersistent(CompiledRule rule, T action, RuntimeState runtimeState);

	/** Stops a persistent action whose rule's conditions are no longer true. */
	void deactivatePersistent(CompiledRule rule, T action);

	/** Fires a one-shot action for a transient trigger. */
	void fireTransient(CompiledRule rule, T action, RuntimeState runtimeState);
}
