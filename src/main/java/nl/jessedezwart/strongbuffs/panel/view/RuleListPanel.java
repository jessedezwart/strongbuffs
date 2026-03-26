package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
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
	private final DefaultListModel<RuleDefinition> listModel = new DefaultListModel<>();
	private final JComboBox<RuleDefinition> ruleSelector = new JComboBox<>();
	private final JButton duplicateButton = new JButton("Duplicate");
	private final JButton deleteButton = new JButton("Delete");
	private boolean suppressSelectionEvents;

	public RuleListPanel(Consumer<String> onSelectRule, Runnable onCreateRule, Runnable onDuplicateRule,
			Runnable onDeleteRule)
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

		JPanel secondaryActions = new JPanel(new GridLayout(1, 2, 6, 0));
		secondaryActions.setBackground(ColorScheme.DARK_GRAY_COLOR);

		duplicateButton.addActionListener(event -> onDuplicateRule.run());
		secondaryActions.add(duplicateButton);

		deleteButton.addActionListener(event -> onDeleteRule.run());
		secondaryActions.add(deleteButton);

		secondaryActions.setAlignmentX(LEFT_ALIGNMENT);
		secondaryActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, secondaryActions.getPreferredSize().height));
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
		listModel.clear();
		ruleSelector.removeAllItems();

		for (RuleDefinition ruleDefinition : rules)
		{
			listModel.addElement(ruleDefinition);
			ruleSelector.addItem(ruleDefinition);
		}

		if (selectedRuleId == null)
		{
			ruleSelector.setSelectedItem(null);
		}
		else
		{
			for (int i = 0; i < listModel.size(); i++)
			{
				if (selectedRuleId.equals(listModel.get(i).getId()))
				{
					ruleSelector.setSelectedItem(listModel.get(i));
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
