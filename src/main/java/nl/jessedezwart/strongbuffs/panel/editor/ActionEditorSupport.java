package nl.jessedezwart.strongbuffs.panel.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;

/**
 * Shared Swing helpers used by both condition and action editor registries.
 */
public final class ActionEditorSupport
{
	private ActionEditorSupport()
	{
	}

	public static JPanel createVerticalPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		return panel;
	}

	public static JPanel labeled(String label, JComponent component)
	{
		JPanel row = new JPanel(new BorderLayout(8, 0));
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		JLabel title = new JLabel(label);
		title.setForeground(ColorScheme.TEXT_COLOR);
		title.setPreferredSize(new Dimension(52, title.getPreferredSize().height));
		row.add(title, BorderLayout.WEST);
		component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
		row.add(component, BorderLayout.CENTER);
		return row;
	}

	public static DocumentListener documentListener(Runnable callback)
	{
		return new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent event)
			{
				callback.run();
			}

			@Override
			public void removeUpdate(DocumentEvent event)
			{
				callback.run();
			}

			@Override
			public void changedUpdate(DocumentEvent event)
			{
				callback.run();
			}
		};
	}
}
