package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

@EqualsAndHashCode(callSuper = true)
/**
 * Persisted condition definition for the local player's current prayer points.
 */
public class PrayerPointsCondition extends NumericConditionDefinition
{
	public PrayerPointsCondition()
	{
		super(ComparisonOperator.LESS_THAN_OR_EQUAL);
	}

	@Override
	public String getEditorLabel()
	{
		return "Prayer points";
	}

	@Override
	public String getEditorUnit()
	{
		return " pts";
	}

	@Override
	public int getMinimumValue()
	{
		return 0;
	}

	@Override
	public int getMaximumValue()
	{
		return 255;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new PrayerPointsCondition();
	}

	@Override
	public String getTypeId()
	{
		return "prayer_points";
	}
}
