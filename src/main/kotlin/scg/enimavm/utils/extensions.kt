package scg.enimavm.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.future
import scg.enimavm.model.Swap
import scg.enimavm.service.MorseCode.DOT
import scg.enimavm.service.MorseCode.DASH
import java.awt.Color
import java.awt.event.KeyEvent
import java.util.Collections
import javax.swing.JPanel
import kotlin.reflect.KProperty

val keyByPosition = buildMap {
    this[0U] =  "A"
    this[1U] =  "B"
    this[2U] =  "C"
    this[3U] =  "D"
    this[4U] =  "E"
    this[5U] =  "F"
    this[6U] =  "G"
    this[7U] =  "H"
    this[8U] =  "I"
    this[9U] =  "J"
    this[10U] = "K"
    this[11U] = "L"
    this[12U] = "M"
    this[13U] = "N"
    this[14U] = "O"
    this[15U] = "P"
    this[16U] = "Q"
    this[17U] = "R"
    this[18U] = "S"
    this[19U] = "T"
    this[20U] = "U"
    this[21U] = "V"
    this[22U] = "W"
    this[23U] = "X"
    this[24U] = "Y"
    this[25U] = "Z"
}

private val positionByKey = buildMap {
    this['A'] = 0U
    this['B'] = 1U
    this['C'] = 2U
    this['D'] = 3U
    this['E'] = 4U
    this['F'] = 5U
    this['G'] = 6U
    this['H'] = 7U
    this['I'] = 8U
    this['J'] = 9U
    this['K'] = 10U
    this['L'] = 11U
    this['M'] = 12U
    this['N'] = 13U
    this['O'] = 14U
    this['P'] = 15U
    this['Q'] = 16U
    this['R'] = 17U
    this['S'] = 18U
    this['T'] = 19U
    this['U'] = 20U
    this['V'] = 21U
    this['W'] = 22U
    this['X'] = 23U
    this['Y'] = 24U
    this['Z'] = 25U
}

private val morseCodesByPosition = buildMap {
    this[0U]  = /* A */ listOf(DOT, DASH)
    this[1U]  = /* B */ listOf(DASH, DOT, DOT, DOT)
    this[2U]  = /* C */ listOf(DASH, DOT, DASH, DOT)
    this[3U]  = /* D */ listOf(DASH, DOT, DOT)
    this[4U]  = /* E */ listOf(DOT)
    this[5U]  = /* F */ listOf(DOT, DOT, DASH, DOT)
    this[6U]  = /* G */ listOf(DASH, DASH, DOT)
    this[7U]  = /* H */ listOf(DOT, DOT, DOT, DOT)
    this[8U]  = /* I */ listOf(DOT, DOT)
    this[9U]  = /* J */ listOf(DOT, DASH, DASH, DASH)
    this[10U] = /* K */ listOf(DASH, DOT, DASH)
    this[11U] = /* L */ listOf(DOT, DASH, DOT, DOT)
    this[12U] = /* M */ listOf(DASH, DASH)
    this[13U] = /* N */ listOf(DASH, DOT)
    this[14U] = /* O */ listOf(DASH, DASH, DASH)
    this[15U] = /* P */ listOf(DOT, DASH, DASH, DOT)
    this[16U] = /* Q */ listOf(DASH, DASH, DOT, DASH)
    this[17U] = /* R */ listOf(DOT, DASH, DOT)
    this[18U] = /* S */ listOf(DOT, DOT, DOT)
    this[19U] = /* T */ listOf(DASH)
    this[20U] = /* U */ listOf(DOT, DOT, DASH)
    this[21U] = /* V */ listOf(DOT, DOT, DOT, DASH)
    this[22U] = /* W */ listOf(DOT, DASH, DASH)
    this[23U] = /* X */ listOf(DASH, DOT, DOT, DASH)
    this[24U] = /* Y */ listOf(DASH, DOT, DASH, DASH)
    this[25U] = /* Z */ listOf(DASH, DASH, DOT, DOT)
}

fun JPanel(background : Color) =
    JPanel().apply { this.background = background }

val KeyEvent.position : Position?
    get() = (keyChar.uppercaseChar()).let { positionByKey[it] }

fun Stack.pop(n : Int) : List<Operand> = buildList { repeat(n) { add(pop()) } }

inline fun <R> Stack.popMap(transform : (Operand) -> R) : R = transform(pop())

inline fun <T> List<T>.forEver(action : (T) -> Unit) {
    if (isEmpty()) return

    var itemIndex = 0

    while(true) action(get(itemIndex)).also {
        itemIndex = (itemIndex + 1) % size
    }
}

fun <T> MutableList<T>.rotate(n: Int) = Collections.rotate(this, n)

operator fun CoroutineDispatcher.invoke(callback : suspend () -> Unit) { CoroutineScope(this).future { callback() } }

fun <A, R> CoroutineScope.traverse(items : List<A>, scope : suspend CoroutineScope.(A) -> Deferred<R>) : List<R> =
    future { items.map { scope(it) }.awaitAll() }.join()

fun Position.toMorseCodes() = morseCodesByPosition.getValue(this)

operator fun <T> ThreadLocal<T>.getValue(receiver : Any?, p : KProperty<*>) : T = get()
operator fun <T> ThreadLocal<T>.setValue(receiver : Any?, p : KProperty<*>, value : T) = set(value)

operator fun UInt.component1() : Swap = Swap.byIndex(this)
operator fun UInt?.component1() : Swap? = this?.let(Swap.Companion::byIndex)