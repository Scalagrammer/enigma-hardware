package scg.enimavm.model

enum class Swap(private val index : UInt) {

    FORWARD(1U), BACKWARD(0U);

    companion object {
        @JvmStatic
        fun byIndex(index : UInt) = entries.firstOrNull { it.index == index } ?: throw NoSuchElementException("Invalid swap index=$index")
    }
}