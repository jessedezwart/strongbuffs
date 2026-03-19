package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

@Data
@EqualsAndHashCode(callSuper = false)
/**
 * Persisted condition definition that checks whether a named item is equipped.
 */
public class ItemEquippedCondition extends ConditionDefinition
{
	private String itemName = "";

	@Override
	public String getEditorLabel()
	{
		return "Equipped item";
	}

	@Override
	public String getEditorDescription()
	{
		return "Equipped: " + itemName;
	}

	@Override
	public ConditionDefinition copy()
	{
		ItemEquippedCondition copy = new ItemEquippedCondition();
		copy.setItemName(itemName);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(EditorField.text("itemName", "Item", 16, this::getItemName, this::setItemName));
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (isBlank(itemName))
		{
			errors.put(fieldPrefix, "Enter an equipped item name.");
		}
	}

	@Override
	public String getTypeId()
	{
		return "item_equipped";
	}
}
