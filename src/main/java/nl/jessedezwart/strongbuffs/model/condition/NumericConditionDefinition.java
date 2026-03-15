package nl.jessedezwart.strongbuffs.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class NumericConditionDefinition extends ConditionDefinition
{
	private ComparisonOperator operator;
	private int threshold;

	protected NumericConditionDefinition(ComparisonOperator operator)
	{
		this.operator = operator;
	}

	public abstract String getEditorLabel();

	public abstract String getEditorUnit();

	public abstract int getMinimumValue();

	public abstract int getMaximumValue();

	protected abstract NumericConditionDefinition createCopy();

	@Override
	public NumericConditionDefinition copy()
	{
		NumericConditionDefinition copy = createCopy();
		copy.setOperator(operator);
		copy.setThreshold(threshold);
		return copy;
	}
}
