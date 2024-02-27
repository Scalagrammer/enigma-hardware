package scg.enimavm.facade

import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import scg.enimavm.StartupLatch
import scg.enimavm.model.Rotor.Companion.byIndex
import scg.enimavm.model.Rotor.UKW_M
import scg.enimavm.model.Swap
import scg.enimavm.service.*
import scg.enimavm.service.Mode.DECRYPTION
import scg.enimavm.service.Mode.ENCRYPTION
import scg.enimavm.utils.Position
import scg.hardware.assembler.model.Hardware
import scg.hardware.assembler.model.Reg
import scg.hardware.assembler.model.Reg.Companion.collect
import scg.hardware.assembler.model.Reg.Companion.extract
import scg.hardware.assembler.state.ExternalCall
import scg.hardware.assembler.state.State
import java.util.concurrent.CountDownLatch

class EnigmaHardware : Hardware<EnigmaHardware>, KoinComponent {

    private val keyboardService     : KeyboardService    by inject()
    private val ledboardService     : LedboardService    by inject()
    private val rotorService        : RotorService       by inject()
    private val soundEffectService  : SoundEffectService by inject()
    private val startupLatch        : StartupLatch       by inject()

    override fun State<EnigmaHardware>.wire() {

        this += AwaitKeyPressed
        this += AwaitKeyReleased

        this += Reflect(defineReg("ex"))

        this += Translate(defineReg("sx"), defineReg("rx"))

        this += ShiftAlphabetSpace(
            defineReg("ra"),
            defineReg("rb"),
            defineReg("rc"),
            defineReg("rd"),
            defineReg("re"),
            defineReg("rf"),
            defineReg("rg"),
            defineReg("rh"),
        )

        defineReg("lx").usr {
            if (it != null) show(it) else hideAll()
        }

        defineReg("mx").usr {
            if (it != null) play(it)
        }

        defineReg("gx").usr {

            play(4U)

            if (it != null) {
                show(it) ; delay(80) ; hideAll() ; delay(60)
            }
        }
    }

    fun rotate(indices : List<Position>) =
        requireStarted { rotorService.rotate(indices.map(::byIndex)) }

    operator fun Swap.invoke(rotorIndex : UInt, position : Position) =
        requireStarted { with(rotorService) { apply(byIndex(rotorIndex), position) } }

    operator fun Swap.invoke(position : Position) =
        requireStarted { with(rotorService) { apply(UKW_M, position) } }

    fun awaitKeyPressed() =
        requireStarted { keyboardService.awaitKeyPressed() }

    fun awaitKeyReleased() =
        requireStarted { keyboardService.awaitKeyReleased() }

    private fun show(position : Position) =
        requireStarted { ledboardService.show(position) }

    private fun hideAll() =
        requireStarted { ledboardService.hideAll() }

    private fun updatePosition(rotor : Position, position : Position) =
        requireStarted { rotorService.updatePosition(byIndex(rotor), position) }

    private fun updateRing(rotor : Position, position : Position) =
        requireStarted { rotorService.updateRing(byIndex(rotor), position) }

    private fun play(position : Position) =
        requireStarted { soundEffectService.play(position) }

    private inline fun <R> requireStarted(action : () -> R) : R {
        startupLatch.await()
        return action()
    }
}

object AwaitKeyPressed : ExternalCall<EnigmaHardware>("await_key_pressed") {
    override fun State<EnigmaHardware>.execute(hardware : EnigmaHardware) = push(hardware.awaitKeyPressed())
}

object AwaitKeyReleased : ExternalCall<EnigmaHardware>("await_key_released") {
    override fun State<EnigmaHardware>.execute(hardware : EnigmaHardware) = hardware.awaitKeyReleased()
}

class Translate(
    private val sx : Reg,
    private val rx : Reg,
) : ExternalCall<EnigmaHardware>("translate") {
    override fun State<EnigmaHardware>.execute(hardware : EnigmaHardware) =
        extract(sx, rx) { (swap), rotorIndex ->
            with(hardware) { swap(rotorIndex, pop()) }.also { push(it) }
        }
}


class Reflect(private val ex : Reg) : ExternalCall<EnigmaHardware>("reflect") {
    override fun State<EnigmaHardware>.execute(hardware : EnigmaHardware) = ex { (swap) -> with(hardware) { swap(pop()) }.also { push(it)} }
}

class ShiftAlphabetSpace(
    private val ra : Reg,
    private val rb : Reg,
    private val rc : Reg,
    private val rd : Reg,
    private val re : Reg,
    private val rf : Reg,
    private val rg : Reg,
    private val rh : Reg,
) : ExternalCall<EnigmaHardware>("shift_alphabet_space") {
    override fun State<EnigmaHardware>.execute(hardware : EnigmaHardware) = collect(ra, rb, rc, rd, re, rf, rg, rh) { hardware.rotate(it) }
}

operator fun UInt.component1() : Swap = Swap.byIndex(this)