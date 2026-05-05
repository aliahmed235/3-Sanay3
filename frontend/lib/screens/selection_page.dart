// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/theme_provider.dart';
import 'package:talat_sanaye3/screens/sanay3y_signp_step1_page.dart';
import 'package:talat_sanaye3/screens/user_signup_page.dart';

class SelectionPage extends StatefulWidget {
  const SelectionPage({super.key});

  @override
  State<SelectionPage> createState() => _SelectionPageState();
}

class _SelectionPageState extends State<SelectionPage>
    with SingleTickerProviderStateMixin {
  int selectedIndex = -1;

  late AnimationController _controller;
  late Animation<double> fadeAnimation;

  @override
  void initState() {
    super.initState();

    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    );

    fadeAnimation = Tween<double>(begin: 0, end: 1).animate(_controller);

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void select(int index) {
    setState(() {
      selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Provider.of<ThemeProvider>(context).isDarkMode;

    return Scaffold(
      body: FadeTransition(
        opacity: fadeAnimation,
        child: Container(
          width: double.infinity,
          height: double.infinity,
          color: isDark ? const Color(0xFF0F0F0F) : Colors.white,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Theme Toggle
              Align(
                alignment: Alignment.topRight,
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: GestureDetector(
                    onTap: () {
                      Provider.of<ThemeProvider>(context, listen: false)
                          .toggleTheme();
                    },
                    child: Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: isDark
                            ? Colors.grey[800]!.withOpacity(0.8)
                            : Colors.grey[100]!.withOpacity(0.8),
                      ),
                      child: Icon(
                        isDark ? Icons.light_mode : Icons.dark_mode,
                        color: isDark ? Colors.amber : Colors.grey[700],
                        size: 22,
                      ),
                    ),
                  ),
                ),
              ),

              Text(
                "Who are you?",
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w700,
                  color: isDark ? Colors.white : Colors.black,
                ),
              ),

              const SizedBox(height: 45),

              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _buildOption(
                    index: 0,
                    icon: Icons.build_rounded,
                    text: "Sanay3y",
                    isDark: isDark,
                  ),

                  const SizedBox(width: 30),

                  _buildOption(
                    index: 1,
                    icon: Icons.person_rounded,
                    text: "User",
                    isDark: isDark,
                  ),
                ],
              ),

              const SizedBox(height: 60),

              _buildContinueButton(isDark),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildOption({
    required int index,
    required IconData icon,
    required String text,
    required bool isDark,
  }) {
    bool isSelected = selectedIndex == index;

    return GestureDetector(
      onTap: () => select(index),
      child: AnimatedScale(
        scale: isSelected ? 1.1 : 1,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
          padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 32),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(16),
            color: isSelected
                ? const Color(0xFF1A1A2E).withOpacity(0.9)
                : (isDark
                    ? Colors.grey[800]!.withOpacity(0.5)
                    : Colors.grey[100]!.withOpacity(0.8)),
            boxShadow: [
              if (isSelected)
                BoxShadow(
                  color: const Color(0xFF1A1A2E).withOpacity(0.3),
                  blurRadius: 20,
                  offset: const Offset(0, 8),
                ),
            ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                icon,
                size: 40,
                color: isSelected
                    ? Colors.white
                    : (isDark ? Colors.grey[400] : Colors.grey[700]),
              ),
              const SizedBox(height: 12),
              Text(
                text,
                style: TextStyle(
                  color: isSelected
                      ? Colors.white
                      : (isDark ? Colors.grey[400] : Colors.grey[700]),
                  fontWeight: FontWeight.w600,
                  fontSize: 16,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildContinueButton(bool isDark) {
    return AnimatedOpacity(
      duration: const Duration(milliseconds: 300),
      opacity: selectedIndex == -1 ? 0.5 : 1,
      child: GestureDetector(
        onTap: selectedIndex == -1
            ? null
            : () {
                if (selectedIndex == 1) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const UserSignUpPage(),
                    ),
                  );
                } else {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const Sanay3yStep1(),
                    ),
                  );
                }
              },
        child: Container(
          width: 200,
          height: 50,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            color: const Color(0xFF1A1A2E),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.15),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Center(
            child: Text(
              "Continue",
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: Colors.white,
              ),
            ),
          ),
        ),
      ),
    );
  }
}