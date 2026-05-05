import 'package:talat_sanaye3/models/Login_model.dart';

class AuthResponse {
  final String? accessToken;
  final String? refreshToken;
  final int? expiresIn;
  final User? user;

  final String? message;
  final int? status;
  final String? timestamp;

  final bool success;

  AuthResponse({
    this.accessToken,
    this.refreshToken,
    this.expiresIn,
    this.user,
    this.message,
    this.status,
    this.timestamp,
    required this.success,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    if (json.containsKey('accessToken')) {
      return AuthResponse(
        success: true,
        accessToken: json['accessToken'] ?? '',
        refreshToken: json['refreshToken'] ?? '',
        expiresIn: json['expiresIn'] ?? 0,
        user: json['user'] != null ? User.fromJson(json['user']) : null,
      );
    }

    // error case
    final message = json['message'];
    return AuthResponse(
      success: false,
      message: message is List
          ? (message).join(', ')
          : message?.toString(),
      status: json['status'],
      timestamp: json['timestamp'],
    );
  }
}
