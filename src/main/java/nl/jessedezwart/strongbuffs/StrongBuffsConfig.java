package nl.jessedezwart.strongbuffs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;

/**
 * RuneLite config group marker for persisted plugin settings.
 *
 * <p>The rule list itself is stored as JSON through {@link RuleDefinitionStore}; this interface is
 * primarily the hook ConfigManager uses to resolve the plugin's config group.</p>
 */
@ConfigGroup("strongbuffs")
public interface StrongBuffsConfig extends Config
{
}
