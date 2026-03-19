package nl.jessedezwart.strongbuffs.runtime.condition.utils;

import net.runelite.api.Skill;

public final class FormatterUtils
{
	private FormatterUtils()
	{
	}

	public static String format(Skill skill)
	{
		String lowerCase = skill.name().toLowerCase().replace('_', ' ');
		return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
	}
}
