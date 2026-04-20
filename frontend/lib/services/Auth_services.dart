import 'package:dio/dio.dart';

class ApiService {
  final Dio dio = Dio(
    BaseOptions(
      baseUrl: "https://3-sanay3-production.up.railway.app",
      headers: {"Content-Type": "application/json"},
    ),
  );

  Future<Map<String, dynamic>> login({
    required String email,
    required String password,
  }) async {
    final response=await dio.post(
      "/auth/login",
      data: {"email": email, "password": password},
    );
    return response.data;
  }
}
