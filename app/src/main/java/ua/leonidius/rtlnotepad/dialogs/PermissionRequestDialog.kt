package ua.leonidius.rtlnotepad.dialogs

/*class PermissionRequestDialog : DialogFragment(), DialogInterface.OnClickListener {

    private var type: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        type = arguments!!.getInt(TYPE)
        val adb = AlertDialog.Builder(context)

        if (type == EditorFragment.READ_PERMISSION_CODE) {
            adb.setMessage(R.string.grant_read_permissions)
        } else {
            adb.setMessage(R.string.grant_write_permissions)
        }

        adb.setPositiveButton(android.R.string.yes, this)
        adb.setNegativeButton(android.R.string.no, this)

        return adb.create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            if (type == EditorFragment.READ_PERMISSION_CODE) {
                (parentFragment as EditorFragment).tryReadingFileAgain()
            } else
                (parentFragment as EditorFragment).tryWritingFileAgain()
        }
    }

    companion object {
        var TYPE = "type"
    }

}*/