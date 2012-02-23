package com.jiubang;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.FileListAdapter.FileInfo;
import com.jiubang.util.FileUtil;
import com.jiubang.util.PreferencesUtil;
import com.jiubang.util.Util;

public class GoDMActivity extends Activity implements OnItemClickListener {
  private static final String TAG = "GODM";

  private static final int OPEN = Menu.FIRST;
  private static final int COPY = Menu.FIRST + 1;
  private static final int CUT = Menu.FIRST + 2;
  private static final int RENAME = Menu.FIRST + 3;
  private static final int DELETE = Menu.FIRST + 4;
  private static final int SELECTALL = Menu.FIRST + 5;
  private static final int ATTRIBUTE = Menu.FIRST + 6;

  public static final String ROOTPATH = "/";// root根路径
  public static final String SDROOT = "/sdcard";// sd卡根路径
  private String sdPath = SDROOT;// 当前路径

  private static boolean exitFlag;// 退出标志
  private static boolean isRoot;// 是否是root目录

  private static int mode;// 0:列表 1：图标
  private static int toolbarVisibility;

  private static int index;
  boolean multFile;// 是否多选

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

  FileListAdapter fileAdapterList;
  FileData currentData;

  ArrayList<FileInfo> fInfos = new ArrayList<FileListAdapter.FileInfo>();
  FileInfo clickInfo;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    PreferencesUtil.init(this.getApplication());
    initComponment();
  }

  @Override
  protected void onResume() {
    initSettings();
    super.onResume();
  }

  /**
   * 
   */
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

  /**
   * 
   */
  private void initSettings() {
    initPreferences();
    registListener();
    initItems(sdPath);
    fileAdapterList.initFileBitmap(mode);

    setTitle();
    toolBarLayout.setVisibility(toolbarVisibility);
  }

  /**
   * 
   */
  private void initPreferences() {
    toolbarVisibility = PreferencesUtil.getVisibilityToolBar();
    mode = PreferencesUtil.getMode();
  }

  private void registListener() {
    tvTitle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        hiddenToolBar();
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
        PreferencesUtil.putMode(mode = (mode == 0 ? 1 : 0));
        initItems(sdPath);
        fileAdapterList.initFileBitmap(mode);
      }
    });

    itemlist.setOnItemClickListener(this);
    itemlist.setOnItemLongClickListener(itemLongClickListener);
    itemlist.setOnCreateContextMenuListener(contextMenuListener);

    itemgrid.setOnItemClickListener(this);
    itemgrid.setOnItemLongClickListener(itemLongClickListener);
    itemgrid.setOnCreateContextMenuListener(contextMenuListener);
  }

  private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
      clickInfo = currentData.fileInfos.get(position);
      index = position;
      return false;
    }
  };

  private OnCreateContextMenuListener contextMenuListener = new OnCreateContextMenuListener() {
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
      menu.setHeaderTitle(R.string.operation).setHeaderIcon(R.drawable.operations);
      menu.add(0, OPEN, 0, R.string.open);
      menu.add(0, COPY, 0, R.string.copy);
      menu.add(0, CUT, 0, R.string.cut);
      menu.add(0, RENAME, 0, R.string.rename);
      menu.add(0, DELETE, 0, R.string.delete);
      menu.add(0, SELECTALL, 0, R.string.selectall);
      menu.add(0, ATTRIBUTE, 0, R.string.attribute);
    }
  };

  /**
   * 
   * @param path
   */
  private void initItems(String path) {
    //setCurrentData(path);
    if (mode == 0) {
      itemlist.setVisibility(View.VISIBLE);
      itemgrid.setVisibility(View.GONE);
      modeBtn.setImageResource(R.drawable.toolbar_mode_list);
      //refreshListItems();
    } else {
      itemlist.setVisibility(View.GONE);
      itemgrid.setVisibility(View.VISIBLE);
      modeBtn.setImageResource(R.drawable.toolbar_mode_icon);
      //refreshGridItems();
    }
    refreshItems(path);
    //fileAdapterList.notifyDataSetChanged();
  }

  private void refreshItems(String path) {
    setCurrentData(path);
    if (mode == 0) {
      refreshListItems();
    } else {
      refreshGridItems();
    }
    fileAdapterList.notifyDataSetChanged();
  }

  private void setCurrentData(String path) {
    findFileInfo(path, fInfos);
    currentData = new FileData(fInfos, null, path);
  }

  private void refreshListItems() {
    exitFlag = false;

    fileAdapterList = new FileListAdapter(this, currentData, mode);
    itemlist.setAdapter(fileAdapterList);
    itemlist.setSelection(index);

    setTitle();
  }

  private void refreshGridItems() {
    exitFlag = false;
    fileAdapterList = new FileGridAdapter(this, currentData, mode);
    itemgrid.setAdapter(fileAdapterList);
    itemgrid.setSelection(index);

    setTitle();
  }

  /**
   * 
   * @param path
   * @param list
   */
  private void findFileInfo(String path, List<FileInfo> list) {

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
        // if (files[i].isHidden()) {
        // continue;
        // }
        list.add(new FileInfo(name, files[i].getAbsolutePath(), FileUtil.switchIcon(files[i]), null, // fileSize(files[i].length()),
            files[i].isDirectory(), FileUtil.getDesc(files[i]))); // //date.toLocaleString(),

      }
      Collections.sort(list);
    }

  }

  private void setTitle() {
    if (SDROOT.equals(sdPath)) {
      tvTitle.setText(sdPath + Util.showSdCardInfo());
    } else {
      tvTitle.setText(sdPath);
    }
  }

  public ArrayList<FileInfo> currentFileInfo() {
    return currentData.fileInfos;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    Log.i(TAG, "item clicked! [" + position + "]");

    clickInfo = currentData.fileInfos.get(position);
    doOpen();
    index = position;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_BACK:
      if (exitFlag) {
        exitApp();
      } else
        goToParent();
      return true;
    default:
      return super.onKeyDown(keyCode, event);
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(1, 11, 0, R.string.operation).setIcon(R.drawable.operations);
    menu.add(2, 21, 0, R.string.create).setIcon(android.R.drawable.ic_menu_add);
    menu.add(3, 31, 0, R.string.setting).setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(5, 51, 0, R.string.hiddenbar).setIcon(android.R.drawable.ic_menu_upload);

    menu.add(4, 41, 0, R.string.refresh).setIcon(R.drawable.refresh);
    menu.add(4, 42, 1, R.string.rotation);
    menu.add(4, 43, 3, R.string.about);
    menu.add(4, 44, 10, R.string.exit);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPEN:
      doOpen();
      break;

    default:
      break;
    }
    return super.onContextItemSelected(item);
  }

  // 监听事件
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case 11:
      break;
    case 21:

      break;
    case 22:

      break;
    case 31:
      openSettingView();
      break;
    case 41:
      refreshItems(sdPath);
      break;
    case 51:
      hiddenToolBar();
      break;
    case 42:

      break;
    case 43:

      break;
    case 44:
      exitApp();
      break;
    }
    return false;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  /**
   * 
   */
  private void exitApp() {
    finish();
    System.exit(0);
  }
  
  /**
   * 
   */
  private void hiddenToolBar() {
    if (toolBarLayout.getVisibility() == View.GONE) {
      toolBarLayout.setVisibility(View.VISIBLE);
      imgico.setImageDrawable(GoDMActivity.this.getResources().getDrawable(R.drawable.ic_tray_expand));
      PreferencesUtil.putVisibilityToolBar(View.VISIBLE);
    } else {
      toolBarLayout.setVisibility(View.GONE);
      imgico.setImageDrawable(GoDMActivity.this.getResources().getDrawable(R.drawable.ic_tray_collapse));
      PreferencesUtil.putVisibilityToolBar(View.GONE);
    }
  }
  /**
   * 
   * @param position
   */
  private void doOpen() {
    String path = clickInfo.path;
    File file = new File(path);
    if (file.isDirectory()) {
      index = 0;
      sdPath = path;
      refreshItems(sdPath);
    } else {
      doOpenFile(path);
    }
  }

  /**
   * 
   * @param filePath
   */
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
      } else if (tmp.endsWith("doc")) {
        type = "application/msword";
      }
    }
    if (type != null) {
      intent.setDataAndType(uri, type);
      try {
        startActivity(intent);
      } catch (ActivityNotFoundException e) {
        Toast.makeText(this, getString(R.string.can_not_open_file), Toast.LENGTH_SHORT).show();
      }
    } else {
      Toast.makeText(this, getString(R.string.can_not_find_a_suitable_program_to_open_this_file), Toast.LENGTH_SHORT).show();
    }

  }

  /**
   * 
   */
  private void goToParent() {
    File file = new File(sdPath);
    File str_pa = file.getParentFile();
    Log.i(TAG, str_pa + "ghghg");
    if (str_pa == null || ROOTPATH.equals(str_pa.getPath())) {
      Toast.makeText(GoDMActivity.this, getString(R.string.dbclickmsg), Toast.LENGTH_SHORT).show();
      exitFlag = true;
    } else {
      sdPath = str_pa.getAbsolutePath();
      refreshItems(sdPath);
    }
  }

  private void openSettingView() {
    Intent intent = new Intent(GoDMActivity.this, MyPreferencsActivity.class);
    startActivity(intent);
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
