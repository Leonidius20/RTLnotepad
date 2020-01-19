package ua.leonidius.navdialogs

import android.content.Context
import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment() {

    protected var initializerFunction : (() -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializerFunction?.invoke()
        initializerFunction = null
    }

}