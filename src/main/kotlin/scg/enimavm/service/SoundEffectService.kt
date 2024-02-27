package scg.enimavm.service

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import scg.enimavm.service.MorseCode.*
import scg.enimavm.utils.*
import java.io.Closeable
import java.util.concurrent.Executors.newSingleThreadExecutor
import javax.sound.sampled.*
import javax.sound.sampled.AudioSystem.*

private class Track(audio : AudioInputStream) : Closeable {

    private val monitor = Object()

    private val timeline = Timeline(audio)

    private val audioBytes = audio.use { it.readAllBytes() }

    init {
        timeline.open(audio.format)
        timeline.start()
    }

    override fun close() = synchronized(monitor) {
        timeline.takeIf { it.isOpen }?.run {
            flush()
            stop()
            close()
        }
    } ?: Unit

    fun replay() = synchronized(monitor) {
        timeline.takeIf { it.isOpen }?.run {
            write(audioBytes, 0, audioBytes.size)
            drain()
        }
    } ?: Unit
}

interface SoundEffectService {
    fun play(position : Position)
}

enum class MorseCode { DASH, DOT }

class MorseCodeSoundEffectServiceImpl : SoundEffectService, Singleton {

    private val IO = (newSingleThreadExecutor()).asCoroutineDispatcher()

    private val morsePlayer = Channel<List<MorseCode>>()

    private val tracks = buildMap {
        this[DOT]  = Track(loadAudioStream(DOT))
        this[DASH] = Track(loadAudioStream(DASH))
    }

    init { IO { for (codes in morsePlayer) codes.forEach { tracks.getValue(it).replay() } } }

    override fun play(position : Position) = IO { morsePlayer.send(position.toMorseCodes()) }

    override fun preDestroy() {
        IO.close()
        tracks.values.forEach(Track::close)
    }

    private companion object {
        private fun loadAudioStream(code : MorseCode) =
            getAudioInputStream(this::class.java.classLoader.getResource("morse-sound-effects/${code.name.lowercase()}.wav"))
    }
}