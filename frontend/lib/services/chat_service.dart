import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:talat_sanaye3/models/customer_models.dart';
import 'package:talat_sanaye3/services/request_service.dart';

class ChatApi {
  static const String baseUrl = ServiceRequestApi.baseUrl;

  Future<List<ChatRoomModel>> getMyChatRooms({required String token}) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/chats'),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
    return listPayload(decoded).map(ChatRoomModel.fromJson).toList();
  }

  Future<ChatRoomModel> getChatRoomByRequest({
    required String token,
    required int requestId,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/chats/request/$requestId'),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
    return ChatRoomModel.fromJson(mapPayload(decoded) ?? {});
  }

  Future<ChatRoomModel> getChatRoomDetails({
    required String token,
    required int chatRoomId,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/chats/$chatRoomId'),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
    return ChatRoomModel.fromJson(mapPayload(decoded) ?? {});
  }

  Future<List<ChatMessageModel>> getMessages({
    required String token,
    required int chatRoomId,
    int page = 0,
    int size = 20,
  }) async {
    final response = await http.get(
      Uri.parse(
        '$baseUrl/api/chats/$chatRoomId/messages?page=$page&size=$size',
      ),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
    return listPayload(decoded).map(ChatMessageModel.fromJson).toList();
  }

  Future<ChatMessageModel?> sendTextMessage({
    required String token,
    required int chatRoomId,
    required String message,
  }) async {
    return _sendMessage(
      token: token,
      chatRoomId: chatRoomId,
      endpoint: 'text',
      body: {'message': message},
    );
  }

  Future<ChatMessageModel?> sendLocationMessage({
    required String token,
    required int chatRoomId,
    required double latitude,
    required double longitude,
  }) async {
    return _sendMessage(
      token: token,
      chatRoomId: chatRoomId,
      endpoint: 'location',
      body: {'latitude': latitude, 'longitude': longitude},
    );
  }

  Future<ChatMessageModel?> sendPhotoMessage({
    required String token,
    required int chatRoomId,
    required String photoUrl,
  }) async {
    return _sendMessage(
      token: token,
      chatRoomId: chatRoomId,
      endpoint: 'photo',
      body: {'photoUrl': photoUrl},
    );
  }

  Future<ChatMessageModel?> getLatestMessage({
    required String token,
    required int chatRoomId,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/chats/$chatRoomId/latest-message'),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
    final payload = mapPayload(decoded);
    return payload == null ? null : ChatMessageModel.fromJson(payload);
  }

  Future<int> getMessageCount({
    required String token,
    required int chatRoomId,
  }) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/chats/$chatRoomId/message-count'),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
    final payload = mapPayload(decoded);
    final value = payload?['count'] ?? payload?['messageCount'] ?? decoded;
    return int.tryParse(value.toString()) ?? 0;
  }

  Future<void> deleteMessage({
    required String token,
    required int chatRoomId,
    required int messageId,
  }) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/api/chats/$chatRoomId/message/$messageId'),
      headers: {'Authorization': 'Bearer $token'},
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
  }

  Future<ChatMessageModel?> _sendMessage({
    required String token,
    required int chatRoomId,
    required String endpoint,
    required Map<String, dynamic> body,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/chats/$chatRoomId/message/$endpoint'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
      body: jsonEncode(body),
    );
    final decoded = _decode(response);
    _throwIfBad(response, decoded);
    final payload = mapPayload(decoded);
    return payload == null ? null : ChatMessageModel.fromJson(payload);
  }

  dynamic _decode(http.Response response) {
    if (response.body.trim().isEmpty) return <String, dynamic>{};
    return jsonDecode(response.body);
  }

  void _throwIfBad(http.Response response, dynamic decoded) {
    if (response.statusCode >= 200 && response.statusCode < 300) return;
    final payload = decoded is Map<String, dynamic> ? decoded : null;
    throw Exception(payload?['message']?.toString() ?? response.body);
  }
}
