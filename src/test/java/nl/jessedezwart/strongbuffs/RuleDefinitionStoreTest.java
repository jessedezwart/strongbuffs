package nl.jessedezwart.strongbuffs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemInInventoryCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionCatalog;
import org.junit.Test;

public class RuleDefinitionStoreTest
{
	private final RuleDefinitionStore store = new RuleDefinitionStore(null);
	private final DefinitionCatalog definitionCatalog = new DefinitionCatalog();

	@Test
	public void serializeAndDeserializeRoundTripsTypedDefinitions()
	{
		RuleDefinition firstRule = new RuleDefinition();
		firstRule.setSchemaVersion(0);
		firstRule.setId("low-hp");
		firstRule.setName("Low HP");
		firstRule.setEnabled(true);
		firstRule.setActivationMode(ActivationMode.WHILE_ACTIVE);
		firstRule.setCooldownTicks(4);
		firstRule.setRootGroup(createConditionTree());

		ScreenFlashAction screenFlashAction = new ScreenFlashAction();
		screenFlashAction.setColorHex("#FF2200");
		screenFlashAction.setDurationTicks(2);
		firstRule.setAction(screenFlashAction);

		RuleDefinition secondRule = new RuleDefinition();
		secondRule.setId("spec-ready");
		secondRule.setName("Spec Ready");
		secondRule.setEnabled(false);
		secondRule.setActivationMode(ActivationMode.ON_ENTER);

		ConditionGroup secondGroup = new ConditionGroup();
		SpecialAttackCondition specCondition = new SpecialAttackCondition();
		specCondition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		specCondition.setThreshold(100);
		secondGroup.getChildren().add(specCondition);
		secondRule.setRootGroup(secondGroup);

		SoundAlertAction soundAlertAction = new SoundAlertAction();
		soundAlertAction.setSoundKey("ding");
		soundAlertAction.setVolumePercent(65);
		secondRule.setAction(soundAlertAction);

		String serialized = store.serialize(Arrays.asList(firstRule, secondRule));
		List<RuleDefinition> restored = store.deserialize(serialized);

		assertTrue(serialized.contains("\"type\":\"group\""));
		assertTrue(serialized.contains("\"type\":\"hp\""));
		assertTrue(serialized.contains("\"type\":\"prayer_points\""));
		assertTrue(serialized.contains("\"type\":\"spec\""));
		assertTrue(serialized.contains("\"type\":\"screen_flash\""));
		assertTrue(serialized.contains("\"type\":\"sound_alert\""));
		assertEquals(2, restored.size());

		RuleDefinition restoredFirst = restored.get(0);
		assertEquals(RuleDefinitionStore.CURRENT_SCHEMA_VERSION, restoredFirst.getSchemaVersion());
		assertEquals("low-hp", restoredFirst.getId());
		assertEquals("Low HP", restoredFirst.getName());
		assertEquals(ActivationMode.WHILE_ACTIVE, restoredFirst.getActivationMode());
		assertEquals(4, restoredFirst.getCooldownTicks());
		assertTrue(restoredFirst.getAction() instanceof ScreenFlashAction);
		assertTrue(restoredFirst.getRootGroup().getChildren().get(0) instanceof HpCondition);
		assertTrue(restoredFirst.getRootGroup().getChildren().get(1) instanceof ConditionGroup);

		ConditionGroup nestedGroup = (ConditionGroup) restoredFirst.getRootGroup().getChildren().get(1);
		assertEquals(ConditionLogic.OR, nestedGroup.getLogic());
		assertTrue(nestedGroup.getChildren().get(0) instanceof PrayerPointsCondition);

		RuleDefinition restoredSecond = restored.get(1);
		assertFalse(restoredSecond.isEnabled());
		assertEquals(ActivationMode.ON_ENTER, restoredSecond.getActivationMode());
		assertTrue(restoredSecond.getAction() instanceof SoundAlertAction);
		assertTrue(restoredSecond.getRootGroup().getChildren().get(0) instanceof SpecialAttackCondition);
	}

	@Test
	public void deserializeSkipsInvalidRuleDefinitions()
	{
		String serialized = "[" +
			"{\"schemaVersion\":1,\"id\":\"broken\",\"name\":\"Broken\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[{\"type\":\"unknown\"}]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"action\":{\"type\":\"overlay_text\",\"text\":\"warn\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}," +
			"{\"schemaVersion\":1,\"id\":\"valid\",\"name\":\"Valid\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"action\":{\"type\":\"overlay_text\",\"text\":\"ok\",\"colorHex\":\"#00FF00\",\"showValue\":false}}" +
			"]";

		List<RuleDefinition> restored = store.deserialize(serialized);

		assertEquals(1, restored.size());
		assertEquals("valid", restored.get(0).getId());
		assertTrue(restored.get(0).getAction() instanceof OverlayTextAction);
	}

	@Test
	public void deserializeReturnsEmptyListForBlankInput()
	{
		assertTrue(store.deserialize(null).isEmpty());
		assertTrue(store.deserialize("").isEmpty());
		assertTrue(store.deserialize("   ").isEmpty());
	}

