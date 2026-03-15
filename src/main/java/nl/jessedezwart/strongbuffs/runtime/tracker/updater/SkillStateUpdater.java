package nl.jessedezwart.strongbuffs.runtime.tracker.updater;

import java.util.EnumSet;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

@Singleton
public class SkillStateUpdater
{
	public void refresh(RuntimeState runtimeState, RuntimeConditionRequirements requirements, Client client)
	{
		if (requirements.tracksHitpoints())
		{
			runtimeState.getSkills().setHitpoints(client.getBoostedSkillLevel(Skill.HITPOINTS));
		}

		if (requirements.tracksPrayerPoints())
		{
			runtimeState.getSkills().setPrayerPoints(client.getBoostedSkillLevel(Skill.PRAYER));
		}

		for (Skill skill : requirements.getRealSkills())
		{
			runtimeState.getSkills().setRealSkillLevel(skill, client.getRealSkillLevel(skill));
		}
	}

	public EnumSet<RuntimeTrigger> onStatChanged(RuntimeState runtimeState, RuntimeConditionRequirements requirements,
		StatChanged event)
	{
		Skill skill = event.getSkill();
		EnumSet<RuntimeTrigger> triggers = EnumSet.noneOf(RuntimeTrigger.class);

		if (requirements.tracksHitpoints() && skill == Skill.HITPOINTS)
		{
			runtimeState.getSkills().setHitpoints(event.getBoostedLevel());
			triggers.add(RuntimeTrigger.HITPOINTS);
		}

		if (requirements.tracksPrayerPoints() && skill == Skill.PRAYER)
		{
			runtimeState.getSkills().setPrayerPoints(event.getBoostedLevel());
			triggers.add(RuntimeTrigger.PRAYER_POINTS);
		}

		if (requirements.getRealSkills().contains(skill))
		{
			runtimeState.getSkills().setRealSkillLevel(skill, event.getLevel());
			triggers.add(RuntimeTrigger.REAL_SKILL);
		}

		if (requirements.getXpGainSkills().contains(skill))
		{
			runtimeState.getSkills().markXpGain(skill);
			triggers.add(RuntimeTrigger.XP_GAIN);
		}

		return triggers;
	}
}
