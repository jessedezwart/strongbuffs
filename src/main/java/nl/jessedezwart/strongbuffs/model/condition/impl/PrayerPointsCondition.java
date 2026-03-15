package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.runtime.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;

@EqualsAndHashCode(callSuper = true)
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
	protected int getValue(RuntimeState state)
	{
		return state.getSkills().getPrayerPoints();
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requirePrayerPoints();
	}

	@Override
	public String getTypeId()
	{
		return "prayer_points";
	}
}
