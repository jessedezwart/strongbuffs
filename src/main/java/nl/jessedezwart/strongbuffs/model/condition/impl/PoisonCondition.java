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
 * Persisted condition definition for the player's poison or venom state.
 */
public class PoisonCondition extends ConditionDefinition
{
	private PoisonType poisonType = PoisonType.POISON_OR_VENOM;

	@Override
	public String getEditorLabel()
	{
		return "Poison";
	}

	@Override
	public String getEditorDescription()
	{
		return poisonType.getEditorLabel() + " is active";
	}

	@Override
	public ConditionDefinition copy()
	{
		PoisonCondition copy = new PoisonCondition();
		copy.setPoisonType(poisonType);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(EditorField.choice("poisonType", "Type", this::getPoisonType, this::setPoisonType,
			Arrays.asList(PoisonType.values()), PoisonType::getEditorLabel));
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (poisonType == null)
		{
			errors.put(fieldPrefix, "Choose a poison state.");
		}
	}

	@Override
	public String getTypeId()
	{
		return "poison";
	}

	public enum PoisonType
	{
		POISON("Poison"),
		VENOM("Venom"),
		POISON_OR_VENOM("Poison or venom");

		private final String editorLabel;

		PoisonType(String editorLabel)
		{
			this.editorLabel = editorLabel;
		}

		public String getEditorLabel()
		{
			return editorLabel;
		}
	}
}
