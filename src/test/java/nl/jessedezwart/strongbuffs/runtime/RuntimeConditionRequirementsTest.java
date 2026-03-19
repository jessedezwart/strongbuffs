package nl.jessedezwart.strongbuffs.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.impl.GroundItemCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemCountCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInInstanceCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInZoneCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerActiveCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SkillLevelCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.XpGainCondition;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeRegistry;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirementCollector;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import org.junit.Test;

public class RuntimeConditionRequirementsTest
{
	private final RuntimeConditionRequirementCollector requirementCollector =
		new RuntimeConditionRequirementCollector(new ConditionRuntimeRegistry());

	@Test
	public void collectsRequirementsFromConditionImplementations()
	{
		HpCondition hpCondition = new HpCondition();

		PrayerActiveCondition prayerCondition = new PrayerActiveCondition();
		prayerCondition.setPrayer(Prayer.THICK_SKIN);

		SkillLevelCondition skillLevelCondition = new SkillLevelCondition();
		skillLevelCondition.setSkill(Skill.ATTACK);

		XpGainCondition xpGainCondition = new XpGainCondition();
		xpGainCondition.setSkill(Skill.STRENGTH);

		ItemCountCondition itemCountCondition = new ItemCountCondition();
		itemCountCondition.setItemName(" Shark ");

		GroundItemCondition groundItemCondition = new GroundItemCondition();
		groundItemCondition.setItemName("Rune arrow");

		PlayerInZoneCondition zoneCondition = new PlayerInZoneCondition();
		PlayerInInstanceCondition instanceCondition = new PlayerInInstanceCondition();

		ConditionGroup rootGroup = new ConditionGroup();
		rootGroup.getChildren().addAll(Arrays.asList(hpCondition, prayerCondition, skillLevelCondition,
			xpGainCondition, itemCountCondition, groundItemCondition, zoneCondition, instanceCondition));

		RuleDefinition enabledRule = new RuleDefinition();
		enabledRule.setRootGroup(rootGroup);

		RuleDefinition disabledRule = new RuleDefinition();
		disabledRule.setEnabled(false);
		disabledRule.getRootGroup().getChildren().add(new HpCondition());

		RuntimeConditionRequirements requirements = requirementCollector.fromRules(Arrays.asList(enabledRule, disabledRule));

		assertTrue(requirements.tracksHitpoints());
		assertFalse(requirements.tracksPrayerPoints());
		assertTrue(requirements.getPrayers().contains(Prayer.THICK_SKIN));
		assertEquals(1, requirements.getPrayers().size());
		assertTrue(requirements.getRealSkills().contains(Skill.ATTACK));
		assertTrue(requirements.getXpGainSkills().contains(Skill.STRENGTH));
		assertTrue(requirements.getInventoryItems().contains("shark"));
		assertTrue(requirements.getGroundItems().contains("rune arrow"));
		assertTrue(requirements.tracksPlayerLocation());
		assertTrue(requirements.tracksPlayerInstance());
		assertTrue(requirements.needsGameTick());
	}
}
