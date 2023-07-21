package org.southasia.ghru.vo

import java.util.*
import java.util.regex.Pattern


data class UserConfig(

    var countryCode: String,
    var mobileCode: String,
    var mobileMaxLength: Int,
    var mobileMinLength: Int,
    var mobileValidationRegex: String,
    var nicValidationRegex: String,
    var nicMaxLength: Int

) {
    companion object {
        fun getUserConfig(countryCode: String?): UserConfig? {
            lateinit var config: UserConfig

            if (countryCode.equals("BD", false)) {

                return UserConfig(
                    "BD",
                    "+880",
                    10,
                    8,
                    "(?=[0-9]*\$)(?:.{8}|.{10})\$",
                    "(?=[0-9]*\$)(?:.{10}|.{13})\$",
                    13
                )

            } else if (countryCode.equals("PK", false)) {

                return UserConfig(
                    "PK",
                    "+92",
                    10,
                    10,
                    "(?=[0-9]*\$)(?:.{10})\$",
                    "(?=[0-9]*\$)(?:.{13})\$",
                    13
                )

            } else if (countryCode.equals("IN", false)) {

                return UserConfig(
                    "IN",
                    "+91",
                    10,
                    10,
                    "(?=[0-9]*\$)(?:.{10})\$",
                    "(?=[0-9]*\$)(?:.{12})\$",
                    12
                )
            } else if (countryCode.equals("LK", false))// default SL
            {
                return UserConfig(
                    "LK",
                    "+94",
                    9,
                    9,
                    "(?=[0-9]*\$)(?:.{9})\$",
                    "(?=[0-9]{9}[x|X|v|V]\$)(?:.{10})\$|(?=[0-9]{12}\$)(?:.{12})",
                    12
                )
            }
            else
            {
                return UserConfig(
                    "UK",
                    "+44",
                    10,
                    10,
                    "(?=[0-9]*\$)(?:.{10})\$",
                    "",
                    10000
                )
            }


        }

        fun isValidPhoneNumber(phoneNumber: String, userConfig: UserConfig): Boolean {

            return (!phoneNumber.isEmpty() &&
                    (phoneNumber.length >= userConfig.mobileMinLength &&
                            phoneNumber.length <= userConfig.mobileMaxLength) &&
                    Pattern.compile(userConfig.mobileValidationRegex).matcher(phoneNumber).matches())

        }

        fun isNICValid(nic: String, userConfig: UserConfig): Boolean {
            return (!nic.isEmpty() &&
                    Pattern.compile(userConfig.nicValidationRegex).matcher(nic).matches())
        }

        fun isValidEMail(email: String): Boolean {
            val regx: String = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"

            return Pattern.compile(regx).matcher(email).matches()
        }

        fun getAge(year: Int, month: Int, day: Int): String {
            val dob = Calendar.getInstance()
            val today = Calendar.getInstance()

            dob.set(year, month, day)

            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            val ageInt = age

            return ageInt.toString()
        }
    }
}


