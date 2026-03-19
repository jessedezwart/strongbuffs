package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.impl.XpGainCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandlerSupport;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class XpGainConditionRuntimeHandler implements ConditionRuntimeHandler<XpGainCondition>
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
	public void contributeRequirements(XpGainCondition condition, RuntimeConditionRequirements.Builder builder)
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

		return ConditionRuntimeHandlerSupport.formatSkill(skill) + " "
				+ (runtimeState.getSkills().hasXpGain(skill) ? "xp" : "idle");
	}
}