	@Test
	public void serializeAndDeserializeSupportsAllRegisteredConditionDefinitions()
	{
		RuleDefinition rule = new RuleDefinition();
		rule.setId("all-conditions");
		rule.setName("All Conditions");

		ConditionGroup rootGroup = new ConditionGroup();

		for (Class<? extends ConditionDefinition> conditionClass : definitionCatalog.getConditionDefinitions())
		{
			ConditionDefinition definition = definitionCatalog.createCondition(conditionClass);

			if (definition instanceof ItemInInventoryCondition)
			{
				((ItemInInventoryCondition) definition).setItemName("Shark");
			}

			rootGroup.getChildren().add(definition);
		}

		rule.setRootGroup(rootGroup);
		rule.setAction(new OverlayTextAction());

		String serialized = store.serialize(List.of(rule));
		List<RuleDefinition> restored = store.deserialize(serialized);

		assertEquals(1, restored.size());
		assertEquals(
			definitionCatalog.getConditionDefinitions().stream().map(Class::getName).collect(Collectors.toList()),
			restored.get(0).getRootGroup().getChildren().stream().map(child -> child.getClass().getName())
				.collect(Collectors.toList()));
	}

	@Test
	public void serializeSkipsNullEntriesAndUpgradesSchemaVersion()
	{
		RuleDefinition rule = new RuleDefinition();
		rule.setSchemaVersion(0);
		rule.setId("only-rule");
		rule.setName("Only Rule");
		rule.setAction(new OverlayTextAction());

		String serialized = store.serialize(Arrays.asList(null, rule));
		List<RuleDefinition> restored = store.deserialize(serialized);

		assertEquals(0, rule.getSchemaVersion());
		assertFalse(serialized.contains("null"));
		assertEquals(1, restored.size());
		assertEquals(RuleDefinitionStore.CURRENT_SCHEMA_VERSION, restored.get(0).getSchemaVersion());
		assertEquals("only-rule", restored.get(0).getId());
	}

	@Test
	public void deserializeReturnsEmptyListForMalformedOrNonArrayJson()
	{
		assertTrue(store.deserialize("{").isEmpty());
		assertTrue(store.deserialize("{\"id\":\"not-an-array\"}").isEmpty());
	}

	@Test
	public void deserializeSkipsFutureSchemaVersions()
	{
		String serialized = "[" +
			"{\"schemaVersion\":99,\"id\":\"future\",\"name\":\"Future\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"action\":{\"type\":\"overlay_text\",\"text\":\"future\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}" +
			"]";

		assertTrue(store.deserialize(serialized).isEmpty());
	}

	@Test
	public void deserializeSkipsRulesMissingRequiredFields()
	{
		String missingAction = "[" +
			"{\"schemaVersion\":1,\"id\":\"missing-action\",\"name\":\"Missing Action\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0}" +
			"]";
		String missingRootGroup = "[" +
			"{\"schemaVersion\":1,\"id\":\"missing-root\",\"name\":\"Missing Root\",\"enabled\":true," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"action\":{\"type\":\"overlay_text\",\"text\":\"warn\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}" +
			"]";
		String legacyDisplayOnly = "[" +
			"{\"schemaVersion\":1,\"id\":\"legacy-display\",\"name\":\"Legacy Display\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"display\":{\"type\":\"overlay_text\",\"text\":\"warn\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}" +
			"]";

		assertTrue(store.deserialize(missingAction).isEmpty());
		assertTrue(store.deserialize(missingRootGroup).isEmpty());
		assertTrue(store.deserialize(legacyDisplayOnly).isEmpty());
	}

	@Test
	public void deserializeMigratesNonPositiveSchemaVersionToCurrent()
	{
		String serialized = "[" +
			"{\"schemaVersion\":0,\"id\":\"legacy\",\"name\":\"Legacy\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"action\":{\"type\":\"overlay_text\",\"text\":\"legacy\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}" +
			"]";

		List<RuleDefinition> restored = store.deserialize(serialized);

		assertEquals(1, restored.size());
		assertEquals(RuleDefinitionStore.CURRENT_SCHEMA_VERSION, restored.get(0).getSchemaVersion());
	}

	private static ConditionGroup createConditionTree()
	{
		ConditionGroup rootGroup = new ConditionGroup();
		rootGroup.setLogic(ConditionLogic.AND);

		HpCondition hpCondition = new HpCondition();
		hpCondition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		hpCondition.setThreshold(30);
		rootGroup.getChildren().add(hpCondition);

		ConditionGroup nestedGroup = new ConditionGroup();
		nestedGroup.setLogic(ConditionLogic.OR);

		PrayerPointsCondition prayerPointsCondition = new PrayerPointsCondition();
		prayerPointsCondition.setOperator(ComparisonOperator.LESS_THAN);
		prayerPointsCondition.setThreshold(10);
		nestedGroup.getChildren().add(prayerPointsCondition);

		rootGroup.getChildren().add(nestedGroup);
		return rootGroup;
	}
}
