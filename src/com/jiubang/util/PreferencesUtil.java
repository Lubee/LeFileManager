package com.jiubang.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 数据处理类
 * 
 * @author ningguangyao
 * 
 */
public class PreferencesUtil {

  private static final String MODE = "file_mode";

  private static final String TOOLBARVISIBILITY = "visibility_toolbar";

  private static SharedPreferences prefs;

  public static void init(Application app) {
    prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE);
  }

  public static int getMode() {
    int mode = prefs.getInt(PreferencesUtil.MODE, 0);
    return mode;
  }

  public static int getVisibilityToolBar() {
    int mode = prefs.getInt(PreferencesUtil.TOOLBARVISIBILITY, 0);
    return mode;
  }

  public static void putMode(int mode) {
    if (mode == -1) {
      throw new IllegalArgumentException();
    }
    put(PreferencesUtil.MODE, mode);
  }

  public static void putVisibilityToolBar(int toolbarVisibility) {
    if (toolbarVisibility == -1) {
      throw new IllegalArgumentException();
    }
    put(PreferencesUtil.TOOLBARVISIBILITY, toolbarVisibility);
  }

  private static void put(String name, Object value) {
    SharedPreferences.Editor editor = prefs.edit();
    if (value.getClass() == Boolean.class) {
      editor.putBoolean(name, (Boolean) value);
    }
    if (value.getClass() == String.class) {
      editor.putString(name, (String) value);
    }
    if (value.getClass() == Integer.class) {
      editor.putInt(name, ((Integer) value).intValue());
    }
    editor.commit();

  }

}
