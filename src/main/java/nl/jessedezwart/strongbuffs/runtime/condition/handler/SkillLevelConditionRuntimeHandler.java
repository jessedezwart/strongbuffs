package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.impl.SkillLevelCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.utils.ConditionMatcherUtils;
import nl.jessedezwart.strongbuffs.runtime.condition.utils.FormatterUtils;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class SkillLevelConditionRuntimeHandler implements ConditionRuntimeAdapter<SkillLevelCondition>
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

		return ConditionMatcherUtils.matches(condition, runtimeState.getSkills().getRealSkillLevel(skill));
	}

	@Override
	public void contributeRequirements(SkillLevelCondition condition, RuntimeStateWatchlist.Builder builder)
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

		return FormatterUtils.format(skill) + " " + runtimeState.getSkills().getRealSkillLevel(skill);
	}
}
