package nl.jessedezwart.strongbuffs.runtime.condition.util;

import net.runelite.api.Skill;

public final class FormatterUtil
{
	private FormatterUtil()
	{
	}

	public static String format(Skill skill)
	{
		String lowerCase = skill.name().toLowerCase().replace('_', ' ');
		return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
	}
}
