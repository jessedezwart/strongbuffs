package nl.jessedezwart.strongbuffs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class StrongBuffsPluginTest
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(StrongBuffsPlugin.class);
		RuneLite.main(args);
	}
}
