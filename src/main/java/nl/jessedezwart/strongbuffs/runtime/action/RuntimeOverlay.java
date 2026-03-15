package nl.jessedezwart.strongbuffs.runtime.action;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class RuntimeOverlay extends Overlay
{
	private static final int PADDING_X = 8;
	private static final int PADDING_Y = 4;
	private static final int ENTRY_GAP = 4;
	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 170);

	private volatile List<TextEntry> textEntries = Collections.emptyList();
	private volatile List<FlashEntry> flashEntries = Collections.emptyList();

	@Inject
	public RuntimeOverlay()
	{
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_HIGH);
	}

	public void setTextEntries(List<TextEntry> textEntries)
	{
		this.textEntries = textEntries == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(textEntries));
	}

	public void setFlashEntries(List<FlashEntry> flashEntries)
	{
		this.flashEntries = flashEntries == null ? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(flashEntries));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long now = System.currentTimeMillis();
		renderFlashes(graphics, now);
		renderTextEntries(graphics, now);
		return null;
	}

	private void renderFlashes(Graphics2D graphics, long now)
	{
		Rectangle clipBounds = graphics.getClipBounds();

		if (clipBounds == null)
		{
			return;
		}

		for (FlashEntry flashEntry : flashEntries)
		{
			if (flashEntry.isExpired(now))
			{
				continue;
			}

			graphics.setComposite(AlphaComposite.SrcOver.derive(flashEntry.getAlpha()));
			graphics.setColor(flashEntry.getColor());
			graphics.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
		}

		graphics.setComposite(AlphaComposite.SrcOver);
	}

	private void renderTextEntries(Graphics2D graphics, long now)
	{
		List<TextEntry> activeEntries = new ArrayList<>();

		for (TextEntry textEntry : textEntries)
		{
			if (!textEntry.isExpired(now))
			{
				activeEntries.add(textEntry);
			}
		}

		if (activeEntries.isEmpty())
		{
			return;
		}

		Font originalFont = graphics.getFont();
		Font overlayFont = originalFont.deriveFont(Font.BOLD, Math.max(12f, originalFont.getSize2D()));
		graphics.setFont(overlayFont);
		FontMetrics metrics = graphics.getFontMetrics();

		int x = 12;
		int y = 24;

		for (TextEntry textEntry : activeEntries)
		{
			String text = textEntry.getText();
			int width = metrics.stringWidth(text) + (PADDING_X * 2);
			int height = metrics.getHeight() + (PADDING_Y * 2);
			int baseline = y + PADDING_Y + metrics.getAscent();

			graphics.setColor(BACKGROUND_COLOR);
			graphics.fillRoundRect(x, y, width, height, 8, 8);
			graphics.setColor(textEntry.getColor());
			graphics.drawString(text, x + PADDING_X, baseline);

			y += height + ENTRY_GAP;
		}

		graphics.setFont(originalFont);
	}

	public static class TextEntry
	{
		private final String text;
		private final Color color;
		private final long expiresAtMillis;

		public TextEntry(String text, Color color, long expiresAtMillis)
		{
			this.text = text;
			this.color = color;
			this.expiresAtMillis = expiresAtMillis;
		}

		public String getText()
		{
			return text;
		}

		public Color getColor()
		{
			return color;
		}

		public boolean isExpired(long now)
		{
			return expiresAtMillis > 0 && now >= expiresAtMillis;
		}
	}

	public static class FlashEntry
	{
		private final Color color;
		private final float alpha;
		private final long expiresAtMillis;

		public FlashEntry(Color color, float alpha, long expiresAtMillis)
		{
			this.color = color;
			this.alpha = alpha;
			this.expiresAtMillis = expiresAtMillis;
		}

		public Color getColor()
		{
			return color;
		}

		public float getAlpha()
		{
			return alpha;
		}

		public boolean isExpired(long now)
		{
			return expiresAtMillis > 0 && now >= expiresAtMillis;
		}
	}
}
