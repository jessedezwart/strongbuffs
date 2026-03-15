package nl.jessedezwart.strongbuffs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import nl.jessedezwart.strongbuffs.model.ActivationMode;
import nl.jessedezwart.strongbuffs.model.AuraDefinition;
import nl.jessedezwart.strongbuffs.model.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.SpecCondition;
import nl.jessedezwart.strongbuffs.model.display.OverlayTextDisplay;
import nl.jessedezwart.strongbuffs.model.display.ScreenFlashDisplay;
import nl.jessedezwart.strongbuffs.model.display.SoundAlertDisplay;
import org.junit.Test;

public class AuraDefinitionStoreTest
{
	private final AuraDefinitionStore store = new AuraDefinitionStore(null);

	@Test
	public void serializeAndDeserializeRoundTripsTypedDefinitions()
	{
		AuraDefinition firstAura = new AuraDefinition();
		firstAura.setSchemaVersion(0);
		firstAura.setId("low-hp");
		firstAura.setName("Low HP");
		firstAura.setEnabled(true);
		firstAura.setActivationMode(ActivationMode.WHILE_ACTIVE);
		firstAura.setCooldownTicks(4);
		firstAura.setRootGroup(createConditionTree());

		ScreenFlashDisplay screenFlashDisplay = new ScreenFlashDisplay();
		screenFlashDisplay.setColorHex("#FF2200");
		screenFlashDisplay.setDurationTicks(2);
		firstAura.setDisplay(screenFlashDisplay);

		AuraDefinition secondAura = new AuraDefinition();
		secondAura.setId("spec-ready");
		secondAura.setName("Spec Ready");
		secondAura.setEnabled(false);
		secondAura.setActivationMode(ActivationMode.ON_ENTER);

		ConditionGroup secondGroup = new ConditionGroup();
		SpecCondition specCondition = new SpecCondition();
		specCondition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		specCondition.setThreshold(100);
		secondGroup.getChildren().add(specCondition);
		secondAura.setRootGroup(secondGroup);

		SoundAlertDisplay soundAlertDisplay = new SoundAlertDisplay();
		soundAlertDisplay.setSoundKey("ding");
		soundAlertDisplay.setVolumePercent(65);
		secondAura.setDisplay(soundAlertDisplay);

		String serialized = store.serialize(Arrays.asList(firstAura, secondAura));
		List<AuraDefinition> restored = store.deserialize(serialized);

		assertTrue(serialized.contains("\"type\":\"group\""));
		assertTrue(serialized.contains("\"type\":\"hp\""));
		assertTrue(serialized.contains("\"type\":\"prayer_points\""));
		assertTrue(serialized.contains("\"type\":\"spec\""));
		assertTrue(serialized.contains("\"type\":\"screen_flash\""));
		assertTrue(serialized.contains("\"type\":\"sound_alert\""));
		assertEquals(2, restored.size());

		AuraDefinition restoredFirst = restored.get(0);
		assertEquals(AuraDefinitionStore.CURRENT_SCHEMA_VERSION, restoredFirst.getSchemaVersion());
		assertEquals("low-hp", restoredFirst.getId());
		assertEquals("Low HP", restoredFirst.getName());
		assertEquals(ActivationMode.WHILE_ACTIVE, restoredFirst.getActivationMode());
		assertEquals(4, restoredFirst.getCooldownTicks());
		assertTrue(restoredFirst.getDisplay() instanceof ScreenFlashDisplay);
		assertTrue(restoredFirst.getRootGroup().getChildren().get(0) instanceof HpCondition);
		assertTrue(restoredFirst.getRootGroup().getChildren().get(1) instanceof ConditionGroup);

		ConditionGroup nestedGroup = (ConditionGroup) restoredFirst.getRootGroup().getChildren().get(1);
		assertEquals(ConditionLogic.OR, nestedGroup.getLogic());
		assertTrue(nestedGroup.getChildren().get(0) instanceof PrayerPointsCondition);

		AuraDefinition restoredSecond = restored.get(1);
		assertFalse(restoredSecond.isEnabled());
		assertEquals(ActivationMode.ON_ENTER, restoredSecond.getActivationMode());
		assertTrue(restoredSecond.getDisplay() instanceof SoundAlertDisplay);
		assertTrue(restoredSecond.getRootGroup().getChildren().get(0) instanceof SpecCondition);
	}

