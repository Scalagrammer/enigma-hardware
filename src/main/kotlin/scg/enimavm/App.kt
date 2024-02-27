package scg.enimavm

import org.koin.dsl.module
import scg.enimavm.facade.EnigmaHardware
import scg.enimavm.service.*
import scg.enimavm.utils.*
import scg.hardware.assembler.Program.Companion.runWith
import java.awt.event.KeyListener
import java.nio.file.Files.readString
import java.nio.file.Paths.get
import java.util.concurrent.CountDownLatch

typealias StartupLatch = CountDownLatch

fun main(args : Array<String>) {

    if (args.isEmpty()) {
        throw IllegalArgumentException("No arguments provided")
    }

    fun run(hardware : EnigmaHardware) = hardware runWith { readString(get(args.first())) }

    val keyboardServiceLazySingleton by lazy(::SwingKeyboardService)

    val morseCodeSoundEffectServiceLazySingleton by lazy(::MorseCodeSoundEffectServiceImpl)

    module {
        single<SoundEffectService>                     { morseCodeSoundEffectServiceLazySingleton }
        single<Singleton>                              { morseCodeSoundEffectServiceLazySingleton }
        single<KeyboardService>                        { keyboardServiceLazySingleton             }
        single<KeyListener>                            { keyboardServiceLazySingleton             }
        single<EnigmaHardware>                         { EnigmaHardware()                         }
        single<RotorService>                           { RotorServiceImpl()                       }
        single<StartupLatch>                           { CountDownLatch(1)                        }
        single<LedboardService>(createdAtStart = true) { SwingUIServiceImpl()                     }
    } bind {
        destroyOnShutdown()
        initialize()
        run(koin.get())
    }
}