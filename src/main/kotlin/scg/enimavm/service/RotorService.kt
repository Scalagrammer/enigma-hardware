package scg.enimavm.service

import scg.enimavm.model.Rotor
import scg.enimavm.model.Rotor.*
import scg.enimavm.model.Swap
import scg.enimavm.model.Swap.BACKWARD
import scg.enimavm.model.Swap.FORWARD
import scg.enimavm.utils.Position
import scg.enimavm.utils.rotate

interface RotorService {
    fun rotate(rotors : List<Rotor>)
    fun updateRing(rotor : Rotor, position : Position)
    fun updatePosition(rotor : Rotor, position : Position)
    fun Swap.apply(rotor: Rotor, position : Position) : Position
}

class RotorServiceImpl : RotorService {

    private fun interface SetPosition {
        operator fun set(index : Int, position : UInt)
    }

    //     +------+-----+-----+-----+-----+-----+-----+-----+-------+-------+-------+-------+
    //  ↓R | ETW  |  I  | I I |I I I| I V |  V  | V I |V I I|V I I I|B e t a| Gamma | UKW_M |
    //  +--+------+-----+-----+-----+-----+-----+-----+-----+-------+-------+-------+-------+
    // | A | [ 0] |  4  |  0  |  1  |  4  | 21  |  9  | 13  |   5   |  11   |   5   |  17   |
    // | B | [ 1] | 10  |  9  |  3  | 18  | 25  | 15  | 25  |  10   |   4   |  18   |   3   |
    // | C | [ 2] | 12  |  3  |  5  | 14  |  1  |  6  |  9  |  16   |  24   |  14   |  14   |
    // | D | [ 3] |  5  | 10  |  7  | 21  | 17  | 21  |  7  |   7   |   9   |  10   |   1   |
    // | E | [ 4] | 11  | 18  |  9  | 15  |  6  | 14  |  6  |  19   |  21   |   0   |   9   |
    // | F | [ 5] |  6  |  8  | 11  | 25  |  8  | 20  | 17  |  11   |   2   |  13   |  20   |
    // | G | [ 6] |  3  | 17  |  2  |  9  | 19  | 12  |  2  |  23   |  13   |  20   |  19   |
    // | H | [ 7] | 16  | 20  | 15  |  0  | 24  |  5  | 23  |  14   |   8   |   4   |  10   |
    // | I | [ 8] | 21  | 23  | 17  | 24  | 20  | 24  | 12  |   2   |  23   |  17   |  21   |
    // | J | [ 9] | 25  |  1  | 19  | 16  | 15  | 16  | 24  |   1   |  22   |   7   |   4   |
    // | K | [10] | 13  | 11  | 23  | 20  | 18  |  1  | 18  |   9   |  15   |  12   |   7   |
    // | L | [11] | 19  |  7  | 21  |  8  |  3  |  4  | 22  |  18   |   1   |   1   |  12   |
    // | M | [12] | 14  | 22  | 25  | 17  | 13  | 13  |  1  |  15   |  16   |  19   |  11   |
    // | N | [13] | 22  | 19  | 13  |  7  |  7  |  7  | 14  |   3   |  12   |   8   |  16   |
    // | O | [14] | 24  | 12  | 24  | 23  | 11  | 25  | 20  |  25   |   3   |  24   |   2   |
    // | P | [15] |  7  |  2  |  4  | 11  | 23  | 17  |  5  |  17   |  17   |   2   |  18   |
    // | Q | [16] | 23  | 16  |  8  | 13  |  0  |  3  |  0  |   0   |  19   |  22   |  13   |
    // | R | [17] | 20  |  6  | 22  |  5  | 22  | 10  |  8  |  12   |   0   |  11   |   0   |
    // | S | [18] | 18  | 25  |  6  | 19  | 12  |  0  | 21  |   4   |  10   |  16   |  15   |
    // | T | [19] | 15  | 13  |  0  |  6  |  9  | 18  | 11  |  22   |  25   |  15   |   6   |
    // | U | [20] |  0  | 15  | 10  | 10  | 16  | 23  | 15  |  13   |   6   |  25   |   5   |
    // | V | [21] |  8  | 24  | 12  |  3  | 14  | 11  |  4  |   8   |   5   |  23   |   8   |
    // | W | [22] |  1  |  5  | 20  |  2  |  5  |  8  | 10  |  20   |  20   |  21   |  22   |
    // | X | [23] | 17  | 21  | 18  | 12  |  4  |  2  | 16  |  24   |   7   |   6   | 24/25 |
    // | Y | [24] |  2  | 14  | 16  | 22  |  2  | 19  |  3  |   6   |  14   |   9   | 25/23 |
    // | Z | [25] |  9  |  4  | 14  |  1  | 10  | 22  | 19  |  21   |  18   |   3   | 23/24 |
    // +---+------+-----+-----+-----+-----+-----+-----+-----+-------+-------+-------+-------+