	@Test
	public void deserializeSkipsInvalidAuraDefinitions()
	{
		String serialized = "[" +
			"{\"schemaVersion\":1,\"id\":\"broken\",\"name\":\"Broken\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[{\"type\":\"unknown\"}]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"display\":{\"type\":\"overlay_text\",\"text\":\"warn\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}," +
			"{\"schemaVersion\":1,\"id\":\"valid\",\"name\":\"Valid\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"display\":{\"type\":\"overlay_text\",\"text\":\"ok\",\"colorHex\":\"#00FF00\",\"showValue\":false}}" +
			"]";

		List<AuraDefinition> restored = store.deserialize(serialized);

		assertEquals(1, restored.size());
		assertEquals("valid", restored.get(0).getId());
		assertTrue(restored.get(0).getDisplay() instanceof OverlayTextDisplay);
	}

	@Test
	public void deserializeReturnsEmptyListForBlankInput()
	{
		assertTrue(store.deserialize(null).isEmpty());
		assertTrue(store.deserialize("").isEmpty());
		assertTrue(store.deserialize("   ").isEmpty());
	}

	@Test
	public void serializeSkipsNullEntriesAndUpgradesSchemaVersion()
	{
		AuraDefinition aura = new AuraDefinition();
		aura.setSchemaVersion(0);
		aura.setId("only-aura");
		aura.setName("Only Aura");
		aura.setDisplay(new OverlayTextDisplay());

		String serialized = store.serialize(Arrays.asList(null, aura));
		List<AuraDefinition> restored = store.deserialize(serialized);

		assertEquals(AuraDefinitionStore.CURRENT_SCHEMA_VERSION, aura.getSchemaVersion());
		assertFalse(serialized.contains("null"));
		assertEquals(1, restored.size());
		assertEquals("only-aura", restored.get(0).getId());
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
			"\"display\":{\"type\":\"overlay_text\",\"text\":\"future\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}" +
			"]";

		assertTrue(store.deserialize(serialized).isEmpty());
	}

	@Test
	public void deserializeSkipsAurasMissingRequiredFields()
	{
		String missingDisplay = "[" +
			"{\"schemaVersion\":1,\"id\":\"missing-display\",\"name\":\"Missing Display\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0}" +
			"]";
		String missingRootGroup = "[" +
			"{\"schemaVersion\":1,\"id\":\"missing-root\",\"name\":\"Missing Root\",\"enabled\":true," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"display\":{\"type\":\"overlay_text\",\"text\":\"warn\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}" +
			"]";

		assertTrue(store.deserialize(missingDisplay).isEmpty());
		assertTrue(store.deserialize(missingRootGroup).isEmpty());
	}

	@Test
	public void deserializeMigratesNonPositiveSchemaVersionToCurrent()
	{
		String serialized = "[" +
			"{\"schemaVersion\":0,\"id\":\"legacy\",\"name\":\"Legacy\",\"enabled\":true," +
			"\"rootGroup\":{\"type\":\"group\",\"logic\":\"AND\",\"children\":[]}," +
			"\"activationMode\":\"WHILE_ACTIVE\",\"cooldownTicks\":0," +
			"\"display\":{\"type\":\"overlay_text\",\"text\":\"legacy\",\"colorHex\":\"#FFFFFF\",\"showValue\":true}}" +
			"]";

		List<AuraDefinition> restored = store.deserialize(serialized);

		assertEquals(1, restored.size());
		assertEquals(AuraDefinitionStore.CURRENT_SCHEMA_VERSION, restored.get(0).getSchemaVersion());
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
