package nl.jessedezwart.strongbuffs.runtime.condition.util;

import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

public final class ConditionMatcherUtil
{
	private ConditionMatcherUtil()
	{
	}

	public static boolean matches(NumericConditionDefinition condition, int actualValue)
	{
		ComparisonOperator operator = condition.getOperator();
		return operator != null && operator.matches(actualValue, condition.getThreshold());
	}

	public static boolean matchesLong(NumericConditionDefinition condition, long actualValue)
	{
		ComparisonOperator operator = condition.getOperator();
		return operator != null && operator.matches(actualValue, (long) condition.getThreshold());
	}
}
