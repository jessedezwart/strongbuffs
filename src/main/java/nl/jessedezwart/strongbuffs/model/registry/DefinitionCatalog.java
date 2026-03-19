package nl.jessedezwart.strongbuffs.model.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.Definition;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;

/**
 * Registry of every supported condition and action type.
 *
 * <p>This is the single source of truth that connects three concerns for each definition type:</p>
 * <ul>
 *   <li><b>Class</b> — the concrete Java type (e.g. {@code HpCondition.class})</li>
 *   <li><b>Metadata</b> — a prototype instance used by the editor for labels, fields, and descriptions</li>
 *   <li><b>Factory</b> — a supplier that creates fresh instances for new rule drafts</li>
 * </ul>
 *
 * <p>Lookups are pre-indexed at construction time by class and by type ID (the JSON discriminator
 * string, e.g. "hp", "overlay_text"), so both Gson deserialization and editor
 * creation resolve through the same registrations.</p>
 *
 * @see DefinitionRegistrations for the list of registered types
 */
@Singleton
public class DefinitionCatalog
{
	// Master lists — insertion order defines the display order in the editor dropdown
	private final List<DefinitionRegistration<? extends ConditionDefinition>> conditionDefinitions =
		DefinitionRegistrations.conditions();
	private final List<DefinitionRegistration<? extends ActionDefinition>> actionDefinitions =
		DefinitionRegistrations.actions();

	// Class → registration lookups, used by createCondition/createAction and getMetadata
	private final Map<Class<? extends ConditionDefinition>, DefinitionRegistration<? extends ConditionDefinition>>
		conditionDefinitionsByClass = indexByClass(conditionDefinitions);
	private final Map<Class<? extends ActionDefinition>, DefinitionRegistration<? extends ActionDefinition>>
		actionDefinitionsByClass = indexByClass(actionDefinitions);

	// Type ID → registration lookups, used by Gson adapters to resolve JSON "type" → class
	private final Map<String, DefinitionRegistration<? extends ConditionDefinition>> conditionDefinitionsByType =
		indexTypeId(conditionDefinitions);
	private final Map<String, DefinitionRegistration<? extends ActionDefinition>> actionDefinitionsByType =
		indexTypeId(actionDefinitions);

	public List<Class<? extends ConditionDefinition>> getConditionDefinitions()
	{
		return toClassList(conditionDefinitions);
	}

	public List<Class<? extends ActionDefinition>> getActionDefinitions()
	{
		return toClassList(actionDefinitions);
	}

	public ConditionDefinition getConditionMetadata(Class<? extends ConditionDefinition> definitionClass)
	{
		return requireRegistration(conditionDefinitionsByClass, definitionClass, "condition").getMetadata();
	}

	public ActionDefinition getActionMetadata(Class<? extends ActionDefinition> definitionClass)
	{
		return requireRegistration(actionDefinitionsByClass, definitionClass, "action").getMetadata();
	}

	public Class<? extends ConditionDefinition> getConditionDefinitionClass(String typeId)
	{
		return requireRegistration(conditionDefinitionsByType, typeId, "condition").getDefinitionClass();
	}

	public Class<? extends ActionDefinition> getActionDefinitionClass(String typeId)
	{
		return requireRegistration(actionDefinitionsByType, typeId, "action").getDefinitionClass();
	}

	public <T extends ConditionDefinition> T createCondition(Class<T> definitionClass)
	{
		return definitionClass.cast(requireRegistration(conditionDefinitionsByClass, definitionClass, "condition")
			.create());
	}

	public <T extends ActionDefinition> T createAction(Class<T> definitionClass)
	{
		return definitionClass.cast(requireRegistration(actionDefinitionsByClass, definitionClass, "action")
			.create());
	}

	private static <T> Map<Class<? extends T>, DefinitionRegistration<? extends T>> indexByClass(
		List<DefinitionRegistration<? extends T>> definitions)
	{
		Map<Class<? extends T>, DefinitionRegistration<? extends T>> indexed = new LinkedHashMap<>();

		for (DefinitionRegistration<? extends T> definition : definitions)
		{
			indexed.put(definition.getDefinitionClass(), definition);
		}

		return Collections.unmodifiableMap(indexed);
	}

	private static <T extends Definition<?>> Map<String, DefinitionRegistration<? extends T>> indexTypeId(
		List<DefinitionRegistration<? extends T>> definitions)
	{
		Map<String, DefinitionRegistration<? extends T>> indexed = new LinkedHashMap<>();

		for (DefinitionRegistration<? extends T> definition : definitions)
		{
			indexed.put(definition.getMetadata().getTypeId(), definition);
		}

		return Collections.unmodifiableMap(indexed);
	}

	private static <T> List<Class<? extends T>> toClassList(List<DefinitionRegistration<? extends T>> definitions)
	{
		List<Class<? extends T>> classes = new ArrayList<>();

		for (DefinitionRegistration<? extends T> definition : definitions)
		{
			classes.add(definition.getDefinitionClass());
		}

		return Collections.unmodifiableList(classes);
	}

	private static <T, K> DefinitionRegistration<? extends T> requireRegistration(
		Map<K, DefinitionRegistration<? extends T>> definitions, K key, String definitionType)
	{
		DefinitionRegistration<? extends T> registration = definitions.get(key);

		if (registration == null)
		{
			throw new IllegalArgumentException("Unsupported " + definitionType + " definition: " + key);
		}

		return registration;
	}
}
