package nl.jessedezwart.strongbuffs.model.registry;

import java.util.List;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;

/**
 * Registry interface for all approved persisted condition and action types.
 *
 * <p>The editor, persistence layer, and runtime tests all depend on this catalog to resolve type
 * ids, fetch metadata, and create new default instances.</p>
 */
public interface DefinitionCatalog
{
	List<Class<? extends ConditionDefinition>> getConditionDefinitions();

	List<Class<? extends ActionDefinition>> getActionDefinitions();

	ConditionDefinition getConditionMetadata(Class<? extends ConditionDefinition> definitionClass);

	ActionDefinition getActionMetadata(Class<? extends ActionDefinition> definitionClass);

	Class<? extends ConditionDefinition> getConditionDefinitionClass(String typeId);

	Class<? extends ActionDefinition> getActionDefinitionClass(String typeId);

	<T extends ConditionDefinition> T createCondition(Class<T> definitionClass);

	<T extends ActionDefinition> T createAction(Class<T> definitionClass);
}
