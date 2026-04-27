import 'package:flutter/material.dart';

class NorthuenTheme {
  static const maroon = Color(0xFF7A1F2B);
  static const ink = Color(0xFF24191A);
  static const cloud = Color(0xFFF8F5F2);
  static const saffron = Color(0xFFE7A92F);
  static const forest = Color(0xFF246B58);

  static ThemeData light() {
    final scheme = ColorScheme.fromSeed(
      seedColor: maroon,
      primary: maroon,
      secondary: saffron,
      tertiary: forest,
      surface: Colors.white,
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: cloud,
      fontFamily: 'Roboto',
      appBarTheme: const AppBarTheme(
        backgroundColor: cloud,
        foregroundColor: ink,
        elevation: 0,
        centerTitle: false,
      ),
      cardTheme: CardThemeData(
        color: Colors.white,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide.none),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: maroon,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(50),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8))),
      ),
    );
  }
}
