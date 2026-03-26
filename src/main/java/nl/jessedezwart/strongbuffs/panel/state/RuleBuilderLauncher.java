package nl.jessedezwart.strongbuffs.panel.state;

import java.awt.Desktop;
import java.net.URI;
import javax.inject.Singleton;

/**
 * Opens the hosted static rule builder in the user's browser.
 */
@Singleton
public class RuleBuilderLauncher
{
	static final String RULE_BUILDER_URL = "https://jessedezwart.github.io/strongbuffs/";

	public boolean openRuleBuilder()
	{
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			return false;
		}

		try
		{
			Desktop.getDesktop().browse(URI.create(RULE_BUILDER_URL));
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public String getRuleBuilderUrl()
	{
		return RULE_BUILDER_URL;
	}
}
