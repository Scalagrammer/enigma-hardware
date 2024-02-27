package scg.enimavm

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
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

private val wiring = module {

    val keyboardServiceLazySingleton by lazy(::SwingKeyboardService)

    single<SoundEffectService>                     { MorseCodeSoundEffectServiceImpl() }
    single<KeyboardService>                        { keyboardServiceLazySingleton      }
    single<KeyListener>                            { keyboardServiceLazySingleton      }
    single<EnigmaHardware>                         { EnigmaHardware()                  }
    single<RotorService>                           { RotorServiceImpl()                }
    single<StartupLatch>                           { CountDownLatch(1)                 }
    factory<Stack>                                 { StackImpl()                       }
    single<LedboardService>(createdAtStart = true) { SwingUIServiceImpl()              }

}

fun main(args : Array<String>) {

    if (args.isEmpty()) {
        throw IllegalArgumentException("No arguments provided")
    }

    fun runProgram(hardware : EnigmaHardware) = hardware.runWith { readString(get(args.first())) }

    startKoin  { modules(wiring)   }
        .run   { koin              }
        .apply { runProgram(get()) }
}