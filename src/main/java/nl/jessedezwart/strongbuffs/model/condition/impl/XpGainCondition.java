package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionEditorOptions;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;
import nl.jessedezwart.strongbuffs.runtime.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;

@Data
@EqualsAndHashCode(callSuper = false)
public class XpGainCondition extends ConditionDefinition
{
	private Skill skill = Skill.ATTACK;

	@Override
	public String getEditorLabel()
	{
		return "XP gain";
	}

	@Override
	public String getEditorDescription()
	{
		return "XP gained in " + skill.getName();
	}

	@Override
	public ConditionDefinition copy()
	{
		XpGainCondition copy = new XpGainCondition();
		copy.setSkill(skill);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(EditorField.choice("skill", "Skill", this::getSkill, this::setSkill,
			ConditionEditorOptions.getSkills(), Skill::getName));
	}

	@Override
	public boolean matches(RuntimeState state)
	{
		return state != null && state.getSkills().hasXpGain(skill);
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requireXpGainSkill(skill);
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (skill == null)
		{
			errors.put(fieldPrefix, "Choose a skill.");
		}
	}

	@Override
	public String getTypeId()
	{
		return "xp_gain";
	}
}
