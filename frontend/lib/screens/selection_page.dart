// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
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
    return Scaffold(
      body: FadeTransition(
        opacity: fadeAnimation,
        child: Container(
          width: double.infinity,
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [
                Color.fromARGB(255, 2, 39, 59),
                Color.fromARGB(255, 5, 35, 31),
              ],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                "Who are you?",
                style: TextStyle(
                  fontSize: 28,
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 40),

              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _buildOption(index: 0, icon: Icons.build, text: "Sanay3y"),

                  const SizedBox(width: 20),

                  Container(height: 80, width: 2, color: Colors.white30),

                  const SizedBox(width: 20),

                  _buildOption(index: 1, icon: Icons.person, text: "User"),
                ],
              ),

              const SizedBox(height: 60),

              _buildContinueButton(),
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
  }) {
    bool isSelected = selectedIndex == index;

    return GestureDetector(
      onTap: () => select(index),
      child: AnimatedScale(
        scale: isSelected ? 1.15 : 1, // 🔥 تكبير بسيط
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
          padding: const EdgeInsets.all(25),
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: isSelected
                ? const Color(0xFF00BFA6).withOpacity(0.15)
                : Colors.white.withOpacity(0.05),

            // 🔥 الإضاءة (Glow)
            boxShadow: isSelected
                ? [
                    BoxShadow(
                      color: const Color(0xFF00BFA6).withOpacity(0.8),
                      blurRadius: 30,
                      spreadRadius: 3,
                    ),
                    BoxShadow(
                      color: const Color(0xFF003049).withOpacity(0.6),
                      blurRadius: 20,
                      spreadRadius: 2,
                    ),
                  ]
                : [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.2),
                      blurRadius: 10,
                    ),
                  ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                icon,
                size: 45,
                color: isSelected ? Colors.white : Colors.white70,
              ),
              const SizedBox(height: 10),
              Text(
                text,
                style: TextStyle(
                  color: isSelected ? Colors.white : Colors.white70,
                  fontWeight: FontWeight.bold,
                  fontSize: 15,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildContinueButton() {
    return AnimatedOpacity(
      duration: const Duration(milliseconds: 300),
      opacity: selectedIndex == -1 ? 0.5 : 1,
      child: Container(
        width: 220,
        height: 55,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(30),
          gradient: const LinearGradient(
            colors: [Color(0xFF00BFA6), Color(0xFF003049)],
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.4),
              blurRadius: 15,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.transparent,
            shadowColor: Colors.transparent,
          ),
          onPressed: selectedIndex == -1
              ? null
              : () {
                  if (selectedIndex == 1) {
                    // User
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

          child: const Text(
            "Continue",
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
        ),
      ),
    );
  }
}
