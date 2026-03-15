package nl.jessedezwart.strongbuffs.runtime.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class SoundAlertActionHandler implements RuntimeActionHandler<SoundAlertAction>
{
	private final SoundAlertController soundAlertController;

	@Inject
	public SoundAlertActionHandler(SoundAlertController soundAlertController)
	{
		this.soundAlertController = soundAlertController;
	}

	@Override
	public Class<SoundAlertAction> getActionType()
	{
		return SoundAlertAction.class;
	}

	@Override
	public void activatePersistent(CompiledRule rule, SoundAlertAction action, RuntimeState runtimeState)
	{
		soundAlertController.play(action);
	}

	@Override
	public void updatePersistent(CompiledRule rule, SoundAlertAction action, RuntimeState runtimeState)
	{
	}

	@Override
	public void deactivatePersistent(CompiledRule rule, SoundAlertAction action)
	{
	}

	@Override
	public void fireTransient(CompiledRule rule, SoundAlertAction action, RuntimeState runtimeState)
	{
		soundAlertController.play(action);
	}

	@Override
	public void shutDown()
	{
		soundAlertController.shutDown();
	}
}
