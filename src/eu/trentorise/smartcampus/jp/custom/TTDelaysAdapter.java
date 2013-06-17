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

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.R;

/**
 * @author raman
 *
 */
public class TTDelaysAdapter extends BaseAdapter {

	protected Map<String, String>[] data = null;
	protected SherlockFragmentActivity mContext;

	public TTDelaysAdapter(SherlockFragmentActivity mContext, Map<String, String>[] delays) {
		super();
		this.mContext = mContext;
		this.data = delays;
	}

	@Override
	public int getCount() {
		return data.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		LinearLayout dll = null;
		if (convertView == null) {
			dll = new LinearLayout(mContext);
			dll.setMinimumWidth(TTAdapter.colWidth(mContext));
			dll.setMinimumHeight(TTAdapter.rowHeight(mContext));
			dll.setOrientation(LinearLayout.HORIZONTAL);
			dll.setBackgroundColor(mContext.getResources().getColor(R.color.sc_light_gray));
			dll.setBackgroundResource(R.drawable.cell_late);
			dll.setGravity(Gravity.CENTER);
		} else {
			dll = (LinearLayout) convertView;
			dll.removeAllViews();
		}

		Map<String, String> delaysStringsMap = data[pos];
		final Map<CreatorType, String> delaysCreatorTypesMap = new HashMap<CreatorType, String>();

		for (Entry<String, String> delay : delaysStringsMap.entrySet()) {
			if (!delay.getValue().equalsIgnoreCase("0")) {
				CreatorType ct = CreatorType.getAlertType(delay.getKey());
				delaysCreatorTypesMap.put(ct, delay.getValue());

				TextView tv = new TextView(mContext);
				tv.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, 1f));
				tv.setBackgroundColor(mContext.getResources().getColor(R.color.sc_light_gray));
				tv.setBackgroundResource(R.drawable.cell_late);
				tv.setGravity(Gravity.CENTER);
				tv.setTextAppearance(mContext, android.R.style.TextAppearance_Small);

				if (ct.equals(CreatorType.USER)) {
					tv.setTextColor(mContext.getResources().getColor(R.color.blue));
					tv.setText(mContext.getString(R.string.smart_check_tt_delay_user,
							delay.getValue()));
				} else {
					tv.setTextColor(mContext.getResources().getColor(R.color.red));
					tv.setText(mContext.getString(R.string.smart_check_tt_delay, delay.getValue()));
				}

				dll.addView(tv);
			}
		}

		if (!delaysCreatorTypesMap.isEmpty()) {
			dll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DelaysDialogFragment delaysDialog = new DelaysDialogFragment();
					Bundle args = new Bundle();
					args.putSerializable(DelaysDialogFragment.ARG_DELAYS, (Serializable) delaysCreatorTypesMap);
					delaysDialog.setArguments(args);
					delaysDialog.show(mContext.getSupportFragmentManager(), "delaysdialog");
				}
			});
		}
		return dll;
	}

	public void setData(Map<String, String>[] delays) {
		this.data = delays;
	}
}