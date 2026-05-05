import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:talat_sanaye3/models/Login_model.dart';

class AuthSessionProvider extends ChangeNotifier {
  static const _accessTokenKey = 'auth_access_token';
  static const _refreshTokenKey = 'auth_refresh_token';
  static const _tokenExpiryKey = 'auth_token_expiry';
  static const _userKey = 'auth_user';
  static const _baseUrl = 'https://3-sanay3-production.up.railway.app';

  String? _accessToken;
  String? _refreshToken;
  DateTime? _tokenExpiry;
  User? _user;
  bool _isLoading = true;

  String? get accessToken => _accessToken;
  String? get refreshToken => _refreshToken;
  User? get user => _user;
  bool get isLoading => _isLoading;
  bool get isLoggedIn => _accessToken != null && _accessToken!.isNotEmpty;

  bool get isTokenExpired {
    if (_tokenExpiry == null) return true;
    // نعتبره منتهي لو باقي عليه أقل من دقيقة
    return DateTime.now().isAfter(
      _tokenExpiry!.subtract(const Duration(minutes: 1)),
    );
  }

  AuthSessionProvider() {
    loadSession();
  }

  Future<void> loadSession() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString(_accessTokenKey);
      final refresh = prefs.getString(_refreshTokenKey);
      final expiryStr = prefs.getString(_tokenExpiryKey);
      final userJson = prefs.getString(_userKey);

      if (token != null && token.isNotEmpty && userJson != null) {
        _accessToken = token;
        _refreshToken = refresh;
        _tokenExpiry = expiryStr != null ? DateTime.tryParse(expiryStr) : null;
        _user = User.fromJson(jsonDecode(userJson));
      }
    } catch (_) {
      _accessToken = null;
      _refreshToken = null;
      _tokenExpiry = null;
      _user = null;
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<void> setSession({
    required String accessToken,
    required User user,
    String? refreshToken,
    int expiresIn = 900, // default 15 دقيقة
  }) async {
    _accessToken = accessToken;
    _refreshToken = refreshToken;
    _tokenExpiry = DateTime.now().add(Duration(seconds: expiresIn));
    _user = user;

    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_accessTokenKey, accessToken);
      await prefs.setString(_userKey, jsonEncode(user.toJson()));
      if (refreshToken != null) {
        await prefs.setString(_refreshTokenKey, refreshToken);
      }
      await prefs.setString(_tokenExpiryKey, _tokenExpiry!.toIso8601String());
    } catch (_) {}

    notifyListeners();
  }

  /// ✅ بيعمل refresh تلقائي لو الـ token منتهي
  /// بيرجع الـ access token الصالح
  Future<String?> getValidToken() async {
    if (!isTokenExpired) return _accessToken;

    log('Token expired, refreshing...');
    final refreshed = await _doRefresh();
    return refreshed ? _accessToken : null;
  }

  Future<bool> _doRefresh() async {
    if (_refreshToken == null || _refreshToken!.isEmpty) return false;

    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/auth/refresh'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'refreshToken': _refreshToken}),
      );

      log('refresh status: ${response.statusCode}');
      log('refresh body: ${response.body}');

      if ((response.statusCode == 200 || response.statusCode == 201) &&
          response.body.trim().isNotEmpty) {
        final data = jsonDecode(response.body);
        final newToken = data['accessToken'] as String?;
        final newRefresh = data['refreshToken'] as String?;
        final expiresIn = data['expiresIn'] as int? ?? 900;

        if (newToken != null && newToken.isNotEmpty) {
          await setSession(
            accessToken: newToken,
            refreshToken: newRefresh ?? _refreshToken,
            user: _user!,
            expiresIn: expiresIn,
          );
          return true;
        }
      }
    } catch (e) {
      log('Refresh failed: $e');
    }

    return false;
  }

  Future<void> clearSession() async {
    _accessToken = null;
    _refreshToken = null;
    _tokenExpiry = null;
    _user = null;

    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_accessTokenKey);
      await prefs.remove(_refreshTokenKey);
      await prefs.remove(_tokenExpiryKey);
      await prefs.remove(_userKey);
    } catch (_) {}

    notifyListeners();
  }
}
