// ignore_for_file: non_constant_identifier_names

import 'dart:convert';
import 'dart:developer';
import 'package:http/http.dart' as http;
import 'package:talat_sanaye3/models/Login_model.dart';
import 'package:talat_sanaye3/models/logout_model.dart';
import 'package:talat_sanaye3/models/user_signup_model.dart';

class LoginService {
  Future<LoginModel> UserSignUp({
    required String email,
    required String password,
  }) async {
    final url = Uri.parse(
      'https://3-sanay3-production.up.railway.app/auth/login',
    );
    final response = await http.post(
      url,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );

    log('login status: ${response.statusCode}');
    log('login body: ${response.body}');

    final data = jsonDecode(response.body);

    if (response.statusCode == 200) {
      return LoginModel.fromJson(data);
    } else {
      throw Exception(response.body);
    }
  }
}

class UserSignUpServices {
  Future<AuthResponse> signUpApi({
    required String email,
    required String password,
    required String name,
    required String phone,
  }) async {
    final url = Uri.parse(
      'https://3-sanay3-production.up.railway.app/auth/register',
    );

    final response = await http.post(
      url,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        "name": name,
        "email": email,
        "phone": phone,
        "password": password,
      }),
    );

    final data = jsonDecode(response.body);

    if (response.statusCode == 200 || response.statusCode == 201) {
      return AuthResponse.fromJson(data);
    } else {
      return AuthResponse.fromJson(data);
    }
  }
}

class LogoutServise {
  static const String baseUrl = 'https://3-sanay3-production.up.railway.app';

  Future<LogoutResponse> logout({required String token}) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/logout'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    log('logout status: ${response.statusCode}');
    log('logout body: ${response.body}');

    final body = response.body.trim();

    if (response.statusCode == 200) {
      if (body.isEmpty) {
        throw Exception('Empty response from server');
      }
      final data = jsonDecode(body);
      if (data is Map<String, dynamic>) {
        return LogoutResponse.fromJson(data);
      }
      throw Exception('Unexpected response format');
    }

    if (body.isEmpty) {
      throw Exception(
        'Error ${response.statusCode}: ${response.reasonPhrase ?? "Logout failed"}',
      );
    }

    String errorMessage = body;
    try {
      final data = jsonDecode(body);
      if (data is Map<String, dynamic>) {
        errorMessage = data['message']?.toString() ?? body;
      }
    } catch (_) {}

    throw Exception(errorMessage);
  }
}
