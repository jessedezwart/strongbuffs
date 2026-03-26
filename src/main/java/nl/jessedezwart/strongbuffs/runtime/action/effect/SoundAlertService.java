package nl.jessedezwart.strongbuffs.runtime.action.effect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;
import nl.jessedezwart.strongbuffs.runtime.action.RuntimeActionHandler;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Synthesizes and plays short alert sounds on a dedicated background thread.
 *
 * <p>
 * Sound presets ("ding", "warning", default) are built as in-memory WAV files
 * from simple sine-wave tones and cached after first use. Playback is submitted
 * to a single-threaded executor so it never blocks the client thread.
 * </p>
 */
@Singleton
@Slf4j
public class SoundAlertService implements RuntimeActionHandler<SoundAlertAction>
{
	private static final int SAMPLE_RATE = 22050;

	private final AudioPlayer audioPlayer = new AudioPlayer();
	private final ExecutorService executorService = Executors.newSingleThreadExecutor(new SoundThreadFactory());
	private final Map<String, byte[]> cachedSounds = new LinkedHashMap<>();

	@Inject
	public SoundAlertService()
	{
	}

	@Override
	public Class<SoundAlertAction> getActionType()
	{
		return SoundAlertAction.class;
	}

	@Override
	public void activatePersistent(CompiledRule rule, SoundAlertAction action, RuntimeState runtimeState)
	{
		play(action);
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
		play(action);
	}

	@Override
	public void shutDown()
	{
		executorService.shutdownNow();
	}

	private void play(SoundAlertAction action)
	{
		if (action == null || action.getVolumePercent() <= 0)
		{
			return;
		}

		byte[] soundData = cachedSounds.computeIfAbsent(action.getSoundKey(), SoundAlertService::buildSound);
		float gain = Math.max(0f, Math.min(1f, action.getVolumePercent() / 100f));

		executorService.submit(() ->
		{
			try
			{
				audioPlayer.play(new ByteArrayInputStream(soundData), gain);
			}
			catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex)
			{
				log.error("Failed to play StrongBuffs sound preset {}", action.getSoundKey(), ex);
			}
		});
	}

	private static byte[] buildSound(String soundKey)
	{
		if ("ding".equals(soundKey))
		{
			return synthesizeWav(new Tone(1320, 140, 0.7), new Tone(1760, 90, 0.55));
		}

		if ("warning".equals(soundKey))
		{
			return synthesizeWav(new Tone(740, 130, 0.75), new Tone(520, 170, 0.75), new Tone(740, 130, 0.75));
		}

		return synthesizeWav(new Tone(880, 120, 0.65), new Tone(1175, 100, 0.45));
	}

	private static byte[] synthesizeWav(Tone... tones)
	{
		ByteArrayOutputStream pcm = new ByteArrayOutputStream();

		for (Tone tone : tones)
		{
			appendTone(pcm, tone);
		}

		byte[] audioData = pcm.toByteArray();
		ByteArrayOutputStream wav = new ByteArrayOutputStream();

		writeAscii(wav, "RIFF");
		writeLittleEndianInt(wav, 36 + audioData.length);
		writeAscii(wav, "WAVE");
		writeAscii(wav, "fmt ");
		writeLittleEndianInt(wav, 16);
		writeLittleEndianShort(wav, (short) 1);
		writeLittleEndianShort(wav, (short) 1);
		writeLittleEndianInt(wav, SAMPLE_RATE);
		writeLittleEndianInt(wav, SAMPLE_RATE * 2);
		writeLittleEndianShort(wav, (short) 2);
		writeLittleEndianShort(wav, (short) 16);
		writeAscii(wav, "data");
		writeLittleEndianInt(wav, audioData.length);
		wav.write(audioData, 0, audioData.length);
		return wav.toByteArray();
	}

	private static void appendTone(ByteArrayOutputStream outputStream, Tone tone)
	{
		int sampleCount = (int) ((tone.durationMillis / 1000.0) * SAMPLE_RATE);

		for (int sample = 0; sample < sampleCount; sample++)
		{
			double envelope = Math.min(1.0, sample / (double) Math.max(1, SAMPLE_RATE / 100));
			double release = Math.min(1.0, (sampleCount - sample) / (double) Math.max(1, SAMPLE_RATE / 80));
			double amplitude = Math.min(envelope, release) * tone.volume;
			double angle = 2.0 * Math.PI * tone.frequency * sample / SAMPLE_RATE;
			short pcmSample = (short) (Math.sin(angle) * amplitude * Short.MAX_VALUE);
			writeLittleEndianShort(outputStream, pcmSample);
		}
	}

	private static void writeAscii(ByteArrayOutputStream outputStream, String value)
	{
		for (int i = 0; i < value.length(); i++)
		{
			outputStream.write((byte) value.charAt(i));
		}
	}

	private static void writeLittleEndianInt(ByteArrayOutputStream outputStream, int value)
	{
		outputStream.write(value & 0xFF);
		outputStream.write((value >> 8) & 0xFF);
		outputStream.write((value >> 16) & 0xFF);
		outputStream.write((value >> 24) & 0xFF);
	}

	private static void writeLittleEndianShort(ByteArrayOutputStream outputStream, int value)
	{
		outputStream.write(value & 0xFF);
		outputStream.write((value >> 8) & 0xFF);
	}

	private static class Tone
	{
		private final int frequency;
		private final int durationMillis;
		private final double volume;

		private Tone(int frequency, int durationMillis, double volume)
		{
			this.frequency = frequency;
			this.durationMillis = durationMillis;
			this.volume = volume;
		}
	}

	private static class SoundThreadFactory implements ThreadFactory
	{
		@Override
		public Thread newThread(Runnable runnable)
		{
			Thread thread = new Thread(runnable, "strongbuffs-audio");
			thread.setDaemon(true);
			return thread;
		}
	}
}
