class OrderItem {
  OrderItem({
    required this.id,
    required this.productId,
    required this.productName,
    required this.unitPrice,
    required this.quantity,
    required this.lineTotal,
  });

  final String id;
  final String productId;
  final String productName;
  final num unitPrice;
  final int quantity;
  final num lineTotal;

  factory OrderItem.fromJson(Map<String, dynamic> json) => OrderItem(
        id: json['id'],
        productId: json['productId'],
        productName: json['productName'],
        unitPrice: json['unitPrice'],
        quantity: json['quantity'],
        lineTotal: json['lineTotal'],
      );
}

class Delivery {
  Delivery({required this.id, required this.orderId, this.driverId, required this.status, this.pickedUpAt, this.deliveredAt});

  final String id;
  final String orderId;
  final String? driverId;
  final String status;
  final DateTime? pickedUpAt;
  final DateTime? deliveredAt;

  factory Delivery.fromJson(Map<String, dynamic> json) => Delivery(
        id: json['id'],
        orderId: json['orderId'],
        driverId: json['driverId'],
        status: json['status'],
        pickedUpAt: json['pickedUpAt'] == null ? null : DateTime.parse(json['pickedUpAt']),
        deliveredAt: json['deliveredAt'] == null ? null : DateTime.parse(json['deliveredAt']),
      );
}

class Order {
  Order({
    required this.id,
    required this.orderType,
    required this.status,
    required this.paymentType,
    required this.paymentStatus,
    required this.subtotal,
    required this.deliveryFee,
    required this.totalAmount,
    required this.pickupAddress,
    required this.dropoffAddress,
    required this.items,
    required this.createdAt,
    this.vendorId,
    this.parcelDescription,
    this.notes,
    this.delivery,
  });

  final String id;
  final String? vendorId;
  final String orderType;
  final String status;
  final String paymentType;
  final String paymentStatus;
  final num subtotal;
  final num deliveryFee;
  final num totalAmount;
  final String pickupAddress;
  final String dropoffAddress;
  final String? parcelDescription;
  final String? notes;
  final List<OrderItem> items;
  final Delivery? delivery;
  final DateTime createdAt;

  factory Order.fromJson(Map<String, dynamic> json) => Order(
        id: json['id'],
        vendorId: json['vendorId'],
        orderType: json['orderType'],
        status: json['status'],
        paymentType: json['paymentType'],
        paymentStatus: json['paymentStatus'],
        subtotal: json['subtotal'],
        deliveryFee: json['deliveryFee'],
        totalAmount: json['totalAmount'],
        pickupAddress: json['pickupAddress'],
        dropoffAddress: json['dropoffAddress'],
        parcelDescription: json['parcelDescription'],
        notes: json['notes'],
        items: ((json['items'] ?? []) as List).map((item) => OrderItem.fromJson(item)).toList(),
        delivery: json['delivery'] == null ? null : Delivery.fromJson(json['delivery']),
        createdAt: DateTime.parse(json['createdAt']),
      );
}
