package cn.xianging.webviewjavascriptbridgeforandroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * Created by xiaoz on 15/10/20.
 */
public class WebViewWithProgress extends WebView {

    private ProgressBar mProgressBar;
    private OnTitleReadyListener mOnTitleReadyListener;

    public WebViewWithProgress(Context context) {
        this(context, null);
    }

    public WebViewWithProgress(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public WebViewWithProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("ALL")
    private void init() {
        mProgressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 6, 0, 0
        );
        mProgressBar.setLayoutParams(params);

        Drawable drawable = getResources().getDrawable(R.drawable.progress_bar_horizontal_states);
        mProgressBar.setProgressDrawable(drawable);

        addView(mProgressBar);
        setWebChromeClient(new WebClient());
//        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // 启用javascript
        getSettings().setJavaScriptEnabled(true);
        getSettings().setUseWideViewPort(true);

        //是否可以缩放
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSettings().setDisplayZoomControls(false);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) mProgressBar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        mProgressBar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public class WebClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mOnTitleReadyListener != null) {
                mOnTitleReadyListener.onTitleReady(title);
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setVisibility(GONE);
            } else {
                if (mProgressBar.getVisibility() == GONE) {
                    mProgressBar.setVisibility(VISIBLE);
                }
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    public interface OnTitleReadyListener {
        void onTitleReady(String title);
    }

    public void setOnTitleReadyListener(OnTitleReadyListener onTitleReadyListener) {
        mOnTitleReadyListener = onTitleReadyListener;
    }
}
