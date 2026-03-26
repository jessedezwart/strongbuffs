package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.panel.state.RuleBuilderLauncher;
import nl.jessedezwart.strongbuffs.panel.state.RuleImportResult;
import nl.jessedezwart.strongbuffs.panel.state.RuleRepository;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Root RuneLite sidebar panel for managing persisted rule definitions.
 */
@Singleton
public class StrongBuffsPanel extends PluginPanel
{
	private final RuleRepository ruleRepository;
	private final RuleBuilderLauncher ruleBuilderLauncher;
	private final RuleListPanel ruleListPanel;
	private String selectedRuleId;

	@Inject
	public StrongBuffsPanel(RuleRepository ruleRepository, RuleBuilderLauncher ruleBuilderLauncher)
	{
		super(true);
		this.ruleRepository = ruleRepository;
		this.ruleBuilderLauncher = ruleBuilderLauncher;

		JPanel wrappedPanel = getWrappedPanel();
		wrappedPanel.setLayout(new BorderLayout());
		wrappedPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrappedPanel.add(content, BorderLayout.NORTH);

		ruleListPanel = new RuleListPanel(this::selectRule, this::openRuleBuilder, this::importRule, this::duplicateRule,
			this::deleteRule);
		content.add(ruleListPanel);
		content.add(Box.createVerticalStrut(6));
		content.add(createInfoPanel());

		refreshFromRepository();
	}

	/**
	 * Reloads persisted rules and refreshes the list.
	 */
	public void reload()
	{
		ruleRepository.reload();
		refreshFromRepository();
	}

	private void selectRule(String ruleId)
	{
		selectedRuleId = ruleId;
		refreshFromRepository();
	}

	private void openRuleBuilder()
	{
		if (!ruleBuilderLauncher.openRuleBuilder())
		{
			JOptionPane.showMessageDialog(this,
				"Open the rule builder in your browser:\n" + ruleBuilderLauncher.getRuleBuilderUrl(),
				"Rule Builder", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void duplicateRule()
	{
		if (selectedRuleId == null)
		{
			return;
		}

		RuleDefinition duplicatedRule = ruleRepository.duplicateRule(selectedRuleId);

		if (duplicatedRule != null)
		{
			selectedRuleId = duplicatedRule.getId();
			refreshFromRepository();
		}
	}

	private void deleteRule()
	{
		if (selectedRuleId == null)
		{
			return;
		}

		int choice = JOptionPane.showConfirmDialog(this, "Delete the selected rule?", "Delete Rule",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (choice != JOptionPane.YES_OPTION)
		{
			return;
		}

		ruleRepository.deleteRule(selectedRuleId);
		selectedRuleId = null;
		refreshFromRepository();
	}

	private void importRule()
	{
		JTextArea textArea = new JTextArea(18, 30);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(360, 280));

		int choice = JOptionPane.showConfirmDialog(this, scrollPane, "Import Rule JSON",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (choice != JOptionPane.OK_OPTION)
		{
			return;
		}

		RuleImportResult result = ruleRepository.importRuleJson(textArea.getText());

		if (!result.isSuccess())
		{
			JOptionPane.showMessageDialog(this, result.getErrorMessage(), "Import Failed",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		selectedRuleId = result.getImportedRule().getId();
		refreshFromRepository();
	}

	private JPanel createInfoPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		javax.swing.JLabel label = new javax.swing.JLabel(
			"<html><body style='width:180px'>Build new rules on the hosted website, then paste the JSON here with Import JSON.</body></html>");
		label.setForeground(ColorScheme.TEXT_COLOR);
		label.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(label);
		return panel;
	}

	private void refreshFromRepository()
	{
		List<RuleDefinition> rules = ruleRepository.getAll();

		if (rules.isEmpty())
		{
			selectedRuleId = null;
		}
		else if (!containsRule(rules, selectedRuleId))
		{
			selectedRuleId = rules.get(0).getId();
		}

		ruleListPanel.refresh(rules, selectedRuleId);
		SwingUtilities.invokeLater(() ->
		{
			revalidate();
			repaint();
		});
	}

	private static boolean containsRule(List<RuleDefinition> rules, String ruleId)
	{
		if (ruleId == null)
		{
			return false;
		}

		for (RuleDefinition rule : rules)
		{
			if (ruleId.equals(rule.getId()))
			{
				return true;
			}
		}

		return false;
	}
}
