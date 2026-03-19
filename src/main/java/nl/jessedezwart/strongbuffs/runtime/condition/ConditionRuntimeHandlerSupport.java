package nl.jessedezwart.strongbuffs.runtime.condition;

import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

public final class ConditionRuntimeHandlerSupport
{
	private ConditionRuntimeHandlerSupport()
	{
	}

	public static boolean matchesNumeric(NumericConditionDefinition condition, int actualValue)
	{
		ComparisonOperator operator = condition.getOperator();
		return operator != null && operator.matches(actualValue, condition.getThreshold());
	}

	public static String formatSkill(Skill skill)
	{
		String lowerCase = skill.name().toLowerCase().replace('_', ' ');
		return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
	}
}
