package com.bouncy.frogue;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

/**
 * A thin shell: it opens the timer (the one and only copy, on GitHub Pages) full screen.
 * On a Fire TV it opens straight into TV mode (?display) and keeps the screen on; on a phone it's the normal timer.
 */
public class MainActivity extends Activity {
    private static final int BG = 0xFF0E1116;
    private WebView web;
    private FrameLayout root;
    private View fullView;                              // the page's own "Full screen" (phone)
    private WebChromeClient.CustomViewCallback fullDone;
    private int lastBg = 0;
    private boolean tv;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String url;
    private static final int PICK_FILE = 1, SAVE_FILE = 2;
    private ValueCallback<Uri[]> picked;                // Import: the page is waiting for a file
    private String toSave;                              // Export: the backup waiting for a place to go

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        tv = isTv(this);
        url = getString(R.string.app_url) + (tv ? "?display" : "");
        if (tv) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // the TV is the clock: never let it nod off

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        web = new WebView(this);
        web.setBackgroundColor(BG);
        // The page sits inside a frame that keeps it clear of the phone's status and navigation bars. (Padding the WebView
        // itself doesn't move the page, it only makes it scroll.) The frame takes the timer's colour, so the bars blend in.
        root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        root.addView(web, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View v, WindowInsets wi) {
                if (Build.VERSION.SDK_INT >= 30) {
                    Insets i = wi.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                    v.setPadding(i.left, i.top, i.right, i.bottom);
                    return WindowInsets.CONSUMED;
                }
                v.setPadding(wi.getSystemWindowInsetLeft(), wi.getSystemWindowInsetTop(), wi.getSystemWindowInsetRight(), wi.getSystemWindowInsetBottom());
                return wi.consumeSystemWindowInsets();
            }
        });
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                       // your workouts and the TV's pairing code live here
        s.setMediaPlaybackRequiresUserGesture(false);       // lets the beeps play without a tap
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setTextZoom(100);                                 // the timer sizes its own text; the phone's font setting would squash the layout
        web.addJavascriptInterface(new Bridge(), "FrogueApp");

        web.setWebChromeClient(new WebChromeClient() {
            // The page's "Full screen" button: show the page on its own and hide the phone's bars until it's done
            @Override public void onShowCustomView(View v, CustomViewCallback done) {
                if (fullView != null) { done.onCustomViewHidden(); return; }
                fullView = v; fullDone = done;
                root.addView(v, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                hideBars(true);
            }
            // Import: open the phone's file picker (any file, as .json files often have no proper type)
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (picked != null) picked.onReceiveValue(null);
                picked = cb;
                Intent in = new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
                try { startActivityForResult(Intent.createChooser(in, "Pick a workouts file"), PICK_FILE); }
                catch (Exception e) { picked = null; cb.onReceiveValue(null); toast("There's no file picker on this device"); return true; }
                return true;
            }
            @Override public void onHideCustomView() {
                if (fullView == null) return;
                root.removeView(fullView); fullView = null; fullDone = null;
                if (!tv) hideBars(false);
                web.requestFocus();
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView v, String to) {
                Uri u = Uri.parse(to), home = Uri.parse(url);
                if (home.getHost() != null && home.getHost().equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { /* nothing to open it with */ }
                return true;                                // other sites open in the browser
            }
            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView v, int code, String desc, String failingUrl) {
                if (url.equals(failingUrl)) retryLater();   // no internet and nothing saved yet (called on every Android version)
            }
        });
        if (state == null) web.loadUrl(url); else web.restoreState(state);
        web.requestFocus();

        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, new OnBackInvokedCallback() {
                @Override public void onBackInvoked() { back(); }
            });
        }
    }

    /** A Fire TV (or any Android TV): open in TV mode. */
    static boolean isTv(Context c) {
        PackageManager pm = c.getPackageManager();
        if (pm.hasSystemFeature("amazon.hardware.fire_tv") || pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) return true;
        UiModeManager ui = (UiModeManager) c.getSystemService(Context.UI_MODE_SERVICE);
        return ui != null && ui.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    /** What the page can ask of the app. */
    class Bridge {
        @JavascriptInterface public boolean isTv() { return tv; }
        /** The page's wake lock: the browser view inside an app has none, so the app keeps the screen on instead. */
        @JavascriptInterface public void keepAwake(final boolean on) {
            runOnUiThread(new Runnable() { public void run() {
                if (on) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else if (!tv) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } });
        }
        /** Export: ask where to save the backup file (the browser view inside an app can't download). */
        @JavascriptInterface public void saveFile(final String name, final String text) {
            runOnUiThread(new Runnable() { public void run() {
                toSave = text;
                Intent in = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json").putExtra(Intent.EXTRA_TITLE, name);
                try { startActivityForResult(in, SAVE_FILE); } catch (Exception e) { toSave = null; toast("There's nowhere to save files on this device"); }
            } });
        }
    }

    @Override
    protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req, result, data);
        Uri u = (result == RESULT_OK && data != null) ? data.getData() : null;
        if (req == PICK_FILE && picked != null) {
            picked.onReceiveValue(u == null ? null : new Uri[] { u });
            picked = null;
        } else if (req == SAVE_FILE && toSave != null) {
            if (u != null) {
                try {
                    java.io.OutputStream out = getContentResolver().openOutputStream(u);
                    out.write(toSave.getBytes("UTF-8")); out.close();
                    toast("Workouts saved");
                } catch (Exception e) { toast("Couldn't save the file"); }
            }
            toSave = null;
        }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void retryLater() {
        web.loadData("<body style='background:#0e1116;color:#fff;font:" + (tv ? "5vh" : "20px") + " sans-serif;display:flex;align-items:center;justify-content:center;height:90vh;margin:0;text-align:center;padding:24px'>"
                + "Frogue can't reach the timer yet. Trying again in a moment…</body>", "text/html", "utf-8");
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() { public void run() { web.loadUrl(url); } }, 5000);
        if (!tv) handler.post(matchBg);
    }

    @SuppressWarnings("deprecation")
    private void hideBars(boolean hide) {
        getWindow().getDecorView().setSystemUiVisibility(hide
                ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                : 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && (tv || fullView != null)) hideBars(true);
    }

    /**
     * Back (phone or remote): first let the page use it (close a dialog, or "Stop this workout?").
     * If it has nothing to do: leave full screen, then leave the app.
     */
    private void back() {
        web.evaluateJavascript("(window.frogueTvBack && window.frogueTvBack()) ? 'yes' : 'no'", new ValueCallback<String>() {
            @Override public void onReceiveValue(String v) {
                if (v != null && v.contains("yes")) return;
                if (fullView != null) { web.evaluateJavascript("document.exitFullscreen && document.exitFullscreen().catch(function(){})", null); return; }
                if (tv) finish(); else moveTaskToBack(true);
            }
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() { back(); }

    /** The Fire TV remote's play/pause, rewind and fast-forward buttons work like OK, left and right. */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        String code = null;
        if (k == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || k == KeyEvent.KEYCODE_MEDIA_PLAY || k == KeyEvent.KEYCODE_MEDIA_PAUSE) code = "MediaPlayPause";
        else if (k == KeyEvent.KEYCODE_MEDIA_REWIND || k == KeyEvent.KEYCODE_MEDIA_PREVIOUS) code = "ArrowLeft";
        else if (k == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || k == KeyEvent.KEYCODE_MEDIA_NEXT) code = "ArrowRight";
        if (code == null) return super.dispatchKeyEvent(e);
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0)
            web.evaluateJavascript("document.dispatchEvent(new KeyboardEvent('keydown',{key:'" + code + "',code:'" + code + "',bubbles:true,cancelable:true}))", null);
        return true;
    }

    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }

    /** Colours the phone's bars to match the timer, which changes colour with each phase. */
    private final Runnable matchBg = new Runnable() {
        public void run() {
            web.evaluateJavascript("getComputedStyle(document.body).backgroundColor", new ValueCallback<String>() {
                @Override public void onReceiveValue(String v) {
                    int c = parseRgb(v);
                    if (c != 0 && c != lastBg) {
                        lastBg = c; root.setBackgroundColor(c);
                        if (Build.VERSION.SDK_INT < 35) { getWindow().setStatusBarColor(c); getWindow().setNavigationBarColor(c); }   // older phones draw their own bars
                    }
                }
            });
            handler.postDelayed(this, 600);
        }
    };
    static int parseRgb(String v) {
        if (v == null) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)(?:,\\s*([\\d.]+))?").matcher(v);
        if (!m.find()) return 0;
        if (m.group(4) != null && Float.parseFloat(m.group(4)) < 0.5f) return 0;   // see-through: keep the last colour
        return 0xFF000000 | (Integer.parseInt(m.group(1)) << 16) | (Integer.parseInt(m.group(2)) << 8) | Integer.parseInt(m.group(3));
    }

    @Override protected void onResume() { super.onResume(); web.onResume(); handler.removeCallbacks(matchBg); if (!tv) handler.post(matchBg); }
    @Override protected void onPause() { handler.removeCallbacks(matchBg); web.onPause(); super.onPause(); }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); web.destroy(); super.onDestroy(); }
}
