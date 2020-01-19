package ua.leonidius.rtlnotepad.dialogs

import android.content.Context
import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment() {

    //protected open val viewModel : ViewModel by Model()

    protected var initializerFunction : (() -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializerFunction?.invoke()
        initializerFunction = null
    }

    /*protected fun obtainViewModel(): ViewModel {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(ViewModel::class.java)
            viewModel.javaClass
        }
        return viewModel
    }*/

}