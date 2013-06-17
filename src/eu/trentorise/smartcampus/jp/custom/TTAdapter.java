/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author raman
 *
 */
public abstract class TTAdapter extends BaseAdapter {

	public static final int ROW_HEIGHT = 33;
	public static final int COL_WIDTH = 66;

	protected List<String> data = new ArrayList<String>();
	protected Context mContext;

	public TTAdapter(Context mContext, List<String> times) {
		super();
		this.mContext = mContext;
		this.data = times;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int pos) {
		return data.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		TextView tv = null;
		if (convertView == null) {
			tv = new TextView(mContext);
			tv.setHeight(rowHeight(mContext));
			decorateTextView(tv, pos);
		} else {
			tv = (TextView) convertView;
		}
		tv.setText(getText(pos));
		return tv;
	}

	protected abstract void decorateTextView(TextView tv, int pos);

	public static int rowHeight(Context ctx) {
		return getPixels(ctx, ROW_HEIGHT);
	}
	public static int colWidth(Context ctx) {
		return getPixels(ctx, COL_WIDTH);
	}
	
	public static int getPixels(Context ctx, int i) {
		float scale = ctx.getResources().getDisplayMetrics().density;
		return (int) (i * scale + 0.5f);
	}

	protected String getText(int pos) {
		return data.get(pos);
	}

}
