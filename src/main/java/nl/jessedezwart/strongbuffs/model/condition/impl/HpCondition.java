package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.runtime.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;

@EqualsAndHashCode(callSuper = true)
public class HpCondition extends NumericConditionDefinition
{
	public HpCondition()
	{
		super(ComparisonOperator.LESS_THAN_OR_EQUAL);
	}

	@Override
	public String getEditorLabel()
	{
		return "HP";
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
		return new HpCondition();
	}

	@Override
	protected int getValue(RuntimeState state)
	{
		return state.getSkills().getHitpoints();
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requireHitpoints();
	}

	@Override
	public String getTypeId()
	{
		return "hp";
	}
}
