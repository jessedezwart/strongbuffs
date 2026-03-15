package nl.jessedezwart.strongbuffs.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.XpGainCondition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import org.junit.Test;

public class RuleCompilerTest
{
	private final RuleCompiler ruleCompiler = new RuleCompiler();

	@Test
	public void compileBuildsAggregateRequirementsAndTriggerIndex()
	{
		RuleDefinition hitpointsRule = new RuleDefinition();
		hitpointsRule.setId("hp");
		hitpointsRule.setRootGroup(withCondition(createHpCondition(30)));
		hitpointsRule.setAction(createOverlayAction("HP"));

		XpGainCondition xpGainCondition = new XpGainCondition();
		xpGainCondition.setSkill(Skill.ATTACK);

		RuleDefinition xpRule = new RuleDefinition();
		xpRule.setId("xp");
		xpRule.setRootGroup(withCondition(xpGainCondition));
		xpRule.setAction(createOverlayAction("XP"));

		CompiledRuleSet compiledRuleSet = ruleCompiler.compile(Arrays.asList(hitpointsRule, xpRule));

		assertEquals(2, compiledRuleSet.getRules().size());
		assertTrue(compiledRuleSet.getRequirements().tracksHitpoints());
		assertTrue(compiledRuleSet.getRequirements().getXpGainSkills().contains(Skill.ATTACK));
		assertEquals(1, compiledRuleSet.getTriggerIndex().getRulesForTriggers(
			java.util.EnumSet.of(RuntimeTrigger.HITPOINTS)).size());
		assertEquals(1, compiledRuleSet.getTriggerIndex().getRulesForTriggers(
			java.util.EnumSet.of(RuntimeTrigger.XP_GAIN)).size());
		assertEquals(1, compiledRuleSet.getTriggerIndex().getRulesForTriggers(
			java.util.EnumSet.of(RuntimeTrigger.GAME_TICK)).size());
	}

	private static ConditionGroup withCondition(nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition condition)
	{
		ConditionGroup group = new ConditionGroup();
		group.getChildren().add(condition);
		return group;
	}

	private static HpCondition createHpCondition(int threshold)
	{
		HpCondition condition = new HpCondition();
		condition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		condition.setThreshold(threshold);
		return condition;
	}

	private static OverlayTextAction createOverlayAction(String text)
	{
		OverlayTextAction action = new OverlayTextAction();
		action.setText(text);
		return action;
	}
}
