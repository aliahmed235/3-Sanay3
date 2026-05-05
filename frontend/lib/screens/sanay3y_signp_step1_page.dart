// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/theme_provider.dart';
import 'package:talat_sanaye3/screens/sanay3y-signup-step2.dart';
import 'package:talat_sanaye3/widgets/Button_widget.dart';
import 'package:talat_sanaye3/widgets/buildDropdown.dart';
import 'package:talat_sanaye3/widgets/logo.dart';
import 'package:talat_sanaye3/widgets/text_feild_widget.dart';

class Sanay3yStep1 extends StatefulWidget {
  const Sanay3yStep1({super.key});

  @override
  State<Sanay3yStep1> createState() => _Sanay3yStep1State();
}

class _Sanay3yStep1State extends State<Sanay3yStep1>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> fadeAnimation;
  late Animation<Offset> slideAnimation;

  @override
  void initState() {
    super.initState();

    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    );

    fadeAnimation = Tween<double>(begin: 0, end: 1).animate(_controller);

    slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.1),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOut));

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Provider.of<ThemeProvider>(context).isDarkMode;

    return Scaffold(
      body: SizedBox(
        height: double.infinity,
        child: FadeTransition(
          opacity: fadeAnimation,
          child: SlideTransition(
            position: slideAnimation,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 40),
              color: isDark ? const Color(0xFF0F0F0F) : Colors.white,
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    // Theme Toggle
                    Align(
                      alignment: Alignment.topRight,
                      child: GestureDetector(
                        onTap: () {
                          Provider.of<ThemeProvider>(
                            context,
                            listen: false,
                          ).toggleTheme();
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

                    // Logo
                    Logo(),

                    const SizedBox(height: 20),

                    Text(
                      "Step 1 of 3",
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                        color: isDark ? Colors.grey[500] : Colors.grey[600],
                        letterSpacing: 0.5,
                      ),
                    ),

                    const SizedBox(height: 10),

                    Text(
                      "Basic Information",
                      style: TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.w700,
                        color: isDark ? Colors.white : Colors.black,
                      ),
                    ),

                    const SizedBox(height: 30),

                    Form(
                      child: Column(
                        children: [
                          TextFeildWidget(
                            icon: Icons.person_outline,
                            hintText: "Name",
                          ),
                          const SizedBox(height: 15),
                          TextFeildWidget(
                            icon: Icons.credit_card_outlined,
                            hintText: "National ID",
                          ),
                          const SizedBox(height: 15),
                          TextFeildWidget(
                            icon: Icons.location_on_outlined,
                            hintText: "Address",
                          ),
                          const SizedBox(height: 15),
                          TextFeildWidget(
                            icon: Icons.phone_outlined,
                            hintText: "Phone Number",
                          ),

                          const SizedBox(height: 20),

                          Align(
                            alignment: Alignment.centerLeft,
                            child: Text(
                              "Specialization",
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w500,
                                color: isDark ? Colors.white : Colors.black,
                              ),
                            ),
                          ),

                          const SizedBox(height: 10),

                          BuildDropdown(),
                        ],
                      ),
                    ),

                    const SizedBox(height: 35),

                    ButtonWidget(
                      buttonText: "Next",
                      callback: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => const Sanay3yStep2(),
                          ),
                        );
                      },
                    ),
                    const SizedBox(height: 20),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
