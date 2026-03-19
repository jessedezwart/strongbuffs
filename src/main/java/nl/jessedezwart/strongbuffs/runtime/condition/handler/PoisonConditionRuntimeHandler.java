package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.PoisonCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class PoisonConditionRuntimeHandler implements ConditionRuntimeHandler<PoisonCondition>
{
	@Override
	public Class<PoisonCondition> getConditionType()
	{
		return PoisonCondition.class;
	}

	@Override
	public boolean matches(PoisonCondition condition, RuntimeState runtimeState)
	{
		if (condition.getPoisonType() == null)
		{
			return false;
		}

		switch (condition.getPoisonType())
		{
		case POISON:
			return runtimeState.getVars().getPoisonState() == RuntimeState.PoisonState.POISON;
		case VENOM:
			return runtimeState.getVars().getPoisonState() == RuntimeState.PoisonState.VENOM;
		case POISON_OR_VENOM:
			return runtimeState.getVars().getPoisonState() != RuntimeState.PoisonState.NONE;
		default:
			return false;
		}
	}

	@Override
	public void contributeRequirements(PoisonCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requirePoison();
	}

	@Override
	public String formatValue(PoisonCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getVars().getPoisonState().name().toLowerCase();
	}
}
