import 'dart:convert';
import 'dart:developer';

import 'package:http/http.dart' as http;
import 'package:talat_sanaye3/models/customer_models.dart';
import 'package:talat_sanaye3/models/request_model.dart';

class ServiceRequestApi {
  static const String baseUrl = 'https://3-sanay3-production.up.railway.app';

  Future<CreatedServiceRequest> createRequest({
    required String token,
    required String serviceType,
    required String title,
    required String description,
    required String address,
    required double latitude,
    required double longitude,
  }) async {
    final payload = {
      'serviceType': serviceType,
      'title': title,
      'description': description,
      'address': address,
      'latitude': latitude,
      'longitude': longitude,
    };

    log('createRequest payload: ${jsonEncode(payload)}');
    log('token: $token');

    final response = await http.post(
      Uri.parse('$baseUrl/requests'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
      body: jsonEncode(payload),
    );

    log('createRequest status: ${response.statusCode}');
    log('createRequest body: "${response.body}"');
    log(
      'serviceType: $serviceType, title: $title, description: $description, address: $address, latitude: $latitude, longitude: $longitude',
    );

    // ✅ لو الـ body فاضي خالص
    if (response.body.trim().isEmpty) {
      if (response.statusCode == 200 || response.statusCode == 201) {
        return const CreatedServiceRequest(
          id: 0,
          serviceType: '',
          title: '',
          description: '',
          address: '',
          status: '',
        );
      }
      throw Exception('Server returned ${response.statusCode} with empty body');
    }

    final data = jsonDecode(response.body);

    if (response.statusCode == 200 || response.statusCode == 201) {
      if (data is Map<String, dynamic>) {
        final responsePayload = data.containsKey('data') && data['data'] is Map
            ? data['data'] as Map<String, dynamic>
            : data;
        return CreatedServiceRequest.fromJson(responsePayload);
      }
      throw Exception('Unexpected response format: $data');
    }

    // استخراج رسالة الخطأ بأمان
    String errorMessage = response.body;
    if (data is Map<String, dynamic>) {
      final msg = data['message'];
      errorMessage = msg?.toString() ?? response.body;
    }

    throw Exception(errorMessage);
  }

  Future<void> acceptOffer({
    required String token,
    required int requestId,
    required int offerId,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/requests/$requestId/accept-offer/$offerId'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    log('acceptOffer status: ${response.statusCode}');
    log('acceptOffer body: "${response.body}"');

    if (response.statusCode < 200 || response.statusCode >= 300) {
      String errorMessage =
          'Server returned ${response.statusCode} with empty body';

      if (response.body.trim().isNotEmpty) {
        try {
          final data = jsonDecode(response.body);
          if (data is Map<String, dynamic>) {
            final msg = data['message'];
            errorMessage = msg?.toString() ?? response.body;
          }
        } catch (_) {
          errorMessage = response.body;
        }
      }

      throw Exception(errorMessage);
    }
  }

  Future<List<OfferModel>> getOffersForRequest({
    required String token,
    required int requestId,
    int page = 0,
    int size = 10,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/offers/request/$requestId?page=$page&size=$size'),
      headers: {'Authorization': 'Bearer $token'},
    );

    final decoded = _decodeResponse(response);
    _throwIfBad(response, decoded);
    return listPayload(decoded).map(OfferModel.fromJson).toList();
  }

  Future<List<CustomerRequestModel>> getMyRequests({
    required String token,
    int page = 0,
    int size = 10,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/requests/my-requests?page=$page&size=$size'),
      headers: {'Authorization': 'Bearer $token'},
    );

    final decoded = _decodeResponse(response);
    _throwIfBad(response, decoded);
    return listPayload(decoded).map(CustomerRequestModel.fromJson).toList();
  }

  Future<CustomerRequestModel> getRequestDetails({
    required String token,
    required int requestId,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/requests/$requestId'),
      headers: {'Authorization': 'Bearer $token'},
    );

    final decoded = _decodeResponse(response);
    _throwIfBad(response, decoded);
    return CustomerRequestModel.fromJson(mapPayload(decoded) ?? {});
  }

  Future<void> cancelRequest({
    required String token,
    required int requestId,
  }) async {
    await _postAction(token: token, path: '/requests/$requestId/cancel');
  }

  Future<void> startService({
    required String token,
    required int requestId,
  }) async {
    await _postAction(token: token, path: '/requests/$requestId/start');
  }

  Future<void> completeService({
    required String token,
    required int requestId,
  }) async {
    await _postAction(token: token, path: '/requests/$requestId/complete');
  }

  Future<void> _postAction({
    required String token,
    required String path,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl$path'),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decodeResponse(response);
    _throwIfBad(response, decoded);
  }

  dynamic _decodeResponse(http.Response response) {
    if (response.body.trim().isEmpty) return <String, dynamic>{};
    return jsonDecode(response.body);
  }

  void _throwIfBad(http.Response response, dynamic decoded) {
    if (response.statusCode >= 200 && response.statusCode < 300) return;
    final payload = decoded is Map<String, dynamic> ? decoded : null;
    throw Exception(payload?['message']?.toString() ?? response.body);
  }
}
