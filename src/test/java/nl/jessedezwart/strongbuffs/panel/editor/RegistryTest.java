package nl.jessedezwart.strongbuffs.panel.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemInInventoryCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInInstanceCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerActiveCondition;
import org.junit.Test;

public class RegistryTest
{
	@Test
	public void conditionRegistryExposesApprovedConditionCatalog()
	{
		ConditionEditorRegistry registry = new ConditionEditorRegistry();

		assertEquals(15, registry.getConditionClasses().size());
		assertTrue(registry.getByConditionClass(HpCondition.class) != null);
		assertTrue(registry.getByConditionClass(PrayerPointsCondition.class) != null);
		assertTrue(registry.getByConditionClass(SpecialAttackCondition.class) != null);
		assertTrue(registry.getByConditionClass(PrayerActiveCondition.class) != null);
		assertTrue(registry.getByConditionClass(ItemInInventoryCondition.class) != null);
		assertTrue(registry.getByConditionClass(PlayerInInstanceCondition.class) != null);
	}

	@Test
	public void actionRegistryExposesOnlyApprovedVerticalSlice()
	{
		ActionEditorRegistry registry = new ActionEditorRegistry();

		assertEquals(3, registry.getActionClasses().size());
		assertTrue(registry.getByActionClass(OverlayTextAction.class) != null);
		assertTrue(registry.getByActionClass(ScreenFlashAction.class) != null);
		assertTrue(registry.getByActionClass(SoundAlertAction.class) != null);
	}

	@Test
	public void soundAlertModelExposesPresetLabels()
	{
		assertEquals("Notification", SoundAlertAction.getSoundLabel("notification"));
		assertEquals("Ding", SoundAlertAction.getSoundLabel("ding"));
	}
}
