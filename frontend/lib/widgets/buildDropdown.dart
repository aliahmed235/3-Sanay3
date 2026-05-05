// ignore_for_file: deprecated_member_use, must_be_immutable

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/theme_provider.dart';

class BuildDropdown extends StatefulWidget {
  BuildDropdown({super.key});
  String? selectedSpecialization;

  final List<String> specializations = ["Electrician", "Plumber", "Carpenter"];

  @override
  State<BuildDropdown> createState() => _BuildDropdownState();
}

class _BuildDropdownState extends State<BuildDropdown> {
  @override
  Widget build(BuildContext context) {
    final isDark = Provider.of<ThemeProvider>(context).isDarkMode;

    return Container(
      decoration: BoxDecoration(
        color: isDark
            ? Colors.grey[800]!.withOpacity(0.3)
            : Colors.grey[100]!.withOpacity(0.6),
        borderRadius: BorderRadius.circular(10),
      ),
      child: DropdownButton<String>(
        value: widget.selectedSpecialization,
        dropdownColor: isDark ? Colors.grey[800] : Colors.grey[200],
        hint: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text(
            "Select specialization",
            style: TextStyle(
              color: isDark ? Colors.grey[600] : Colors.grey[600],
              fontSize: 14,
            ),
          ),
        ),
        isExpanded: true,
        underline: const SizedBox(),
        iconEnabledColor: isDark ? Colors.grey[600] : Colors.grey[700],
        padding: const EdgeInsets.symmetric(horizontal: 12),
        items: widget.specializations.map((String value) {
          return DropdownMenuItem(
            value: value,
            child: Text(
              value,
              style: TextStyle(
                color: isDark ? Colors.white : Colors.black,
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          );
        }).toList(),
        onChanged: (val) {
          setState(() {
            widget.selectedSpecialization = val;
          });
        },
      ),
    );
  }
}
