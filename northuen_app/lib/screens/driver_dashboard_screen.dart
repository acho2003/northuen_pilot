import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/order_model.dart';
import '../state/app_state.dart';
import '../widgets/money_text.dart';
import '../widgets/status_chip.dart';
import 'delivery_screen.dart';
import 'profile_screen.dart';

class DriverDashboardScreen extends StatefulWidget {
  const DriverDashboardScreen({super.key});

  @override
  State<DriverDashboardScreen> createState() => _DriverDashboardScreenState();
}

class _DriverDashboardScreenState extends State<DriverDashboardScreen> {
  bool _online = true;
  int _index = 0;
  String _view = 'AVAILABLE';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        final app = context.read<AppState>();
        app.setDriverAvailable(true);
        app.loadDriverWork();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final pages = [_workBoard(context), const ProfileScreen()];
    return Scaffold(
      appBar: AppBar(
        title: const Text('Driver'),
        actions: [
          IconButton(
            tooltip: 'Refresh',
            onPressed: () => context.read<AppState>().loadDriverWork(),
            icon: const Icon(Icons.refresh_rounded),
          ),
        ],
      ),
      body: pages[_index],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (value) => setState(() => _index = value),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.route_rounded), label: 'Work'),
          NavigationDestination(icon: Icon(Icons.person_rounded), label: 'Profile'),
        ],
      ),
    );
  }

  Widget _workBoard(BuildContext context) {
    final app = context.watch<AppState>();
    final visibleOrders = _view == 'AVAILABLE' ? app.availableDriverOrders : app.assignedOrders;
    final todayCash = app.assignedOrders.fold<num>(0, (sum, order) => sum + (order.paymentStatus == 'PAID' ? order.totalAmount : 0));
    final pendingCash = app.assignedOrders.fold<num>(0, (sum, order) => sum + (order.paymentStatus == 'PENDING' ? order.totalAmount : 0));

    return RefreshIndicator(
      onRefresh: app.loadDriverWork,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.primary,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Ready for Bhutan deliveries', style: Theme.of(context).textTheme.titleLarge?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
                          const SizedBox(height: 4),
                          Text(_online ? 'You can accept available COD jobs.' : 'Go online to accept new work.', style: const TextStyle(color: Colors.white70)),
                        ],
                      ),
                    ),
                    Switch(
                      value: _online,
                      activeThumbColor: Colors.white,
                      activeTrackColor: Colors.white38,
                      onChanged: (value) async {
                        setState(() => _online = value);
                        await context.read<AppState>().setDriverAvailable(value);
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                Row(
                  children: [
                    Expanded(child: _CashTile(label: 'Collected', amount: todayCash, dark: true)),
                    const SizedBox(width: 10),
                    Expanded(child: _CashTile(label: 'To collect', amount: pendingCash, dark: true)),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          SegmentedButton<String>(
            segments: [
              ButtonSegment(value: 'AVAILABLE', label: Text('Available (${app.availableDriverOrders.length})'), icon: const Icon(Icons.work_rounded)),
              ButtonSegment(value: 'MINE', label: Text('Mine (${app.assignedOrders.length})'), icon: const Icon(Icons.delivery_dining_rounded)),
            ],
            selected: {_view},
            onSelectionChanged: (value) => setState(() => _view = value.first),
          ),
          const SizedBox(height: 14),
          if (app.loading && visibleOrders.isEmpty) const Center(child: Padding(padding: EdgeInsets.all(24), child: CircularProgressIndicator())),
          if (!app.loading && visibleOrders.isEmpty)
            _EmptyDriverState(
              availableView: _view == 'AVAILABLE',
              online: _online,
              onRefresh: app.loadDriverWork,
            ),
          ...visibleOrders.map((order) => _DriverOrderCard(
                order: order,
                available: _view == 'AVAILABLE',
                online: _online,
                onAccept: order.delivery == null || !_online ? null : () => context.read<AppState>().acceptAvailableDelivery(order.delivery!.id),
                onOpen: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => DeliveryScreen(order: order))),
              )),
        ],
      ),
    );
  }
}

class _DriverOrderCard extends StatelessWidget {
  const _DriverOrderCard({required this.order, required this.available, required this.online, required this.onAccept, required this.onOpen});

  final Order order;
  final bool available;
  final bool online;
  final VoidCallback? onAccept;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) {
    final title = available ? 'New ${order.orderType.toLowerCase()} delivery' : '${order.orderType} delivery';
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(child: Text(title, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16))),
                  StatusChip(order.status),
                ],
              ),
              const SizedBox(height: 10),
              _RouteLine(icon: Icons.storefront_rounded, label: 'Pickup', value: order.pickupAddress),
              const SizedBox(height: 8),
              _RouteLine(icon: Icons.location_on_rounded, label: 'Dropoff', value: order.dropoffAddress),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: Row(
                      children: [
                        const Icon(Icons.payments_rounded, size: 20),
                        const SizedBox(width: 8),
                        MoneyText(order.totalAmount, style: const TextStyle(fontWeight: FontWeight.w900)),
                      ],
                    ),
                  ),
                  if (available)
                    FilledButton.icon(onPressed: onAccept, icon: const Icon(Icons.check_rounded), label: Text(online ? 'Accept' : 'Offline'))
                  else
                    FilledButton.tonalIcon(onPressed: onOpen, icon: const Icon(Icons.navigation_rounded), label: const Text('Open')),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _RouteLine extends StatelessWidget {
  const _RouteLine({required this.icon, required this.label, required this.value});

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 20, color: Theme.of(context).colorScheme.primary),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: Theme.of(context).textTheme.labelMedium),
              Text(value, style: const TextStyle(fontWeight: FontWeight.w700)),
            ],
          ),
        ),
      ],
    );
  }
}

class _CashTile extends StatelessWidget {
  const _CashTile({required this.label, required this.amount, this.dark = false});

  final String label;
  final num amount;
  final bool dark;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: dark ? Colors.white.withValues(alpha: .12) : Colors.white, borderRadius: BorderRadius.circular(8)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: TextStyle(color: dark ? Colors.white70 : null)),
          const SizedBox(height: 4),
          MoneyText(amount, style: TextStyle(color: dark ? Colors.white : null, fontWeight: FontWeight.w900)),
        ],
      ),
    );
  }
}

class _EmptyDriverState extends StatelessWidget {
  const _EmptyDriverState({required this.availableView, required this.online, required this.onRefresh});

  final bool availableView;
  final bool online;
  final Future<void> Function() onRefresh;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(top: 10),
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(8)),
      child: Column(
        children: [
          Icon(availableView ? Icons.work_outline_rounded : Icons.route_outlined, size: 44, color: Theme.of(context).colorScheme.primary),
          const SizedBox(height: 10),
          Text(
            availableView ? (online ? 'No open jobs right now' : 'You are offline') : 'No active deliveries',
            style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
          ),
          const SizedBox(height: 6),
          Text(
            availableView ? 'Accepted vendor orders and parcel requests will appear here.' : 'Jobs you accept will move into this list.',
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),
          OutlinedButton.icon(onPressed: onRefresh, icon: const Icon(Icons.refresh_rounded), label: const Text('Check again')),
        ],
      ),
    );
  }
}
