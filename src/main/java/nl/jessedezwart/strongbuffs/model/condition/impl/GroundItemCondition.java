package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.EditorField;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;

@Data
@EqualsAndHashCode(callSuper = false)
/**
 * Persisted condition definition that matches when a nearby ground item name is present.
 */
public class GroundItemCondition extends ConditionDefinition
{
	private String itemName = "";

	@Override
	public String getEditorLabel()
	{
		return "Ground item";
	}

	@Override
	public String getEditorDescription()
	{
		return "Ground item nearby: " + itemName;
	}

	@Override
	public ConditionDefinition copy()
	{
		GroundItemCondition copy = new GroundItemCondition();
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
			errors.put(fieldPrefix, "Enter a ground item name.");
		}
	}

	@Override
	public String getTypeId()
	{
		return "ground_item";
	}
}
