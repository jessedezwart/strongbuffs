package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionChecker;
import nl.jessedezwart.strongbuffs.runtime.action.ActionDispatcher;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeStateListener;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

@Singleton
public class RuleEngine implements RuntimeStateListener
{
	private final ConditionChecker conditionChecker;
	private final ActionDispatcher actionDispatcher;

	private final Map<String, RuleState> ruleStates = new LinkedHashMap<>();

	private CompiledRuleSet compiledRuleSet = CompiledRuleSet.empty();
	private boolean initialized;

	@Inject
	public RuleEngine(ConditionChecker conditionChecker, ActionDispatcher actionDispatcher)
	{
		this.conditionChecker = conditionChecker;
		this.actionDispatcher = actionDispatcher;
	}

	public void setCompiledRuleSet(CompiledRuleSet compiledRuleSet)
	{
		this.compiledRuleSet = compiledRuleSet == null ? CompiledRuleSet.empty() : compiledRuleSet;
		reset();
	}

	public void reset()
	{
		ruleStates.clear();
		initialized = false;
		actionDispatcher.clearAll();
	}

	@Override
	public void onRuntimeStateChanged(Set<RuntimeTrigger> triggers, RuntimeState runtimeState)
	{
		if (triggers == null || triggers.isEmpty())
		{
			return;
		}

		if (triggers.contains(RuntimeTrigger.CLEAR))
		{
			reset();
			return;
		}

		if (!initialized || triggers.contains(RuntimeTrigger.FULL_REFRESH))
		{
			syncBaseline(runtimeState);
			return;
		}

		Collection<CompiledRule> rules = compiledRuleSet.getTriggerIndex().getRulesForTriggers(triggers);

		for (CompiledRule rule : rules)
		{
			evaluateRule(rule, runtimeState);
		}
	}

	private void syncBaseline(RuntimeState runtimeState)
	{
		actionDispatcher.clearAll();
		ruleStates.clear();

		for (CompiledRule rule : compiledRuleSet.getRules())
		{
			boolean matches = conditionChecker.evaluate(rule.getRootGroup(), runtimeState);
			RuleState ruleState = getRuleState(rule);
			ruleState.previousMatch = matches;

			if (rule.getActivationMode() == ActivationMode.WHILE_ACTIVE && matches)
			{
				ruleState.persistentActive = true;
				actionDispatcher.activatePersistent(rule, runtimeState);
			}
		}

		initialized = true;
	}

	private void evaluateRule(CompiledRule rule, RuntimeState runtimeState)
	{
		RuleState ruleState = getRuleState(rule);
		boolean matches = conditionChecker.evaluate(rule.getRootGroup(), runtimeState);
		int currentTick = runtimeState.getSkills().getCurrentTick();

		if (rule.getActivationMode() == ActivationMode.WHILE_ACTIVE)
		{
			if (matches)
			{
				if (!ruleState.persistentActive)
				{
					ruleState.persistentActive = true;
					ruleState.lastActivationTick = currentTick;
					actionDispatcher.activatePersistent(rule, runtimeState);
				}
				else
				{
					actionDispatcher.updatePersistent(rule, runtimeState);
				}
			}
			else if (ruleState.persistentActive)
			{
				ruleState.persistentActive = false;
				actionDispatcher.deactivatePersistent(rule);
			}
		}
		else if (rule.getActivationMode() == ActivationMode.ON_ENTER)
		{
			if (!ruleState.previousMatch && matches && isCooldownComplete(rule, ruleState, currentTick))
			{
				ruleState.lastActivationTick = currentTick;
				actionDispatcher.fireTransient(rule, runtimeState);
			}
		}
		else if (rule.getActivationMode() == ActivationMode.ON_EXIT)
		{
			if (ruleState.previousMatch && !matches && isCooldownComplete(rule, ruleState, currentTick))
			{
				ruleState.lastActivationTick = currentTick;
				actionDispatcher.fireTransient(rule, runtimeState);
			}
		}

		ruleState.previousMatch = matches;
	}

	private static boolean isCooldownComplete(CompiledRule rule, RuleState ruleState, int currentTick)
	{
		if (rule.getCooldownTicks() <= 0 || ruleState.lastActivationTick < 0)
		{
			return true;
		}

		return currentTick - ruleState.lastActivationTick >= rule.getCooldownTicks();
	}

	private RuleState getRuleState(CompiledRule rule)
	{
		return ruleStates.computeIfAbsent(rule.getId(), ignored -> new RuleState());
	}

	private static class RuleState
	{
		private boolean previousMatch;
		private boolean persistentActive;
		private int lastActivationTick = -1;
	}
}
