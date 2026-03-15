package nl.jessedezwart.strongbuffs.panel.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;
import org.junit.Test;

public class RulePanelControllerTest
{
	private final ConditionEditorRegistry conditionRegistry = new ConditionEditorRegistry();
	private final ActionEditorRegistry actionRegistry = new ActionEditorRegistry();

	@Test
	public void createRuleRequiresValidDraftBeforeSaving()
	{
		RecordingStore store = new RecordingStore(Collections.emptyList());
		RulePanelController controller = new RulePanelController(store, conditionRegistry, actionRegistry);

		controller.requestCreateRule();

		assertFalse(controller.getValidationResult().isValid());

		RuleDraft draft = controller.getDraft();
		draft.setName("Low HP");
		draft.getRootGroup().getChildren().add(createHpCondition(30));
		draft.setAction(new OverlayTextAction());
		((OverlayTextAction) draft.getAction()).setText("Danger");
		controller.revalidateDraft();

		assertTrue(controller.getValidationResult().isValid());
		assertTrue(controller.saveDraft().isSuccess());
		assertEquals(1, store.savedRules.size());
		assertFalse(controller.hasUnsavedChanges());
	}

	@Test
	public void unsavedSelectionChangeCanDiscardPendingChanges()
	{
		RecordingStore store = new RecordingStore(List.of(createRule("a", "First"), createRule("b", "Second")));
		RulePanelController controller = new RulePanelController(store, conditionRegistry, actionRegistry);

		controller.requestSelectRule("a");
		controller.getDraft().setName("Changed");
		controller.revalidateDraft();

		RuleControllerActionResult result = controller.requestSelectRule("b");

		assertTrue(result.requiresUnsavedConfirmation());
		controller.resolvePendingAction(UnsavedResolution.DISCARD);
		assertEquals("b", controller.getSelectedRuleId());
		assertEquals("Second", controller.getDraft().getName());
	}

	@Test
	public void duplicateCreatesUnsavedCopyWithNewIdentity()
	{
		RecordingStore store = new RecordingStore(List.of(createRule("a", "First")));
		RulePanelController controller = new RulePanelController(store, conditionRegistry, actionRegistry);

		controller.requestSelectRule("a");
		controller.requestDuplicateSelectedRule();

		RuleDraft duplicated = controller.getDraft();
		assertTrue(duplicated.isNewRule());
		assertNotEquals("a", duplicated.getId());
		assertEquals("First Copy", duplicated.getName());
		assertEquals(2, controller.getVisibleRules().size());
	}

	@Test
	public void cancelOnUnsavedDraftClearsSelection()
	{
		RecordingStore store = new RecordingStore(Collections.emptyList());
		RulePanelController controller = new RulePanelController(store, conditionRegistry, actionRegistry);

		controller.requestCreateRule();
		controller.cancelDraft();

		assertNull(controller.getDraft());
		assertNull(controller.getSelectedRuleId());
	}

	@Test
	public void deleteRemovesPersistedRule()
	{
		RecordingStore store = new RecordingStore(List.of(createRule("a", "First")));
		RulePanelController controller = new RulePanelController(store, conditionRegistry, actionRegistry);

		controller.requestSelectRule("a");
		controller.requestDeleteSelectedRule();

		assertTrue(store.savedRules.isEmpty());
		assertTrue(controller.getVisibleRules().isEmpty());
	}

	private static RuleDefinition createRule(String id, String name)
	{
		RuleDefinition ruleDefinition = new RuleDefinition();
		ruleDefinition.setId(id);
		ruleDefinition.setName(name);
		ruleDefinition.setActivationMode(ActivationMode.WHILE_ACTIVE);

		ConditionGroup root = new ConditionGroup();
		root.getChildren().add(createHpCondition(25));
		ruleDefinition.setRootGroup(root);

		OverlayTextAction action = new OverlayTextAction();
		action.setText("Warn");
		ruleDefinition.setAction(action);
		return ruleDefinition;
	}

	private static HpCondition createHpCondition(int threshold)
	{
		HpCondition condition = new HpCondition();
		condition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		condition.setThreshold(threshold);
		return condition;
	}

	private static final class RecordingStore extends RuleDefinitionStore
	{
		private List<RuleDefinition> rules;
		private List<RuleDefinition> savedRules = new ArrayList<>();

		private RecordingStore(List<RuleDefinition> rules)
		{
			super(null);
			this.rules = new ArrayList<>(rules);
			this.savedRules = new ArrayList<>(rules);
		}

		@Override
		public List<RuleDefinition> load()
		{
			return new ArrayList<>(rules);
		}

		@Override
		public void save(List<RuleDefinition> rules)
		{
			this.rules = new ArrayList<>(rules);
			this.savedRules = new ArrayList<>(rules);
		}
	}
}
