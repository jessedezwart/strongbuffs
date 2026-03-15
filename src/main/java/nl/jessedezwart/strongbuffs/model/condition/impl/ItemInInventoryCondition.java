package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;
import nl.jessedezwart.strongbuffs.runtime.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;

@Data
@EqualsAndHashCode(callSuper = false)
public class ItemInInventoryCondition extends ConditionDefinition
{
	private String itemName = "";

	@Override
	public String getEditorLabel()
	{
		return "Inventory item";
	}

	@Override
	public String getEditorDescription()
	{
		return "Inventory contains " + itemName;
	}

	@Override
	public ConditionDefinition copy()
	{
		ItemInInventoryCondition copy = new ItemInInventoryCondition();
		copy.setItemName(itemName);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(EditorField.text("itemName", "Item", 16, this::getItemName, this::setItemName));
	}

	@Override
	public boolean matches(RuntimeState state)
	{
		return state != null && state.getInventory().hasInventoryItem(itemName);
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requireInventoryItem(itemName);
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (isBlank(itemName))
		{
			errors.put(fieldPrefix, "Enter an inventory item name.");
		}
	}

	@Override
	public String getTypeId()
	{
		return "item_in_inventory";
	}
}
