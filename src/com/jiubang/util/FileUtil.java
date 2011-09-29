package com.jiubang.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.jiubang.FileListAdapter;

public class FileUtil {

  public static final int switchIcon(File file) {

    String suffix = getSuffix(file);
    if (file.isDirectory())
      return FileListAdapter.DIRECTORY;
    if (suffix == null) {
      return FileListAdapter.UNKNOW;
    }
    suffix = suffix.toLowerCase();
    if (suffix.equals("txt")) {
      return FileListAdapter.TXT;
    } else if (suffix.equals("html") || suffix.equals("htm") || suffix.equals("xml")) {
      return FileListAdapter.HTM;
    } else if (suffix.equals("jpeg") || suffix.equals("jpg") || suffix.equals("bmp") || suffix.equals("gif") || suffix.equals("png")) {
      return FileListAdapter.PHOTO;
    } else if (suffix.equals("rmvb") || suffix.equals("rmb") || suffix.equals("avi") || suffix.equals("wmv") || suffix.equals("mp4")
        || suffix.equals("3gp") || suffix.equals("flv")) {
      return FileListAdapter.MOVIE;
    } else if (suffix.equals("mp3") || suffix.equals("wav") || suffix.equals("wma")) {
      return FileListAdapter.MUSIC;
    } else if (suffix.equals("apk")) {
      return FileListAdapter.APK;
    } else if (suffix.equals("zip") || suffix.equals("tar") || suffix.equals("bar") || suffix.equals("bz2") || suffix.equals("bz")
        || suffix.equals("gz") || suffix.equals("rar")) {
      return FileListAdapter.ZIP;
    } else if (suffix.equals("pdf")) {
      return FileListAdapter.PDF;
    } else if (suffix.equals("doc") || suffix.equals("ppt") || suffix.equals("xls")) {
      return FileListAdapter.WORD;
    } else if (suffix.equals("chm")) {
      return FileListAdapter.CHM;
    }
    return FileListAdapter.UNKNOW;
  }

  private static String getSuffix(File file) {
    String suffix;
    String name = file.getName();
    int la = name.lastIndexOf('.');
    if (la == -1)
      suffix = null;
    else
      suffix = name.substring(la + 1).toLowerCase();
    return suffix;
  }

  public Drawable getAPKDrawable(String filePath,Resources res){
    Drawable dr = null;
    if (filePath != null){

      String PATH_PackageParser = "android.content.pm.PackageParser";  
          String PATH_AssetManager = "android.content.res.AssetManager";  
          try {  

              Class pkgParserCls = Class.forName(PATH_PackageParser);  
              Class[] typeArgs = new Class[1];  
              typeArgs[0] = String.class;  
              Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);  
              Object[] valueArgs = new Object[1];  
              valueArgs[0] = filePath;  
              Object pkgParser = pkgParserCt.newInstance(valueArgs);  
    
              DisplayMetrics metrics = new DisplayMetrics();  
              metrics.setToDefaults();  
  
              typeArgs = new Class[4];  
              typeArgs[0] = File.class;  
              typeArgs[1] = String.class;  
              typeArgs[2] = DisplayMetrics.class;  
              typeArgs[3] = Integer.TYPE;  
              Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",  
                      typeArgs);  
              valueArgs = new Object[4];  
              valueArgs[0] = new File(filePath);  
              valueArgs[1] = filePath;  
              valueArgs[2] = metrics;  
              valueArgs[3] = 0;  
              Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);  
          
              // ApplicationInfo info = mPkgInfo.applicationInfo;  
              Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");  
              ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);  
          
              Class assetMagCls = Class.forName(PATH_AssetManager);  
              Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);  
              Object assetMag = assetMagCt.newInstance((Object[]) null);  
              typeArgs = new Class[1];  
              typeArgs[0] = String.class;  
              Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",  
                      typeArgs);  
              valueArgs = new Object[1];  
              valueArgs[0] = filePath;  
              assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);  
//              Resources res = getResources();  
              typeArgs = new Class[3];  
              typeArgs[0] = assetMag.getClass();  
              typeArgs[1] = res.getDisplayMetrics().getClass();  
              typeArgs[2] = res.getConfiguration().getClass();  
              Constructor resCt = Resources.class.getConstructor(typeArgs);  
              valueArgs = new Object[3];  
              valueArgs[0] = assetMag;  
              valueArgs[1] = res.getDisplayMetrics();  
              valueArgs[2] = res.getConfiguration();  
              res = (Resources) resCt.newInstance(valueArgs);  
              CharSequence label = null;  
              if (info.labelRes != 0) {  
                  label = res.getText(info.labelRes);  
              }  
      
              if (info.icon != 0) {  
                dr = res.getDrawable(info.icon);
              }  
          } catch (Exception e) {  
              e.printStackTrace();  
          }  
    }
    return dr;
  }
  
}