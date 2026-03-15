package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.runtime.state.InventoryRuntimeState;

public final class RuntimeConditionRequirements
{
	private static final RuntimeConditionRequirements EMPTY = builder().build();

	private final boolean hitpoints;
	private final boolean prayerPoints;
	private final boolean specialAttack;
	private final boolean runEnergy;
	private final boolean poison;
	private final boolean slayerTask;
	private final boolean playerLocation;
	private final boolean playerInstance;
	private final Set<Prayer> prayers;
	private final Set<Skill> realSkills;
	private final Set<Skill> xpGainSkills;
	private final Set<String> inventoryItems;
	private final Set<String> equippedItems;
	private final Set<String> groundItems;

	private RuntimeConditionRequirements(boolean hitpoints, boolean prayerPoints, boolean specialAttack,
		boolean runEnergy, boolean poison, boolean slayerTask, boolean playerLocation, boolean playerInstance,
		Set<Prayer> prayers, Set<Skill> realSkills, Set<Skill> xpGainSkills, Set<String> inventoryItems,
		Set<String> equippedItems, Set<String> groundItems)
	{
		this.hitpoints = hitpoints;
		this.prayerPoints = prayerPoints;
		this.specialAttack = specialAttack;
		this.runEnergy = runEnergy;
		this.poison = poison;
		this.slayerTask = slayerTask;
		this.playerLocation = playerLocation;
		this.playerInstance = playerInstance;
		this.prayers = Collections.unmodifiableSet(prayers);
		this.realSkills = Collections.unmodifiableSet(realSkills);
		this.xpGainSkills = Collections.unmodifiableSet(xpGainSkills);
		this.inventoryItems = Collections.unmodifiableSet(inventoryItems);
		this.equippedItems = Collections.unmodifiableSet(equippedItems);
		this.groundItems = Collections.unmodifiableSet(groundItems);
	}

