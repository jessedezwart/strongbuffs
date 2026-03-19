package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.action.ActionDispatcher;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeConditionTracker;

/**
 * Top-level coordinator for the runtime rule pipeline.
 *
 * <p>This class owns the handshake between compiled rules, runtime tracking requirements, the rule
 * engine, and action lifecycle. The panel repository publishes new persisted rules here after each
 * save.</p>
 */
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

	/**
	 * Starts runtime tracking and action infrastructure with the current compiled rules.
	 */
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

	/**
	 * Stops runtime tracking and clears any active actions.
	 */
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

	/**
	 * Recompiles the provided persisted rules and republishes the resulting runtime requirements.
	 */
	public void setRules(List<RuleDefinition> rules)
	{
		compiledRuleSet = ruleCompiler.compile(rules);
		ruleEngine.setCompiledRuleSet(compiledRuleSet);
		runtimeConditionTracker.setRequirements(compiledRuleSet.getRequirements());
	}
}
