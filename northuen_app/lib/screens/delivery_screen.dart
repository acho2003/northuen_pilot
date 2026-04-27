import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';

import '../models/order_model.dart';
import '../state/app_state.dart';
import '../widgets/money_text.dart';
import '../widgets/status_chip.dart';

class DeliveryScreen extends StatelessWidget {
  const DeliveryScreen({super.key, required this.order});

  final Order order;

  @override
  Widget build(BuildContext context) {
    final deliveryId = order.delivery?.id;
    return Scaffold(
      appBar: AppBar(title: const Text('Delivery')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          StatusChip(order.status),
          const SizedBox(height: 12),
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: SizedBox(
              height: 230,
              child: FlutterMap(
                options: const MapOptions(initialCenter: LatLng(27.4782, 89.6320), initialZoom: 13),
                children: [
                  TileLayer(urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png', userAgentPackageName: 'bt.northuen.app'),
                  const MarkerLayer(markers: [
                    Marker(point: LatLng(27.4728, 89.6390), child: Icon(Icons.storefront_rounded, color: Colors.deepOrange, size: 34)),
                    Marker(point: LatLng(27.4782, 89.6320), child: Icon(Icons.delivery_dining_rounded, color: Color(0xFF7A1F2B), size: 38)),
                    Marker(point: LatLng(27.4850, 89.6250), child: Icon(Icons.location_on_rounded, color: Colors.green, size: 36)),
                  ]),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(child: ListTile(leading: const Icon(Icons.storefront_rounded), title: const Text('Pickup'), subtitle: Text(order.pickupAddress))),
          const SizedBox(height: 10),
          Card(child: ListTile(leading: const Icon(Icons.location_on_rounded), title: const Text('Dropoff'), subtitle: Text(order.dropoffAddress))),
          const SizedBox(height: 10),
          Card(child: ListTile(leading: const Icon(Icons.payments_rounded), title: const Text('Cash to collect'), trailing: MoneyText(order.totalAmount, style: const TextStyle(fontWeight: FontWeight.w900)))),
          const SizedBox(height: 18),
          if (deliveryId != null) ...[
            _Action(label: 'Accept Delivery', icon: Icons.check_rounded, onPressed: () => context.read<AppState>().updateDelivery(deliveryId, 'ACCEPTED')),
            _Action(label: 'Picked Up', icon: Icons.shopping_bag_rounded, onPressed: () => context.read<AppState>().updateDelivery(deliveryId, 'PICKED_UP')),
            _Action(label: 'On The Way', icon: Icons.navigation_rounded, onPressed: () => context.read<AppState>().updateDelivery(deliveryId, 'ON_THE_WAY')),
            _Action(label: 'Send Live Location', icon: Icons.my_location_rounded, onPressed: () => context.read<AppState>().sendLocation(deliveryId, 27.4782, 89.6320)),
            const SizedBox(height: 8),
            ElevatedButton.icon(
              onPressed: () => context.read<AppState>().completeDelivery(deliveryId),
              icon: const Icon(Icons.price_check_rounded),
              label: const Text('Mark Delivered & Payment Collected'),
            ),
          ],
        ],
      ),
    );
  }
}

class _Action extends StatelessWidget {
  const _Action({required this.label, required this.icon, required this.onPressed});
  final String label;
  final IconData icon;
  final VoidCallback onPressed;
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 10),
        child: OutlinedButton.icon(onPressed: onPressed, icon: Icon(icon), label: Text(label)),
      );
}
