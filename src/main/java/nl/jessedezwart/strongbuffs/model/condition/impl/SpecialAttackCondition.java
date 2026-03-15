package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

@EqualsAndHashCode(callSuper = true)
public class SpecialAttackCondition extends NumericConditionDefinition
{
	public SpecialAttackCondition()
	{
		super(ComparisonOperator.GREATER_THAN_OR_EQUAL);
	}

	@Override
	public String getEditorLabel()
	{
		return "Special attack";
	}

	@Override
	public String getEditorUnit()
	{
		return " %";
	}

	@Override
	public int getMinimumValue()
	{
		return 0;
	}

	@Override
	public int getMaximumValue()
	{
		return 100;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new SpecialAttackCondition();
	}

	@Override
	public String getTypeId()
	{
		return "spec";
	}
}