    private val tables = buildMap {
        this[I] = buildTable {
            this[0]  =  4U
            this[1]  = 10U
            this[2]  = 12U
            this[3]  =  5U
            this[4]  = 11U
            this[5]  =  6U
            this[6]  =  3U
            this[7]  = 16U
            this[8]  = 21U
            this[9]  = 25U
            this[10] = 13U
            this[11] = 19U
            this[12] = 14U
            this[13] = 22U
            this[14] = 24U
            this[15] =  7U
            this[16] = 23U
            this[17] = 20U
            this[18] = 18U
            this[19] = 15U
            this[20] =  0U
            this[21] =  8U
            this[22] =  1U
            this[23] = 17U
            this[24] =  2U
            this[25] =  9U
        }

        this[II] = buildTable {
            this[0]  =  0U
            this[1]  =  9U
            this[2]  =  3U
            this[3]  = 10U
            this[4]  = 18U
            this[5]  =  8U
            this[6]  = 17U
            this[7]  = 20U
            this[8]  = 23U
            this[9]  =  1U
            this[10] = 11U
            this[11] =  7U
            this[12] = 22U
            this[13] = 19U
            this[14] = 12U
            this[15] =  2U
            this[16] = 16U
            this[17] =  6U
            this[18] = 25U
            this[19] = 13U
            this[20] = 15U
            this[21] = 24U
            this[22] =  5U
            this[23] = 21U
            this[24] = 14U
            this[25] =  4U
        }

        this[III] = buildTable {
            this[0]  = 1U
            this[1]  = 3U
            this[2]  = 5U
            this[3]  = 7U
            this[4]  = 9U
            this[5]  = 11U
            this[6]  = 2U
            this[7]  = 15U
            this[8]  = 17U
            this[9]  = 19U
            this[10] = 23U
            this[11] = 21U
            this[12] = 25U
            this[13] = 13U
            this[14] = 24U
            this[15] = 4U
            this[16] = 8U
            this[17] = 22U
            this[18] = 6U
            this[19] = 0U
            this[20] = 10U
            this[21] = 12U
            this[22] = 20U
            this[23] = 18U
            this[24] = 16U
            this[25] = 14U
        }

        this[IV] = buildTable {
            this[0]  =  4U
            this[1]  = 18U
            this[2]  = 14U
            this[3]  = 21U
            this[4]  = 15U
            this[5]  = 25U
            this[6]  =  9U
            this[7]  =  0U
            this[8]  = 24U
            this[9]  = 16U
            this[10] = 20U
            this[11] =  8U
            this[12] = 17U
            this[13] =  7U
            this[14] = 23U
            this[15] = 11U
            this[16] = 13U
            this[17] =  5U
            this[18] = 19U
            this[19] =  6U
            this[20] = 10U
            this[21] =  3U
            this[22] =  2U
            this[23] = 12U
            this[24] = 22U
            this[25] =  1U
        }

        this[V] = buildTable {
            this[0]  = 21U
            this[1]  = 25U
            this[2]  =  1U
            this[3]  = 17U
            this[4]  =  6U
            this[5]  =  8U
            this[6]  = 19U
            this[7]  = 24U
            this[8]  = 20U
            this[9]  = 15U
            this[10] = 18U
            this[11] =  3U
            this[12] = 13U
            this[13] =  7U
            this[14] = 11U
            this[15] = 23U
            this[16] =  0U
            this[17] = 22U
            this[18] = 12U
            this[19] =  9U
            this[20] = 16U
            this[21] = 14U
            this[22] =  5U
            this[23] =  4U
            this[24] =  2U
            this[25] = 10U
        }

        this[VI] = buildTable {
            this[0]  =  9U
            this[1]  = 15U
            this[2]  =  6U
            this[3]  = 21U
            this[4]  = 14U
            this[5]  = 20U
            this[6]  = 12U
            this[7]  =  5U
            this[8]  = 24U
            this[9]  = 16U
            this[10] =  1U
            this[11] =  4U
            this[12] = 13U
            this[13] =  7U
            this[14] = 25U
            this[15] = 17U
            this[16] =  3U
            this[17] = 10U
            this[18] =  0U
            this[19] = 18U
            this[20] = 23U
            this[21] = 11U
            this[22] =  8U
            this[23] =  2U
            this[24] = 19U
            this[25] = 22U
        }

        this[VII] = buildTable {
            this[0]  = 13U
            this[1]  = 25U
            this[2]  =  9U
            this[3]  =  7U
            this[4]  =  6U
            this[5]  = 17U
            this[6]  =  2U
            this[7]  = 23U
            this[8]  = 12U
            this[9]  = 24U
            this[10] = 18U
            this[11] = 22U
            this[12] =  1U
            this[13] = 14U
            this[14] = 20U
            this[15] =  5U
            this[16] =  0U
            this[17] =  8U
            this[18] = 21U
            this[19] = 11U
            this[20] = 15U
            this[21] =  4U
            this[22] = 10U
            this[23] = 16U
            this[24] =  3U
            this[25] = 19U
        }

        this[VIII] = buildTable {
            this[0]  =  5U
            this[1]  = 10U
            this[2]  = 16U
            this[3]  =  7U
            this[4]  = 19U
            this[5]  = 11U
            this[6]  = 23U
            this[7]  = 14U
            this[8]  =  2U
            this[9]  =  1U
            this[10] =  9U
            this[11] = 18U
            this[12] = 15U
            this[13] =  3U
            this[14] = 25U
            this[15] = 17U
            this[16] =  0U
            this[17] = 12U
            this[18] =  4U
            this[19] = 22U
            this[20] = 13U
            this[21] =  8U
            this[22] = 20U
            this[23] = 24U
            this[24] =  6U
            this[25] = 21U
        }

        this[BETA] = buildTable {
            this[0] = 11U
            this[1] =  4U
            this[2] = 24U
            this[3] =  9U
            this[4] = 21U
            this[5] =  2U
            this[6] = 13U
            this[7] =  8U
            this[8] = 23U
            this[9] = 22U
            this[1] = 15U
            this[1] =  1U
            this[1] = 16U
            this[1] = 12U
            this[1] =  3U
            this[1] = 17U
            this[1] = 19U
            this[1] =  0U
            this[1] = 10U
            this[1] = 25U
            this[2] =  6U
            this[2] =  5U
            this[2] = 20U
            this[2] =  7U
            this[2] = 14U
            this[2] = 18U
        }

        this[GAMMA] = buildTable {
            this[0]  =  5U
            this[1]  = 18U
            this[2]  = 14U
            this[3]  = 10U
            this[4]  =  0U
            this[5]  = 13U
            this[6]  = 20U
            this[7]  =  4U
            this[8]  = 17U
            this[9]  =  7U
            this[10] = 12U
            this[11] =  1U
            this[12] = 19U
            this[13] =  8U
            this[14] = 24U
            this[15] =  2U
            this[16] = 22U
            this[17] = 11U
            this[18] = 16U
            this[19] = 15U
            this[20] = 25U
            this[21] = 23U
            this[22] = 21U
            this[23] =  6U
            this[24] =  9U
            this[25] =  3U
        }

        this[UKW_M] = buildTable(22) {
            this[0]  = 17U
            this[1]  =  3U
            this[2]  = 14U
            this[3]  =  1U
            this[4]  =  9U
            this[5]  = 20U
            this[6]  = 19U
            this[7]  = 10U
            this[8]  = 21U
            this[9]  =  4U
            this[10] =  7U
            this[11] = 12U
            this[12] = 11U
            this[13] = 16U
            this[14] =  2U
            this[15] = 18U
            this[16] = 13U
            this[17] =  0U
            this[18] = 15U
            this[19] =  6U
            this[20] =  5U
            this[21] =  8U
        }
    }

