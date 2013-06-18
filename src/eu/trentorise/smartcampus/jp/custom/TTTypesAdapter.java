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
import android.view.Gravity;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.R;

/**
 * @author raman
 *
 */
public class TTTypesAdapter extends TTAdapter {
	public static final int COL_PLACE_WIDTH = 115;

	public TTTypesAdapter(Context mContext, List<String> times) {
		super(mContext, times);
	}



	@Override
	protected void decorateTextView(TextView tv, final int pos) {
		tv.setMinimumWidth(COL_WIDTH);
		tv.setMinimumHeight(ROW_HEIGHT);
		tv.setBackgroundColor(mContext.getResources().getColor(R.color.sc_light_gray));
		tv.setBackgroundResource(R.drawable.cell_late);
		tv.setGravity(Gravity.CENTER);
	}



	@Override
	protected String getText(int pos) {
		if (data.get(pos).toLowerCase().startsWith("r"))
			return "R";
		if (data.get(pos).toLowerCase().startsWith("e"))
			 return  "E";
		if (data.get(pos).toLowerCase().startsWith("i"))
			return "IC";
		return super.getText(pos);
	}
	
	
}
