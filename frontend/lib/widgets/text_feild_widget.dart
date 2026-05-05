// ignore_for_file: prefer_const_constructors_in_immutables, must_be_immutable, deprecated_member_use
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/theme_provider.dart';

class TextFeildWidget extends StatefulWidget {
  TextFeildWidget({
    super.key,
    required this.icon,
    required this.hintText,
    this.obscureText = false,
    this.onChanged,
    this.controller,
    this.keyboardType = TextInputType.text,
  });

  final IconData icon;
  final String hintText;
  bool obscureText;
  final Function(String)? onChanged;
  final TextEditingController? controller;
  final TextInputType keyboardType;

  @override
  State<TextFeildWidget> createState() => _TextFeildWidgetState();
}

class _TextFeildWidgetState extends State<TextFeildWidget> {
  late bool _obscureText;

  @override
  void initState() {
    super.initState();
    _obscureText = widget.obscureText;
  }

  String? _validateInput(String? value) {
    if (value == null || value.isEmpty) {
      return 'This field is required';
    }

    // Phone Number validation (11 digits)
    if (widget.hintText == 'Phone Number' || widget.hintText == 'Phone') {
      if (!RegExp(r'^[0-9]{11}$').hasMatch(value)) {
        return 'Phone must be 11 digits';
      }
    }

    // National ID validation (14 digits)
    if (widget.hintText == 'National ID') {
      if (!RegExp(r'^[0-9]{14}$').hasMatch(value)) {
        return 'National ID must be 14 digits';
      }
    }

    // Email validation
    if (widget.hintText == 'Email') {
      if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value)) {
        return 'Please enter a valid email';
      }
    }

    // Password validation (minimum 6 characters)
    if (widget.hintText == 'Password' ||
        widget.hintText == 'Confirm Password') {
      if (value.length < 6) {
        return 'Password must be at least 6 characters';
      }
    }

    // Experience Years validation (1-2 digits)
    if (widget.hintText == 'Experience Years') {
      if (!RegExp(r'^[0-9]{1,2}$').hasMatch(value)) {
        return 'Enter a valid experience (1-99 years)';
      }
    }

    // Hourly Rate validation (positive number)
    if (widget.hintText == 'Hourly Rate (SAR)') {
      if (!RegExp(r'^[0-9]+(\.[0-9]{1,2})?$').hasMatch(value)) {
        return 'Enter a valid hourly rate';
      }
    }

    // Name validation (letters and spaces only)
    if (widget.hintText == 'Name') {
      if (!RegExp(r'^[a-zA-Z\s]+$').hasMatch(value)) {
        return 'Name can only contain letters';
      }
      if (value.length < 3) {
        return 'Name must be at least 3 characters';
      }
    }

    // Address validation
    if (widget.hintText == 'Address') {
      if (value.length < 5) {
        return 'Address must be at least 5 characters';
      }
    }

    return null;
  }

  TextInputType _getKeyboardType() {
    if (widget.keyboardType != TextInputType.text) {
      return widget.keyboardType;
    }

    if (widget.hintText == 'Phone Number' ||
        widget.hintText == 'Phone' ||
        widget.hintText == 'National ID' ||
        widget.hintText == 'Experience Years' ||
        widget.hintText == 'Hourly Rate (SAR)') {
      return TextInputType.number;
    }

    if (widget.hintText == 'Email') {
      return TextInputType.emailAddress;
    }

    return TextInputType.text;
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Provider.of<ThemeProvider>(context).isDarkMode;

    return TextFormField(
      controller: widget.controller,
      validator: _validateInput,
      onChanged: widget.onChanged,
      obscureText: _obscureText,
      keyboardType: _getKeyboardType(),
      style: TextStyle(
        color: isDark ? Colors.white : Colors.black,
        fontSize: 14,
        fontWeight: FontWeight.w500,
      ),
      autovalidateMode: AutovalidateMode.onUserInteraction,
      inputFormatters: widget.hintText == 'Name'
          ? [FilteringTextInputFormatter.allow(RegExp(r'[a-zA-Z\s]'))]
          : widget.hintText == 'Phone Number' ||
                widget.hintText == 'Phone' ||
                widget.hintText == 'National ID' ||
                widget.hintText == 'Experience Years'
          ? [FilteringTextInputFormatter.allow(RegExp(r'[0-9]'))]
          : widget.hintText == 'Hourly Rate (SAR)'
          ? [FilteringTextInputFormatter.allow(RegExp(r'[0-9.]'))]
          : null,
      maxLength: widget.hintText == 'Phone Number' || widget.hintText == 'Phone'
          ? 11
          : widget.hintText == 'National ID'
          ? 14
          : widget.hintText == 'Experience Years'
          ? 2
          : null,
      decoration: InputDecoration(
        filled: true,
        fillColor: isDark
            ? Colors.grey[800]!.withOpacity(0.3)
            : Colors.grey[100]!.withOpacity(0.6),
        prefixIcon: Icon(
          widget.icon,
          color: isDark ? Colors.grey[600] : Colors.grey[700],
          size: 20,
        ),
        suffixIcon: widget.obscureText
            ? GestureDetector(
                onTap: () {
                  setState(() {
                    _obscureText = !_obscureText;
                  });
                },
                child: Icon(
                  _obscureText
                      ? Icons.visibility_off_outlined
                      : Icons.visibility_outlined,
                  color: isDark ? Colors.grey[600] : Colors.grey[700],
                  size: 20,
                ),
              )
            : null,
        hintText: widget.hintText,
        hintStyle: TextStyle(
          color: isDark ? Colors.grey[600] : Colors.grey[600],
          fontSize: 14,
          fontWeight: FontWeight.w400,
        ),
        contentPadding: const EdgeInsets.symmetric(
          vertical: 16,
          horizontal: 16,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: BorderSide(
            color: const Color(0xFF1A1A2E).withOpacity(0.3),
            width: 1,
          ),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: Colors.redAccent, width: 1),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: Colors.redAccent, width: 1),
        ),
        errorStyle: const TextStyle(
          color: Colors.redAccent,
          fontSize: 11,
          height: 1,
          fontWeight: FontWeight.w400,
        ),
        counterText: '',
      ),
    );
  }
}
