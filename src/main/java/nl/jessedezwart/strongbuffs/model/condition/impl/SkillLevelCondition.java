package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionEditorOptions;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * Persisted condition definition for a real skill level threshold.
 */
public class SkillLevelCondition extends NumericConditionDefinition
{
	private Skill skill = Skill.ATTACK;

	public SkillLevelCondition()
	{
		super(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		setThreshold(1);
	}

	@Override
	public String getEditorLabel()
	{
		return "Skill level";
	}

	@Override
	public String getEditorDescription()
	{
		return skill.getName() + " real level " + getOperator().getEditorLabel() + " " + getThreshold();
	}

	@Override
	public String getEditorUnit()
	{
		return "";
	}

	@Override
	public int getMinimumValue()
	{
		return 1;
	}

	@Override
	public int getMaximumValue()
	{
		return 99;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		List<EditorField> fields = new ArrayList<>();
		fields.add(EditorField.choice("skill", "", this::getSkill, this::setSkill, ConditionEditorOptions.getSkills(),
			Skill::getName));
		fields.addAll(super.getEditorFields());
		return fields;
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (skill == null)
		{
			errors.put(fieldPrefix, "Choose a skill.");
			return;
		}

		super.validate(errors, fieldPrefix);
	}

	@Override
	public SkillLevelCondition copy()
	{
		SkillLevelCondition copy = new SkillLevelCondition();
		copyTo(copy);
		copy.setSkill(skill);
		return copy;
	}

	@Override
	protected NumericConditionDefinition createCopy()
	{
		return new SkillLevelCondition();
	}

	@Override
	public String getTypeId()
	{
		return "skill_level";
	}
}
