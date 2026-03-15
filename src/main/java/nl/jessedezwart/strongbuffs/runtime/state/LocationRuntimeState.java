package nl.jessedezwart.strongbuffs.runtime.state;

import net.runelite.api.coords.WorldPoint;

public class LocationRuntimeState
{
	private int runEnergyPercent;
	private boolean inInstance;
	private WorldPoint playerLocation;

	public int getRunEnergyPercent()
	{
		return runEnergyPercent;
	}

	public void setRunEnergyPercent(int runEnergyPercent)
	{
		this.runEnergyPercent = runEnergyPercent;
	}

	public boolean isInInstance()
	{
		return inInstance;
	}

	public void setInInstance(boolean inInstance)
	{
		this.inInstance = inInstance;
	}

	public WorldPoint getPlayerLocation()
	{
		return playerLocation;
	}

	public void setPlayerLocation(WorldPoint playerLocation)
	{
		this.playerLocation = playerLocation;
	}

	public void clear()
	{
		runEnergyPercent = 0;
		inInstance = false;
		playerLocation = null;
	}
}
