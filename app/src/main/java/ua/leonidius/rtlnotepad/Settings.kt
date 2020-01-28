package ua.leonidius.rtlnotepad

import com.chibatching.kotpref.KotprefModel
import java.util.*

object Settings : KotprefModel() {

    const val SIZE_SMALL = 14
    const val SIZE_MEDIUM = 18
    const val SIZE_LARGE = 22
    const val PREF_THEME_LIGHT = "light"
    const val PREF_THEME_DARK = "dark"

    var useLegacyDialogs by booleanPref(false)
    var theme by stringPref(PREF_THEME_LIGHT)
    var textSize by intPref(SIZE_MEDIUM)
    val lastFiles by stringSetPref { LinkedHashSet() }

}