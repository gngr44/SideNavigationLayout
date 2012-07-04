/***
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.ceenic.sidenavigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * A custom FrameLayout that copies the side navigation UI design pattern found
 * in the YouTube app. It expects two child views to be added to it. The first
 * one will be the navigation view and the second one will be the content view.
 */
public class SideNavigationLayout extends FrameLayout {

    public interface SideNavigationListener {
        public void onShowNavigationView(SideNavigationLayout view);

        public void onShowContentView(SideNavigationLayout view);
    }

    private static final int SCROLL_DURATION = 500;

    private Scroller mScroller;

    private VelocityTracker mTracker;

    private SideNavigationListener mListener;

    private float mCurrX;

    private float mCurrY;

    private int mOffsetX;

    private int mTouchSlop;

    private int mMinimumVelocity;

    private int mMaximumVelocity;

    private boolean mScrolledHorizontally;

    private boolean mScrolledVertically;

    private boolean mShowingNavigation;

    public SideNavigationLayout(Context context) {
        super(context);
        init();
    }

    public SideNavigationLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SideNavigationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        ViewConfiguration viewConfiguration = ViewConfiguration
                .get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
    }

    public void setNavigationListener(SideNavigationListener listener) {
        mListener = listener;
    }

    public void showNavigationView() {
        mScroller.startScroll((int) mOffsetX, 0,
                (int) (getNavigationViewWidth() - mOffsetX), SCROLL_DURATION);
        requestLayout();
        invalidate();
    }

    public void showContentView() {
        mScroller.startScroll((int) mOffsetX, 0, (int) -mOffsetX, 0,
                SCROLL_DURATION);
        requestLayout();
        invalidate();
    }

    public boolean isShowingNavigationView() {
        return mShowingNavigation;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.isFinished()) {
            return;
        }

        mScroller.computeScrollOffset();
        mOffsetX = mScroller.getCurrX();
        requestLayout();
        invalidate();

        if (mScroller.isFinished()) {
            boolean wasShowingNavigation = mShowingNavigation;
            mShowingNavigation = mOffsetX != 0;
            getChildAt(0).setVisibility(
                    mShowingNavigation ? View.VISIBLE : View.GONE);
            if (wasShowingNavigation != mShowingNavigation) {
                if (mListener != null) {
                    if (mShowingNavigation) {
                        mListener.onShowNavigationView(this);
                    } else {
                        mListener.onShowContentView(this);
                    }
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean shouldIntercept = false;
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mScroller.forceFinished(true);
            mTracker = VelocityTracker.obtain();
            mTracker.addMovement(event);
            mCurrX = event.getX();
            mCurrY = event.getY();
            mScrolledHorizontally = false;
            mScrolledVertically = false;
            if (mShowingNavigation && event.getX() > getNavigationViewWidth()) {
                shouldIntercept = true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mScrolledVertically) {
                // Don't intercept if the user has already scrolled vertically.
            } else if (Math.abs(mCurrY - event.getY()) > mTouchSlop) {
                mScrolledVertically = true;
            } else if (Math.abs(mCurrX - event.getX()) > mTouchSlop) {
                mScrolledHorizontally = true;
                shouldIntercept = true;
                getChildAt(0).setVisibility(View.VISIBLE);
            }
        } else if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            mTracker.recycle();
            mTracker = null;
            scrollToNavigationOrContentView();
        }
        return shouldIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            mScrolledHorizontally = true;
            mTracker.addMovement(event);

            final float x = event.getX();
            final float diffX = x - mCurrX;
            final int oldOffset = mOffsetX;

            mOffsetX += diffX;
            mOffsetX = Math.max(0, mOffsetX);
            mOffsetX = Math.min(getNavigationViewWidth(), mOffsetX);
            mCurrX = x;
            if (oldOffset != mOffsetX) {
                requestLayout();
            }
            return true;
        } else if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            mTracker.addMovement(event);
            // We intercepted the event because the user either
            // 1) scrolled horizontally OR
            // 2) tapped on the content view while the navigation view is
            // showing
            if (mScrolledHorizontally) {
                // If the user flinged, we should scroll in the direction of the
                // fling. Otherwise, we scroll whichever one we're closer to.
                mTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float velocity = mTracker.getXVelocity();
                if (Math.abs(velocity) > mMinimumVelocity) {
                    if (velocity > 0) {
                        showNavigationView();
                    } else {
                        showContentView();
                    }
                } else {
                    scrollToNavigationOrContentView();
                }
            } else {
                // We want to show the content view if the user tapped on it
                // while the navigation is showing.
                showContentView();
            }
            mTracker.recycle();
            mTracker = null;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Scroll to the navigation of content view depending on whichever one the
     * offset is closer to.
     */
    private void scrollToNavigationOrContentView() {
        if (mOffsetX < (getNavigationViewWidth() / 2)) {
            showContentView();
        } else {
            showNavigationView();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (getChildCount() > 1) {
            View contentView = getChildAt(1);
            int childLeft = contentView.getLeft() + mOffsetX;
            int childTop = contentView.getTop();
            int childRight = contentView.getRight() + mOffsetX;
            int childBottom = contentView.getBottom();
            contentView.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    private int getNavigationViewWidth() {
        if (getChildCount() > 0) {
            return getChildAt(0).getWidth();
        }
        return 0;
    }
}
