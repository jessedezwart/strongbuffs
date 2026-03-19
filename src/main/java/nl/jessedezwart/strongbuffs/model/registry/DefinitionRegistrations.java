package nl.jessedezwart.strongbuffs.model.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import lombok.experimental.UtilityClass;

/**
 * Declares every supported condition and action type.
 *
 * <p>Add new condition or action types here. Insertion order defines the display order in the
 * editor dropdown.</p>
 */
@UtilityClass
class DefinitionRegistrations
{
	List<DefinitionRegistration<? extends ConditionDefinition>> conditions()
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

	List<DefinitionRegistration<? extends ActionDefinition>> actions()
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
}
