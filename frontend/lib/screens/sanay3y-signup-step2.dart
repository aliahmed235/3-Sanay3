// ignore_for_file: deprecated_member_use

import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:file_picker/file_picker.dart';
import 'package:talat_sanaye3/screens/sanay3y_signup_step3_page.dart';

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

  String? criminalStatus = "no";

  final ImagePicker picker = ImagePicker();

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

  // 📸 pick profile image
  Future pickProfileImage() async {
    final picked = await picker.pickImage(source: ImageSource.gallery);

    if (picked != null) {
      setState(() {
        profileImage = File(picked.path);
      });
    }
  }

Future pickFile() async {
  final result = await FilePicker.platform.pickFiles(
    type: FileType.any,
  );

  if (result != null &&
      result.files.single.path != null) {
    setState(() {
      criminalFile = File(result.files.single.path!);
    });
  }
}


  // input field
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
            contentPadding: const EdgeInsets.symmetric(vertical: 18),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SizedBox.expand(
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
                    // 🔥 LOGO
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

                    const SizedBox(height: 20),

                    const Text(
                      "Step 2 of 3",
                      style: TextStyle(color: Colors.white70),
                    ),

                    const SizedBox(height: 10),

                    const Text(
                      "Professional Details",
                      style: TextStyle(
                        fontSize: 26,
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 30),

                    // experience
                    buildField("Experience Years", Icons.work),

                    const SizedBox(height: 25),

                    // PROFILE IMAGE
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        "Profile Image",
                        style: TextStyle(color: Colors.white70),
                      ),
                    ),

                    const SizedBox(height: 10),

                    GestureDetector(
                      onTap: pickProfileImage,
                      child: CircleAvatar(
                        radius: 45,
                        backgroundColor: Colors.white.withOpacity(0.1),
                        backgroundImage: profileImage != null
                            ? FileImage(profileImage!)
                            : null,
                        child: profileImage == null
                            ? const Icon(
                                Icons.camera_alt,
                                color: Colors.white,
                                size: 30,
                              )
                            : null,
                      ),
                    ),

                    const SizedBox(height: 10),

                    const Text(
                      "Upload Profile Photo",
                      style: TextStyle(color: Colors.white54),
                    ),

                    const SizedBox(height: 25),

                    // CRIMINAL RECORD
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        "Criminal Record",
                        style: TextStyle(color: Colors.white70),
                      ),
                    ),

                    Theme(
                      data: Theme.of(
                        context,
                      ).copyWith(unselectedWidgetColor: Colors.white70),
                      child: RadioListTile(
                        value: "yes",
                        groupValue: criminalStatus,
                        onChanged: (val) {
                          setState(() {
                            criminalStatus = val.toString();
                          });
                        },
                        title: const Text(
                          "Criminal record",
                          style: TextStyle(color: Colors.white),
                        ),
                        activeColor: Color(0xFF00BFA6),
                      ),
                    ),

                    Theme(
                      data: Theme.of(
                        context,
                      ).copyWith(unselectedWidgetColor: Colors.white70),
                      child: RadioListTile(
                        value: "no",
                        groupValue: criminalStatus,
                        onChanged: (val) {
                          setState(() {
                            criminalStatus = val.toString();
                          });
                        },
                        title: const Text(
                          "No criminal record",
                          style: TextStyle(color: Colors.white),
                        ),
                        activeColor: Color(0xFF00BFA6),
                      ),
                    ),

                    const SizedBox(height: 20),

                    // UPLOAD FILE
                    GestureDetector(
                      onTap: pickFile,
                      child: Container(
                        padding: const EdgeInsets.all(20),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Column(
                          children: [
                            const Icon(
                              Icons.upload_file,
                              color: Colors.white,
                              size: 40,
                            ),
                            const SizedBox(height: 10),
                            Text(
                              criminalFile == null
                                  ? "Upload Police Record File"
                                  : "File Selected ✔",
                              style: const TextStyle(color: Colors.white70),
                            ),
                          ],
                        ),
                      ),
                    ),

                    const SizedBox(height: 30),

                    // NEXT BUTTON
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
                        onPressed:
                            (profileImage == null || criminalFile == null)
                            ? null
                            : () {
                                Navigator.push(context, MaterialPageRoute(builder: (context) => const Sanay3yStep3()));
                              },
                        child: const Text(
                          "Next",
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
      ),
    );
  }
}
