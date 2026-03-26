package nl.jessedezwart.strongbuffs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.panel.state.RuleImportResult;
import nl.jessedezwart.strongbuffs.panel.state.RuleImportService;
import org.junit.Test;

public class RuleImportServiceTest
{
	private final RuleJsonCodec ruleJsonCodec = new RuleJsonCodec();
	private final RuleImportService ruleImportService =
		new RuleImportService(ruleJsonCodec, new RuleDefinitionValidator());

	@Test
	public void importRuleAssignsFreshIdAndTrimsName()
	{
		RuleDefinition sourceRule = new RuleDefinition();
		sourceRule.setId("existing-id");
		sourceRule.setName(" Imported Rule ");
		sourceRule.setRootGroup(new ConditionGroup());
		sourceRule.getRootGroup().getChildren().add(createHpCondition());

		OverlayTextAction action = new OverlayTextAction();
		action.setText("Warn");
		sourceRule.setAction(action);

		RuleImportResult result = ruleImportService.importRule(ruleJsonCodec.serializeRule(sourceRule));

		assertTrue(result.isSuccess());
		assertNotEquals("existing-id", result.getImportedRule().getId());
		assertEquals("Imported Rule", result.getImportedRule().getName());
	}

	@Test
	public void importRuleRejectsInvalidRulePayload()
	{
		RuleImportResult result = ruleImportService.importRule("{\"name\":\"Broken\"}");

		assertFalse(result.isSuccess());
		assertEquals("Rule JSON must include rootGroup and action.", result.getErrorMessage());
	}

	@Test
	public void importRuleRejectsBlankActionText()
	{
		RuleDefinition sourceRule = new RuleDefinition();
		sourceRule.setName("Alert");
		sourceRule.setRootGroup(new ConditionGroup());
		sourceRule.getRootGroup().getChildren().add(createHpCondition());

		OverlayTextAction action = new OverlayTextAction();
		action.setText(" ");
		sourceRule.setAction(action);

		RuleImportResult result = ruleImportService.importRule(ruleJsonCodec.serializeRule(sourceRule));

		assertFalse(result.isSuccess());
		assertEquals("Text is required.", result.getErrorMessage());
	}

	private static HpCondition createHpCondition()
	{
		HpCondition condition = new HpCondition();
		condition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		condition.setThreshold(30);
		return condition;
	}
}
