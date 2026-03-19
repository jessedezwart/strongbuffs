package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.EditorField;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * Persisted condition definition that compares an item's GE price to a threshold.
 */
public class ItemPriceCondition extends NumericConditionDefinition
{
	private String itemName = "";

	public ItemPriceCondition()
	{
		super(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		setThreshold(1000);
	}

	@Override
	public String getEditorLabel()
	{
		return "Item price";
	}

	@Override
	public String getEditorDescription()
	{
		return itemName + " GE price " + getOperator().getEditorLabel() + " " + getThreshold() + " gp";
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
	public List<EditorField> getEditorFields()
	{
		List<EditorField> fields = new ArrayList<>();
		fields.add(EditorField.text("itemName", "Item", 14, this::getItemName, this::setItemName));
		fields.addAll(super.getEditorFields());
		return fields;
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (isBlank(itemName))
		{
			errors.put(fieldPrefix, "Enter an item name.");
			return;
		}

		super.validate(errors, fieldPrefix);
	}

	@Override
	public ItemPriceCondition copy()
	{
		ItemPriceCondition copy = new ItemPriceCondition();
		copyTo(copy);
		copy.setItemName(itemName);
		return copy;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new ItemPriceCondition();
	}

	@Override
	public String getTypeId()
	{
		return "item_price";
	}
}
