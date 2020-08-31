package cn.modificator.launcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AutoFitRecyclerView extends RecyclerView {
  private GridLayoutManager layoutManager;
  private int columnEstimatedWidth = -1;

  public AutoFitRecyclerView(Context context) {
    super(context);
    initAutoFit(context, null);
  }

  public AutoFitRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initAutoFit(context, attrs);
  }

  public AutoFitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initAutoFit(context, attrs);
  }

  private void initAutoFit(Context context, AttributeSet attrs) {
    if (attrs != null) {
      int[] attrsArray = {
              R.attr.columnEstimatedWidth
      };
      TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
      columnEstimatedWidth = array.getDimensionPixelSize(0, -1);
      array.recycle();
    }

    layoutManager = new GridLayoutManager(getContext(), 1);
    setLayoutManager(layoutManager);
  }

  @Override
  protected void onMeasure(int widthSpec, int heightSpec) {
    super.onMeasure(widthSpec, heightSpec);
    if (columnEstimatedWidth > 0) {
      int spanCount = Math.max(1, getMeasuredWidth() / columnEstimatedWidth);
      layoutManager.setSpanCount(spanCount);
    }
  }
}