import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/app_state.dart';
import 'order_tracking_screen.dart';

class ParcelRequestScreen extends StatefulWidget {
  const ParcelRequestScreen({super.key});

  @override
  State<ParcelRequestScreen> createState() => _ParcelRequestScreenState();
}

class _ParcelRequestScreenState extends State<ParcelRequestScreen> {
  final _pickup = TextEditingController(text: 'Clock Tower Square, Thimphu');
  final _dropoff = TextEditingController(text: 'Motithang, Thimphu');
  final _description = TextEditingController(text: 'Small parcel');
  final _notes = TextEditingController();
  bool _saving = false;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('Parcel delivery', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900)),
        const SizedBox(height: 14),
        TextField(controller: _pickup, decoration: const InputDecoration(labelText: 'Pickup address')),
        const SizedBox(height: 12),
        TextField(controller: _dropoff, decoration: const InputDecoration(labelText: 'Dropoff address')),
        const SizedBox(height: 12),
        TextField(controller: _description, decoration: const InputDecoration(labelText: 'Parcel description')),
        const SizedBox(height: 12),
        TextField(controller: _notes, decoration: const InputDecoration(labelText: 'Notes'), minLines: 2, maxLines: 4),
        const SizedBox(height: 14),
        const ListTile(leading: Icon(Icons.payments_rounded), title: Text('Pay Cash on Delivery')),
        const SizedBox(height: 12),
        ElevatedButton.icon(onPressed: _saving ? null : _request, icon: const Icon(Icons.local_shipping_rounded), label: const Text('Request Parcel Delivery')),
      ],
    );
  }

  Future<void> _request() async {
    setState(() => _saving = true);
    final order = await context.read<AppState>().createParcel(_pickup.text, _dropoff.text, _description.text, _notes.text);
    setState(() => _saving = false);
    if (mounted) Navigator.of(context).push(MaterialPageRoute(builder: (_) => OrderTrackingScreen(order: order)));
  }
}
