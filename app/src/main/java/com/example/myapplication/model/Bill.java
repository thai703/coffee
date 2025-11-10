package com.example.myapplication.model;

import com.example.myapplication.Item.CartItem;

import java.util.Map;
import java.io.Serializable;
import java.util.List;
import java.io.Serializable;

public class Bill implements Serializable {
    private String userName;
    private String timestamp;
    private double total;
    private List<CartItem> items;
    private String billId;
    private String paymentMethod;

    private String userId;
    private String storeId;
    private String pickupMethod;  // "pickup" | "dine-in"
    private String tableNumber;   // optional
    private String paymentStatus; // "unpaid" | "paid"
    
    private String status;
    private java.util.Map<String, Long> statusHistory;

    private String pickupCode;    // "6 chữ số"
    private long createdAt;
    private long updatedAt;

    private String deviceToken;   // FCM token của KH
    private AssignedTo assignedTo; // barista đang pha (uid, name)

    // Tạo inner class đơn giản:
    public static class AssignedTo {
        public String uid;
        public String name;
        public AssignedTo() {}
        public AssignedTo(String uid, String name){ 
            this.uid = uid; this.name = name; 
        }
        
        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
    }

    // Constructor mặc định cho Firebase
    public Bill() {
    }

    // CẬP NHẬT CONSTRUCTOR ĐỂ BAO GỒM paymentMethod
    public Bill(String userName, String timestamp, double total, List<CartItem> items, String paymentMethod,String voucher) {
        this.userName = userName;
        this.timestamp = timestamp;
        this.total = total;
        this.items = items;
        this.paymentMethod = paymentMethod;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }


    // THÊM GETTER VÀ SETTER CHO paymentMethod
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // ===== Getter & Setter mới =====
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getPickupMethod() { return pickupMethod; }
    public void setPickupMethod(String pickupMethod) { this.pickupMethod = pickupMethod; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Long> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(Map<String, Long> statusHistory) { this.statusHistory = statusHistory; }

    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }

    public AssignedTo getAssignedTo() { return assignedTo; }
    public void setAssignedTo(AssignedTo assignedTo) { this.assignedTo = assignedTo; }

}