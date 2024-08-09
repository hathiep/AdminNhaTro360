package com.example.adminnhatro360.mainActivity.manageRoomFragment.detailListFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adminnhatro360.R;
import com.example.adminnhatro360.mainActivity.manageRoomFragment.OnRoomClickListener;
import com.example.adminnhatro360.model.Room;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.util.concurrent.TimeUnit;

public class RoomAdapterSingle extends RecyclerView.Adapter<RoomAdapterSingle.RoomViewHolder> {

    private List<Room> roomList;
    private List<Boolean> selectedList;
    private OnRoomClickListener onRoomClickListener;
    private boolean roomStatus;
    private Context context;

    public RoomAdapterSingle(List<Room> roomList, OnRoomClickListener onRoomClickListener, boolean roomStatus, Context context) {
        this.roomList = roomList;
        this.selectedList = new ArrayList<>(Collections.nCopies(roomList.size(), false));
        this.onRoomClickListener = onRoomClickListener;
        this.roomStatus = roomStatus;
        this.context = context;
    }

    public void updateSelectedList() {
        selectedList = new ArrayList<>(Collections.nCopies(roomList.size(), false));
    }

    public void selectAllCheckboxes(boolean selectAll) {
        for (int i = 0; i < selectedList.size(); i++) {
            selectedList.set(i, selectAll);
        }
        notifyDataSetChanged();
    }


    public void setSelected(int position, boolean selected) {
        if (selectedList.size() > 0 && position >= 0 && position < selectedList.size()) {
            selectedList.set(position, selected);
            notifyItemChanged(position);
        }
    }

    public List<Integer> getSelectedPositions() {
        List<Integer> selectedPositions = new ArrayList<>();
        for (int i = 0; i < selectedList.size(); i++) {
            if (selectedList.get(i)) {
                selectedPositions.add(i);
            }
        }
        return selectedPositions;
    }

