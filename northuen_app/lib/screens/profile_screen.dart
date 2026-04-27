import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/app_state.dart';
import 'auth_screen.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final user = app.user!;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          child: ListTile(
            leading: CircleAvatar(backgroundColor: Theme.of(context).colorScheme.primary, child: Text(user.fullName.substring(0, 1), style: const TextStyle(color: Colors.white))),
            title: Text(user.fullName, style: const TextStyle(fontWeight: FontWeight.w800)),
            subtitle: Text('${user.email}\n${user.phone}'),
            isThreeLine: true,
            trailing: Text(user.role),
          ),
        ),
        const SizedBox(height: 14),
        ListTile(leading: const Icon(Icons.payments_rounded), title: const Text('Payment'), subtitle: const Text('Cash on Delivery only')),
        const SizedBox(height: 18),
        OutlinedButton.icon(
          onPressed: () async {
            await context.read<AppState>().logout();
            if (context.mounted) Navigator.of(context).pushAndRemoveUntil(MaterialPageRoute(builder: (_) => const AuthScreen()), (_) => false);
          },
          icon: const Icon(Icons.logout_rounded),
          label: const Text('Logout'),
        ),
      ],
    );
  }
}
