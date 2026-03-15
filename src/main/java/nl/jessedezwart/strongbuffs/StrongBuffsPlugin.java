package nl.jessedezwart.strongbuffs;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "StrongBuffs",
	description = "A WeakAuras-like plugin for RuneLite. Only supports explicitly approved features.",
	tags = {"overlay", "buffs", "aura", "timers", "alerts", "weakauras"}
)
public class StrongBuffsPlugin extends Plugin
{
	@Inject
	private StrongBuffsConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("StrongBuffs started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("StrongBuffs stopped!");
	}

	@Provides
	StrongBuffsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StrongBuffsConfig.class);
	}
}
