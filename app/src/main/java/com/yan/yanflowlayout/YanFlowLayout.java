package com.yan.yanflowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 作者: 024347
 * 时间: 2021/6/9
 * 版本:
 * 说明: 流式布局
 */
public class YanFlowLayout extends ViewGroup {
    private static final String TAG = YanFlowLayout.class.getSimpleName();
    // 列间距
    private float columnSpace;
    // 行间距
    private float rowSpace;

    public YanFlowLayout(Context context) {
        this(context, null);
    }

    public YanFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YanFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.YanFlowLayout);
            // 获取列间距
            columnSpace = typedArray.getDimension(R.styleable.YanFlowLayout_yan_flow_column_space, 0);
            // 获取行间距
            rowSpace = typedArray.getDimension(R.styleable.YanFlowLayout_yan_flow_row_space, 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (typedArray == null) {
                // 这里记得释放
                typedArray.recycle();
            }
        }
        init();
    }

    private void init() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 测量子View，注意这里没用measureChildWithMargins，所以不能使用margin
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        // 宽的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        // 建议的宽度
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // 高的模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 建议的高度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "widthMode==>>" + widthMode);
        Log.d(TAG, "widthSize==>>" + widthSize);
        Log.d(TAG, "heightMode==>>" + heightMode);
        Log.d(TAG, "heightSize==>>" + heightSize);

        float width, height;
        // 注意的是当View宽度设置具体值或者match_parent或者wrap_content时都当match_parent处理
        width = widthSize;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            // 子View个数
            int childCount = getChildCount();
            if (childCount == 0) {
                height = 0;
            } else {
                // 最大剩余宽度
                float maxSurplusWith = width - getPaddingLeft() - getPaddingRight();
                // 剩余可以放子View的空间
                float surplusWith = maxSurplusWith;
                // 当前行数
                int row = 1;
                // 便利子View
                for (int i = 0; i < childCount; i++) {
                    // 获取子View
                    View child = getChildAt(i);
                    // 获取子View宽度
                    int measuredWidth = child.getMeasuredWidth();
                    // 子View的宽度小于等于剩余宽度，说明不用换行
                    if (measuredWidth <= surplusWith) {
                        // 剩余的宽度减去子View宽度
                        surplusWith -= measuredWidth;
                        // 该行已有View，不是新开的行
                    } else {
                        // 正常情况下的一行放不下（左边有子View）
                        row++;
                        surplusWith = maxSurplusWith - measuredWidth;
                    }
                    // 减去行间距
                    surplusWith -= columnSpace;
                    Log.d(TAG, "子View行数==>>" + i + "==" + row);
                }

                Log.d(TAG, "子View行数==>>" + row);
                // 这里取的第一个子View的高度，本控件默认所有子View高度一致
                int childHeight = getChildAt(0).getMeasuredHeight();
                // 高度等于子View行数乘以子View高度加上所有行间距再加上上下padding值
                height = childHeight * row + (row - 1) * rowSpace + getPaddingTop() + getPaddingBottom();
            }
        }
        // 这是控件宽高
        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        // 当前行数
        int row = 1;
        // 已使用行宽
        float usedWidth = 0;
        // 遍历子View
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            // 子View宽度
            int measuredWidth = child.getMeasuredWidth();
            // 子View高度
            int measuredHeight = child.getMeasuredHeight();
            // 剩余可用行宽度
            float surplusWith = getWidth() - usedWidth - getPaddingLeft() - getPaddingRight();
            if (surplusWith >= measuredWidth) { // 当前剩余可用宽度够放当前子View
                // 左等于已使用宽度加上左padding值
                left = (int) (usedWidth + getPaddingLeft());
            } else {
                // 行数加一
                row++;
                // 重开的一行，所以左等于左padding值
                left = getPaddingLeft();
                // 已使用重置为0（这里的已使用不包含当前子View）
                usedWidth = 0;
            }
            // 右等于左加上子View宽度
            right = left + measuredWidth;
            // 上等于子View行宽和加上行间距和加上上padding值
            top = (int) ((row - 1) * measuredHeight + (row - 1) * rowSpace + getPaddingTop());
            // 下等于上加上View高度
            bottom = top + measuredHeight;
            // 计算已使用宽度
            usedWidth = usedWidth + measuredWidth + columnSpace;
            Log.d(TAG, "left=" + left + "  top=" + top + "  right=" + right + "  bottom=" + bottom);
            // 设置子View位置
            child.layout(left, top, right, bottom);
        }
    }
}
