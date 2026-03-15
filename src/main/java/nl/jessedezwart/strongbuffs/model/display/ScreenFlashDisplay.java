package nl.jessedezwart.strongbuffs.model.display;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScreenFlashDisplay extends DisplayDefinition
{
	private String colorHex = "#FF0000";
	private int durationTicks = 1;
}
