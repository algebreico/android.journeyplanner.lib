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

import java.util.List;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.jp.R;

/**
 * @author raman
 *
 */
public class TTStopsAdapter extends TTAdapter {
	public static final int COL_PLACE_WIDTH = 115;

	public TTStopsAdapter(Context mContext, List<String> times) {
		super(mContext, times);
	}



	@Override
	protected void decorateTextView(TextView tv, final int pos) {
		tv.setMinimumHeight(rowHeight(mContext));
		tv.setWidth(getPixels(mContext, COL_PLACE_WIDTH));
		tv.setEllipsize(TruncateAt.MARQUEE);
		tv.setFocusable(true);
		tv.setFocusableInTouchMode(true);
		tv.setMarqueeRepeatLimit(1);
		tv.setHorizontallyScrolling(true);
		tv.setSingleLine(true);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setTextAppearance(mContext, R.style.place_tt_jp);
		tv.setBackgroundResource(R.drawable.cell_place);
		tv.setPadding(10, 0, 0, 0);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// this is needed to view all content of the cell
				Toast.makeText(mContext, getItem(pos).toString(), Toast.LENGTH_LONG).show();
			}
		});
	}
	
}
