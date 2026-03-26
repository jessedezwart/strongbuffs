package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.PoisonCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.VarRuntimeState;

public class PoisonConditionRuntimeHandler implements ConditionRuntimeAdapter<PoisonCondition>
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
			return runtimeState.getVars().getPoisonState() == VarRuntimeState.PoisonState.POISON;
		case VENOM:
			return runtimeState.getVars().getPoisonState() == VarRuntimeState.PoisonState.VENOM;
		case POISON_OR_VENOM:
			return runtimeState.getVars().getPoisonState() != VarRuntimeState.PoisonState.NONE;
		default:
			return false;
		}
	}

	@Override
	public void contributeRequirements(PoisonCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requirePoison();
	}

	@Override
	public String formatValue(PoisonCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getVars().getPoisonState().name().toLowerCase();
	}
}
