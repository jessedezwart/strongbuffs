package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;

@EqualsAndHashCode(callSuper = false)
public class PlayerInInstanceCondition extends ConditionDefinition
{
	@Override
	public String getEditorLabel()
	{
		return "Player in instance";
	}

	@Override
	public String getEditorDescription()
	{
		return "Inside a private instance";
	}

	@Override
	public ConditionDefinition copy()
	{
		return new PlayerInInstanceCondition();
	}

	@Override
	public String getTypeId()
	{
		return "player_in_instance";
	}
}
