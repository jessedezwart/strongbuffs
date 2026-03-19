package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.impl.SkillLevelCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandlerSupport;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class SkillLevelConditionRuntimeHandler implements ConditionRuntimeHandler<SkillLevelCondition>
{
	@Override
	public Class<SkillLevelCondition> getConditionType()
	{
		return SkillLevelCondition.class;
	}

	@Override
	public boolean matches(SkillLevelCondition condition, RuntimeState runtimeState)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return false;
		}

		return ConditionRuntimeHandlerSupport.matchesNumeric(condition,
				runtimeState.getSkills().getRealSkillLevel(skill));
	}

	@Override
	public void contributeRequirements(SkillLevelCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requireRealSkill(condition.getSkill());
	}

	@Override
	public String formatValue(SkillLevelCondition condition, RuntimeState runtimeState)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return null;
		}

		return ConditionRuntimeHandlerSupport.formatSkill(skill) + " "
				+ runtimeState.getSkills().getRealSkillLevel(skill);
	}
}
