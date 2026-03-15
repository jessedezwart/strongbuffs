package nl.jessedezwart.strongbuffs.model.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.impl.GroundItemCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemCountCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemEquippedCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemInInventoryCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInInstanceCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInZoneCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PoisonCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerActiveCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.RunEnergyCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SkillLevelCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SlayerTaskCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.XpGainCondition;

@Singleton
public class DefaultDefinitionCatalog implements DefinitionCatalog
{
	private final List<DefinitionRegistration<? extends ConditionDefinition>> conditionDefinitions =
		createConditionDefinitions();
	private final List<DefinitionRegistration<? extends ActionDefinition>> actionDefinitions =
		createActionDefinitions();
	private final Map<Class<? extends ConditionDefinition>, DefinitionRegistration<? extends ConditionDefinition>>
		conditionDefinitionsByClass = indexByClass(conditionDefinitions);
	private final Map<Class<? extends ActionDefinition>, DefinitionRegistration<? extends ActionDefinition>>
		actionDefinitionsByClass = indexByClass(actionDefinitions);
	private final Map<String, DefinitionRegistration<? extends ConditionDefinition>> conditionDefinitionsByType =
		indexConditionTypeId(conditionDefinitions);
	private final Map<String, DefinitionRegistration<? extends ActionDefinition>> actionDefinitionsByType =
		indexActionTypeId(actionDefinitions);

	@Override
	public List<Class<? extends ConditionDefinition>> getConditionDefinitions()
	{
		return toClassList(conditionDefinitions);
	}

	@Override
	public List<Class<? extends ActionDefinition>> getActionDefinitions()
	{
		return toClassList(actionDefinitions);
	}

	@Override
	public ConditionDefinition getConditionMetadata(Class<? extends ConditionDefinition> definitionClass)
	{
		return requireRegistration(conditionDefinitionsByClass, definitionClass, "condition").getMetadata();
	}

	@Override
	public ActionDefinition getActionMetadata(Class<? extends ActionDefinition> definitionClass)
	{
		return requireRegistration(actionDefinitionsByClass, definitionClass, "action").getMetadata();
	}

	@Override
	public Class<? extends ConditionDefinition> getConditionDefinitionClass(String typeId)
	{
		return requireRegistration(conditionDefinitionsByType, typeId, "condition").getDefinitionClass();
	}

	@Override
	public Class<? extends ActionDefinition> getActionDefinitionClass(String typeId)
	{
		return requireRegistration(actionDefinitionsByType, typeId, "action").getDefinitionClass();
	}

	@Override
	public <T extends ConditionDefinition> T createCondition(Class<T> definitionClass)
	{
		return definitionClass.cast(requireRegistration(conditionDefinitionsByClass, definitionClass, "condition")
			.create());
	}

	@Override
	public <T extends ActionDefinition> T createAction(Class<T> definitionClass)
	{
		return definitionClass.cast(requireRegistration(actionDefinitionsByClass, definitionClass, "action")
			.create());
	}

	private static List<DefinitionRegistration<? extends ConditionDefinition>> createConditionDefinitions()
	{
		List<DefinitionRegistration<? extends ConditionDefinition>> definitions = new ArrayList<>();
		definitions.add(new DefinitionRegistration<>(HpCondition.class, new HpCondition(), HpCondition::new));
		definitions.add(new DefinitionRegistration<>(PrayerActiveCondition.class, new PrayerActiveCondition(),
			PrayerActiveCondition::new));
		definitions.add(new DefinitionRegistration<>(PrayerPointsCondition.class, new PrayerPointsCondition(),
			PrayerPointsCondition::new));
		definitions.add(new DefinitionRegistration<>(RunEnergyCondition.class, new RunEnergyCondition(),
			RunEnergyCondition::new));
		definitions.add(new DefinitionRegistration<>(PoisonCondition.class, new PoisonCondition(), PoisonCondition::new));
		definitions.add(new DefinitionRegistration<>(SlayerTaskCondition.class, new SlayerTaskCondition(),
			SlayerTaskCondition::new));
		definitions.add(new DefinitionRegistration<>(SkillLevelCondition.class, new SkillLevelCondition(),
			SkillLevelCondition::new));
		definitions.add(new DefinitionRegistration<>(XpGainCondition.class, new XpGainCondition(), XpGainCondition::new));
		definitions.add(new DefinitionRegistration<>(ItemInInventoryCondition.class, new ItemInInventoryCondition(),
			ItemInInventoryCondition::new));
		definitions.add(new DefinitionRegistration<>(ItemCountCondition.class, new ItemCountCondition(),
			ItemCountCondition::new));
		definitions.add(new DefinitionRegistration<>(ItemEquippedCondition.class, new ItemEquippedCondition(),
			ItemEquippedCondition::new));
		definitions.add(new DefinitionRegistration<>(GroundItemCondition.class, new GroundItemCondition(),
			GroundItemCondition::new));
		definitions.add(new DefinitionRegistration<>(PlayerInZoneCondition.class, new PlayerInZoneCondition(),
			PlayerInZoneCondition::new));
		definitions.add(new DefinitionRegistration<>(PlayerInInstanceCondition.class, new PlayerInInstanceCondition(),
			PlayerInInstanceCondition::new));
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
