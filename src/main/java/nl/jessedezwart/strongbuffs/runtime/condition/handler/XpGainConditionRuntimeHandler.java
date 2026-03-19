package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.impl.XpGainCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.utils.FormatterUtils;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class XpGainConditionRuntimeHandler implements ConditionRuntimeAdapter<XpGainCondition>
{
	@Override
	public Class<XpGainCondition> getConditionType()
	{
		return XpGainCondition.class;
	}

	@Override
	public boolean matches(XpGainCondition condition, RuntimeState runtimeState)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return false;
		}

		return runtimeState.getSkills().hasXpGain(skill);
	}

	@Override
	public void contributeRequirements(XpGainCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireXpGainSkill(condition.getSkill());
	}

	@Override
	public String formatValue(XpGainCondition condition, RuntimeState runtimeState)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return null;
		}

		return FormatterUtils.format(skill) + " " + (runtimeState.getSkills().hasXpGain(skill) ? "xp" : "idle");
	}
}
