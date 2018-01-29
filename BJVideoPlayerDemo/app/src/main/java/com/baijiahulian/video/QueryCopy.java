package com.baijiahulian.video;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by yanglei on 2016/11/3.
 */
public class QueryCopy {
    private final View contentView;
    private View view;

    private QueryCopy(View contentView) {
        this.contentView = contentView;
    }

    public View contentView() {
        return this.contentView;
    }

    public static QueryCopy with(View contentView) {
        return new QueryCopy(contentView);
    }

    public QueryCopy id(int id) {
        view = contentView.findViewById(id);
        return this;
    }

    public View view() {
        return view;
    }

    public QueryCopy image(int resId) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(resId);
        }
        return this;
    }

    public QueryCopy visible() {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public QueryCopy gone() {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
        return this;
    }

    public QueryCopy invisible() {
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        }
        return this;
    }

    public QueryCopy clicked(View.OnClickListener handler) {
        if (view != null) {
            view.setOnClickListener(handler);
        }
        return this;
    }
    public QueryCopy text(CharSequence text) {
        if (view != null && view instanceof TextView) {
            ((TextView) view).setText(text);
        }
        return this;
    }

    public QueryCopy visibility(int visible) {
        if (view != null) {
            view.setVisibility(visible);
        }
        return this;
    }

    public QueryCopy enable(boolean enable) {
        if (view != null) {
            view.setEnabled(enable);
        }
        return this;
    }


}
