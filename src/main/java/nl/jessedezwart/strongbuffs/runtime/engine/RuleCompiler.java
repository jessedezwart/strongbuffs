package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeRegistry;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirementCollector;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;

@Singleton
public class RuleCompiler
{
	private final RuntimeConditionRequirementCollector requirementCollector;
	private final RuntimeTriggerPlanner runtimeTriggerPlanner;

	public RuleCompiler()
	{
		this(new RuntimeConditionRequirementCollector(new ConditionRuntimeRegistry()), new RuntimeTriggerPlanner());
	}

	@Inject
	public RuleCompiler(RuntimeConditionRequirementCollector requirementCollector, RuntimeTriggerPlanner runtimeTriggerPlanner)
	{
		this.requirementCollector = requirementCollector;
		this.runtimeTriggerPlanner = runtimeTriggerPlanner;
	}

	public CompiledRuleSet compile(List<RuleDefinition> rules)
	{
		List<CompiledRule> compiledRules = new ArrayList<>();
		RuntimeConditionRequirements.Builder aggregateRequirements = RuntimeConditionRequirements.builder();

		if (rules == null || rules.isEmpty())
		{
			return new CompiledRuleSet(compiledRules, aggregateRequirements.build(), RuleTriggerIndex.fromRules(compiledRules));
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

		RuntimeConditionRequirements requirements = aggregateRequirements.build();
		return new CompiledRuleSet(compiledRules, requirements, RuleTriggerIndex.fromRules(compiledRules));
	}

	private CompiledRule compileRule(RuleDefinition rule)
	{
		if (rule == null || !rule.isEnabled() || rule.getId() == null || rule.getRootGroup() == null || rule.getAction() == null)
		{
			return null;
		}

		RuntimeConditionRequirements requirements = requirementCollector.fromRules(Collections.singletonList(rule));
		return new CompiledRule(rule.getId(), rule.getName(), rule.getRootGroup(), rule.getActivationMode(),
			rule.getCooldownTicks(), rule.getAction(), requirements, runtimeTriggerPlanner.plan(requirements));
	}
}
