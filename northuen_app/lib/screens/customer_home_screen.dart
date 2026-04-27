import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/cart_state.dart';
import 'cart_screen.dart';
import 'order_history_screen.dart';
import 'parcel_request_screen.dart';
import 'profile_screen.dart';
import 'vendor_list_screen.dart';

class CustomerHomeScreen extends StatefulWidget {
  const CustomerHomeScreen({super.key});

  @override
  State<CustomerHomeScreen> createState() => _CustomerHomeScreenState();
}

class _CustomerHomeScreenState extends State<CustomerHomeScreen> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final cartCount = context.watch<CartState>().lines.length;
    final pages = const [VendorListScreen(), ParcelRequestScreen(), OrderHistoryScreen(), ProfileScreen()];
    return Scaffold(
      appBar: AppBar(
        title: const Text('Northuen'),
        actions: [
          IconButton(
            tooltip: 'Cart',
            onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const CartScreen())),
            icon: Badge(label: Text('$cartCount'), isLabelVisible: cartCount > 0, child: const Icon(Icons.shopping_bag_rounded)),
          ),
        ],
      ),
      body: pages[_index],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (value) => setState(() => _index = value),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.storefront_rounded), label: 'Vendors'),
          NavigationDestination(icon: Icon(Icons.inventory_2_rounded), label: 'Parcel'),
          NavigationDestination(icon: Icon(Icons.receipt_long_rounded), label: 'Orders'),
          NavigationDestination(icon: Icon(Icons.person_rounded), label: 'Profile'),
        ],
      ),
    );
  }
}
