package scg.enimavm.service

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import scg.enimavm.service.MorseCode.*
import scg.enimavm.utils.Position
import scg.enimavm.utils.invoke
import scg.enimavm.utils.toMorseCodes
import java.util.concurrent.Executors
import java.util.concurrent.Executors.newSingleThreadExecutor
import javax.sound.sampled.*
import javax.sound.sampled.AudioSystem.*
import javax.sound.sampled.DataLine.Info

private class Track(audio : AudioInputStream) {

    private val dataLine = getLine(Info(SourceDataLine::class.java, audio.format)) as SourceDataLine

    private val audioBytes = audio.readAllBytes()

    init {
        dataLine.open(audio.format)
        dataLine.start()
    }

    fun replay() {
        dataLine.write(audioBytes, 0, audioBytes.size)
        dataLine.drain()
    }
}

interface SoundEffectService {
    fun play(position : Position)
}

enum class MorseCode { DASH, DOT }

class MorseCodeSoundEffectServiceImpl : SoundEffectService {

    private val morsePlayer = Channel<List<MorseCode>>()

    init { IO { for (codes in morsePlayer) codes.forEach { it.play() } } }

    override fun play(position : Position) = IO { morsePlayer.send(position.toMorseCodes()) }

    private companion object {

        private val IO = (newSingleThreadExecutor()).asCoroutineDispatcher()

        private val dotTrack  = Track(getAudioStream(DOT))
        private val dashTrack = Track(getAudioStream(DASH))

        private suspend fun MorseCode.play() {
            when (this) {
                DOT  -> dotTrack.replay()
                DASH -> dashTrack.replay()
            }
        }

        private fun getAudioStream(code : MorseCode) : AudioInputStream {
            return getAudioInputStream(this::class.java.classLoader.getResource("morse-sound-effects/${code.name.lowercase()}.wav"))
        }
    }
}