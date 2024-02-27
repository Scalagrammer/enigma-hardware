package scg.enimavm.service

import scg.enimavm.utils.Position
import scg.enimavm.utils.position
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.atomic.AtomicReference

sealed interface KeyboardService {
    fun awaitKeyPressed() : Position
    fun awaitKeyReleased()
}

class SwingKeyboardService : KeyAdapter(), KeyboardService {

    private val pressedKeys          = ArrayBlockingQueue<UInt>(5, true)
    private val releasedKeysFeedback = ArrayBlockingQueue<Unit>(5, false)
    private val lastPressed          = AtomicReference<Char>()

    override fun keyPressed(e : KeyEvent) = e { pressedKeys.put(it) }

    override fun keyReleased(e : KeyEvent) = e { releasedKeysFeedback.put(Unit) }

    override fun awaitKeyPressed() : Position = pressedKeys.take()

    override fun awaitKeyReleased() { releasedKeysFeedback.take() }

    private operator fun KeyEvent.invoke(action : (Position) -> Unit) {

        println(this)

        val keyPosition = position ?: return

        val proceed = when (id) {
            KEY_PRESSED  -> lastPressed.compareAndSet(null, keyChar)
            KEY_RELEASED -> lastPressed.compareAndSet(keyChar, null)
            else         -> false
        }

        if (proceed) {
            action(keyPosition)
        }
    }
}