package com.averi.worldscribe.views;

import android.content.Context;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * A Marquee-able Android Toolbar.
 *
 * <p>
 *     Credit to InsanityOnABun. The Gist for this class can be found
 *     <a href='https://gist.github.com/InsanityOnABun/95c0757f2f527cc50e39'>here</a>.
 * </p>
 */
public class MarqueeToolbar extends Toolbar {

    TextView title;
    boolean reflected = false;

    public MarqueeToolbar(Context context) {
        super(context);
    }

    public MarqueeToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!reflected) {
            reflected = reflectTitle();
        }
        super.setTitle(title);
        // Due to postDelayed(), selectTitle() will cause an exception if it's called before
        // the title TextView has been created. In Activities, setSupportActionBar() might
        // call selectTitle() too early, thus causing an exception.
        // We can prevent this by checking reflected, which can only be set
        // to true if the title TextView exists.
        if (reflected) {
            selectTitle();
        }
    }

    @Override
    public void setTitle(int resId) {
        if (!reflected) {
            reflected = reflectTitle();
        }
        super.setTitle(resId);
        // Due to postDelayed(), selectTitle() will cause an exception if it's called before
        // the title TextView has been created. In Activities, setSupportActionBar() might
        // call selectTitle() too early, thus causing an exception.
        // We can prevent this by checking reflected, which can only be set
        // to true if the title TextView exists.
        if (reflected) {
            selectTitle();
        }
    }

    private boolean reflectTitle() {
        try {
            Field field = Toolbar.class.getDeclaredField("mTitleTextView");
            field.setAccessible(true);
            title = (TextView) field.get(this);
            title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            title.setMarqueeRepeatLimit(-1);
            return true;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void selectTitle() {
        title.postDelayed(new Runnable() {
            @Override
            public void run() {
                title.setSelected(true);
            }
        }, 1000);
    }
}
