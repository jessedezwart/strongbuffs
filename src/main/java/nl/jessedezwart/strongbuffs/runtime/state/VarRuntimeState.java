package nl.jessedezwart.strongbuffs.runtime.state;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.Prayer;

public class VarRuntimeState
{
	private int specialAttackPercent;
	private RuntimeState.PoisonState poisonState = RuntimeState.PoisonState.NONE;
	private boolean slayerTaskActive;
	private int slayerTaskRemaining;
	private final Set<Prayer> activePrayers = EnumSet.noneOf(Prayer.class);

	public int getSpecialAttackPercent()
	{
		return specialAttackPercent;
	}

	public void setSpecialAttackPercent(int specialAttackPercent)
	{
		this.specialAttackPercent = specialAttackPercent;
	}

	public RuntimeState.PoisonState getPoisonState()
	{
		return poisonState;
	}

	public void setPoisonState(RuntimeState.PoisonState poisonState)
	{
		this.poisonState = poisonState == null ? RuntimeState.PoisonState.NONE : poisonState;
	}

	public boolean isSlayerTaskActive()
	{
		return slayerTaskActive;
	}

	public void setSlayerTaskActive(boolean slayerTaskActive)
	{
		this.slayerTaskActive = slayerTaskActive;
	}

	public int getSlayerTaskRemaining()
	{
		return slayerTaskRemaining;
	}

	public void setSlayerTaskRemaining(int slayerTaskRemaining)
	{
		this.slayerTaskRemaining = slayerTaskRemaining;
	}

	public boolean isPrayerActive(Prayer prayer)
	{
		return prayer != null && activePrayers.contains(prayer);
	}

	public void setPrayerActive(Prayer prayer, boolean active)
	{
		if (prayer == null)
		{
			return;
		}

		if (active)
		{
			activePrayers.add(prayer);
		}
		else
		{
			activePrayers.remove(prayer);
		}
	}

	public void setActivePrayers(Collection<Prayer> prayers)
	{
		activePrayers.clear();

		if (prayers != null)
		{
			activePrayers.addAll(prayers);
		}
	}

	public Set<Prayer> getActivePrayers()
	{
		return Collections.unmodifiableSet(activePrayers);
	}

	public void clear()
	{
		specialAttackPercent = 0;
		poisonState = RuntimeState.PoisonState.NONE;
		slayerTaskActive = false;
		slayerTaskRemaining = 0;
		activePrayers.clear();
	}
}
