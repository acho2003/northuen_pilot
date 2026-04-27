import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/app_state.dart';
import '../state/cart_state.dart';
import 'order_tracking_screen.dart';

class CheckoutScreen extends StatefulWidget {
  const CheckoutScreen({super.key});

  @override
  State<CheckoutScreen> createState() => _CheckoutScreenState();
}

class _CheckoutScreenState extends State<CheckoutScreen> {
  final _address = TextEditingController(text: 'Changzamtog, Thimphu');
  final _notes = TextEditingController();
  bool _saving = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Checkout')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const ListTile(leading: Icon(Icons.payments_rounded), title: Text('Pay Cash on Delivery'), subtitle: Text('Customer pays after delivery. Driver collects cash.')),
          const SizedBox(height: 12),
          TextField(controller: _address, decoration: const InputDecoration(labelText: 'Delivery address')),
          const SizedBox(height: 12),
          TextField(controller: _notes, decoration: const InputDecoration(labelText: 'Notes'), minLines: 2, maxLines: 4),
          const SizedBox(height: 18),
          ElevatedButton.icon(onPressed: _saving ? null : _place, icon: const Icon(Icons.check_circle_rounded), label: const Text('Place COD Order')),
        ],
      ),
    );
  }

  Future<void> _place() async {
    setState(() => _saving = true);
    final order = await context.read<AppState>().createShopOrder(context.read<CartState>(), _address.text, _notes.text);
    setState(() => _saving = false);
    if (mounted) Navigator.of(context).pushAndRemoveUntil(MaterialPageRoute(builder: (_) => OrderTrackingScreen(order: order)), (route) => route.isFirst);
  }
}
