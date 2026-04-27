import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/app_state.dart';
import 'admin_dashboard_screen.dart';
import 'auth_screen.dart';
import 'customer_home_screen.dart';
import 'driver_dashboard_screen.dart';
import 'vendor_dashboard_screen.dart';

class RoleHomeScreen extends StatelessWidget {
  const RoleHomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final user = app.user;
    if (user == null) return const AuthScreen();
    final body = switch (user.role) {
      'DRIVER' => const DriverDashboardScreen(),
      'VENDOR' => const VendorDashboardScreen(),
      'ADMIN' => const AdminDashboardScreen(),
      _ => const CustomerHomeScreen(),
    };
    return body;
  }
}
