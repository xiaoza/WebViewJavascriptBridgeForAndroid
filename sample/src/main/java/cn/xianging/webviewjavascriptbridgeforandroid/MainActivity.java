package cn.xianging.webviewjavascriptbridgeforandroid;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity implements
        WebFragment.WebFragmentDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        WebFragment mFragment = WebFragment.newInstance("file:///android_asset/ExampleApp.html");
        mFragment.setDelegate(this);

        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_container, mFragment, getClass().getName());
        transaction.commit();
    }

    @Override
    public boolean shouldInterceptWebViewRequest(WebView view, String url) {
        return false;
    }
}
