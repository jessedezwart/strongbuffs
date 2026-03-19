package nl.jessedezwart.strongbuffs.runtime.condition;

import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Runtime strategy for one persisted condition type.
 */
public interface ConditionRuntimeHandler<T extends ConditionDefinition>
{
	Class<T> getConditionType();

	boolean matches(T condition, RuntimeState runtimeState);

	void contributeRequirements(T condition, RuntimeConditionRequirements.Builder builder);

	String formatValue(T condition, RuntimeState runtimeState);
}
