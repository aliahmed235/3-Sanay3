// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/theme_provider.dart';

class Logo extends StatelessWidget {
  const Logo({super.key});

  @override
  Widget build(BuildContext context) {
    final isDark = Provider.of<ThemeProvider>(context).isDarkMode;

    return Container(
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(
            color: isDark
                ? Colors.black.withOpacity(0.3)
                : Colors.grey.withOpacity(0.2),
            blurRadius: 15,
            offset: const Offset(0, 5),
          ),
        ],
      ),
      child: ClipOval(
        child: ColorFiltered(
          colorFilter: ColorFilter.mode(
            isDark
                ? const Color.fromARGB(255, 86, 86, 110)
                : const Color(0xFF1A1A2E),
            BlendMode.srcATop,
          ),
          child: Image.asset(
            'assets/images/logo.png',
            width: 110,
            height: 110,
            fit: BoxFit.cover,
          ),
        ),
      ),
    );
  }
}
