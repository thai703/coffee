package com.example.myapplication.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.UI.product.OrderDetailActivity;   // KEEP
import com.example.myapplication.model.Bill;                      // KEEP
import com.example.myapplication.UI.profile.OrderHistoryActivity; // KEEP

import java.text.DecimalFormat; // NEW
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Bill> orderList; // KEEP
    private OrderHistoryActivity context; // KEEP

    public OrderAdapter(List<Bill> orderList, OrderHistoryActivity context) {
        this.orderList = orderList;  // KEEP
        this.context = context;      // KEEP
    }

    public void setOrders(List<Bill> orderList) { // KEEP
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // KEEP
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) { // CHANGED
        Bill bill = orderList.get(position);

        // CHANGED: format tiền #,### VNĐ
        DecimalFormat df = new DecimalFormat("#,###"); // NEW

        // KEEP/CHANGED: các dòng cũ
        holder.textOrderId.setText("Mã đơn: " + bill.getBillId()); // KEEP
        String time = bill.getTimestamp() != null ? bill.getTimestamp() : "-"; // NEW
        holder.textOrderDate.setText("Thời gian: " + time); // CHANGED
        holder.textOrderTotal.setText("Tổng: " + df.format((long) bill.getTotal()) + " VNĐ"); // CHANGED

        // NEW: hiển thị trạng thái + pickup code (nếu có)
        if (holder.textOrderStatus != null) {
            holder.textOrderStatus.setText(mapStatus(bill.getStatus())); // NEW
        }
        if (holder.textPickupCode != null) {
            String code = bill.getPickupCode();
            holder.textPickupCode.setText(code != null ? ("Mã: " + code) : "Mã: ------"); // NEW
        }

        // KEEP: mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderId", bill.getBillId()); // KEEP
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { // KEEP
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        // KEEP: view cũ
        TextView textOrderId, textOrderDate, textOrderTotal;
        // NEW: view mới
        TextView textOrderStatus; // NEW
        TextView textPickupCode;  // NEW

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // KEEP: bind cũ
            textOrderId = itemView.findViewById(R.id.text_order_id);
            textOrderDate = itemView.findViewById(R.id.text_order_date);
            textOrderTotal = itemView.findViewById(R.id.text_order_total);
            // NEW: bind mới (sẽ thêm trong XML)
            textOrderStatus = itemView.findViewById(R.id.text_order_status); // NEW
            textPickupCode  = itemView.findViewById(R.id.text_pickup_code);  // NEW
        }
    }

    // NEW: map trạng thái sang tiếng Việt gọn
    private String mapStatus(String s) {
        if (s == null) return "Đang chờ";
        switch (s) {
            case "pending":   return "Đang chờ";
            case "accepted":  return "Đã nhận";
            case "making":    return "Đang pha";
            case "ready":     return "Đã pha xong";
            case "completed": return "Đã giao";
            case "canceled":  return "Đã hủy";
            default:          return s;
        }
    }
}
