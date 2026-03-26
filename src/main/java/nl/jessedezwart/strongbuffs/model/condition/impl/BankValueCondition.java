package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * Persisted condition definition that compares the total GE value of bank items to a threshold.
 *
 * <p>Bank value is only updated when the bank interface is open.</p>
 */
public class BankValueCondition extends NumericConditionDefinition
{
	public BankValueCondition()
	{
		super(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		setThreshold(1000000);
	}

	@Override
	public String getEditorLabel()
	{
		return "Bank value";
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
	public BankValueCondition copy()
	{
		BankValueCondition copy = new BankValueCondition();
		copyTo(copy);
		return copy;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new BankValueCondition();
	}

	@Override
	public String getTypeId()
	{
		return "bank_value";
	}
}
