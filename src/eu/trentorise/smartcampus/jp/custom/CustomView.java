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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import eu.trentorise.smartcampus.jp.R;

/**
 * @author raman
 *
 */
public class CustomView extends View {

	private Paint mVLinePaint;
	private Paint mHLinePaint;

	private int numRows;
	private int numCols;
	private int rowHeight;
	private int colWidth;
	
	private List<String> texts;

	private Paint mTextPaint;
	
	/**
	 * @param context
	 */
	public CustomView(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CustomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);	
		canvas.drawLine(0, 0, numCols*colWidth+numCols, 0, mHLinePaint);
		for (int i = 0; i < numRows; i++) {
			int y = (rowHeight)*(i+1);
			canvas.drawLine(0, y, numCols*colWidth+numCols, y, mHLinePaint);
		}
		canvas.drawLine(0, 0, 0, numRows*rowHeight+numRows, mVLinePaint);
		for (int i = 0; i < numCols; i++) {
			int x = (colWidth+1)*(i+1);
			canvas.drawLine(x, 0, x, numRows*rowHeight+numRows, mVLinePaint);
		}
		
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				String text = texts.get(i*numCols+j);
				int xPos = (int)((colWidth+1)*j + colWidth/2 - mTextPaint.measureText(text)/2);
				int yPos = (int) (((rowHeight)*i+rowHeight / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2)) ; 
				
				canvas.drawText(text, xPos, yPos, mTextPaint);
			}
		}
	}

	private void init() {
		mVLinePaint = new Paint(0);
		mVLinePaint.setColor(getResources().getColor(android.R.color.black));
		mVLinePaint.setStrokeWidth(2);

		mHLinePaint = new Paint(0);
		mHLinePaint.setColor(getResources().getColor(R.color.sc_light_gray));
		mHLinePaint.setStrokeWidth(2);

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(18);
	}

	/**
	 * @return the numRows
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * @param numRows the numRows to set
	 */
	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	/**
	 * @return the numCols
	 */
	public int getNumCols() {
		return numCols;
	}

	/**
	 * @param numCols the numCols to set
	 */
	public void setNumCols(int numCols) {
		this.numCols = numCols;
	}

	/**
	 * @return the rowHeight
	 */
	public int getRowHeight() {
		return rowHeight;
	}

	/**
	 * @param rowHeight the rowHeight to set
	 */
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	/**
	 * @return the colWidth
	 */
	public int getColWidth() {
		return colWidth;
	}

	/**
	 * @param colWidth the colWidth to set
	 */
	public void setColWidth(int colWidth) {
		this.colWidth = colWidth;
	}

	/**
	 * @return the texts
	 */
	public List<String> getTexts() {
		return texts;
	}

	/**
	 * @param texts the texts to set
	 */
	public void setTexts(List<String> texts) {
		this.texts = texts;
	}
}
