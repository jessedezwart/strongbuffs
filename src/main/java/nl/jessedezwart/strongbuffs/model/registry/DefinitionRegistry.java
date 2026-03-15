package nl.jessedezwart.strongbuffs.model.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;

/**
 * Central registry for persisted condition and action model types discovered
 * from explicit {@code META-INF/services} declarations.
 */
public final class DefinitionRegistry
{
	private static final List<Class<? extends ConditionDefinition>> CONDITION_DEFINITIONS = loadConditionDefinitions();

	private static final List<Class<? extends ActionDefinition>> ACTION_DEFINITIONS = loadActionDefinitions();

	private DefinitionRegistry()
	{
	}

	public static List<Class<? extends ConditionDefinition>> getConditionDefinitions()
	{
		return CONDITION_DEFINITIONS;
	}

	public static List<Class<? extends ActionDefinition>> getActionDefinitions()
	{
		return ACTION_DEFINITIONS;
	}

	private static List<Class<? extends ConditionDefinition>> loadConditionDefinitions()
	{
		Map<Class<? extends ConditionDefinition>, Class<? extends ConditionDefinition>> definitions = new LinkedHashMap<>();

		for (ConditionDefinition definition : ServiceLoader.load(ConditionDefinition.class))
		{
			Class<? extends ConditionDefinition> definitionClass = definition.getClass()
					.asSubclass(ConditionDefinition.class);
			definitions.put(definitionClass, definitionClass);
		}

		return toUnmodifiableList(definitions, "condition");
	}

	private static List<Class<? extends ActionDefinition>> loadActionDefinitions()
	{
		Map<Class<? extends ActionDefinition>, Class<? extends ActionDefinition>> definitions = new LinkedHashMap<>();

		for (ActionDefinition definition : ServiceLoader.load(ActionDefinition.class))
		{
			Class<? extends ActionDefinition> definitionClass = definition.getClass()
				.asSubclass(ActionDefinition.class);
			definitions.put(definitionClass, definitionClass);
		}

		return toUnmodifiableList(definitions, "action");
	}

	private static <T> List<Class<? extends T>> toUnmodifiableList(
			Map<Class<? extends T>, Class<? extends T>> definitions, String definitionType)
	{
		if (definitions.isEmpty())
		{
			throw new IllegalStateException("No approved " + definitionType + " definitions were registered.");
		}

		return Collections.unmodifiableList(definitions.values().stream().collect(Collectors.toList()));
	}
}
