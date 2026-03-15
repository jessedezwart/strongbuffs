package nl.jessedezwart.strongbuffs.model.condition.impl;

import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.runtime.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;

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
	public boolean matches(RuntimeState state)
	{
		return state != null && state.getLocation().isInInstance();
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requirePlayerInstance();
	}

	@Override
	public String getTypeId()
	{
		return "player_in_instance";
	}
}
