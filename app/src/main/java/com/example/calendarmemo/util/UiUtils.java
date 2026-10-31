package com.example.calendarmemo.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.util.TypedValue;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * 页面通用 UI 工具：沉浸式状态栏、系统栏安全边距、键盘收起、dp 转换。
 *
 * <p>Android 15 起 targetSdk 35+ 默认强制 edge-to-edge，因此这里显式处理
 * WindowInsets，避免标题被状态栏遮挡、底部按钮被导航栏遮挡。</p>
 */
public final class UiUtils {

    private UiUtils() {
    }

    public static void applyEdgeToEdge(Activity activity, View root, boolean includeIme) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            // 同时合并 displayCutout：刘海/挖孔区域的 top inset 可能大于状态栏本身。
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            int bottom = bars.bottom;
            if (includeIme) {
                Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
                bottom = Math.max(bottom, ime.bottom);
            }
            view.setPadding(initialLeft + bars.left,
                    initialTop + bars.top,
                    initialRight + bars.right,
                    initialBottom + bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    public static void hideKeyboard(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus == null) {
            focus = activity.getWindow().getDecorView();
        }
        InputMethodManager manager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    public static int dp(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }
}
