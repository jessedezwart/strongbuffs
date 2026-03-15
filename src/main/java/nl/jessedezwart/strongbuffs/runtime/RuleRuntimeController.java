package nl.jessedezwart.strongbuffs.runtime;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.action.ActionDispatcher;

@Singleton
public class RuleRuntimeController
{
	private final RuntimeConditionTracker runtimeConditionTracker;
	private final RuleCompiler ruleCompiler;
	private final RuleEngine ruleEngine;
	private final ActionDispatcher actionDispatcher;

	private CompiledRuleSet compiledRuleSet = CompiledRuleSet.empty();
	private boolean started;

	@Inject
	public RuleRuntimeController(RuntimeConditionTracker runtimeConditionTracker, RuleCompiler ruleCompiler,
		RuleEngine ruleEngine, ActionDispatcher actionDispatcher)
	{
		this.runtimeConditionTracker = runtimeConditionTracker;
		this.ruleCompiler = ruleCompiler;
		this.ruleEngine = ruleEngine;
		this.actionDispatcher = actionDispatcher;
	}

	public void startUp()
	{
		if (started)
		{
			return;
		}

		started = true;
		actionDispatcher.startUp();
		runtimeConditionTracker.addListener(ruleEngine);
		ruleEngine.setCompiledRuleSet(compiledRuleSet);
		runtimeConditionTracker.setRequirements(compiledRuleSet.getRequirements());
		runtimeConditionTracker.startUp();
	}

	public void shutDown()
	{
		if (!started)
		{
			return;
		}

		runtimeConditionTracker.shutDown();
		runtimeConditionTracker.removeListener(ruleEngine);
		actionDispatcher.shutDown();
		started = false;
	}

	public void setRules(List<RuleDefinition> rules)
	{
		compiledRuleSet = ruleCompiler.compile(rules);
		ruleEngine.setCompiledRuleSet(compiledRuleSet);
		runtimeConditionTracker.setRequirements(compiledRuleSet.getRequirements());
	}
}
