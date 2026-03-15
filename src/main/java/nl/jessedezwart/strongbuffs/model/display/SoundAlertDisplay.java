package nl.jessedezwart.strongbuffs.model.display;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SoundAlertDisplay extends DisplayDefinition
{
	private String soundKey = "notification";
	private int volumePercent = 100;
}
