package nl.jessedezwart.strongbuffs.runtime.action;

import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
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
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.InventoryRuntimeState;

public class RuntimeValueFormatter
{
	public String format(ConditionGroup rootGroup, RuntimeState runtimeState)
	{
		ConditionDefinition primaryCondition = findPrimaryCondition(rootGroup);

		if (primaryCondition == null || runtimeState == null)
		{
			return null;
		}

		if (primaryCondition instanceof HpCondition)
		{
			return runtimeState.getSkills().getHitpoints() + " hp";
		}

		if (primaryCondition instanceof PrayerPointsCondition)
		{
			return runtimeState.getSkills().getPrayerPoints() + " prayer";
		}

		if (primaryCondition instanceof SpecialAttackCondition)
		{
			return runtimeState.getVars().getSpecialAttackPercent() + "%";
		}

		if (primaryCondition instanceof RunEnergyCondition)
		{
			return runtimeState.getLocation().getRunEnergyPercent() + "%";
		}

		if (primaryCondition instanceof PoisonCondition)
		{
			return runtimeState.getVars().getPoisonState().name().toLowerCase();
		}

		if (primaryCondition instanceof PrayerActiveCondition)
		{
			PrayerActiveCondition condition = (PrayerActiveCondition) primaryCondition;
			return runtimeState.getVars().isPrayerActive(condition.getPrayer()) ? "active" : "inactive";
		}

		if (primaryCondition instanceof SlayerTaskCondition)
		{
			SlayerTaskCondition condition = (SlayerTaskCondition) primaryCondition;

			if (condition.getCheck() == SlayerTaskCondition.SlayerTaskCheck.KILLS_REMAINING)
			{
				return runtimeState.getVars().getSlayerTaskRemaining() + " left";
			}

			return runtimeState.getVars().isSlayerTaskActive() ? "active" : "inactive";
		}

		if (primaryCondition instanceof SkillLevelCondition)
		{
			SkillLevelCondition condition = (SkillLevelCondition) primaryCondition;
			Skill skill = condition.getSkill();
			return skill == null ? null : formatSkill(skill) + " " + runtimeState.getSkills().getRealSkillLevel(skill);
		}

		if (primaryCondition instanceof XpGainCondition)
		{
			XpGainCondition condition = (XpGainCondition) primaryCondition;
			Skill skill = condition.getSkill();
			return skill == null ? null : formatSkill(skill) + " " +
				(runtimeState.getSkills().hasXpGain(skill) ? "xp" : "idle");
		}

		if (primaryCondition instanceof ItemInInventoryCondition)
		{
			ItemInInventoryCondition condition = (ItemInInventoryCondition) primaryCondition;
			return runtimeState.getInventory().hasInventoryItem(condition.getItemName()) ? "present" : "missing";
		}

		if (primaryCondition instanceof ItemCountCondition)
		{
			ItemCountCondition condition = (ItemCountCondition) primaryCondition;
			return String.valueOf(runtimeState.getInventory().getInventoryItemCount(condition.getItemName()));
		}

		if (primaryCondition instanceof ItemEquippedCondition)
		{
			ItemEquippedCondition condition = (ItemEquippedCondition) primaryCondition;
			return runtimeState.getInventory().hasEquippedItem(condition.getItemName()) ? "equipped" : "unequipped";
		}

		if (primaryCondition instanceof GroundItemCondition)
		{
			GroundItemCondition condition = (GroundItemCondition) primaryCondition;
			return String.valueOf(runtimeState.getGroundItems().getNearbyGroundItemCounts()
				.getOrDefault(InventoryRuntimeState.normalizeName(condition.getItemName()), 0));
		}

		if (primaryCondition instanceof PlayerInZoneCondition)
		{
			return ((PlayerInZoneCondition) primaryCondition).matches(runtimeState) ? "inside" : "outside";
		}

		if (primaryCondition instanceof PlayerInInstanceCondition)
		{
			return runtimeState.getLocation().isInInstance() ? "instance" : "world";
		}

		return null;
	}

	private ConditionDefinition findPrimaryCondition(ConditionNode node)
	{
		if (node instanceof ConditionDefinition)
		{
			return (ConditionDefinition) node;
		}

		if (node instanceof ConditionGroup)
		{
			for (ConditionNode child : ((ConditionGroup) node).getChildren())
			{
				ConditionDefinition primary = findPrimaryCondition(child);

				if (primary != null)
				{
					return primary;
				}
			}
		}

		return null;
	}

	private static String formatSkill(Skill skill)
	{
		String lowerCase = skill.name().toLowerCase().replace('_', ' ');
		return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
	}
}