	public static RuntimeConditionRequirements empty()
	{
		return EMPTY;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public boolean tracksHitpoints()
	{
		return hitpoints;
	}

	public boolean tracksPrayerPoints()
	{
		return prayerPoints;
	}

	public boolean tracksSpecialAttack()
	{
		return specialAttack;
	}

	public boolean tracksRunEnergy()
	{
		return runEnergy;
	}

	public boolean tracksPoison()
	{
		return poison;
	}

	public boolean tracksSlayerTask()
	{
		return slayerTask;
	}

	public boolean tracksPlayerLocation()
	{
		return playerLocation;
	}

	public boolean tracksPlayerInstance()
	{
		return playerInstance;
	}

	public Set<Prayer> getPrayers()
	{
		return prayers;
	}

	public Set<Skill> getRealSkills()
	{
		return realSkills;
	}

	public Set<Skill> getXpGainSkills()
	{
		return xpGainSkills;
	}

	public Set<String> getInventoryItems()
	{
		return inventoryItems;
	}

	public Set<String> getEquippedItems()
	{
		return equippedItems;
	}

	public Set<String> getGroundItems()
	{
		return groundItems;
	}

	public boolean needsGameTick()
	{
		return runEnergy || playerLocation || playerInstance || !xpGainSkills.isEmpty();
	}

	public boolean hasInventoryTracking()
	{
		return !inventoryItems.isEmpty();
	}

	public boolean hasEquipmentTracking()
	{
		return !equippedItems.isEmpty();
	}

	public boolean hasGroundItemTracking()
	{
		return !groundItems.isEmpty();
	}

	public static final class Builder
	{
		private boolean hitpoints;
		private boolean prayerPoints;
		private boolean specialAttack;
		private boolean runEnergy;
		private boolean poison;
		private boolean slayerTask;
		private boolean playerLocation;
		private boolean playerInstance;
		private final Set<Prayer> prayers = EnumSet.noneOf(Prayer.class);
		private final Set<Skill> realSkills = EnumSet.noneOf(Skill.class);
		private final Set<Skill> xpGainSkills = EnumSet.noneOf(Skill.class);
		private final Set<String> inventoryItems = new LinkedHashSet<>();
		private final Set<String> equippedItems = new LinkedHashSet<>();
		private final Set<String> groundItems = new LinkedHashSet<>();

		public Builder requireHitpoints()
		{
			hitpoints = true;
			return this;
		}

		public Builder requirePrayerPoints()
		{
			prayerPoints = true;
			return this;
		}

		public Builder requireSpecialAttack()
		{
			specialAttack = true;
			return this;
		}

		public Builder requireRunEnergy()
		{
			runEnergy = true;
			return this;
		}

		public Builder requirePoison()
		{
			poison = true;
			return this;
		}

		public Builder requireSlayerTask()
		{
			slayerTask = true;
			return this;
		}

		public Builder requirePlayerLocation()
		{
			playerLocation = true;
			return this;
		}

		public Builder requirePlayerInstance()
		{
			playerInstance = true;
			return this;
		}

		public Builder requirePrayer(Prayer prayer)
		{
			if (prayer != null)
			{
				prayers.add(prayer);
			}

			return this;
		}

		public Builder requireRealSkill(Skill skill)
		{
			if (skill != null)
			{
				realSkills.add(skill);
			}

			return this;
		}

		public Builder requireXpGainSkill(Skill skill)
		{
			if (skill != null)
			{
				xpGainSkills.add(skill);
			}

			return this;
		}

		public Builder requireInventoryItem(String itemName)
		{
			addName(inventoryItems, itemName);
			return this;
		}

		public Builder requireEquippedItem(String itemName)
		{
			addName(equippedItems, itemName);
			return this;
		}

		public Builder requireGroundItem(String itemName)
		{
			addName(groundItems, itemName);
			return this;
		}

		public RuntimeConditionRequirements build()
		{
			return new RuntimeConditionRequirements(hitpoints, prayerPoints, specialAttack, runEnergy, poison,
				slayerTask, playerLocation, playerInstance, copyPrayers(), copyRealSkills(), copyXpGainSkills(),
				new LinkedHashSet<>(inventoryItems), new LinkedHashSet<>(equippedItems), new LinkedHashSet<>(groundItems));
		}

		public Builder merge(RuntimeConditionRequirements requirements)
		{
			if (requirements == null)
			{
				return this;
			}

			if (requirements.tracksHitpoints())
			{
				requireHitpoints();
			}

			if (requirements.tracksPrayerPoints())
			{
				requirePrayerPoints();
			}

			if (requirements.tracksSpecialAttack())
			{
				requireSpecialAttack();
			}

			if (requirements.tracksRunEnergy())
			{
				requireRunEnergy();
			}

			if (requirements.tracksPoison())
			{
				requirePoison();
			}

			if (requirements.tracksSlayerTask())
			{
				requireSlayerTask();
			}

			if (requirements.tracksPlayerLocation())
			{
				requirePlayerLocation();
			}

			if (requirements.tracksPlayerInstance())
			{
				requirePlayerInstance();
			}

			for (Prayer prayer : requirements.getPrayers())
			{
				requirePrayer(prayer);
			}

			for (Skill skill : requirements.getRealSkills())
			{
				requireRealSkill(skill);
			}

			for (Skill skill : requirements.getXpGainSkills())
			{
				requireXpGainSkill(skill);
			}

			for (String itemName : requirements.getInventoryItems())
			{
				requireInventoryItem(itemName);
			}

			for (String itemName : requirements.getEquippedItems())
			{
				requireEquippedItem(itemName);
			}

			for (String itemName : requirements.getGroundItems())
			{
				requireGroundItem(itemName);
			}

			return this;
		}

		private static void addName(Set<String> target, String value)
		{
			String normalized = InventoryRuntimeState.normalizeName(value);

			if (normalized != null)
			{
				target.add(normalized);
			}
		}

		private Set<Prayer> copyPrayers()
		{
			return prayers.isEmpty() ? EnumSet.noneOf(Prayer.class) : EnumSet.copyOf(prayers);
		}

		private Set<Skill> copyRealSkills()
		{
			return realSkills.isEmpty() ? EnumSet.noneOf(Skill.class) : EnumSet.copyOf(realSkills);
		}

		private Set<Skill> copyXpGainSkills()
		{
			return xpGainSkills.isEmpty() ? EnumSet.noneOf(Skill.class) : EnumSet.copyOf(xpGainSkills);
		}
	}
}
