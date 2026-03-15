package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

@Data
@EqualsAndHashCode(callSuper = true)
public class SlayerTaskCondition extends NumericConditionDefinition
{
	private SlayerTaskCheck check = SlayerTaskCheck.TASK_ACTIVE;

	public SlayerTaskCondition()
	{
		super(ComparisonOperator.LESS_THAN_OR_EQUAL);
		setThreshold(10);
	}

	@Override
	public String getEditorLabel()
	{
		return "Slayer task";
	}

	@Override
	public String getEditorDescription()
	{
		if (check == SlayerTaskCheck.TASK_ACTIVE)
		{
			return "Slayer task is active";
		}

		return "Slayer kills remaining " + getOperator().getEditorLabel() + " " + getThreshold();
	}

	@Override
	public String getEditorUnit()
	{
		return "";
	}

	@Override
	public int getMinimumValue()
	{
		return 0;
	}

	@Override
	public int getMaximumValue()
	{
		return 10000;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		List<EditorField> fields = new ArrayList<>();
		fields.add(EditorField.choice("check", "", this::getCheck, this::setCheck, Arrays.asList(SlayerTaskCheck.values()),
			SlayerTaskCheck::getEditorLabel));
		fields.addAll(super.getEditorFields());
		return fields;
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (check == null)
		{
			errors.put(fieldPrefix, "Choose a slayer task check.");
			return;
		}

		if (check == SlayerTaskCheck.KILLS_REMAINING)
		{
			super.validate(errors, fieldPrefix);
		}
	}

	@Override
	public SlayerTaskCondition copy()
	{
		SlayerTaskCondition copy = new SlayerTaskCondition();
		copyTo(copy);
		copy.setCheck(check);
		return copy;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new SlayerTaskCondition();
	}

	@Override
	public String getTypeId()
	{
		return "slayer_task";
	}

	public enum SlayerTaskCheck
	{
		TASK_ACTIVE("Task active"),
		KILLS_REMAINING("Kills remaining");

		private final String editorLabel;

		SlayerTaskCheck(String editorLabel)
		{
			this.editorLabel = editorLabel;
		}

		public String getEditorLabel()
		{
			return editorLabel;
		}
	}
}
