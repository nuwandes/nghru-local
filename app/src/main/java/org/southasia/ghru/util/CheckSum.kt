package org.southasia.ghru.util

import org.southasia.ghru.vo.MessageCheckSum


fun validateChecksum(result: String, type: String): MessageCheckSum {

    try {
        if (result.contains("-", false)) {
            var validated: Boolean = false
            var splitedValues = result.split("-")
            val first: String = splitedValues.first()
            val second: String = splitedValues.get(1)
            val checksum: String = splitedValues.last()
            val input = "$first-$second"


            val expected = calculateCheckDigit(convertIDToDigits(input)!!)
            if (expected.toString().equals(checksum)) {

                var firstLetter = result.toString().toCharArray()[0].toUpperCase();
                if (type.equals(firstLetter.toString(), true)) {
                    return MessageCheckSum(false, "")
                } else {
                    var message: String = ""
                    if (type.equals(Constants.TYPE_ENUMERATION))
                        message = "Invalid code. Please check for format 'EXX-XXXX-X'"
                    else if (type.equals(Constants.TYPE_PARTICIPANT))
                        message = "Invalid code. Please check for format 'PXX-XXXX-X'"
                    else if (type.equals(Constants.TYPE_SAMPLE))
                        message = "Invalid code. Please check for format 'SXX-XXXX-X'"
                    else if (type.equals(Constants.TYPE_STORAGE))
                        message = "Invalid code. Please check for format 'CXX-XXXX-X'"
                    else if (type.equals(Constants.TYPE_FREEZER_BOX))
                        message = "Invalid code. Please check for format 'FXX-XXXX-X'"

                    return MessageCheckSum(true, message)
                }
            } else {
                return MessageCheckSum(true, "Invalid code")
            }
        } else {
            return MessageCheckSum(true, "Invalid code")
        }
    } catch (exeption: Exception) {
        return MessageCheckSum(true, "Invalid code")
    }
}


fun convertIDToDigits(string: String): String? {

    var digits: String? = string.substringAfterLast("-")
    val chars: String? = string.substringBefore("-")

    val charsList = chars?.reversed()
    charsList?.forEach {
        val num = it.toUpperCase().toInt()
        digits = num.toString() + digits
    }
    print("string $string digits $digits")
    return digits
}

//fun checkSum(digits: CharSequence): Char {
//    return digits.sumByIndexed { index, digit ->
//        when {
//            index.isEven -> digit.asInt()
//            else -> sumDigits(2 * digit.asInt())
//        }
//    }.let { sum -> 10 - sum % 10 }.asChar()
//}

//inline fun CharSequence.sumByIndexed(selector: (Int, Char) -> Int): Int {
//    return foldIndexed(0) { index, sum, element -> sum + selector(index, element) }
//}
//
//val Int.isEven: Boolean
//    get() = and(1) == 0
//
//fun Char.asInt() = toInt() - 48
//fun Int.asChar() = toChar() + 48
//
//val Int.magnitude: Int
//    get() = if (this < 0) -this else this
//
//fun sumDigits(n: Int): Int = if (n.magnitude < 10) n else n % 10 + sumDigits(n / 10)


//fun checkSum(card: String?): Boolean {
//    if (card == null)
//        return false
//    val checkDigit = card[card.length - 1]
//    val digit = calculateCheckDigit(card.substring(0, card.length - 1))
//    return checkDigit == digit!![0]
//}

/**
 * Calculates the last digits for the card number received as parameter
 *
 * @param card
 * [String] number
 * @return [String] the check digit
 */
fun calculateCheckDigit(card: String?): String? {
    if (card == null)
        return null
    val digit: String
    /* convert to array of int for simplicity */
    val digits = IntArray(card.length)
    for (i in 0 until card.length) {
        digits[i] = Character.getNumericValue(card[i])
    }

    /* double every other starting from right - jumping from 2 in 2 */
    run {
        var i = digits.size - 1
        while (i >= 0) {
            digits[i] += digits[i]

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9
            }
            i -= 2
        }
    }
    var sum = 0
    for (i in digits.indices) {
        sum += digits[i]
    }
    /* multiply by 9 step */
    sum = sum * 9

    /* convert to string to be easier to take the last digit */
    digit = sum.toString() + ""
    return digit.substring(digit.length - 1)
}

