package scg.enimavm.model

import scg.enimavm.utils.Index

enum class KeyboardEventType(val index : Index) {

    KEY_RELEASED(0U),
    KEY_PRESSED(1U);

    companion object {

        private val keyEventTypeByIndex = entries.associateBy(KeyboardEventType::index)

        @JvmStatic
        fun byIndex(index : Index) : KeyboardEventType =
            keyEventTypeByIndex[index] ?: throw NoSuchElementException("$index")
    }
}