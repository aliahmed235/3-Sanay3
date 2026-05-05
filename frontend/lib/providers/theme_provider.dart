import 'package:flutter/material.dart';

class ThemeProvider extends ChangeNotifier {
  final bool _isDarkMode = false;

  bool get isDarkMode => _isDarkMode;

  void toggleTheme() {}
}
