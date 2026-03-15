package nl.jessedezwart.strongbuffs.runtime.action;

import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public interface RuntimeActionHandler<T extends ActionDefinition>
{
	Class<T> getActionType();

	default void startUp()
	{
	}

	default void shutDown()
	{
	}

	default void clear()
	{
	}

	void activatePersistent(CompiledRule rule, T action, RuntimeState runtimeState);

	void updatePersistent(CompiledRule rule, T action, RuntimeState runtimeState);

	void deactivatePersistent(CompiledRule rule, T action);

	void fireTransient(CompiledRule rule, T action, RuntimeState runtimeState);
}
