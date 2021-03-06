package eu.trentorise.smartcampus.jp.custom;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class EndlessLinkedScrollView extends LinkedScrollView {

	public interface TimetableNavigation {
		public void onLeftOverScrolled();

		public void onRightOverScrolled();
	}

	private TimetableNavigation mOnEndListener;

	public int tollerance = 10;

	public EndlessLinkedScrollView(Context context, TimetableNavigation endListener) {
		super(context);
		this.mOnEndListener = endListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		ViewGroup table = ((ViewGroup) (this.getChildAt(0)));
//		if (table != null) {
//			ViewGroup row = ((ViewGroup) (table.getChildAt(0)));
//			if (row != null && row.getChildCount() > 0) {
//				int endLeft = this.getWidth() - row.getChildAt(row.getChildCount() - 1).getWidth();
//				Rect end = new Rect(endLeft - tollerance, 0, this.getWidth() + tollerance, this.getHeight());
//				Rect touch = new Rect((int) (event.getX() - tollerance), 0, (int) (event.getX() + tollerance), this.getHeight());
//				if (Rect.intersects(touch, end))
//					mOnEndListener.onRightOverScrolled();
//				int endRight = row.getChildAt(0).getWidth();
//				Rect start = new Rect(0, 0, endRight, this.getRight());
//				if (Rect.intersects(touch, start)) {
//					mOnEndListener.onLeftOverScrolled();
//				}
//			}
//		}
		return super.onTouchEvent(event);
	}

}