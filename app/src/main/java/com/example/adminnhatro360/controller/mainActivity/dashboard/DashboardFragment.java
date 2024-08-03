package com.example.adminnhatro360.controller.mainActivity.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.adminnhatro360.R;
import com.example.adminnhatro360.model.Room;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";
    private FirebaseFirestore db;
    private BarChart barChartRoomTypes;
    private LineChart lineChartPrice;
    private LineChart lineChartArea;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        init(view);
        db = FirebaseFirestore.getInstance();
        fetchRoomsFromFirestore();
        return view;
    }

    private void init(View view) {
        barChartRoomTypes = view.findViewById(R.id.bar_chart_room_types);
        lineChartPrice = view.findViewById(R.id.line_chart_price);
        lineChartArea = view.findViewById(R.id.line_chart_area);
    }

    private void fetchRoomsFromFirestore() {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> roomList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = document.toObject(Room.class);
                            room.setId(document.getId());
                            if (room.getStatus() == 1) roomList.add(room);
                        }

                        sortRoomsByTimePosted(roomList);

                        // Populate the chart data
                        populateChartData(roomList);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void sortRoomsByTimePosted(List<Room> rooms) {
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room room1, Room room2) {
                if (room1.getTimePosted() == null || room2.getTimePosted() == null) {
                    return 0;
                }
                return room2.getTimePosted().compareTo(room1.getTimePosted());
            }
        });
    }

    private void populateChartData(List<Room> roomList) {
        // Khởi tạo map để đếm số lượng phòng theo loại
        Map<Integer, Integer> roomTypeCount = new HashMap<>();
        roomTypeCount.put(1, 0);
        roomTypeCount.put(2, 0);
        roomTypeCount.put(3, 0);
        roomTypeCount.put(4, 0);

        Map<String, Integer> priceCount = new HashMap<>();
        Map<String, Integer> areaCount = new HashMap<>();

        for (Room room : roomList) {
            // Đếm số lượng phòng theo loại
            int type = room.getRoomType();
            roomTypeCount.put(type, roomTypeCount.getOrDefault(type, 0) + 1);

            // Đếm số lượng phòng theo giá
            String priceRange = getPriceRange(Integer.parseInt(room.getPrice()));
            priceCount.put(priceRange, priceCount.getOrDefault(priceRange, 0) + 1);

            // Đếm số lượng phòng theo diện tích
            String areaRange = getAreaRange(Integer.parseInt(room.getArea()));
            areaCount.put(areaRange, areaCount.getOrDefault(areaRange, 0) + 1);
        }

        // Tạo dữ liệu cho biểu đồ cột (loại phòng)
        ArrayList<BarEntry> typeEntries = new ArrayList<>();
        typeEntries.add(new BarEntry(0, roomTypeCount.get(1)));
        typeEntries.add(new BarEntry(1, roomTypeCount.get(2)));
        typeEntries.add(new BarEntry(2, roomTypeCount.get(3)));
        typeEntries.add(new BarEntry(3, roomTypeCount.get(4)));

        BarDataSet typeDataSet = new BarDataSet(typeEntries, "Loại phòng");
        typeDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData barData = new BarData(typeDataSet);
        barChartRoomTypes.setData(barData);

        // Cấu hình trục x
        barChartRoomTypes.getXAxis().setValueFormatter(new XAxisValueFormatter());
        barChartRoomTypes.getXAxis().setGranularity(1); // Đảm bảo các cột không bị chồng lấp
        barChartRoomTypes.getXAxis().setGranularityEnabled(true);

        // Đặt các nhãn của trục X ở dưới cùng
        barChartRoomTypes.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // Cấu hình trục Y
        barChartRoomTypes.getAxisLeft().setGranularity(1);
        barChartRoomTypes.getAxisLeft().setAxisMinimum(0); // Đảm bảo trục Y bắt đầu từ 0
        barChartRoomTypes.getAxisRight().setEnabled(false); // Tắt trục Y bên phải

        barChartRoomTypes.getDescription().setEnabled(false);
        barChartRoomTypes.getLegend().setEnabled(true);
        barChartRoomTypes.invalidate(); // Refresh chart

        // Tạo dữ liệu cho biểu đồ đường (giá)
        ArrayList<Entry> priceEntries = new ArrayList<>();

        // Thêm dữ liệu vào các khoảng giá
        for (int i = 0; i <= 20; i++) {
            String range = getPriceRange(i * 1000000);
            int count = priceCount.getOrDefault(range, 0);
            priceEntries.add(new Entry(i, count));
        }

        LineDataSet priceDataSet = new LineDataSet(priceEntries, "Số phòng theo giá (triệu đồng)");
        priceDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        priceDataSet.setCircleColor(ColorTemplate.COLORFUL_COLORS[0]);
        priceDataSet.setLineWidth(2f);

        LineData lineDataPrice = new LineData(priceDataSet);
        lineChartPrice.setData(lineDataPrice);

        lineChartPrice.getXAxis().setValueFormatter(new PriceValueFormatter());
        lineChartPrice.getXAxis().setGranularity(1); // Chỉ hiển thị các khoảng giá cách nhau 1,000,000
        lineChartPrice.getXAxis().setLabelCount(10); // Chỉ hiển thị 5 nhãn trên trục X
        lineChartPrice.getXAxis().setDrawLabels(true);
        lineChartPrice.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChartPrice.getAxisLeft().setGranularity(1);
        lineChartPrice.getAxisLeft().setAxisMinimum(0);
        lineChartPrice.getAxisRight().setEnabled(false);

        lineChartPrice.getDescription().setEnabled(false);
        lineChartPrice.getLegend().setEnabled(true);
        lineChartPrice.invalidate(); // Refresh chart

        // Tạo dữ liệu cho biểu đồ đường (diện tích)
        ArrayList<Entry> areaEntries = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            String range = getAreaRange(i * 10);
            int count = areaCount.getOrDefault(range, 0);
            areaEntries.add(new Entry(i, count));
        }

        LineDataSet areaDataSet = new LineDataSet(areaEntries, "Số phòng theo diện tích (m²)");
        areaDataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        areaDataSet.setCircleColor(ColorTemplate.COLORFUL_COLORS[1]);
        areaDataSet.setLineWidth(2f);

        LineData lineDataArea = new LineData(areaDataSet);
        lineChartArea.setData(lineDataArea);

        lineChartArea.getXAxis().setValueFormatter(new AreaValueFormatter());
        lineChartArea.getXAxis().setGranularity(1);
        lineChartArea.getXAxis().setLabelCount(10); // Chỉ hiển thị 5 nhãn trên trục X
        lineChartArea.getXAxis().setDrawLabels(true);
        lineChartArea.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChartArea.getAxisLeft().setGranularity(1);
        lineChartArea.getAxisLeft().setAxisMinimum(0);
        lineChartArea.getAxisRight().setEnabled(false);

        lineChartArea.getDescription().setEnabled(false);
        lineChartArea.getLegend().setEnabled(true);
        lineChartArea.invalidate(); // Refresh chart
    }

    private String getPriceRange(int price) {
        int lowerBound = (price / 1000000) * 1000000;
        int upperBound = lowerBound + 1000000;
        return lowerBound + " - " + upperBound;
    }

    private String getAreaRange(int area) {
        int lowerBound = (area / 10) * 10;
        int upperBound = lowerBound + 10;
        return lowerBound + " - " + upperBound;
    }

    // Custom formatter cho trục X của biểu đồ cột (loại phòng)
    public class XAxisValueFormatter extends ValueFormatter {
        private final String[] labels = {"Phòng trọ", "Căn hộ", "Nhà nguyên căn", "Chung cư mini"};

        @Override
        public String getFormattedValue(float value) {
            return labels[(int) value];
        }
    }

    // Custom formatter cho trục X của biểu đồ đường (giá)
    public class PriceValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format("%d", (int) (value));
        }
    }

    // Custom formatter cho trục X của biểu đồ đường (diện tích)
    public class AreaValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format("%d", (int) (value * 10));
        }
    }
}
