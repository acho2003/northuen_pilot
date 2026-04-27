import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/vendor_model.dart';
import '../state/app_state.dart';
import '../state/cart_state.dart';
import '../widgets/money_text.dart';

class ProductMenuScreen extends StatefulWidget {
  const ProductMenuScreen({super.key, required this.vendor});
  final Vendor vendor;

  @override
  State<ProductMenuScreen> createState() => _ProductMenuScreenState();
}

class _ProductMenuScreenState extends State<ProductMenuScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) context.read<AppState>().loadProducts(widget.vendor.id);
    });
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    return Scaffold(
      appBar: AppBar(title: Text(widget.vendor.name)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(widget.vendor.address, style: Theme.of(context).textTheme.bodyLarge),
          const SizedBox(height: 16),
          ...app.products.map((product) => Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Card(
                  child: ListTile(
                    contentPadding: const EdgeInsets.all(14),
                    leading: const Icon(Icons.lunch_dining_rounded),
                    title: Text(product.name, style: const TextStyle(fontWeight: FontWeight.w800)),
                    subtitle: Text(product.description),
                    trailing: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        MoneyText(product.price, style: const TextStyle(fontWeight: FontWeight.w900)),
                        IconButton.filledTonal(
                          tooltip: 'Add',
                          onPressed: () async {
                            context.read<CartState>().add(widget.vendor, product);
                            await context.read<AppState>().addBackendCartItem(product.id, 1);
                          },
                          icon: const Icon(Icons.add_rounded),
                        ),
                      ],
                    ),
                  ),
                ),
              )),
        ],
      ),
    );
  }
}
