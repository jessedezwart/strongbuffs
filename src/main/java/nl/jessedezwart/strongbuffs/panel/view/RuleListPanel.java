package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import net.runelite.client.ui.ColorScheme;

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

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		actions.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton newButton = new JButton("New Rule");
		newButton.addActionListener(event -> onCreateRule.run());
		actions.add(newButton);

		duplicateButton.addActionListener(event -> onDuplicateRule.run());
		actions.add(duplicateButton);

		deleteButton.addActionListener(event -> onDeleteRule.run());
		actions.add(deleteButton);

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

			RuleDefinition selected = (RuleDefinition) ruleSelector.getSelectedItem();
			onSelectRule.accept(selected == null ? null : selected.getId());
		});

		ruleSelector.setPreferredSize(new Dimension(0, ruleSelector.getPreferredSize().height));
		ruleSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, ruleSelector.getPreferredSize().height));
		add(ruleSelector, BorderLayout.CENTER);
	}

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
