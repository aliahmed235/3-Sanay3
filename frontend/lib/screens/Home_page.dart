import 'package:flutter/material.dart';

class Homepage extends StatelessWidget {
  const Homepage({super.key});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 200,
      height: 50,
      child: ElevatedButton(
        onPressed: () {
          // هنا ممكن تضيفي الكود اللي عايزة تنفذيه لما يتضغط الزر
        },
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.blue, // لون الخلفية
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(30), // نصف قطر الحواف
          ),
        ),
        child: const Text(
          'تسجيل الدخول',
          style: TextStyle(
            color: Colors.white, // لون النص
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
