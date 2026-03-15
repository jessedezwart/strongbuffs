package nl.jessedezwart.strongbuffs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.state.RulePanelController;
import nl.jessedezwart.strongbuffs.panel.view.StrongBuffsPanel;
import nl.jessedezwart.strongbuffs.runtime.RuleRuntimeController;
import org.junit.Test;

public class StrongBuffsPluginLifecycleTest
{
	@Test
	public void startupAddsNavigationAndShutdownRemovesIt() throws Exception
	{
		StrongBuffsPlugin plugin = new StrongBuffsPlugin();
		ClientToolbar clientToolbar = createToolbar();
		StrongBuffsPanel panel = new TestStrongBuffsPanel();

		setField(plugin, "clientToolbar", clientToolbar);
		setField(plugin, "strongBuffsPanel", panel);
		setField(plugin, "ruleRuntimeController", new TestRuleRuntimeController());

		plugin.startUp();
		SwingUtilities.invokeAndWait(() ->
		{
		});

		NavigationButton navigationButton = (NavigationButton) getField(plugin, "navigationButton");
		assertNotNull(navigationButton);
		assertSame(panel, navigationButton.getPanel());
		assertEquals(1, getSidebarEntries(clientToolbar).size());

		plugin.shutDown();
		SwingUtilities.invokeAndWait(() ->
		{
		});

		assertEquals(0, getSidebarEntries(clientToolbar).size());
		assertNull(getField(plugin, "navigationButton"));
	}

	private static final class TestRuleRuntimeController extends RuleRuntimeController
	{
		private TestRuleRuntimeController()
		{
			super(null, null, null, null);
		}

		@Override
		public void startUp()
		{
		}

		@Override
		public void shutDown()
		{
		}
	}

	private static Object getField(Object target, String name) throws Exception
	{
		Field field = findField(target.getClass(), name);
		field.setAccessible(true);
		return field.get(target);
	}

	private static void setField(Object target, String name, Object value) throws Exception
	{
		Field field = findField(target.getClass(), name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static Field findField(Class<?> type, String name) throws Exception
	{
		Class<?> current = type;

		while (current != null)
		{
			try
			{
				return current.getDeclaredField(name);
			}
			catch (NoSuchFieldException ex)
			{
				current = current.getSuperclass();
			}
		}

		throw new NoSuchFieldException(name);
	}

	private static final class TestStrongBuffsPanel extends StrongBuffsPanel
	{
		private TestStrongBuffsPanel()
		{
			super(new RulePanelController(new RecordingStore(), new ConditionEditorRegistry(), new ActionEditorRegistry()),
				new ConditionEditorRegistry(), new ActionEditorRegistry());
		}
	}

	private static final class RecordingStore extends RuleDefinitionStore
	{
		private RecordingStore()
		{
			super(null);
		}

		@Override
		public java.util.List<nl.jessedezwart.strongbuffs.model.rule.RuleDefinition> load()
		{
			return new java.util.ArrayList<>();
		}

		@Override
		public void save(java.util.List<nl.jessedezwart.strongbuffs.model.rule.RuleDefinition> rules)
		{
		}
	}

	private static ClientToolbar createToolbar() throws Exception
	{
		sun.misc.Unsafe unsafe = getUnsafe();
		ClientUI clientUI = (ClientUI) unsafe.allocateInstance(ClientUI.class);

		@SuppressWarnings("unchecked")
		Comparator<NavigationButton> comparator = (Comparator<NavigationButton>) getStaticField(NavigationButton.class, "COMPARATOR");
		setField(clientUI, "sidebarEntries", new TreeSet<>(comparator));
		setField(clientUI, "selectedTabHistory", new ArrayDeque<>());
		setField(clientUI, "sidebar", new JTabbedPane());

		java.lang.reflect.Constructor<ClientToolbar> constructor =
			ClientToolbar.class.getDeclaredConstructor(ClientUI.class);
		constructor.setAccessible(true);
		return constructor.newInstance(clientUI);
	}

	@SuppressWarnings("unchecked")
	private static TreeSet<NavigationButton> getSidebarEntries(ClientToolbar toolbar) throws Exception
	{
		ClientUI clientUI = (ClientUI) getField(toolbar, "clientUI");
		return (TreeSet<NavigationButton>) getField(clientUI, "sidebarEntries");
	}

	private static sun.misc.Unsafe getUnsafe() throws Exception
	{
		Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (sun.misc.Unsafe) field.get(null);
	}

	private static Object getStaticField(Class<?> type, String name) throws Exception
	{
		Field field = findField(type, name);
		field.setAccessible(true);
		return field.get(null);
	}
}
