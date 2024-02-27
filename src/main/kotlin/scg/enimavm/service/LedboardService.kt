package scg.enimavm.service

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import scg.enimavm.StartupLatch
import scg.enimavm.utils.*
import scg.enimavm.utils.JPanel
import java.awt.BorderLayout
import java.awt.Color.BLACK
import java.awt.event.*
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities.invokeLater
import javax.swing.WindowConstants.EXIT_ON_CLOSE

interface LedboardService {
    fun show(position : Position)
    fun hideAll()
}

private data class KeyImageIcons(val grey : ImageIcon, val yellowIcon : ImageIcon)

class SwingUIServiceImpl : ComponentAdapter(), KoinComponent, LedboardService {

    private val keyListener  : KeyListener  by inject()
    private val startupLatch : StartupLatch by inject()

    // Q W E R T Z U I O
    private val northRow = listOf(16U, 22U, 4U, 17U, 19U, 25U, 20U, 8U, 14U)
    // A S D F G H J K
    private val centerRow = listOf(0U, 18U, 3U, 5U, 6U, 7U, 9U, 10U)
    // P Y X C V B N M l
    private val southRow = listOf(15U, 24U, 23U, 2U, 21U, 1U, 13U, 12U, 11U)

    private val keyImageIconsByPosition = getKeyImageIconsByPosition()

    private val labelByPosition = extractGreyLabels(keyImageIconsByPosition)

    private val ledsPane = JPanel(BorderLayout())

    init {
        JPanel(background = BLACK).run {
            northRow.forEach { add(getIconLabel(it)) }
            ledsPane.add(this, BorderLayout.NORTH)
        }

        JPanel(background = BLACK).run {
            centerRow.forEach { add(getIconLabel(it)) }
            ledsPane.add(this, BorderLayout.CENTER)
        }

        JPanel(background = BLACK).run {
            southRow.forEach { add(getIconLabel(it)) }
            ledsPane.add(this, BorderLayout.SOUTH)
        }

        with(JFrame()) {
            addKeyListener(keyListener)
            add(ledsPane)
            setSize(535, 210)
            setLocationRelativeTo(null)
            isVisible             = true
            isResizable           = false
            title                 = "F4 lampboard"
            defaultCloseOperation = EXIT_ON_CLOSE

            addComponentListener(this@SwingUIServiceImpl)

            toFront()
        }
    }

    override fun componentShown(ignored : ComponentEvent) { startupLatch.countDown() }

    override fun show(position : Position) = repaintAfter {
        getIconLabel(position).icon = getYellowImageIcon(position)
    }

    override fun hideAll() = repaintAfter {
        labelByPosition.forEach { (position, label) -> label.icon = getGreyImageIcon(position) }
    }

    private companion object {
        private fun SwingUIServiceImpl.repaintAfter(action : () -> Unit) =
            invokeLater {
                action()
                ledsPane.repaint()
                ledsPane.revalidate()
            }

        private fun extractGreyLabels(keyImageIconsByPosition : Map<Position, KeyImageIcons>) =
            keyImageIconsByPosition.mapValues { JLabel(it.value.grey) }

        private fun SwingUIServiceImpl.getIconLabel(position : Position) =
            labelByPosition.getValue(position)

        private fun SwingUIServiceImpl.getYellowImageIcon(position : Position) =
            keyImageIconsByPosition.getValue(position).yellowIcon

        private fun SwingUIServiceImpl.getGreyImageIcon(position : Position) =
            keyImageIconsByPosition.getValue(position).grey

        private fun getKeyImageIconsByPosition(): Map<Position, KeyImageIcons> {

            val classloader = this::class.java.classLoader

            fun Key.loadIcon(suffix : String) = ImageIcon(classloader.getResource("key-icons/$this-$suffix.png"), "$this-$suffix")

            return keyByPosition.mapValues {
                KeyImageIcons(it.value.loadIcon("grey"), it.value.loadIcon("yellow"))
            }
        }
    }
}