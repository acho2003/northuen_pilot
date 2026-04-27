import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';

import '../models/order_model.dart';
import '../state/app_state.dart';
import '../widgets/money_text.dart';
import '../widgets/status_chip.dart';

class OrderTrackingScreen extends StatefulWidget {
  const OrderTrackingScreen({super.key, required this.order});

  final Order order;

  @override
  State<OrderTrackingScreen> createState() => _OrderTrackingScreenState();
}

class _OrderTrackingScreenState extends State<OrderTrackingScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) context.read<AppState>().loadTracking(widget.order.id);
    });
  }

  @override
  Widget build(BuildContext context) {
    final pickup = const LatLng(27.4728, 89.6390);
    final dropoff = const LatLng(27.4850, 89.6250);
    final tracking = context.watch<AppState>().trackingPoints;
    final driver = tracking.isEmpty ? const LatLng(27.4782, 89.6320) : LatLng(tracking.first.latitude.toDouble(), tracking.first.longitude.toDouble());
    return Scaffold(
      appBar: AppBar(title: const Text('Order tracking')),
      body: Column(
        children: [
          Expanded(
            child: FlutterMap(
              options: MapOptions(initialCenter: driver, initialZoom: 13),
              children: [
                TileLayer(urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png', userAgentPackageName: 'bt.northuen.app'),
                MarkerLayer(markers: [
                  Marker(point: pickup, child: const Icon(Icons.storefront_rounded, color: Colors.deepOrange, size: 34)),
                  Marker(point: driver, child: const Icon(Icons.delivery_dining_rounded, color: Color(0xFF7A1F2B), size: 38)),
                  Marker(point: dropoff, child: const Icon(Icons.location_on_rounded, color: Colors.green, size: 36)),
                ]),
              ],
            ),
          ),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            color: Colors.white,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [StatusChip(widget.order.status), StatusChip(widget.order.paymentStatus)]),
                const SizedBox(height: 12),
                Text(widget.order.dropoffAddress, style: const TextStyle(fontWeight: FontWeight.w800)),
                const SizedBox(height: 6),
                Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [const Text('Pay Cash on Delivery'), MoneyText(widget.order.totalAmount, style: const TextStyle(fontWeight: FontWeight.w900))]),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(child: OutlinedButton.icon(onPressed: () => context.read<AppState>().loadTracking(widget.order.id), icon: const Icon(Icons.refresh_rounded), label: const Text('Refresh tracking'))),
                    if (widget.order.status == 'DELIVERED') ...[
                      const SizedBox(width: 10),
                      Expanded(child: FilledButton.icon(onPressed: _review, icon: const Icon(Icons.star_rounded), label: const Text('Rate'))),
                    ],
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _review() async {
    var vendorRating = 5;
    var driverRating = 5;
    final comment = TextEditingController();
    await showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Rate delivery'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            DropdownButtonFormField(initialValue: vendorRating, decoration: const InputDecoration(labelText: 'Vendor'), items: [1, 2, 3, 4, 5].map((v) => DropdownMenuItem(value: v, child: Text('$v stars'))).toList(), onChanged: (value) => vendorRating = value!),
            const SizedBox(height: 10),
            DropdownButtonFormField(initialValue: driverRating, decoration: const InputDecoration(labelText: 'Driver'), items: [1, 2, 3, 4, 5].map((v) => DropdownMenuItem(value: v, child: Text('$v stars'))).toList(), onChanged: (value) => driverRating = value!),
            const SizedBox(height: 10),
            TextField(controller: comment, decoration: const InputDecoration(labelText: 'Comment')),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(dialogContext), child: const Text('Cancel')),
          FilledButton(
            onPressed: () async {
              await context.read<AppState>().reviewOrder(widget.order.id, vendorRating, driverRating, comment.text);
              if (dialogContext.mounted) Navigator.pop(dialogContext);
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
  }
}
