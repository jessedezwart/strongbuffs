package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * Persisted condition definition that compares the total GE value of inventory items to a threshold.
 */
public class InventoryValueCondition extends NumericConditionDefinition
{
	public InventoryValueCondition()
	{
		super(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		setThreshold(100000);
	}

	@Override
	public String getEditorLabel()
	{
		return "Inventory value";
	}

	@Override
	public String getEditorUnit()
	{
		return " gp";
	}

	@Override
	public int getMinimumValue()
	{
		return 0;
	}

	@Override
	public int getMaximumValue()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public InventoryValueCondition copy()
	{
		InventoryValueCondition copy = new InventoryValueCondition();
		copyTo(copy);
		return copy;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new InventoryValueCondition();
	}

	@Override
	public String getTypeId()
	{
		return "inventory_value";
	}
}
