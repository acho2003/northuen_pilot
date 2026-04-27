import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/app_state.dart';
import 'auth_screen.dart';
import 'role_home_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  bool _started = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_started) {
      _started = true;
      final app = context.read<AppState>();
      final navigator = Navigator.of(context);
      _restore(app, navigator);
    }
  }

  Future<void> _restore(AppState app, NavigatorState navigator) async {
    await app.restore();
    if (!mounted) return;
    navigator.pushReplacement(MaterialPageRoute(builder: (_) => app.authenticated ? const RoleHomeScreen() : const AuthScreen()));
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.local_shipping_rounded, size: 72, color: Color(0xFF7A1F2B)),
            SizedBox(height: 16),
            Text('Northuen', style: TextStyle(fontSize: 34, fontWeight: FontWeight.w800)),
            SizedBox(height: 8),
            Text('Bhutan delivery, paid in cash on arrival'),
          ],
        ),
      ),
    );
  }
}
