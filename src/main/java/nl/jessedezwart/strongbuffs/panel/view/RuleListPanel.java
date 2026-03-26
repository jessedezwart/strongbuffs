package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import net.runelite.client.ui.ColorScheme;

/**
 * Scrollable list of visible rules shown at the top of the sidebar.
 */
public class RuleListPanel extends JPanel
{
	private final JComboBox<RuleDefinition> ruleSelector = new JComboBox<>();
	private final JButton importButton = new JButton("Import JSON");
	private final JButton duplicateButton = new JButton("Duplicate");
	private final JButton deleteButton = new JButton("Delete");
	private boolean suppressSelectionEvents;

	public RuleListPanel(Consumer<String> onSelectRule, Runnable onCreateRule, Runnable onImportRule,
		Runnable onDuplicateRule, Runnable onDeleteRule)
	{
		setLayout(new BorderLayout(0, 6));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createTitledBorder("Rules"));

		JPanel actions = new JPanel();
		actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
		actions.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton newButton = new JButton("New Rule");
		newButton.addActionListener(event -> onCreateRule.run());
		newButton.setAlignmentX(LEFT_ALIGNMENT);
		newButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, newButton.getPreferredSize().height));
		actions.add(newButton);
		actions.add(Box.createVerticalStrut(6));

		importButton.addActionListener(event -> onImportRule.run());
		importButton.setAlignmentX(LEFT_ALIGNMENT);
		importButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, importButton.getPreferredSize().height));
		actions.add(importButton);
		actions.add(Box.createVerticalStrut(6));

		JPanel secondaryActions = new JPanel();
		secondaryActions.setLayout(new BoxLayout(secondaryActions, BoxLayout.Y_AXIS));
		secondaryActions.setBackground(ColorScheme.DARK_GRAY_COLOR);

		duplicateButton.setAlignmentX(LEFT_ALIGNMENT);
		duplicateButton.addActionListener(event -> onDuplicateRule.run());
		secondaryActions.add(duplicateButton);
		secondaryActions.add(Box.createVerticalStrut(6));

		deleteButton.setAlignmentX(LEFT_ALIGNMENT);
		deleteButton.addActionListener(event -> onDeleteRule.run());
		secondaryActions.add(deleteButton);

		secondaryActions.setAlignmentX(LEFT_ALIGNMENT);
		secondaryActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		actions.add(secondaryActions);

		add(actions, BorderLayout.NORTH);

		ruleSelector.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		ruleSelector.setForeground(ColorScheme.TEXT_COLOR);
		ruleSelector.setRenderer((list, value, index, isSelected, cellHasFocus) ->
		{
			JLabel label = (JLabel) new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(list,
					ruleLabel(value), index, isSelected, cellHasFocus);
			label.setForeground(ColorScheme.TEXT_COLOR);
			return label;
		});
		ruleSelector.addActionListener(event ->
		{
			if (suppressSelectionEvents)
			{
				return;
			}

			int selectedIndex = ruleSelector.getSelectedIndex();
			RuleDefinition selected = selectedIndex < 0 ? null : ruleSelector.getItemAt(selectedIndex);
			onSelectRule.accept(selected == null ? null : selected.getId());
		});

		ruleSelector.setPreferredSize(new Dimension(0, ruleSelector.getPreferredSize().height));
		ruleSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, ruleSelector.getPreferredSize().height));
		add(ruleSelector, BorderLayout.CENTER);
	}

	/**
	 * Refreshes the list contents and selection from controller state.
	 */
	public void refresh(List<RuleDefinition> rules, String selectedRuleId)
	{
		suppressSelectionEvents = true;
		ruleSelector.removeAllItems();

		for (RuleDefinition ruleDefinition : rules)
		{
			ruleSelector.addItem(ruleDefinition);
		}

		if (selectedRuleId == null)
		{
			ruleSelector.setSelectedItem(null);
		}
		else
		{
			for (int i = 0; i < ruleSelector.getItemCount(); i++)
			{
				RuleDefinition ruleDefinition = ruleSelector.getItemAt(i);

				if (selectedRuleId.equals(ruleDefinition.getId()))
				{
					ruleSelector.setSelectedItem(ruleDefinition);
					break;
				}
			}
		}

		boolean hasSelection = selectedRuleId != null;
		duplicateButton.setEnabled(hasSelection);
		deleteButton.setEnabled(hasSelection);
		suppressSelectionEvents = false;
	}

	private static String ruleLabel(RuleDefinition ruleDefinition)
	{
		if (ruleDefinition == null || ruleDefinition.getName() == null || ruleDefinition.getName().trim().isEmpty())
		{
			return "Unnamed rule";
		}

		return ruleDefinition.getName();
	}
}
