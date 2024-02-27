package scg.enimavm.model

enum class Rotor(val index: UInt) {

    I(1U),
    II(2U),
    III(3U),
    IV(4U),
    V(5U),
    VI(6U),
    VII(7U),
    VIII(8U),
    BETA(9U),
    GAMMA(10U),
    UKW_M(11U);

    companion object {
        private val rotorByIndex = entries.associateBy(Rotor::index)
        @JvmStatic
        fun byIndex(index: UInt) : Rotor = rotorByIndex.getValue(index)

        @JvmStatic
        fun byName(name: String) : Rotor {

            for (rotor in entries) {
                if (name == rotor.name) {
                    return rotor
                }
            }

            throw NoSuchElementException(name)

        }
    }
}