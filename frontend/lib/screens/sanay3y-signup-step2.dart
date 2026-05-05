// ignore_for_file: deprecated_member_use

import 'dart:io';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'package:file_picker/file_picker.dart';
import 'package:talat_sanaye3/providers/theme_provider.dart';
import 'package:talat_sanaye3/screens/sanay3y_signup_step3_page.dart';
import 'package:talat_sanaye3/widges/logo.dart';
import 'package:talat_sanaye3/widges/text_feild_widget.dart';

class Sanay3yStep2 extends StatefulWidget {
  const Sanay3yStep2({super.key});

  @override
  State<Sanay3yStep2> createState() => _Sanay3yStep2State();
}

class _Sanay3yStep2State extends State<Sanay3yStep2>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> fadeAnimation;
  late Animation<Offset> slideAnimation;

  File? profileImage;
  File? criminalFile;

  String? criminalFileName;

  final ImagePicker picker = ImagePicker();

  final TextEditingController _hourlyRateController = TextEditingController();
  final TextEditingController _experienceController = TextEditingController();

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
    _hourlyRateController.dispose();
    _experienceController.dispose();
    super.dispose();
  }

  Future pickProfileImage() async {
    final picked = await picker.pickImage(source: ImageSource.gallery);

    if (picked != null) {
      setState(() {
        profileImage = File(picked.path);
      });
    }
  }

  Future pickFile() async {
    final result = await FilePicker.platform.pickFiles(type: FileType.any);

    if (result != null && result.files.single.path != null) {
      setState(() {
        criminalFile = File(result.files.single.path!);
        criminalFileName = result.files.single.name;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Provider.of<ThemeProvider>(context).isDarkMode;

    return Scaffold(
      body: SizedBox.expand(
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
                      "Step 2 of 3",
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                        color: isDark ? Colors.grey[500] : Colors.grey[600],
                        letterSpacing: 0.5,
                      ),
                    ),

                    const SizedBox(height: 10),

                    Text(
                      "Professional Profile",
                      style: TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.w700,
                        color: isDark ? Colors.white : Colors.black,
                      ),
                    ),

                    const SizedBox(height: 30),

                    // Experience Years
                    TextFeildWidget(
                      hintText: "Experience Years",
                      icon: Icons.work_outline,
                      controller: _experienceController,
                    ),

                    const SizedBox(height: 15),

                    // Hourly Rate
                    TextFeildWidget(
                      hintText: "Hourly Rate (SAR)",
                      icon: Icons.attach_money,
                      controller: _hourlyRateController,
                    ),

                    const SizedBox(height: 30),

                    // Profile Image Section
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        "Profile Picture",
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: isDark ? Colors.white : Colors.black,
                        ),
                      ),
                    ),

                    const SizedBox(height: 15),

                    GestureDetector(
                      onTap: pickProfileImage,
                      child: Container(
                        width: 110,
                        height: 110,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: isDark
                              ? Colors.grey[800]!.withOpacity(0.5)
                              : Colors.grey[200]!.withOpacity(0.8),
                          border: Border.all(
                            color: isDark
                                ? Colors.grey[700]!.withOpacity(0.5)
                                : Colors.grey[300]!.withOpacity(0.8),
                            width: 2,
                          ),
                          image: profileImage != null
                              ? DecorationImage(
                                  image: FileImage(profileImage!),
                                  fit: BoxFit.cover,
                                )
                              : null,
                        ),
                        child: profileImage == null
                            ? Icon(
                                Icons.camera_alt_outlined,
                                color: isDark
                                    ? Colors.grey[500]
                                    : Colors.grey[700],
                                size: 35,
                              )
                            : null,
                      ),
                    ),

                    const SizedBox(height: 12),

                    Text(
                      profileImage == null
                          ? "Tap to upload photo"
                          : "Photo uploaded ✓",
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w400,
                        color: isDark ? Colors.grey[500] : Colors.grey[600],
                      ),
                    ),

                    const SizedBox(height: 30),

                    // Police Record Section
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        "Police Record",
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: isDark ? Colors.white : Colors.black,
                        ),
                      ),
                    ),

                    const SizedBox(height: 15),

                    GestureDetector(
                      onTap: pickFile,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 20,
                          vertical: 25,
                        ),
                        decoration: BoxDecoration(
                          color: isDark
                              ? Colors.grey[800]!.withOpacity(0.3)
                              : Colors.grey[100]!.withOpacity(0.5),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: isDark
                                ? Colors.grey[700]!.withOpacity(0.5)
                                : Colors.grey[300]!.withOpacity(0.5),
                            width: 1.5,
                          ),
                        ),
                        child: Column(
                          children: [
                            Icon(
                              Icons.upload_file_outlined,
                              color: isDark
                                  ? Colors.grey[500]
                                  : Colors.grey[700],
                              size: 32,
                            ),
                            const SizedBox(height: 10),
                            Text(
                              criminalFile == null
                                  ? "Upload Police Record"
                                  : "File: $criminalFileName",
                              style: TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w500,
                                color: isDark
                                    ? Colors.grey[400]
                                    : Colors.grey[700],
                              ),
                              textAlign: TextAlign.center,
                            ),
                            if (criminalFile != null)
                              Padding(
                                padding: const EdgeInsets.only(top: 8),
                                child: Text(
                                  "✓ File selected",
                                  style: TextStyle(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w500,
                                    color: Colors.green[600],
                                  ),
                                ),
                              ),
                          ],
                        ),
                      ),
                    ),

                    const SizedBox(height: 35),

                    // Next Button
                    GestureDetector(
                      onTap: (profileImage == null || criminalFile == null)
                          ? null
                          : () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => const Sanay3yStep3(),
                                ),
                              );
                            },
                      child: Container(
                        width: double.infinity,
                        height: 50,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(12),
                          color: const Color(0xFF1A1A2E).withOpacity(
                            (profileImage != null && criminalFile != null)
                                ? 1
                                : 0.5,
                          ),
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
                            "Next",
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ),
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
