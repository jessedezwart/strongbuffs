package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapterRegistry;
import nl.jessedezwart.strongbuffs.runtime.condition.RuleConditionRequirementCollector;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeTrackingPlan;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeTrackingPlanner;

/**
 * Compiles persisted rules into runtime-ready structures.
 *
 * <p>
 * Compilation precomputes the requirement watchlist and trigger plan for each
 * enabled rule so the engine can react incrementally instead of reevaluating
 * everything on every event.
 * </p>
 */
@Singleton
public class RuleCompiler
{
	private final RuleConditionRequirementCollector requirementCollector;
	private final RuntimeTrackingPlanner runtimeRequirementPlanner;

	public RuleCompiler()
	{
		this(new RuleConditionRequirementCollector(new ConditionRuntimeAdapterRegistry()),
				new RuntimeTrackingPlanner());
	}

	@Inject
	public RuleCompiler(RuleConditionRequirementCollector requirementCollector,
			RuntimeTrackingPlanner runtimeRequirementPlanner)
	{
		this.requirementCollector = requirementCollector;
		this.runtimeRequirementPlanner = runtimeRequirementPlanner;
	}

	/**
	 * Compiles the enabled persisted rules into a runtime rule set and trigger
	 * index.
	 */
	public CompiledRuleSet compile(List<RuleDefinition> rules)
	{
		List<CompiledRule> compiledRules = new ArrayList<>();
		RuntimeStateWatchlist.Builder aggregateRequirements = RuntimeStateWatchlist.builder();

		if (rules == null || rules.isEmpty())
		{
			return new CompiledRuleSet(compiledRules, runtimeRequirementPlanner.plan(aggregateRequirements.build()),
					RuleTriggerIndex.fromRules(compiledRules));
		}

		for (RuleDefinition rule : rules)
		{
			CompiledRule compiledRule = compileRule(rule);

			if (compiledRule == null)
			{
				continue;
			}

			compiledRules.add(compiledRule);
			aggregateRequirements.merge(compiledRule.getRequirements());
		}

		RuntimeStateWatchlist requirements = aggregateRequirements.build();
		RuntimeTrackingPlan requirementPlan = runtimeRequirementPlanner.plan(requirements);
		return new CompiledRuleSet(compiledRules, requirementPlan, RuleTriggerIndex.fromRules(compiledRules));
	}

	private CompiledRule compileRule(RuleDefinition rule)
	{
		if (rule == null || !rule.isEnabled() || rule.getId() == null || rule.getRootGroup() == null
				|| rule.getAction() == null)
		{
			return null;
		}

		RuntimeStateWatchlist requirements = requirementCollector.fromRules(Collections.singletonList(rule));
		RuntimeTrackingPlan requirementPlan = runtimeRequirementPlanner.plan(requirements);
		return new CompiledRule(rule.getId(), rule.getName(), rule.getRootGroup(), rule.getActivationMode(),
				rule.getCooldownTicks(), rule.getAction(), requirements, requirementPlan.getTriggers());
	}
}
