package nl.jessedezwart.strongbuffs.runtime;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.action.ActionDispatcher;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionTreeEvaluator;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.engine.RuleCompiler;
import nl.jessedezwart.strongbuffs.runtime.engine.RuleEngine;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;
import org.junit.Test;

public class RuleEngineTest
{
	private final RuleCompiler ruleCompiler = new RuleCompiler();

	@Test
	public void baselineActivatesWhileActiveRuleWithoutTransientFire()
	{
		RecordingActionDispatcher actionDispatcher = new RecordingActionDispatcher();
		RuleEngine ruleEngine = new RuleEngine(new ConditionTreeEvaluator(), actionDispatcher);

		ruleEngine.setCompiledRuleSet(ruleCompiler.compile(List.of(createRule("a", ActivationMode.WHILE_ACTIVE, 0))));

		RuntimeState runtimeState = createState(20, 10);
		ruleEngine.onRuntimeStateChanged(EnumSet.of(RuntimeTrigger.FULL_REFRESH), runtimeState);

		assertEquals(List.of("activate:a"), actionDispatcher.events);
	}

	@Test
	public void enterAndExitRulesRespectTransitionsAndCooldown()
	{
		RecordingActionDispatcher actionDispatcher = new RecordingActionDispatcher();
		RuleEngine ruleEngine = new RuleEngine(new ConditionTreeEvaluator(), actionDispatcher);

		ruleEngine.setCompiledRuleSet(ruleCompiler.compile(List.of(createRule("enter", ActivationMode.ON_ENTER, 2),
				createRule("exit", ActivationMode.ON_EXIT, 0))));

		RuntimeState runtimeState = createState(40, 1);
		ruleEngine.onRuntimeStateChanged(EnumSet.of(RuntimeTrigger.FULL_REFRESH), runtimeState);

		runtimeState.getSkills().setCurrentTick(2);
		runtimeState.getSkills().setHitpoints(20);
		ruleEngine.onRuntimeStateChanged(EnumSet.of(RuntimeTrigger.HITPOINTS), runtimeState);

		runtimeState.getSkills().setCurrentTick(3);
		runtimeState.getSkills().setHitpoints(40);
		ruleEngine.onRuntimeStateChanged(EnumSet.of(RuntimeTrigger.HITPOINTS), runtimeState);

		runtimeState.getSkills().setCurrentTick(4);
		runtimeState.getSkills().setHitpoints(20);
		ruleEngine.onRuntimeStateChanged(EnumSet.of(RuntimeTrigger.HITPOINTS), runtimeState);

		runtimeState.getSkills().setCurrentTick(5);
		runtimeState.getSkills().setHitpoints(40);
		ruleEngine.onRuntimeStateChanged(EnumSet.of(RuntimeTrigger.HITPOINTS), runtimeState);

		assertEquals(List.of("fire:enter", "fire:exit", "fire:enter", "fire:exit"), actionDispatcher.events);
	}

	private static RuleDefinition createRule(String id, ActivationMode activationMode, int cooldownTicks)
	{
		RuleDefinition ruleDefinition = new RuleDefinition();
		ruleDefinition.setId(id);
		ruleDefinition.setName(id);
		ruleDefinition.setActivationMode(activationMode);
		ruleDefinition.setCooldownTicks(cooldownTicks);
		ruleDefinition.setRootGroup(withCondition(createHpCondition(30)));

		OverlayTextAction action = new OverlayTextAction();
		action.setText(id);
		ruleDefinition.setAction(action);
		return ruleDefinition;
	}

	private static ConditionGroup withCondition(
			nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition condition)
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

	private static RuntimeState createState(int hitpoints, int tick)
	{
		RuntimeState runtimeState = new RuntimeState();
		runtimeState.getSkills().setHitpoints(hitpoints);
		runtimeState.getSkills().setCurrentTick(tick);
		return runtimeState;
	}

	private static class RecordingActionDispatcher extends ActionDispatcher
	{
		private final List<String> events = new ArrayList<>();

		private RecordingActionDispatcher()
		{
			super(null, null, null);
		}

		@Override
		public void activatePersistent(CompiledRule rule, RuntimeState runtimeState)
		{
			events.add("activate:" + rule.getId());
		}

		@Override
		public void deactivatePersistent(CompiledRule rule)
		{
			events.add("deactivate:" + rule.getId());
		}

		@Override
		public void fireTransient(CompiledRule rule, RuntimeState runtimeState)
		{
			events.add("fire:" + rule.getId());
		}

		@Override
		public void clearAll()
		{
		}
	}
}
