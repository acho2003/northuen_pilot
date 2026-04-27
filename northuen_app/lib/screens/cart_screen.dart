import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/cart_state.dart';
import '../widgets/money_text.dart';
import 'checkout_screen.dart';

class CartScreen extends StatelessWidget {
  const CartScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartState>();
    return Scaffold(
      appBar: AppBar(title: const Text('Cart')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (cart.lines.isEmpty) const Center(child: Padding(padding: EdgeInsets.all(32), child: Text('Your cart is empty'))),
          ...cart.lines.map((line) => Card(
                child: ListTile(
                  title: Text(line.product.name),
                  subtitle: Text('Qty ${line.quantity}'),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      MoneyText(line.lineTotal),
                      IconButton(tooltip: 'Remove', onPressed: () => cart.remove(line.product.id), icon: const Icon(Icons.delete_outline_rounded)),
                    ],
                  ),
                ),
              )),
          const SizedBox(height: 16),
          if (cart.lines.isNotEmpty) ...[
            _TotalRow('Subtotal', cart.subtotal),
            _TotalRow('Delivery', cart.deliveryFee),
            const Divider(),
            _TotalRow('Total COD', cart.total, bold: true),
            const SizedBox(height: 8),
            const ListTile(leading: Icon(Icons.payments_rounded), title: Text('Pay Cash on Delivery')),
            const SizedBox(height: 14),
            ElevatedButton.icon(onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const CheckoutScreen())), icon: const Icon(Icons.arrow_forward_rounded), label: const Text('Checkout')),
          ],
        ],
      ),
    );
  }
}

class _TotalRow extends StatelessWidget {
  const _TotalRow(this.label, this.amount, {this.bold = false});
  final String label;
  final num amount;
  final bool bold;
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [Text(label, style: TextStyle(fontWeight: bold ? FontWeight.w900 : FontWeight.w500)), MoneyText(amount, style: TextStyle(fontWeight: bold ? FontWeight.w900 : FontWeight.w500))],
        ),
      );
}
