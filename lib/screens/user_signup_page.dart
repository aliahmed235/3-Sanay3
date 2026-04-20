// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';

class UserSignUpPage extends StatefulWidget {
  const UserSignUpPage({super.key});

  @override
  State<UserSignUpPage> createState() => _UserSignUpPageState();
}

class _UserSignUpPageState extends State<UserSignUpPage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> fadeAnimation;
  late Animation<Offset> slideAnimation;

  bool isChecked = false;

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

  Widget buildField(String hint, IconData icon, {bool obscure = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.1),
          borderRadius: BorderRadius.circular(30),
        ),
        child: TextField(
          obscureText: obscure,
          style: const TextStyle(color: Colors.white),
          decoration: InputDecoration(
            prefixIcon: Icon(icon, color: Colors.white70),
            hintText: hint,
            hintStyle: const TextStyle(color: Colors.white54),
            border: InputBorder.none,
            contentPadding: const EdgeInsets.symmetric(vertical: 18),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: FadeTransition(
        opacity: fadeAnimation,
        child: SlideTransition(
          position: slideAnimation,
          child: Container(
            height: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 50),
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
            child: SingleChildScrollView(
              child: Column(
                children: [
                  // Logo
                  Container(
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.6),
                          blurRadius: 30,
                          spreadRadius: 5,
                          offset: const Offset(0, 10),
                        ),
                      ],
                    ),
                    child: const CircleAvatar(
                      radius: 60,
                      backgroundImage: AssetImage('assets/images/logo.jpeg'),
                    ),
                  ),

                  const SizedBox(height: 30),

                  const Text(
                    "Sign Up to User",
                    style: TextStyle(
                      fontSize: 28,
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                  ),

                  const SizedBox(height: 30),

                  buildField("Name", Icons.person),
                  buildField("National ID", Icons.credit_card),
                  buildField("Address", Icons.location_on),
                  buildField("Phone Number", Icons.phone),
                  buildField("Email", Icons.email),
                  buildField("Password", Icons.lock, obscure: true),
                  buildField("Confirm Password", Icons.lock, obscure: true),

                  const SizedBox(height: 20),

                  Row(
                    children: [
                      Checkbox(
                        value: isChecked,
                        onChanged: (val) {
                          setState(() {
                            isChecked = val!;
                          });
                        },
                        activeColor: const Color(0xFF00BFA6),
                      ),
                      const Expanded(
                        child: Text(
                          "I agree with privacy policy",
                          style: TextStyle(color: Colors.white70),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 20),

                  Container(
                    width: 220,
                    height: 55,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(30),
                      gradient: const LinearGradient(
                        colors: [Color(0xFF00BFA6), Color(0xFF003049)],
                      ),
                    ),
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.transparent,
                        shadowColor: Colors.transparent,
                      ),
                      onPressed: isChecked ? () {} : null,
                      child: const Text(
                        "Create Account",
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
