package nl.jessedezwart.strongbuffs.runtime.state.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Skill;

public class SkillRuntimeState
{
	private int hitpoints;
	private int prayerPoints;
	private int currentTick;
	private final Map<Skill, Integer> realSkillLevels = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> xpGainTicks = new EnumMap<>(Skill.class);

	public int getHitpoints()
	{
		return hitpoints;
	}

	public void setHitpoints(int hitpoints)
	{
		this.hitpoints = hitpoints;
	}

	public int getPrayerPoints()
	{
		return prayerPoints;
	}

	public void setPrayerPoints(int prayerPoints)
	{
		this.prayerPoints = prayerPoints;
	}

	public int getCurrentTick()
	{
		return currentTick;
	}

	public void setCurrentTick(int currentTick)
	{
		this.currentTick = currentTick;
	}

	public int getRealSkillLevel(Skill skill)
	{
		if (skill == null)
		{
			return 0;
		}

		return realSkillLevels.getOrDefault(skill, 0);
	}

	public void setRealSkillLevel(Skill skill, int level)
	{
		if (skill != null)
		{
			realSkillLevels.put(skill, level);
		}
	}

	public Map<Skill, Integer> getRealSkillLevels()
	{
		return Collections.unmodifiableMap(realSkillLevels);
	}

	public boolean hasXpGain(Skill skill)
	{
		return skill != null && xpGainTicks.getOrDefault(skill, -1) == currentTick;
	}

	public void markXpGain(Skill skill)
	{
		if (skill != null)
		{
			xpGainTicks.put(skill, currentTick);
		}
	}

	public void clear()
	{
		hitpoints = 0;
		prayerPoints = 0;
		currentTick = 0;
		realSkillLevels.clear();
		xpGainTicks.clear();
	}
}
