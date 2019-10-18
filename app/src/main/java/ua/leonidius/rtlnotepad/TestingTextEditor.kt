package ua.leonidius.rtlnotepad

import android.content.Context
import android.graphics.Canvas
import android.text.DynamicLayout
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

/**
 * This is an attempt to reimplement EditTest in a way that it only renders the text visible
 * on screen at any given point. It is expected to give a performance boost for editing long text files.
 */

class TestingTextEditor : View {

    private var layout: DynamicLayout? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setText(text: CharSequence) {
        val textPaint = TextPaint()
        //int width = get screen width
        layout = DynamicLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 10f, 10f, false)
        invalidate()
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        if (layout != null) layout!!.draw(canvas)
        canvas.restore()
    }

}