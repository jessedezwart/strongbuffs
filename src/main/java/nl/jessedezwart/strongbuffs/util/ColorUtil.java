package nl.jessedezwart.strongbuffs.util;

/**
 * Shared color validation utilities.
 */
public final class ColorUtil
{
	private ColorUtil()
	{
	}

	/**
	 * @return true if the colorHex is a valid HTML hex color string, e.g. "#A1B2C3"
	 */
	public static boolean isValidColorHex(String colorHex)
	{
		return colorHex != null && colorHex.matches("^#[0-9A-Fa-f]{6}$");
	}
}
