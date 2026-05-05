// ignore_for_file: use_build_context_synchronously, deprecated_member_use

import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/auth_session_provider.dart';
import 'package:talat_sanaye3/providers/theme_provider.dart';
import 'package:talat_sanaye3/screens/user/user_home_screen.dart';
import 'package:talat_sanaye3/services/Auth_services.dart';
import 'package:talat_sanaye3/widgets/logo.dart';
import 'package:talat_sanaye3/widgets/text_feild_widget.dart';

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
  GlobalKey<FormState> formKey = GlobalKey();

  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _nationalIdController = TextEditingController();
  final TextEditingController _addressController = TextEditingController();
  final TextEditingController _phoneController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final TextEditingController _confirmPasswordController =
      TextEditingController();

  bool isLoading = false;
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
    _nameController.dispose();
    _nationalIdController.dispose();
    _addressController.dispose();
    _phoneController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Provider.of<ThemeProvider>(context).isDarkMode;

    return Scaffold(
      body: FadeTransition(
        opacity: fadeAnimation,
        child: SlideTransition(
          position: slideAnimation,
          child: Container(
            height: double.infinity,
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 40),
            color: isDark ? const Color(0xFF0F0F0F) : Colors.white,
            child: SingleChildScrollView(
              child: Column(
                children: [
                  // Logo
                  Logo(),

                  const SizedBox(height: 30),

                  Text(
                    "Create Account",
                    style: TextStyle(
                      fontSize: 26,
                      fontWeight: FontWeight.w700,
                      color: isDark ? Colors.white : Colors.black,
                    ),
                  ),

                  const SizedBox(height: 10),

                  Text(
                    "As a User",
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w400,
                      color: isDark ? Colors.grey[500] : Colors.grey[600],
                    ),
                  ),

                  const SizedBox(height: 30),

                  Form(
                    key: formKey,
                    child: Column(
                      children: [
                        TextFeildWidget(
                          hintText: "Name",
                          icon: Icons.person_outline,
                          controller: _nameController,
                        ),
                        const SizedBox(height: 15),
                        TextFeildWidget(
                          hintText: "National ID",
                          icon: Icons.credit_card_outlined,
                          controller: _nationalIdController,
                        ),
                        const SizedBox(height: 15),
                        TextFeildWidget(
                          hintText: "Address",
                          icon: Icons.location_on_outlined,
                          controller: _addressController,
                        ),
                        const SizedBox(height: 15),
                        TextFeildWidget(
                          hintText: "Phone Number",
                          icon: Icons.phone_outlined,
                          controller: _phoneController,
                        ),
                        const SizedBox(height: 15),
                        TextFeildWidget(
                          hintText: "Email",
                          icon: Icons.email_outlined,
                          controller: _emailController,
                        ),
                        const SizedBox(height: 15),
                        TextFeildWidget(
                          hintText: "Password",
                          icon: Icons.lock_outline,
                          obscureText: true,
                          controller: _passwordController,
                        ),
                        const SizedBox(height: 15),
                        TextFeildWidget(
                          hintText: "Confirm Password",
                          icon: Icons.lock_outline,
                          obscureText: true,
                          controller: _confirmPasswordController,
                        ),
                      ],
                    ),
                  ),

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
                        activeColor: const Color(0xFF1A1A2E),
                      ),
                      Expanded(
                        child: Text(
                          "I agree with privacy policy",
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w400,
                            color: isDark ? Colors.grey[500] : Colors.grey[600],
                          ),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 25),

                  GestureDetector(
                    onTap: isChecked && !isLoading
                        ? () async {
                            if (!formKey.currentState!.validate()) return;

                            final password = _passwordController.text.trim();
                            final confirmPassword = _confirmPasswordController
                                .text
                                .trim();

                            if (password != confirmPassword) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text("Passwords do not match"),
                                ),
                              );
                              return;
                            }

                            setState(() => isLoading = true);

                            try {
                              final result = await UserSignUpServices()
                                  .signUpApi(
                                    name: _nameController.text.trim(),
                                    phone: _phoneController.text.trim(),
                                    email: _emailController.text.trim(),
                                    password: password,
                                  );

                              if (result.success) {
                                final token = result.accessToken;
                                final user = result.user;
                                if (token != null &&
                                    token.isNotEmpty &&
                                    user != null) {
                                  await context
                                      .read<AuthSessionProvider>()
                                      .setSession(
                                        accessToken: token,
                                        user: user,
                                      );
                                }
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text(
                                      'Account created successfully',
                                    ),
                                  ),
                                );

                                log(result.accessToken ?? '');
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) =>
                                        const UserHomeScreen(),
                                  ),
                                );
                              } else {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(
                                    content: Text(
                                      result.message ?? "Error occurred",
                                    ),
                                  ),
                                );
                              }
                            } catch (e) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(content: Text('Sign up failed: $e')),
                              );
                            }

                            setState(() => isLoading = false);
                          }
                        : null,
                    child: Container(
                      width: double.infinity,
                      height: 50,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(12),
                        color: const Color(
                          0xFF1A1A2E,
                        ).withOpacity(isChecked ? 1 : 0.5),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.15),
                            blurRadius: 10,
                            offset: const Offset(0, 4),
                          ),
                        ],
                      ),
                      child: Center(
                        child: isLoading
                            ? const SizedBox(
                                width: 20,
                                height: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  valueColor: AlwaysStoppedAnimation<Color>(
                                    Colors.white,
                                  ),
                                ),
                              )
                            : Text(
                                "Create Account",
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
    );
  }
}
