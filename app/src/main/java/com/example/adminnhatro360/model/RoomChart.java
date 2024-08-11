package com.example.adminnhatro360.model;

import android.content.Context;
import android.widget.TextView;

import com.example.adminnhatro360.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class RoomChart extends MarkerView {

    private final TextView tvContent;

    public RoomChart(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Lấy thông tin từ PieEntry
        PieEntry pieEntry = (PieEntry) e;
        String label = pieEntry.getLabel();
        int roomCount = (int) pieEntry.getData(); // Lấy roomCount từ PieEntry
        float percentage = pieEntry.getValue();
        String content = String.format("%s\nSố lượng: %d\nTỷ lệ: %.1f%%", label, roomCount, percentage);

        tvContent.setText(content);
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // Đặt vị trí của MarkerView
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
