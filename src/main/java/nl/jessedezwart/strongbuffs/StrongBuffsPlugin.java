package nl.jessedezwart.strongbuffs;

import com.google.inject.Provides;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import nl.jessedezwart.strongbuffs.panel.view.StrongBuffsPanel;
import nl.jessedezwart.strongbuffs.runtime.RuleRuntimeController;

@Slf4j
@PluginDescriptor(name = "StrongBuffs", description = "A WeakAuras-like plugin for RuneLite. Only supports explicitly approved features.", tags =
{ "overlay", "buffs", "rule", "timers", "alerts", "weakauras" })
public class StrongBuffsPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private StrongBuffsPanel strongBuffsPanel;

	@Inject
	private RuleRuntimeController ruleRuntimeController;

	private NavigationButton navigationButton;

	@Override
	protected void startUp() throws Exception
	{
		strongBuffsPanel.reload();
		ruleRuntimeController.startUp();
		SwingUtilities.updateComponentTreeUI(strongBuffsPanel.getWrappedPanel());
		navigationButton = NavigationButton.builder().tooltip("Strong Buffs").icon(createNavigationIcon()).priority(6)
				.panel(strongBuffsPanel).build();
		clientToolbar.addNavigation(navigationButton);
		log.debug("StrongBuffs started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		ruleRuntimeController.shutDown();

		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
			navigationButton = null;
		}

		log.debug("StrongBuffs stopped!");
	}

	@Provides
	StrongBuffsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StrongBuffsConfig.class);
	}

	private static BufferedImage createNavigationIcon()
	{
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = icon.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.setColor(new Color(32, 32, 32, 220));
		graphics.fillRoundRect(1, 1, 14, 14, 4, 4);

		graphics.setColor(new Color(255, 153, 0));
		graphics.setStroke(new BasicStroke(2f));
		graphics.drawRoundRect(1, 1, 14, 14, 4, 4);

		graphics.setColor(Color.WHITE);
		graphics.fillRect(4, 9, 2, 3);
		graphics.fillRect(7, 6, 2, 6);
		graphics.fillRect(10, 4, 2, 8);
		graphics.dispose();
		return icon;
	}
}
