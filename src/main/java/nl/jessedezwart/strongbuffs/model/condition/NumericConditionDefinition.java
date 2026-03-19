package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

@Data
@EqualsAndHashCode(callSuper = false)
/**
 * Base class for conditions that compare one runtime number against a threshold.
 *
 * <p>This keeps the common editor rendering, validation, and copy logic in one place while each
 * concrete condition supplies its own label, unit, and numeric bounds.</p>
 */
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
		// Numeric subclasses only need to produce the correct concrete type.
		copyTo(copy);
		return copy;
	}

	@Override
	public String getEditorDescription()
	{
		return getEditorLabel() + " " + operator.getEditorLabel() + " " + threshold + getEditorUnit();
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

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (operator == null)
		{
			errors.put(fieldPrefix, getEditorLabel() + " requires a comparison.");
			return;
		}

		if (threshold < getMinimumValue() || threshold > getMaximumValue())
		{
			errors.put(fieldPrefix, getEditorLabel() + " must be between " + getMinimumValue() + " and " +
				getMaximumValue() + ".");
		}
	}

	protected final void copyTo(NumericConditionDefinition copy)
	{
		copy.setOperator(operator);
		copy.setThreshold(threshold);
	}
}