    //  +------+------+-------+
    //  |  RT  | NTCH | TRNVR |
    //  +------+------+-------+
    //  | ETW  | -    | -     |
    //  | I	   | 24   | 16    |
    //  | II   | 12   | 4     |
    //  | III  | 3    | 21    |
    //  | IV   | 17   | 9     |
    //  | V	   | 7    | 25    |
    //  | VI   | 7/20 | 25/12 |
    //  | VII  | 7/20 | 25/12 |
    //  | VIII | 7/20 | 25/12 |
    //  +------+------+-------+

    // A | [ 0]
    //B | [ 1]
    //C | [ 2]
    //D | [ 3]
    //E | [ 4]
    //F | [ 5]
    //G | [ 6]
    //H | [ 7]
    //I | [ 8]
    //J | [ 9]
    //K | [10]
    //L | [11]
    //M | [12]
    //N | [13]
    //O | [14]
    //P | [15]
    //Q | [16]
    //R | [17]
    //S | [18]
    //T | [19]
    //U | [20]
    //V | [21]
    //W | [22]
    //X | [23]
    //Y | [24]
    //Z | [25]

    val notches = buildMap {
        this[I]    = setOf(2)
        this[II]   = setOf(3)
        this[III]  = setOf(5)
        this[IV]   = setOf(7)
        this[V]    = setOf(11)
        this[VI]   = setOf(13)
        this[VII]  = setOf(17)
        this[VIII] = setOf(19)
    }

