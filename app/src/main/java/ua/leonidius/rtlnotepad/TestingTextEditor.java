package ua.leonidius.rtlnotepad;

import android.content.Context;
import android.graphics.Canvas;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * This is an attempt to reimplement EditTest in a way that it only renders the text visible
 * on screen at any given point. It is expected to give a performance boost for editing long text files.
 */

public class TestingTextEditor extends View {

    private DynamicLayout layout;

    public TestingTextEditor(Context context) {
        super(context);
    }

    public TestingTextEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestingTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setText(CharSequence text) {
        TextPaint textPaint = new TextPaint();
        //int width = get screen width
        layout = new DynamicLayout(text, textPaint, getWidth(), Layout.Alignment.ALIGN_NORMAL, 10, 10, false);
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        if (layout != null) layout.draw(canvas);
        canvas.restore();
    }
}
