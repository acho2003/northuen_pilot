import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/cash_report_model.dart';
import '../models/driver_model.dart';
import '../models/order_model.dart';
import '../models/product_model.dart';
import '../models/tracking_point_model.dart';
import '../models/user_model.dart';
import '../models/vendor_model.dart';
import '../services/api_client.dart';
import 'cart_state.dart';

class AppState extends ChangeNotifier {
  AppState(this.api);

  final ApiClient api;
  AppUser? user;
  bool loading = false;
  String? error;
  List<Vendor> vendors = [];
  List<Product> products = [];
  List<Order> orders = [];
  List<Order> assignedOrders = [];
  List<Order> availableDriverOrders = [];
  List<Driver> drivers = [];
  CashReport? cashReport;
  List<TrackingPoint> trackingPoints = [];

  bool get authenticated => user != null;

  Future<void> restore() async {
    final prefs = await SharedPreferences.getInstance();
    api.token = prefs.getString('token');
    if (api.token == null) return;
    await _run(() async {
      user = AppUser.fromJson(await api.get('/api/auth/me'));
    });
  }

  Future<void> login(String email, String password) async {
    await _auth('/api/auth/login', {'email': email, 'password': password});
  }

  Future<void> register(String fullName, String email, String phone, String password, String role) async {
    await _auth('/api/auth/register', {'fullName': fullName, 'email': email, 'phone': phone, 'password': password, 'role': role});
  }

  Future<void> _auth(String path, Map<String, dynamic> body) async {
    await _run(() async {
      final json = await api.post(path, body);
      api.token = json['token'];
      user = AppUser.fromJson(json['user']);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', api.token!);
    });
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
    api.token = null;
    user = null;
    orders = [];
    assignedOrders = [];
    notifyListeners();
  }

  Future<void> loadVendors({String? query, String? category}) async {
    final params = <String>[];
    if (query != null && query.trim().isNotEmpty) params.add('q=${Uri.encodeQueryComponent(query.trim())}');
    if (category != null && category != 'ALL') params.add('category=${Uri.encodeQueryComponent(category)}');
    final suffix = params.isEmpty ? '' : '?${params.join('&')}';
    await _run(() async => vendors = (await api.get('/api/vendors$suffix') as List).map((json) => Vendor.fromJson(json)).toList());
  }

  Future<void> loadProducts(String vendorId) async {
    await _run(() async => products = (await api.get('/api/vendors/$vendorId/products') as List).map((json) => Product.fromJson(json)).toList());
  }

  Future<void> addBackendCartItem(String productId, int quantity) async {
    await api.post('/api/cart/items', {'productId': productId, 'quantity': quantity});
  }

  Future<void> loadOrders() async {
    await _run(() async => orders = (await api.get('/api/orders/my') as List).map((json) => Order.fromJson(json)).toList());
  }

  Future<void> loadTracking(String orderId) async {
    await _run(() async => trackingPoints = (await api.get('/api/orders/$orderId/tracking') as List).map((json) => TrackingPoint.fromJson(json)).toList());
  }

  Future<void> reviewOrder(String orderId, int vendorRating, int driverRating, String comment) async {
    await _run(() async => api.post('/api/orders/$orderId/review', {'vendorRating': vendorRating, 'driverRating': driverRating, 'comment': comment}));
  }

  Future<Order> createShopOrder(CartState cart, String dropoffAddress, String notes) async {
    final json = await api.post('/api/orders', {
      'orderType': cart.vendor!.category == 'SHOP' ? 'SHOP' : 'FOOD',
      'vendorId': cart.vendor!.id,
      'items': cart.lines.map((line) => {'productId': line.product.id, 'quantity': line.quantity}).toList(),
      'pickupAddress': cart.vendor!.address,
      'dropoffAddress': dropoffAddress,
      'deliveryFee': cart.deliveryFee,
      'notes': notes,
    });
    final order = Order.fromJson(json);
    cart.clear();
    await loadOrders();
    return order;
  }

  Future<Order> createParcel(String pickup, String dropoff, String description, String notes) async {
    final json = await api.post('/api/orders', {
      'orderType': 'PARCEL',
      'pickupAddress': pickup,
      'dropoffAddress': dropoff,
      'parcelDescription': description,
      'deliveryFee': 80,
      'notes': notes,
    });
    final order = Order.fromJson(json);
    await loadOrders();
    return order;
  }

  Future<void> loadAssignedOrders() async {
    await _run(() async => assignedOrders = (await api.get('/api/drivers/assigned-orders') as List).map((json) => Order.fromJson(json)).toList());
  }

  Future<void> loadAvailableDriverOrders() async {
    await _run(() async => availableDriverOrders = (await api.get('/api/drivers/available-orders') as List).map((json) => Order.fromJson(json)).toList());
  }

  Future<void> loadDriverWork() async {
    await _run(() async {
      availableDriverOrders = (await api.get('/api/drivers/available-orders') as List).map((json) => Order.fromJson(json)).toList();
      assignedOrders = (await api.get('/api/drivers/assigned-orders') as List).map((json) => Order.fromJson(json)).toList();
    });
  }

  Future<void> acceptAvailableDelivery(String deliveryId) async {
    await _run(() async => api.patch('/api/drivers/available-orders/$deliveryId/accept', {}));
    await loadDriverWork();
  }

  Future<void> setDriverAvailable(bool value) async => _run(() async => api.patch('/api/drivers/availability', {'available': value}));

  Future<void> updateDelivery(String deliveryId, String status) async {
    await _run(() async => api.patch('/api/deliveries/$deliveryId/status', {'status': status}));
    await loadDriverWork();
  }

  Future<void> sendLocation(String deliveryId, double latitude, double longitude) async {
    await api.post('/api/deliveries/$deliveryId/location', {'latitude': latitude, 'longitude': longitude});
  }

  Future<void> completeDelivery(String deliveryId) async {
    await _run(() async => api.patch('/api/deliveries/$deliveryId/complete', {}));
    await loadDriverWork();
  }

  Future<void> loadAdmin() async {
    await _run(() async {
      orders = (await api.get('/api/admin/orders') as List).map((json) => Order.fromJson(json)).toList();
      drivers = (await api.get('/api/admin/drivers') as List).map((json) => Driver.fromJson(json)).toList();
      cashReport = CashReport.fromJson(await api.get('/api/admin/cash-report'));
    });
  }

  Future<void> assignDriver(String orderId, String driverId) async {
    await _run(() async => api.patch('/api/admin/orders/$orderId/assign-driver', {'driverId': driverId}));
    await loadAdmin();
  }

  Future<void> saveVendor(Map<String, dynamic> body) async => _run(() async => api.put('/api/vendor/profile', body));

  Future<void> saveProduct(Map<String, dynamic> body, {String? id}) async {
    await _run(() async {
      if (id == null) {
        await api.post('/api/vendor/products', body);
      } else {
        await api.put('/api/vendor/products/$id', body);
      }
    });
  }

  Future<void> loadVendorProducts() async {
    await _run(() async => products = (await api.get('/api/vendor/products') as List).map((json) => Product.fromJson(json)).toList());
  }

  Future<void> loadVendorOrders() async {
    await _run(() async => orders = (await api.get('/api/vendor/orders') as List).map((json) => Order.fromJson(json)).toList());
  }

  Future<void> updateOrderStatus(String orderId, String status) async {
    await _run(() async => api.patch('/api/orders/$orderId/status', {'status': status}));
  }

  Future<void> _run(Future<void> Function() task) async {
    loading = true;
    error = null;
    notifyListeners();
    try {
      await task();
    } catch (e) {
      error = e.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }
}
