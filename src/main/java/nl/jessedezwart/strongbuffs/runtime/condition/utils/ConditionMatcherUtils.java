package nl.jessedezwart.strongbuffs.runtime.condition.utils;

import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

public final class ConditionMatcherUtils
{
	private ConditionMatcherUtils()
	{
	}

	public static boolean matches(NumericConditionDefinition condition, int actualValue)
	{
		ComparisonOperator operator = condition.getOperator();
		return operator != null && operator.matches(actualValue, condition.getThreshold());
	}
}
