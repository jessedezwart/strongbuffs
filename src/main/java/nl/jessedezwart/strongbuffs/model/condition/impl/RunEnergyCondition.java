package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

@EqualsAndHashCode(callSuper = true)
/**
 * Persisted condition definition for the local player's current run energy percentage.
 */
public class RunEnergyCondition extends NumericConditionDefinition
{
	public RunEnergyCondition()
	{
		super(ComparisonOperator.LESS_THAN_OR_EQUAL);
	}

	@Override
	public String getEditorLabel()
	{
		return "Run energy";
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
		return new RunEnergyCondition();
	}

	@Override
	public String getTypeId()
	{
		return "run_energy";
	}
}
