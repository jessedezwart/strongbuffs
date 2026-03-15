package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItemCountCondition extends NumericConditionDefinition
{
	private String itemName = "";

	public ItemCountCondition()
	{
		super(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		setThreshold(1);
	}

	@Override
	public String getEditorLabel()
	{
		return "Item count";
	}

	@Override
	public String getEditorDescription()
	{
		return itemName + " count " + getOperator().getEditorLabel() + " " + getThreshold();
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
		return 1000000;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		List<EditorField> fields = new ArrayList<>();
		fields.add(EditorField.text("itemName", "", 14, this::getItemName, this::setItemName));
		fields.addAll(super.getEditorFields());
		return fields;
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (isBlank(itemName))
		{
			errors.put(fieldPrefix, "Enter an inventory item name.");
			return;
		}

		super.validate(errors, fieldPrefix);
	}

	@Override
	public ItemCountCondition copy()
	{
		ItemCountCondition copy = new ItemCountCondition();
		copyTo(copy);
		copy.setItemName(itemName);
		return copy;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new ItemCountCondition();
	}

	@Override
	public String getTypeId()
	{
		return "item_count";
	}
}
