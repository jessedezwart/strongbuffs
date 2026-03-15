package nl.jessedezwart.strongbuffs.model.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;

/**
 * Central registry for approved persisted condition and action model types.
 */
public final class DefinitionRegistry
{
	private static final List<DefinitionRegistration<? extends ConditionDefinition>> CONDITION_DEFINITIONS =
		createConditionDefinitions();
	private static final List<DefinitionRegistration<? extends ActionDefinition>> ACTION_DEFINITIONS =
		createActionDefinitions();
	private static final Map<Class<? extends ConditionDefinition>, DefinitionRegistration<? extends ConditionDefinition>>
		CONDITION_DEFINITIONS_BY_CLASS = indexByClass(CONDITION_DEFINITIONS);
	private static final Map<Class<? extends ActionDefinition>, DefinitionRegistration<? extends ActionDefinition>>
		ACTION_DEFINITIONS_BY_CLASS = indexByClass(ACTION_DEFINITIONS);
	private static final Map<String, DefinitionRegistration<? extends ConditionDefinition>> CONDITION_DEFINITIONS_BY_TYPE =
		indexConditionTypeId(CONDITION_DEFINITIONS);
	private static final Map<String, DefinitionRegistration<? extends ActionDefinition>> ACTION_DEFINITIONS_BY_TYPE =
		indexActionTypeId(ACTION_DEFINITIONS);

	private DefinitionRegistry()
	{
	}

	public static List<Class<? extends ConditionDefinition>> getConditionDefinitions()
	{
		return toClassList(CONDITION_DEFINITIONS);
	}

	public static List<Class<? extends ActionDefinition>> getActionDefinitions()
	{
		return toClassList(ACTION_DEFINITIONS);
	}

	public static ConditionDefinition getConditionMetadata(Class<? extends ConditionDefinition> definitionClass)
	{
		return requireRegistration(CONDITION_DEFINITIONS_BY_CLASS, definitionClass, "condition").getMetadata();
	}

	public static ActionDefinition getActionMetadata(Class<? extends ActionDefinition> definitionClass)
	{
		return requireRegistration(ACTION_DEFINITIONS_BY_CLASS, definitionClass, "action").getMetadata();
	}

	public static Class<? extends ConditionDefinition> getConditionDefinitionClass(String typeId)
	{
		return requireRegistration(CONDITION_DEFINITIONS_BY_TYPE, typeId, "condition").getDefinitionClass();
	}

	public static Class<? extends ActionDefinition> getActionDefinitionClass(String typeId)
	{
		return requireRegistration(ACTION_DEFINITIONS_BY_TYPE, typeId, "action").getDefinitionClass();
	}

	public static <T extends ConditionDefinition> T createCondition(Class<T> definitionClass)
	{
		return definitionClass.cast(requireRegistration(CONDITION_DEFINITIONS_BY_CLASS, definitionClass, "condition")
			.create());
	}

	public static <T extends ActionDefinition> T createAction(Class<T> definitionClass)
	{
		return definitionClass.cast(requireRegistration(ACTION_DEFINITIONS_BY_CLASS, definitionClass, "action")
			.create());
	}

	private static List<DefinitionRegistration<? extends ConditionDefinition>> createConditionDefinitions()
	{
		List<DefinitionRegistration<? extends ConditionDefinition>> definitions = new ArrayList<>();
		definitions.add(new DefinitionRegistration<>(HpCondition.class, new HpCondition(), HpCondition::new));
		definitions.add(new DefinitionRegistration<>(PrayerPointsCondition.class, new PrayerPointsCondition(),
			PrayerPointsCondition::new));
		definitions.add(new DefinitionRegistration<>(SpecialAttackCondition.class, new SpecialAttackCondition(),
			SpecialAttackCondition::new));
		return Collections.unmodifiableList(definitions);
	}

	private static List<DefinitionRegistration<? extends ActionDefinition>> createActionDefinitions()
	{
		List<DefinitionRegistration<? extends ActionDefinition>> definitions = new ArrayList<>();
		definitions.add(new DefinitionRegistration<>(OverlayTextAction.class, new OverlayTextAction(),
			OverlayTextAction::new));
		definitions.add(new DefinitionRegistration<>(ScreenFlashAction.class, new ScreenFlashAction(),
			ScreenFlashAction::new));
		definitions.add(new DefinitionRegistration<>(SoundAlertAction.class, new SoundAlertAction(),
			SoundAlertAction::new));
		return Collections.unmodifiableList(definitions);
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

	private static Map<String, DefinitionRegistration<? extends ConditionDefinition>> indexConditionTypeId(
		List<DefinitionRegistration<? extends ConditionDefinition>> definitions)
	{
		Map<String, DefinitionRegistration<? extends ConditionDefinition>> indexed = new LinkedHashMap<>();

		for (DefinitionRegistration<? extends ConditionDefinition> definition : definitions)
		{
			indexed.put(definition.getMetadata().getTypeId(), definition);
		}

		return Collections.unmodifiableMap(indexed);
	}

	private static Map<String, DefinitionRegistration<? extends ActionDefinition>> indexActionTypeId(
		List<DefinitionRegistration<? extends ActionDefinition>> definitions)
	{
		Map<String, DefinitionRegistration<? extends ActionDefinition>> indexed = new LinkedHashMap<>();

		for (DefinitionRegistration<? extends ActionDefinition> definition : definitions)
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

	private static final class DefinitionRegistration<T>
	{
		private final Class<? extends T> definitionClass;
		private final T metadata;
		private final Supplier<? extends T> factory;

		private DefinitionRegistration(Class<? extends T> definitionClass, T metadata, Supplier<? extends T> factory)
		{
			this.definitionClass = definitionClass;
			this.metadata = metadata;
			this.factory = factory;
		}

		private Class<? extends T> getDefinitionClass()
		{
			return definitionClass;
		}

		private T getMetadata()
		{
			return metadata;
		}

		private T create()
		{
			return factory.get();
		}
	}
}
