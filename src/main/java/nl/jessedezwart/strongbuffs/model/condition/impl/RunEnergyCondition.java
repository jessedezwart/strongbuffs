package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.runtime.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;

@EqualsAndHashCode(callSuper = true)
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
	protected int getValue(RuntimeState state)
	{
		return state.getLocation().getRunEnergyPercent();
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requireRunEnergy();
	}

	@Override
	public String getTypeId()
	{
		return "run_energy";
	}
}
