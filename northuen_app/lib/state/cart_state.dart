import 'package:flutter/foundation.dart';

import '../models/product_model.dart';
import '../models/vendor_model.dart';

class CartLine {
  CartLine({required this.product, required this.quantity});
  final Product product;
  int quantity;
  num get lineTotal => product.price * quantity;
}

class CartState extends ChangeNotifier {
  Vendor? vendor;
  final Map<String, CartLine> _lines = {};

  List<CartLine> get lines => _lines.values.toList();
  num get subtotal => lines.fold<num>(0, (sum, line) => sum + line.lineTotal);
  num get deliveryFee => lines.isEmpty ? 0 : 80;
  num get total => subtotal + deliveryFee;

  void add(Vendor selectedVendor, Product product) {
    if (vendor?.id != selectedVendor.id) {
      vendor = selectedVendor;
      _lines.clear();
    }
    _lines.update(product.id, (line) {
      line.quantity += 1;
      return line;
    }, ifAbsent: () => CartLine(product: product, quantity: 1));
    notifyListeners();
  }

  void remove(String productId) {
    _lines.remove(productId);
    if (_lines.isEmpty) vendor = null;
    notifyListeners();
  }

  void clear() {
    vendor = null;
    _lines.clear();
    notifyListeners();
  }
}
