// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:talat_sanaye3/screens/sanay3y-signup-step2.dart';

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

  String? selectedSpecialization;

  final List<String> specializations = [
    "Electrician",
    "Plumber",
    "Carpenter",
  ];

  @override
  void initState() {
    super.initState();

    _controller =
        AnimationController(vsync: this, duration: const Duration(seconds: 1));

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

  Widget buildField(String hint, IconData icon) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.1),
          borderRadius: BorderRadius.circular(30),
        ),
        child: TextField(
          style: const TextStyle(color: Colors.white),
          decoration: InputDecoration(
            prefixIcon: Icon(icon, color: Colors.white70),
            hintText: hint,
            hintStyle: const TextStyle(color: Colors.white54),
            border: InputBorder.none,
            contentPadding:
                const EdgeInsets.symmetric(vertical: 18),
          ),
        ),
      ),
    );
  }

  Widget buildDropdown() {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 10),
      padding: const EdgeInsets.symmetric(horizontal: 20),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.1),
        borderRadius: BorderRadius.circular(30),
      ),
      child: DropdownButton<String>(
        value: selectedSpecialization,
        dropdownColor: const Color(0xFF003049),
        hint: const Text(
          "Select specialization",
          style: TextStyle(color: Colors.white54),
        ),
        isExpanded: true,
        underline: const SizedBox(),
        iconEnabledColor: Colors.white,
        items: specializations.map((String value) {
          return DropdownMenuItem(
            value: value,
            child: Text(
              value,
              style: const TextStyle(color: Colors.white),
            ),
          );
        }).toList(),
        onChanged: (val) {
          setState(() {
            selectedSpecialization = val;
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SizedBox(
        height: double.infinity,
        child: FadeTransition(
          opacity: fadeAnimation,
          child: SlideTransition(
            position: slideAnimation,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 50),
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Color.fromARGB(255, 2, 39, 59),
                    Color.fromARGB(255, 5, 35, 31),
                  ],
                ),
              ),
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    // 🔥 Logo
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
                        backgroundImage:
                            AssetImage('assets/images/logo.jpeg'),
                      ),
                    ),
        
                    const SizedBox(height: 20),
        
                    const Text(
                      "Step 1 of 3",
                      style: TextStyle(color: Colors.white70),
                    ),
        
                    const SizedBox(height: 10),
        
                    const Text(
                      "Sign Up to Sanay3y",
                      style: TextStyle(
                        fontSize: 26,
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
        
                    const SizedBox(height: 30),
        
                    buildField("Name", Icons.person),
                    buildField("National ID", Icons.credit_card),
                    buildField("Address", Icons.location_on),
                    buildField("Phone Number", Icons.phone),
        
                    const SizedBox(height: 10),
        
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        "Specialization",
                        style: TextStyle(color: Colors.white70),
                      ),
                    ),
        
                    buildDropdown(),
        
                    const SizedBox(height: 30),
        
                    // Next Button
                    Container(
                      width: 220,
                      height: 55,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(30),
                        gradient: const LinearGradient(
                          colors: [
                            Color(0xFF00BFA6),
                            Color(0xFF003049)
                          ],
                        ),
                      ),
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.transparent,
                          shadowColor: Colors.transparent,
                        ),
                        onPressed: selectedSpecialization == null
                            ? null
                            : () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) =>
                                        const Sanay3yStep2(),
                                  ),
                                );
                              },
                        child: const Text(
                          "Next",
                          style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: Colors.white),
                        ),
                      ),
                    ),
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
