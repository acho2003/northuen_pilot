import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/app_theme.dart';
import 'screens/splash_screen.dart';
import 'services/api_client.dart';
import 'state/app_state.dart';
import 'state/cart_state.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const NorthuenApp());
}

class NorthuenApp extends StatelessWidget {
  const NorthuenApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider(create: (_) => ApiClient()),
        ChangeNotifierProxyProvider<ApiClient, AppState>(
          create: (context) => AppState(context.read<ApiClient>()),
          update: (_, api, state) => state ?? AppState(api),
        ),
        ChangeNotifierProvider(create: (_) => CartState()),
      ],
      child: MaterialApp(
        title: 'Northuen',
        debugShowCheckedModeBanner: false,
        theme: NorthuenTheme.light(),
        home: const SplashScreen(),
      ),
    );
  }
}
