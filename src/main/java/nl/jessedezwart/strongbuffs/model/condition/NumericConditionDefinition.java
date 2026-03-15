package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

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

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(
			EditorField.choice("operator", "", this::getOperator, this::setOperator, Arrays.asList(ComparisonOperator.values()),
				ComparisonOperator::getEditorLabel),
			EditorField.spinner("threshold", "", this::getThreshold, this::setThreshold, getMinimumValue(),
				getMaximumValue(), 1, getEditorUnit().trim()));
	}
}
