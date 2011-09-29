package com.jiubang;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.FileListAdapter.FileInfo;
import com.jiubang.util.FileUtil;
import com.jiubang.util.Preferences;
import com.jiubang.util.Util;

public class GoDMActivity extends Activity implements OnItemClickListener {
  private static final String TAG = "GODM";

  public static final String ROOTPATH = "/";
  public static final String SDROOT = "/sdcard";
  private String sdPath = SDROOT;

  private static boolean exitFlag;
  private static boolean isRoot;

  private static int mode;// 0:列表 1：图标
  private static int toolbarVisibility;

  private static int index;
  boolean multFile = false;

  private TextView tvTitle;
  private ImageView imgico;
  private ImageButton rootBtn;
  private ImageButton searchBtn;
  private ImageButton multiSelectBtn;
  private ImageButton upBtn;
  private ImageButton modeBtn;
  ListView itemlist = null;
  GridView itemgrid = null;
  RelativeLayout toolBarLayout;
  List<Map<String, Object>> list;

  FileListAdapter fileAdapterList;

  private FileData currentData;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Preferences.init(this.getApplication());
    initComponment();
    initSettings();
  }

  private void initComponment() {
    tvTitle = (TextView) findViewById(R.id.dmtitle);
    imgico = (ImageView) findViewById(R.id.toolbaricon);
    itemlist = (ListView) findViewById(R.id.listView);
    itemgrid = (GridView) findViewById(R.id.gridView);
    toolBarLayout = (RelativeLayout) findViewById(R.id.toolbar);

    rootBtn = (ImageButton) findViewById(R.id.toolroot);
    searchBtn = (ImageButton) findViewById(R.id.toolsearch);
    multiSelectBtn = (ImageButton) findViewById(R.id.toolselect);
    upBtn = (ImageButton) findViewById(R.id.tooluplevel);
    modeBtn = (ImageButton) findViewById(R.id.toolmode);
  }

  private void initSettings() {
    initPreferences();
    registListener();
    initFileBitmap(mode);
    initItems(sdPath);

    setTitle();
    toolBarLayout.setVisibility(toolbarVisibility);
  }

  private void initPreferences() {
    toolbarVisibility = Preferences.getVisibilityToolBar();
    mode = Preferences.getMode();
  }

  private void registListener() {
    tvTitle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (toolBarLayout.getVisibility() == View.GONE) {
          toolBarLayout.setVisibility(View.VISIBLE);
          imgico.setImageDrawable(GoDMActivity.this.getResources().getDrawable(R.drawable.ic_tray_expand));
          Preferences.putVisibilityToolBar(View.VISIBLE);
        } else {
          toolBarLayout.setVisibility(View.GONE);
          imgico.setImageDrawable(GoDMActivity.this.getResources().getDrawable(R.drawable.ic_tray_collapse));
          Preferences.putVisibilityToolBar(View.GONE);
        }
      }
    });

    upBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (exitFlag)
          finish();
        else
          goToParent();
      }
    });

    rootBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isRoot) {
          refreshItems(SDROOT);
          rootBtn.setImageResource(R.drawable.toolbar_root);
          isRoot = false;
        } else {
          refreshItems(ROOTPATH);
          rootBtn.setImageResource(R.drawable.toolbar_home);
          isRoot = true;
        }

      }
    });

    modeBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Preferences.putMode(mode = (mode == 0 ? 1 : 0));
        initFileBitmap(mode);
        initItems(sdPath);
      }
    });
  }

  private void initItems(String path) {
    if (mode == 0) {
      itemlist.setVisibility(View.VISIBLE);
      itemgrid.setVisibility(View.GONE);
      modeBtn.setImageResource(R.drawable.toolbar_mode_list);
    } else {
      itemlist.setVisibility(View.GONE);
      itemgrid.setVisibility(View.VISIBLE);
      modeBtn.setImageResource(R.drawable.toolbar_mode_icon);
    }
    refreshItems(path);
  }

  private void refreshItems(String path) {
    if (mode == 0) {
      refreshListItems(path);
    } else {
      refreshGridItems(path);
    }
  }

  ArrayList<FileInfo> fInfos = new ArrayList<FileListAdapter.FileInfo>();
  private void refreshListItems(String path) {
    exitFlag = false;
//    list = buildListForSimpleAdapter(path);
//    SimpleAdapter notes = new SimpleAdapter(this, list, R.layout.file_listitem, new String[] { "name", "desc", "img", "path" }, new int[] {
//        R.id.name, R.id.desc, R.id.img, R.id.path });
    
    findFileInfo(path, fInfos);
    currentData  = new FileData(fInfos, null, path);
    fileAdapterList = new FileListAdapter(this, currentData, mode);
    itemlist.setAdapter(fileAdapterList);
    itemlist.setOnItemClickListener(this);
    itemlist.setSelection(index);

    setTitle();
  }

  private List<Map<String, Object>> buildListForSimpleAdapter(String path) {
    File[] files = new File(path).listFiles();
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(files == null ? 0 : files.length);
    if (null != files) {
      for (File file : files) {
        Map<String, Object> map = new HashMap<String, Object>();
        int type = FileUtil.switchIcon(file);
        map.put("img", type);
        map.put("name", file.getName());
        map.put("desc", Util.getDesc(file));
        map.put("path", file.getPath());
        list.add(map);
      }
    }
    return list;
  }
  public ArrayList<FileInfo> currentFileInfo() {
    return currentData.fileInfos;
  }
  private void refreshGridItems(String path) {
    exitFlag = false;
    ArrayList<FileInfo> fInfos = new ArrayList<FileListAdapter.FileInfo>();
        
    findFileInfo(path, fInfos);;
    
 
    // list = buildGridForSimpleAdapter(path);
    // SimpleAdapter notes = new SimpleAdapter(this, list,
    // R.layout.file_griditem, new String[] { "name", "img", "path" }, new int[]
    // {
    // R.id.gridname, R.id.gridimg, R.id.gridpath });
    fileAdapterList = new FileListAdapter(this, currentData, mode);
    itemgrid.setAdapter(fileAdapterList);
    itemgrid.setOnItemClickListener(this);
    itemgrid.setSelection(index);

    setTitle();
  }
  private void findFileInfo(String path, List<FileInfo> list){

      synchronized (list) {
      list.clear();
        File base = new File(path);
        File[] files = base.listFiles();
        if (files == null || files.length == 0)
          return;
        String name;
        int length = files.length;
        for (int i = 0; i < length; i++) {
          name = files[i].getName();
//          if (files[i].isHidden()) {
//            continue;
//          }
          list.add(new FileInfo(name, files[i].getAbsolutePath(),
              FileUtil.switchIcon(files[i]), null, // fileSize(files[i].length()),
              files[i].isDirectory(),Util.getDesc(files[i])));     // //date.toLocaleString(),
           
        }
      Collections.sort(list);
      }

    }
  
  private List<Map<String, Object>> buildGridForSimpleAdapter(String path) {
    File[] files = new File(path).listFiles();
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(files == null ? 0 : files.length);
    if (null != files) {
      for (File file : files) {
        Map<String, Object> map = new HashMap<String, Object>();
        int type = FileUtil.switchIcon(file);
        map.put("img", type);
        map.put("name", file.getName());
        map.put("path", file.getPath());
        list.add(map);
      }
    }
    return list;
  }

  private void setTitle() {
    if (SDROOT.equals(sdPath)) {
      tvTitle.setText(sdPath + Util.showSdCardInfo());
    } else {
      tvTitle.setText(sdPath);
    }
  }

  private void goToParent() {
    File file = new File(sdPath);
    File str_pa = file.getParentFile();
    Log.i(TAG, str_pa + "ghghg");
    if (str_pa == null || ROOTPATH.equals(str_pa.getPath())) {
      Toast.makeText(GoDMActivity.this, getString(R.string.dbclickmsg), Toast.LENGTH_SHORT).show();
      exitFlag = true;
      // refreshListItems(sdPath);
    } else {
      sdPath = str_pa.getAbsolutePath();
      refreshItems(sdPath);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    Log.i(TAG, "item clicked! [" + position + "]");

    String path = (String) list.get(position).get("path");
    File file = new File(path);
    if (file.isDirectory()) {
      index = position;
      sdPath = path;
      refreshItems(sdPath);
    } else {
      doOpenFile(path);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_BACK:
      if (exitFlag) {
        finish();
        System.exit(0);
      } else
        goToParent();
      return true;
    default:
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  public static int DIRECTORY;
  public static int TXT;
  public static int HTM;
  public static int MOVIE;
  public static int MUSIC;
  public static int PHOTO;
  public static int APK;
  public static int ZIP;
  public static int WORD;
  public static int PDF;
  public static int CHM;
  public static int UNKNOW;

  /* 初始化文件图标 */
  private void initFileBitmap(int type) {

    DIRECTORY = R.drawable.folder_32 + type;
    TXT = R.drawable.text_32 + type;
    HTM = R.drawable.html_32 + type;
    MOVIE = R.drawable.format_media_32 + type;
    MUSIC = R.drawable.format_music_32 + type;
    PHOTO = R.drawable.format_picture_32 + type;
    APK = R.drawable.format_app_32 + type;
    ZIP = R.drawable.zip_icon_32 + type;
    UNKNOW = R.drawable.file_32 + type;
    WORD = R.drawable.word_32 + type;
    PDF = R.drawable.pdf_32 + type;
    CHM = R.drawable.chm_32 + type;

  }

  private void doOpenFile(String filePath) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.parse("file://" + filePath);
    String type = null;
    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(filePath));
    if (type == null) {
      String tmp = filePath.toLowerCase();
      if (tmp.endsWith("mp3") || tmp.endsWith("wav") || tmp.endsWith("wma")) {
        type = "audio/*";
      } else if (tmp.endsWith("apk")) {
        type = "application/vnd.android.package-archive";
      } else if (tmp.endsWith("pdf")) {
        type = "application/pdf";
      } else if (tmp.endsWith("txt")) {
        type = "text/plain";
      }
    }
    if (type != null) {
      intent.setDataAndType(uri, type);
      try {
        startActivityForResult(intent, 1);
      } catch (ActivityNotFoundException e) {
        Toast.makeText(this, getString(R.string.can_not_open_file), Toast.LENGTH_SHORT).show();
      }
    } else {
      Toast.makeText(this, getString(R.string.can_not_find_a_suitable_program_to_open_this_file), Toast.LENGTH_SHORT).show();
    }

  }

}

class FileData {
  public ArrayList<FileInfo> fileInfos;
  public ArrayList<Integer> selectedId;
  public String path;
  public boolean searchingTag = false;

  public FileData(ArrayList<FileInfo> fileInfos, ArrayList<Integer> selectedId, String path) {
    if (fileInfos == null)
      this.fileInfos = new ArrayList<FileListAdapter.FileInfo>();
    else
      this.fileInfos = fileInfos;
    if (selectedId == null)
      this.selectedId = new ArrayList<Integer>();
    else
      this.selectedId = selectedId;
    if (path == null)
      this.path = "/sdcard";
    else
      this.path = path;
  }
}
