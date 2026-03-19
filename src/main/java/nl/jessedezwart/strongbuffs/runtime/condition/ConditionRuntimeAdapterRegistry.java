package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.GroundItemConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.HpConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.ItemCountConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.ItemEquippedConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.ItemInInventoryConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.PlayerInInstanceConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.PlayerInZoneConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.PoisonConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.PrayerActiveConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.PrayerPointsConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.RunEnergyConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.SkillLevelConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.SlayerTaskConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.SpecialAttackConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.handler.XpGainConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Runtime mapping layer between persisted condition definitions and live cached
 * state.
 *
 * <p>
 * Each registration answers three related questions for one condition type: how
 * to match it against {@link RuntimeState}, which runtime slices must be
 * tracked for it, and how to format the current live value for user-facing
 * actions.
 * </p>
 */
@Singleton
public class ConditionRuntimeAdapterRegistry
{
	private final Map<Class<? extends ConditionDefinition>, ConditionRuntimeAdapter<? extends ConditionDefinition>> handlers = new LinkedHashMap<>();

	public ConditionRuntimeAdapterRegistry()
	{
		register(new HpConditionRuntimeHandler());
		register(new PrayerPointsConditionRuntimeHandler());
		register(new SpecialAttackConditionRuntimeHandler());
		register(new RunEnergyConditionRuntimeHandler());
		register(new PoisonConditionRuntimeHandler());
		register(new PrayerActiveConditionRuntimeHandler());
		register(new SlayerTaskConditionRuntimeHandler());
		register(new SkillLevelConditionRuntimeHandler());
		register(new XpGainConditionRuntimeHandler());
		register(new ItemInInventoryConditionRuntimeHandler());
		register(new ItemCountConditionRuntimeHandler());
		register(new ItemEquippedConditionRuntimeHandler());
		register(new GroundItemConditionRuntimeHandler());
		register(new PlayerInZoneConditionRuntimeHandler());
		register(new PlayerInInstanceConditionRuntimeHandler());
	}

	/**
	 * Evaluates one persisted condition against the cached runtime snapshot.
	 */
	public boolean matches(ConditionDefinition conditionDefinition, RuntimeState runtimeState)
	{
		if (conditionDefinition == null || runtimeState == null)
		{
			return false;
		}

		return getHandler(conditionDefinition).matches(conditionDefinition, runtimeState);
	}

	/**
	 * Adds the runtime state requirements needed to evaluate one condition type.
	 */
	public void contributeRequirements(ConditionDefinition conditionDefinition, RuntimeStateWatchlist.Builder builder)
	{
		if (conditionDefinition == null)
		{
			return;
		}

		getHandler(conditionDefinition).contributeRequirements(conditionDefinition, builder);
	}

	/**
	 * Formats the current live value for actions that display runtime context to
	 * the user.
	 */
	public String formatValue(ConditionDefinition conditionDefinition, RuntimeState runtimeState)
	{
		if (conditionDefinition == null || runtimeState == null)
		{
			return null;
		}

		return getHandler(conditionDefinition).formatValue(conditionDefinition, runtimeState);
	}

	@SuppressWarnings("unchecked")
	private <T extends ConditionDefinition> ConditionRuntimeAdapter<T> getHandler(
			ConditionDefinition conditionDefinition)
	{
		ConditionRuntimeAdapter<?> handler = handlers.get(conditionDefinition.getClass());

		if (handler == null)
		{
			throw new IllegalArgumentException("Unsupported condition definition: " + conditionDefinition.getClass());
		}

		return (ConditionRuntimeAdapter<T>) handler;
	}

	private <T extends ConditionDefinition> void register(ConditionRuntimeAdapter<T> handler)
	{
		// Keeping match logic, requirement planning, and formatting together prevents
		// new condition
		// types from drifting across three separate registries.
		handlers.put(handler.getConditionType(), handler);
	}
}
