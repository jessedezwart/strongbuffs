package nl.jessedezwart.strongbuffs.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

@Singleton
public class RuleCompiler
{
	@Inject
	public RuleCompiler()
	{
	}

	public CompiledRuleSet compile(List<RuleDefinition> rules)
	{
		List<CompiledRule> compiledRules = new ArrayList<>();
		RuntimeConditionRequirements.Builder aggregateRequirements = RuntimeConditionRequirements.builder();

		if (rules != null)
		{
			for (RuleDefinition rule : rules)
			{
				CompiledRule compiledRule = compileRule(rule);

				if (compiledRule == null)
				{
					continue;
				}

				compiledRules.add(compiledRule);
				mergeRequirements(aggregateRequirements, compiledRule.getRequirements());
			}
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

		RuntimeConditionRequirements requirements = RuntimeConditionRequirements.fromRules(Collections.singletonList(rule));
		return new CompiledRule(rule.getId(), rule.getName(), rule.getRootGroup(), rule.getActivationMode(),
			rule.getCooldownTicks(), rule.getAction(), requirements, buildTriggers(requirements));
	}

	private static EnumSet<RuntimeTrigger> buildTriggers(RuntimeConditionRequirements requirements)
	{
		EnumSet<RuntimeTrigger> triggers = EnumSet.noneOf(RuntimeTrigger.class);

		if (requirements.tracksHitpoints())
		{
			triggers.add(RuntimeTrigger.HITPOINTS);
		}

		if (requirements.tracksPrayerPoints())
		{
			triggers.add(RuntimeTrigger.PRAYER_POINTS);
		}

		if (requirements.tracksSpecialAttack())
		{
			triggers.add(RuntimeTrigger.SPECIAL_ATTACK);
		}

		if (requirements.tracksRunEnergy())
		{
			triggers.add(RuntimeTrigger.RUN_ENERGY);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (requirements.tracksPoison())
		{
			triggers.add(RuntimeTrigger.POISON);
		}

		if (requirements.tracksSlayerTask())
		{
			triggers.add(RuntimeTrigger.SLAYER_TASK);
		}

		if (requirements.tracksPlayerLocation())
		{
			triggers.add(RuntimeTrigger.PLAYER_LOCATION);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (requirements.tracksPlayerInstance())
		{
			triggers.add(RuntimeTrigger.PLAYER_INSTANCE);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (!requirements.getPrayers().isEmpty())
		{
			triggers.add(RuntimeTrigger.PRAYER);
		}

		if (!requirements.getRealSkills().isEmpty())
		{
			triggers.add(RuntimeTrigger.REAL_SKILL);
		}

		if (!requirements.getXpGainSkills().isEmpty())
		{
			triggers.add(RuntimeTrigger.XP_GAIN);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (requirements.hasInventoryTracking())
		{
			triggers.add(RuntimeTrigger.INVENTORY);
		}

		if (requirements.hasEquipmentTracking())
		{
			triggers.add(RuntimeTrigger.EQUIPMENT);
		}

		if (requirements.hasGroundItemTracking())
		{
			triggers.add(RuntimeTrigger.GROUND_ITEMS);
		}

		return triggers;
	}

	private static void mergeRequirements(RuntimeConditionRequirements.Builder builder,
		RuntimeConditionRequirements requirements)
	{
		if (requirements.tracksHitpoints())
		{
			builder.requireHitpoints();
		}

		if (requirements.tracksPrayerPoints())
		{
			builder.requirePrayerPoints();
		}

		if (requirements.tracksSpecialAttack())
		{
			builder.requireSpecialAttack();
		}

		if (requirements.tracksRunEnergy())
		{
			builder.requireRunEnergy();
		}

		if (requirements.tracksPoison())
		{
			builder.requirePoison();
		}

		if (requirements.tracksSlayerTask())
		{
			builder.requireSlayerTask();
		}

		if (requirements.tracksPlayerLocation())
		{
			builder.requirePlayerLocation();
		}

		if (requirements.tracksPlayerInstance())
		{
			builder.requirePlayerInstance();
		}

		for (Prayer prayer : requirements.getPrayers())
		{
			builder.requirePrayer(prayer);
		}

		for (Skill skill : requirements.getRealSkills())
		{
			builder.requireRealSkill(skill);
		}

		for (Skill skill : requirements.getXpGainSkills())
		{
			builder.requireXpGainSkill(skill);
		}

		for (String itemName : requirements.getInventoryItems())
		{
			builder.requireInventoryItem(itemName);
		}

		for (String itemName : requirements.getEquippedItems())
		{
			builder.requireEquippedItem(itemName);
		}

		for (String itemName : requirements.getGroundItems())
		{
			builder.requireGroundItem(itemName);
		}
	}
}
