package nl.jessedezwart.strongbuffs.panel.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.RuleDefinitionValidator;
import nl.jessedezwart.strongbuffs.RuleJsonCodec;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.engine.RuleRuntimeController;
import org.junit.Test;

public class RuleRepositoryTest
{
	@Test
	public void duplicateRulePersistsCopyAndPublishesRuntimeUpdate()
	{
		RecordingStore store = new RecordingStore(List.of(createRule("rule-a", "First")));
		RecordingRuntimeController runtimeController = new RecordingRuntimeController();
		RuleRepository repository = new RuleRepository(store, runtimeController,
			new RuleImportService(new RuleJsonCodec(), new RuleDefinitionValidator()));

		repository.reload();
		RuleDefinition copy = repository.duplicateRule("rule-a");

		assertEquals(2, repository.getAll().size());
		assertNotEquals("rule-a", copy.getId());
		assertEquals("First Copy", copy.getName());
		assertEquals(2, runtimeController.lastPublishedRules.size());
	}

	@Test
	public void importRuleJsonPersistsImportedRuleAndPublishesRuntimeUpdate()
	{
		RecordingStore store = new RecordingStore(List.of(createRule("rule-a", "First")));
		RecordingRuntimeController runtimeController = new RecordingRuntimeController();
		RuleRepository repository = new RuleRepository(store, runtimeController,
			new RuleImportService(new RuleJsonCodec(), new RuleDefinitionValidator()));
		repository.reload();

		RuleDefinition imported = createRule("rule-b", "Imported");
		RuleImportResult result = repository.importRuleJson(new RuleJsonCodec().serializeRule(imported));

		assertTrue(result.isSuccess());
		assertEquals(2, store.savedRules.size());
		assertEquals(2, runtimeController.lastPublishedRules.size());
	}

	private static RuleDefinition createRule(String id, String name)
	{
		RuleDefinition ruleDefinition = new RuleDefinition();
		ruleDefinition.setId(id);
		ruleDefinition.setName(name);
		ruleDefinition.setRootGroup(new ConditionGroup());
		ruleDefinition.getRootGroup().getChildren().add(createHpCondition());

		OverlayTextAction action = new OverlayTextAction();
		action.setText("Warn");
		ruleDefinition.setAction(action);
		return ruleDefinition;
	}

	private static HpCondition createHpCondition()
	{
		HpCondition condition = new HpCondition();
		condition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		condition.setThreshold(30);
		return condition;
	}

	private static final class RecordingStore extends RuleDefinitionStore
	{
		private final List<RuleDefinition> rules = new ArrayList<>();
		private List<RuleDefinition> savedRules = new ArrayList<>();

		private RecordingStore(List<RuleDefinition> initialRules)
		{
			super(null);
			rules.addAll(initialRules);
			savedRules = new ArrayList<>(initialRules);
		}

		@Override
		public List<RuleDefinition> load()
		{
			return new ArrayList<>(rules);
		}

		@Override
		public void save(List<RuleDefinition> updatedRules)
		{
			rules.clear();
			rules.addAll(updatedRules);
			savedRules = new ArrayList<>(updatedRules);
		}
	}

	private static final class RecordingRuntimeController extends RuleRuntimeController
	{
		private List<RuleDefinition> lastPublishedRules = new ArrayList<>();

		private RecordingRuntimeController()
		{
			super(null, null, null, null);
		}

		@Override
		public void setRules(List<RuleDefinition> rules)
		{
			lastPublishedRules = new ArrayList<>(rules);
		}
	}
}
