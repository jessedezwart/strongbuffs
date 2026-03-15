package nl.jessedezwart.strongbuffs.model.display;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OverlayTextDisplay extends DisplayDefinition
{
	private String text;
	private String colorHex = "#FFFFFF";
	private boolean showValue = true;
}