    public boolean isSelected(int position) {
        return position >= 0 && position < selectedList.size() && selectedList.get(position);
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_single, parent, false);
        return new RoomViewHolder(itemView, onRoomClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        if (position >= 0 && position < roomList.size()) {
            Room room = roomList.get(position);
            boolean isSelected = (selectedList != null && position < selectedList.size()) ? selectedList.get(position) : false;
            holder.bind(room, isSelected);
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CheckBox checkBox;
        private ImageView imvImage, imvDelete;
        private TextView tvTitle, tvPrice, tvAddress, tvArea, tvTimePosted;
        private OnRoomClickListener onRoomClickListener;

        public RoomViewHolder(@NonNull View itemView, OnRoomClickListener onRoomClickListener) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            imvImage = itemView.findViewById(R.id.item_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvTimePosted = itemView.findViewById(R.id.tv_time_posted);
            imvDelete = itemView.findViewById(R.id.imv_delete);

            this.onRoomClickListener = onRoomClickListener;
            itemView.setOnClickListener(this);

            if (!roomStatus) {
                imvDelete.setVisibility(View.GONE);
            } else {
                imvDelete.setVisibility(View.VISIBLE);
                imvDelete.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Room room = roomList.get(position);
                        showDeleteConfirmationDialog(room);
                    }
                });
            }
        }

        public void bind(Room room, Boolean isSelected) {
            checkBox.setChecked(isSelected);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isSelected(getAdapterPosition()) != isChecked) {
                    setSelected(getAdapterPosition(), isChecked);
                }
            });

            if (room.getImages() != null && !room.getImages().isEmpty()) {
                ViewTreeObserver viewTreeObserver = imvImage.getViewTreeObserver();

                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        imvImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        int width = imvImage.getWidth();
                        int height = (width * 4) / 3;
                        imvImage.getLayoutParams().height = height;
                        imvImage.requestLayout();
                    }
                });
                Glide.with(imvImage.getContext()).load(room.getImages().get(room.getAvatar())).into(imvImage);
            }
            tvTitle.setText(room.getTitle());
            tvPrice.setText(formatPrice(room.getPrice()));
            tvAddress.setText(room.getAddress());
            tvArea.setText("DT " + room.getArea() + " m2");

            Timestamp timePosted = room.getTimePosted();
            long timeDiff = Timestamp.now().getSeconds() - timePosted.getSeconds();

            if (timeDiff < TimeUnit.HOURS.toSeconds(1)) {
                long minutes = TimeUnit.SECONDS.toMinutes(timeDiff);
                tvTimePosted.setText(minutes + " phút");
            } else if (timeDiff < TimeUnit.DAYS.toSeconds(1)) {
                long hours = TimeUnit.SECONDS.toHours(timeDiff);
                tvTimePosted.setText(hours + " giờ");
            } else {
                long days = TimeUnit.SECONDS.toDays(timeDiff);
                tvTimePosted.setText(days + " ngày");
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Room room = roomList.get(position);
                onRoomClickListener.onRoomClick(room);
            }
        }

        private void showDeleteConfirmationDialog(Room room) {
            new AlertDialog.Builder(context).setTitle("Xác nhận xóa").setMessage("Bạn có chắc chắn muốn xóa phòng này?").setPositiveButton("Yes", (dialog, which) -> {
                showDeletingDialog(room);
            }).setNegativeButton("No", null).show();
        }

        private void showDeletingDialog(Room room) {
            AlertDialog deletingDialog = new AlertDialog.Builder(context).setTitle("Đang xóa...").setView(new ProgressBar(context)).setCancelable(false).show();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            List<String> imagePaths = room.getImages();

            // Function to delete images from Storage
            deleteImagesFromStorage(imagePaths, room, db, storage, deletingDialog);
        }

        private void deleteImagesFromStorage(List<String> imagePaths, Room room, FirebaseFirestore db, FirebaseStorage storage, AlertDialog deletingDialog) {
            if (imagePaths == null || imagePaths.isEmpty()) {
                // If no images to delete, proceed with deleting room
                deleteRoomFromFirestore(room, db, deletingDialog);
                return;
            }

            for (String imagePath : imagePaths) {
                StorageReference imageRef = storage.getReferenceFromUrl(imagePath);
                imageRef.delete().addOnSuccessListener(aVoid -> {
                    Log.d("RoomAdapterSingle", "Image successfully deleted!");
                    // Check if all images are deleted
                    if (imagePaths.indexOf(imagePath) == imagePaths.size() - 1) {
                        // All images deleted, proceed with deleting room
                        deleteRoomFromFirestore(room, db, deletingDialog);
                    }
                }).addOnFailureListener(exception -> {
                    Log.w("RoomAdapterSingle", "Error deleting image", exception);
                    deletingDialog.dismiss();
                });
            }
        }

        private void deleteRoomFromFirestore(Room room, FirebaseFirestore db, AlertDialog deletingDialog) {
            db.collection("rooms").document(room.getId()).delete().addOnSuccessListener(aVoid -> {
                db.collection("users").whereEqualTo("email", FirebaseAuth.getInstance().getCurrentUser().getEmail()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                        db.collection("users").document(userDoc.getId()).update("listPostedRoom", FieldValue.arrayRemove(room.getId())).addOnSuccessListener(aVoid2 -> {
                            roomList.remove(room);
                            notifyItemRemoved(getAdapterPosition());
                            notifyItemRangeChanged(getAdapterPosition(), roomList.size());
                            Log.d("RoomAdapterSingle", "Room successfully deleted!");
                            deletingDialog.dismiss();
                            Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.w("RoomAdapterSingle", "Error deleting room ID from user", e);
                            deletingDialog.dismiss();
                        });
                    } else {
                        deletingDialog.dismiss();
                    }
                }).addOnFailureListener(e -> {
                    deletingDialog.dismiss();
                    Log.w("RoomAdapterSingle", "Error finding user", e);
                });

            }).addOnFailureListener(e -> {
                Log.w("RoomAdapterSingle", "Error deleting room", e);
                deletingDialog.dismiss();
            });
        }


    }

    private String formatPrice(String price) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        double millions = Integer.parseInt(price) / 1_000_000.0;
        return decimalFormat.format(millions) + " triệu";
    }
}
