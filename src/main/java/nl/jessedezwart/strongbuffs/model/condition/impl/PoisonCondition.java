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
	public boolean matches(RuntimeState state)
	{
		if (state == null || poisonType == null)
		{
			return false;
		}

		switch (poisonType)
		{
			case POISON:
				return state.getVars().getPoisonState() == RuntimeState.PoisonState.POISON;
			case VENOM:
				return state.getVars().getPoisonState() == RuntimeState.PoisonState.VENOM;
			case POISON_OR_VENOM:
				return state.getVars().getPoisonState() != RuntimeState.PoisonState.NONE;
			default:
				return false;
		}
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requirePoison();
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
