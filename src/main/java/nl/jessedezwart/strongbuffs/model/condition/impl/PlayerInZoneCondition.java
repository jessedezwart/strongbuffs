package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;
import nl.jessedezwart.strongbuffs.runtime.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;
import net.runelite.api.coords.WorldPoint;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerInZoneCondition extends ConditionDefinition
{
	private int southWestX;
	private int southWestY;
	private int northEastX;
	private int northEastY;
	private int plane;

	@Override
	public String getEditorLabel()
	{
		return "Player in zone";
	}

	@Override
	public String getEditorDescription()
	{
		return "Zone [" + southWestX + ", " + southWestY + "] to [" + northEastX + ", " + northEastY +
			"] plane " + plane;
	}

	@Override
	public ConditionDefinition copy()
	{
		PlayerInZoneCondition copy = new PlayerInZoneCondition();
		copy.setSouthWestX(southWestX);
		copy.setSouthWestY(southWestY);
		copy.setNorthEastX(northEastX);
		copy.setNorthEastY(northEastY);
		copy.setPlane(plane);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(
			EditorField.spinner("southWestX", "SW X", this::getSouthWestX, this::setSouthWestX, 0, 20000, 1, ""),
			EditorField.spinner("southWestY", "SW Y", this::getSouthWestY, this::setSouthWestY, 0, 20000, 1, ""),
			EditorField.spinner("northEastX", "NE X", this::getNorthEastX, this::setNorthEastX, 0, 20000, 1, ""),
			EditorField.spinner("northEastY", "NE Y", this::getNorthEastY, this::setNorthEastY, 0, 20000, 1, ""),
			EditorField.spinner("plane", "Plane", this::getPlane, this::setPlane, 0, 3, 1, ""));
	}

	@Override
	public boolean matches(RuntimeState state)
	{
		if (state == null)
		{
			return false;
		}

		WorldPoint playerLocation = state.getLocation().getPlayerLocation();

		if (playerLocation == null)
		{
			return false;
		}

		return playerLocation.getPlane() == plane &&
			playerLocation.getX() >= southWestX &&
			playerLocation.getX() <= northEastX &&
			playerLocation.getY() >= southWestY &&
			playerLocation.getY() <= northEastY;
	}

	@Override
	public void contributeRequirements(RuntimeConditionRequirements.Builder builder)
	{
		builder.requirePlayerLocation();
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (southWestX > northEastX || southWestY > northEastY)
		{
			errors.put(fieldPrefix, "Zone southwest coordinates must be less than or equal to northeast coordinates.");
		}
	}

	@Override
	public String getTypeId()
	{
		return "player_in_zone";
	}
}
