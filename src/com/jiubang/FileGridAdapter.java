package com.jiubang;

import android.widget.TextView;

public class FileGridAdapter extends FileListAdapter
			implements FileAdapter{

	public FileGridAdapter(GoDMActivity context, FileData infos, int style) {
		super(context, infos, style);
	}
	protected void setViewExceptIcon(Viewholder holder, FileInfo fInfo) {
		TextView tv = holder.getName();
		if (tv == null) return;
		tv.setText(fInfo.name);
	}
	@Override
	public int getIconId() {
		return R.id.gridimg;
	}
	@Override
	public int getFileNameTextId() {
		return R.id.gridname;
	}
	@Override
	public int getLayoutId() {
		return R.layout.file_griditem;
	}
	protected final int getStartSelfUpdateCount() { return 19;}
}
