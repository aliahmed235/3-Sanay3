// // ignore_for_file: use_build_context_synchronously, non_constant_identifier_names, deprecated_member_use

// import 'package:flutter/material.dart';
// import 'package:talat_sanaye3/models/Login_model.dart';
// import 'package:talat_sanaye3/screens/Home_page.dart';
// import 'package:talat_sanaye3/services/Auth_services.dart';
// import 'dart:developer' as developer;

// class ButtonWidget extends StatelessWidget {
// ButtonWidget({
//     super.key,
//     required this.buttonText,
//     this.emailController,
//     this.passwordController,
//     required this.onPressed,
//   });

//   final String buttonText;
//   final TextEditingController? emailController;
//   final TextEditingController? passwordController;
//   final VoidCallback onPressed;
//     final api = ApiService();

//   @override
//   Widget build(BuildContext context) {
//     return Container(
//       width: 220,
//       height: 55,
//       decoration: BoxDecoration(
//         borderRadius: BorderRadius.circular(35),
//         gradient: const LinearGradient(
//           colors: [Color(0xFF00BFA6), Color(0xFF003049)],
//           begin: Alignment.topLeft,
//           end: Alignment.bottomRight,
//         ),
//         boxShadow: [
//           BoxShadow(
//             color: Colors.black.withOpacity(0.5),
//             blurRadius: 20,
//             offset: const Offset(0, 8),
//           ),
//         ],
//       ),
//       child: ElevatedButton(
//         style: ElevatedButton.styleFrom(
//           backgroundColor: Colors.transparent,
//           shadowColor: Colors.transparent,
//           shape: RoundedRectangleBorder(
//             borderRadius: BorderRadius.circular(35),
//           ),
//         ),
//         onPressed: () async {
//   try {
//     final response = await api.login(
//       email: emailController?.text ?? '',
//       password: passwordController?.text ?? '',
//     );

//     final loginData = LoginResponse.fromJson(response);

//     developer.log("Token: ${loginData.accessToken}");
//     developer.log("User: ${loginData.user.name}");
//     Navigator.push(context,
//       MaterialPageRoute(builder: (context) =>Homepage() ),
//     );

//     // 🔥 هنا ممكن تنقلي لصفحة تانية
//   } catch (e) {
//     developer.log("Error: $e");
//     ScaffoldMessenger.of(context).showSnackBar(
//       const SnackBar(content: Text("Login failed. Please try again.")),
//     );
//   }
// },
//         child: Text(
//           buttonText,
//           style: const TextStyle(
//             fontSize: 18,
//             fontWeight: FontWeight.bold,
//             letterSpacing: 1,
//             color: Colors.white,
//           ),
//         ),
//       ),
//     );
//   }
// }
