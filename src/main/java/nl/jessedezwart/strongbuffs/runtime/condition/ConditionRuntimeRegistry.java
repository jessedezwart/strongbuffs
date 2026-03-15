package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.inject.Singleton;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.impl.GroundItemCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemCountCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemEquippedCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemInInventoryCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInInstanceCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInZoneCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PoisonCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerActiveCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.RunEnergyCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SkillLevelCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SlayerTaskCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.XpGainCondition;
import nl.jessedezwart.strongbuffs.runtime.state.InventoryRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class ConditionRuntimeRegistry
{
	private final Map<Class<? extends ConditionDefinition>, ConditionRuntimeRegistration<? extends ConditionDefinition>>
		registrations = new LinkedHashMap<>();

	public ConditionRuntimeRegistry()
	{
		register(HpCondition.class,
			(condition, state) -> matchesNumeric(condition, state.getSkills().getHitpoints()),
			(condition, builder) -> builder.requireHitpoints(),
			(condition, state) -> state.getSkills().getHitpoints() + " hp");
		register(PrayerPointsCondition.class,
			(condition, state) -> matchesNumeric(condition, state.getSkills().getPrayerPoints()),
			(condition, builder) -> builder.requirePrayerPoints(),
			(condition, state) -> state.getSkills().getPrayerPoints() + " prayer");
		register(SpecialAttackCondition.class,
			(condition, state) -> matchesNumeric(condition, state.getVars().getSpecialAttackPercent()),
			(condition, builder) -> builder.requireSpecialAttack(),
			(condition, state) -> state.getVars().getSpecialAttackPercent() + "%");
		register(RunEnergyCondition.class,
			(condition, state) -> matchesNumeric(condition, state.getLocation().getRunEnergyPercent()),
			(condition, builder) -> builder.requireRunEnergy(),
			(condition, state) -> state.getLocation().getRunEnergyPercent() + "%");
		register(PoisonCondition.class, this::matchesPoison, (condition, builder) -> builder.requirePoison(),
			this::formatPoison);
		register(PrayerActiveCondition.class, this::matchesPrayerActive,
			(condition, builder) -> builder.requirePrayer(condition.getPrayer()), this::formatPrayerState);
		register(SlayerTaskCondition.class, this::matchesSlayerTask,
			(condition, builder) -> builder.requireSlayerTask(), this::formatSlayerTask);
		register(SkillLevelCondition.class, this::matchesSkillLevel,
			(condition, builder) -> builder.requireRealSkill(condition.getSkill()), this::formatSkillLevel);
		register(XpGainCondition.class, this::matchesXpGain,
			(condition, builder) -> builder.requireXpGainSkill(condition.getSkill()), this::formatXpGain);
		register(ItemInInventoryCondition.class, this::matchesInventoryItem,
			(condition, builder) -> builder.requireInventoryItem(condition.getItemName()), this::formatInventoryItem);
		register(ItemCountCondition.class, this::matchesItemCount,
			(condition, builder) -> builder.requireInventoryItem(condition.getItemName()), this::formatItemCount);
		register(ItemEquippedCondition.class, this::matchesEquippedItem,
			(condition, builder) -> builder.requireEquippedItem(condition.getItemName()), this::formatEquippedItem);
		register(GroundItemCondition.class, this::matchesGroundItem,
			(condition, builder) -> builder.requireGroundItem(condition.getItemName()), this::formatGroundItem);
		register(PlayerInZoneCondition.class, this::matchesPlayerZone,
			(condition, builder) -> builder.requirePlayerLocation(),
			(condition, state) -> matchesPlayerZone(condition, state) ? "inside" : "outside");
		register(PlayerInInstanceCondition.class,
			(condition, state) -> state.getLocation().isInInstance(),
			(condition, builder) -> builder.requirePlayerInstance(),
			(condition, state) -> state.getLocation().isInInstance() ? "instance" : "world");
	}

	public boolean matches(ConditionDefinition conditionDefinition, RuntimeState runtimeState)
	{
		if (conditionDefinition == null || runtimeState == null)
		{
			return false;
		}

		return getRegistration(conditionDefinition).matches(conditionDefinition, runtimeState);
	}

	public void contributeRequirements(ConditionDefinition conditionDefinition,
		RuntimeConditionRequirements.Builder builder)
	{
		if (conditionDefinition == null)
		{
			return;
		}

		getRegistration(conditionDefinition).contributeRequirements(conditionDefinition, builder);
	}

	public String formatValue(ConditionDefinition conditionDefinition, RuntimeState runtimeState)
	{
		if (conditionDefinition == null || runtimeState == null)
		{
			return null;
		}

		return getRegistration(conditionDefinition).formatValue(conditionDefinition, runtimeState);
	}

	private boolean matchesPoison(PoisonCondition condition, RuntimeState state)
	{
		if (condition.getPoisonType() == null)
		{
			return false;
		}

		switch (condition.getPoisonType())
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

	private String formatPoison(PoisonCondition condition, RuntimeState state)
	{
		return state.getVars().getPoisonState().name().toLowerCase();
	}

	private boolean matchesPrayerActive(PrayerActiveCondition condition, RuntimeState state)
	{
		return condition.isActive() == state.getVars().isPrayerActive(condition.getPrayer());
	}

	private String formatPrayerState(PrayerActiveCondition condition, RuntimeState state)
	{
		return state.getVars().isPrayerActive(condition.getPrayer()) ? "active" : "inactive";
	}

	private boolean matchesSlayerTask(SlayerTaskCondition condition, RuntimeState state)
	{
		if (condition.getCheck() == null)
		{
			return false;
		}

		if (condition.getCheck() == SlayerTaskCondition.SlayerTaskCheck.TASK_ACTIVE)
		{
			return state.getVars().isSlayerTaskActive();
		}

		return matchesNumeric(condition, state.getVars().getSlayerTaskRemaining());
	}

	private String formatSlayerTask(SlayerTaskCondition condition, RuntimeState state)
	{
		if (condition.getCheck() == SlayerTaskCondition.SlayerTaskCheck.KILLS_REMAINING)
		{
			return state.getVars().getSlayerTaskRemaining() + " left";
		}

		return state.getVars().isSlayerTaskActive() ? "active" : "inactive";
	}

	private boolean matchesSkillLevel(SkillLevelCondition condition, RuntimeState state)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return false;
		}

		return matchesNumeric(condition, state.getSkills().getRealSkillLevel(skill));
	}

	private String formatSkillLevel(SkillLevelCondition condition, RuntimeState state)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return null;
		}

		return formatSkill(skill) + " " + state.getSkills().getRealSkillLevel(skill);
	}

	private boolean matchesXpGain(XpGainCondition condition, RuntimeState state)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return false;
		}

		return state.getSkills().hasXpGain(skill);
	}

	private String formatXpGain(XpGainCondition condition, RuntimeState state)
	{
		Skill skill = condition.getSkill();

		if (skill == null)
		{
			return null;
		}

		return formatSkill(skill) + " " + (state.getSkills().hasXpGain(skill) ? "xp" : "idle");
	}

	private boolean matchesInventoryItem(ItemInInventoryCondition condition, RuntimeState state)
	{
		return state.getInventory().hasInventoryItem(condition.getItemName());
	}

	private String formatInventoryItem(ItemInInventoryCondition condition, RuntimeState state)
	{
		return state.getInventory().hasInventoryItem(condition.getItemName()) ? "present" : "missing";
	}

	private boolean matchesItemCount(ItemCountCondition condition, RuntimeState state)
	{
		return matchesNumeric(condition, state.getInventory().getInventoryItemCount(condition.getItemName()));
	}

	private String formatItemCount(ItemCountCondition condition, RuntimeState state)
	{
		return String.valueOf(state.getInventory().getInventoryItemCount(condition.getItemName()));
	}

	private boolean matchesEquippedItem(ItemEquippedCondition condition, RuntimeState state)
	{
		return state.getInventory().hasEquippedItem(condition.getItemName());
	}

	private String formatEquippedItem(ItemEquippedCondition condition, RuntimeState state)
	{
		return state.getInventory().hasEquippedItem(condition.getItemName()) ? "equipped" : "unequipped";
	}

	private boolean matchesGroundItem(GroundItemCondition condition, RuntimeState state)
	{
		return state.getGroundItems().hasNearbyGroundItem(condition.getItemName());
	}

	private String formatGroundItem(GroundItemCondition condition, RuntimeState state)
	{
		return String.valueOf(state.getGroundItems().getNearbyGroundItemCounts().getOrDefault(
			InventoryRuntimeState.normalizeName(condition.getItemName()), 0));
	}

	private boolean matchesPlayerZone(PlayerInZoneCondition condition, RuntimeState state)
	{
		WorldPoint playerLocation = state.getLocation().getPlayerLocation();

		if (playerLocation == null)
		{
			return false;
		}

		return playerLocation.getPlane() == condition.getPlane() &&
			playerLocation.getX() >= condition.getSouthWestX() &&
			playerLocation.getX() <= condition.getNorthEastX() &&
			playerLocation.getY() >= condition.getSouthWestY() &&
			playerLocation.getY() <= condition.getNorthEastY();
	}

	private static boolean matchesNumeric(NumericConditionDefinition condition, int actualValue)
	{
		ComparisonOperator operator = condition.getOperator();
		return operator != null && operator.matches(actualValue, condition.getThreshold());
	}

	private static String formatSkill(Skill skill)
	{
		String lowerCase = skill.name().toLowerCase().replace('_', ' ');
		return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
	}

	@SuppressWarnings("unchecked")
	private <T extends ConditionDefinition> ConditionRuntimeRegistration<T> getRegistration(
		ConditionDefinition conditionDefinition)
	{
		ConditionRuntimeRegistration<?> registration = registrations.get(conditionDefinition.getClass());

		if (registration == null)
		{
			throw new IllegalArgumentException("Unsupported condition definition: " + conditionDefinition.getClass());
		}

		return (ConditionRuntimeRegistration<T>) registration;
	}

	private <T extends ConditionDefinition> void register(Class<T> conditionType,
		BiFunction<T, RuntimeState, Boolean> matcher,
		BiConsumer<T, RuntimeConditionRequirements.Builder> requirementContributor,
		BiFunction<T, RuntimeState, String> valueFormatter)
	{
		registrations.put(conditionType,
			new ConditionRuntimeRegistration<>(conditionType, matcher, requirementContributor, valueFormatter));
	}

	private static final class ConditionRuntimeRegistration<T extends ConditionDefinition>
	{
		private final Class<T> conditionType;
		private final BiFunction<T, RuntimeState, Boolean> matcher;
		private final BiConsumer<T, RuntimeConditionRequirements.Builder> requirementContributor;
		private final BiFunction<T, RuntimeState, String> valueFormatter;

		private ConditionRuntimeRegistration(Class<T> conditionType, BiFunction<T, RuntimeState, Boolean> matcher,
			BiConsumer<T, RuntimeConditionRequirements.Builder> requirementContributor,
			BiFunction<T, RuntimeState, String> valueFormatter)
		{
			this.conditionType = conditionType;
			this.matcher = matcher;
			this.requirementContributor = requirementContributor;
			this.valueFormatter = valueFormatter;
		}

		private boolean matches(ConditionDefinition conditionDefinition, RuntimeState runtimeState)
		{
			return matcher.apply(conditionType.cast(conditionDefinition), runtimeState);
		}

		private void contributeRequirements(ConditionDefinition conditionDefinition,
			RuntimeConditionRequirements.Builder builder)
		{
			requirementContributor.accept(conditionType.cast(conditionDefinition), builder);
		}

		private String formatValue(ConditionDefinition conditionDefinition, RuntimeState runtimeState)
		{
			return valueFormatter.apply(conditionType.cast(conditionDefinition), runtimeState);
		}
	}
}