    val turnovers = buildMap {
        this[I]    = 0
        this[II]   = 0
        this[III]  = 0
        this[IV]   = 0
        this[V]    = 0
        this[VI]   = 0
        this[VII]  = 0
        this[VIII] = 0
    }.toMutableMap()

    override fun Swap.apply(rotor : Rotor, position : Position): Position {

        val table = tables.getValue(rotor)

        return when {
            rotor == UKW_M && position == 22U                     -> { 22U                              }
            rotor == UKW_M && equals(FORWARD)  && position == 23U -> { 24U                              }
            rotor == UKW_M && equals(FORWARD)  && position == 24U -> { 25U                              }
            rotor == UKW_M && equals(FORWARD)  && position == 25U -> { 23U                              }
            rotor == UKW_M && equals(BACKWARD) && position == 23U -> { 25U                              }
            rotor == UKW_M && equals(BACKWARD) && position == 25U -> { 24U                              }
            rotor == UKW_M && equals(BACKWARD) && position == 24U -> { 23U                              }
            equals(BACKWARD)                                      -> { table.indexOf(position).toUInt() }
            else /*FORWARD*/                                      -> { table.get(position.toInt())      }
        }
    }

    override fun updatePosition(rotor : Rotor, position : Position) {

        if (position > 25U) return

        tables.getValue(rotor).apply {

            val turns = (25 - (turnovers[rotor] ?: return)) + position.toInt()

            rotate(turns)

            println("$rotor.[$position]")

        }
    }

    override fun rotate(rotors : List<Rotor>) {

        var rotateNext = true

        for (rotor in rotors) if (rotateNext) {
            tables.getValue(rotor).run { rotate(1) }
            rotateNext = turnovers.compute(rotor) { _, position -> ((position ?: 0) + 1) % 26 } in notches.getValue(rotor)
        } else break
    }

    override fun updateRing(rotor : Rotor, position : Position) {
        println("$rotor'[$position]")
    }

    private fun buildTable(size : Int = 26, positions : SetPosition.() -> Unit) : MutableList<Position> {
        return ArrayList<Position>(size).apply { SetPosition(::add).also(positions) }
    }

}