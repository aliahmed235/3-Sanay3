class OfferModel {
  final int id;
  final double? price;
  final String note;
  final String status;
  final DateTime? createdAt;
  final ProviderSummary? provider;

  const OfferModel({
    required this.id,
    this.price,
    required this.note,
    required this.status,
    this.createdAt,
    this.provider,
  });

  factory OfferModel.fromJson(Map<String, dynamic> json) {
    final providerJson = _mapFromAny(
      json['provider'] ?? json['serviceProvider'] ?? json['sanay3y'],
    );
    return OfferModel(
      id: _intOf(json['id'] ?? json['offerId']),
      price: _doubleOf(json['price'] ?? json['amount'] ?? json['totalPrice']),
      note: _stringOf(json['note'] ?? json['message'] ?? json['description']),
      status: _stringOf(json['status']),
      createdAt: _dateOf(json['createdAt']),
      provider: providerJson == null
          ? null
          : ProviderSummary.fromJson(providerJson),
    );
  }
}

class ProviderSummary {
  final int id;
  final String name;
  final String phone;
  final double? rating;
  final int? completedJobs;

  const ProviderSummary({
    required this.id,
    required this.name,
    required this.phone,
    this.rating,
    this.completedJobs,
  });

  factory ProviderSummary.fromJson(Map<String, dynamic> json) {
    return ProviderSummary(
      id: _intOf(json['id'] ?? json['providerId']),
      name: _stringOf(json['name'] ?? json['fullName']),
      phone: _stringOf(json['phone']),
      rating: _doubleOf(json['rating'] ?? json['averageRating']),
      completedJobs: _nullableIntOf(json['completedJobs'] ?? json['jobCount']),
    );
  }
}

class CustomerRequestModel {
  final int id;
  final String serviceType;
  final String title;
  final String description;
  final String address;
  final String status;
  final int offerCount;
  final double? rating;
  final ProviderSummary? acceptedProvider;
  final DateTime? createdAt;
  final DateTime? acceptedAt;
  final DateTime? startedAt;
  final DateTime? completedAt;

  const CustomerRequestModel({
    required this.id,
    required this.serviceType,
    required this.title,
    required this.description,
    required this.address,
    required this.status,
    required this.offerCount,
    this.rating,
    this.acceptedProvider,
    this.createdAt,
    this.acceptedAt,
    this.startedAt,
    this.completedAt,
  });

  factory CustomerRequestModel.fromJson(Map<String, dynamic> json) {
    final providerJson = _mapFromAny(
      json['acceptedProvider'] ?? json['provider'],
    );
    return CustomerRequestModel(
      id: _intOf(json['id'] ?? json['requestId']),
      serviceType: _stringOf(json['serviceType']),
      title: _stringOf(json['title']),
      description: _stringOf(json['description']),
      address: _stringOf(json['address']),
      status: _stringOf(json['status']),
      offerCount: _intOf(json['offerCount']),
      rating: _doubleOf(json['rating']),
      acceptedProvider: providerJson == null
          ? null
          : ProviderSummary.fromJson(providerJson),
      createdAt: _dateOf(json['createdAt']),
      acceptedAt: _dateOf(json['acceptedAt']),
      startedAt: _dateOf(json['startedAt']),
      completedAt: _dateOf(json['completedAt']),
    );
  }
}

class ChatRoomModel {
  final int id;
  final int? requestId;
  final String title;
  final ProviderSummary? provider;
  final ChatMessageModel? latestMessage;

  const ChatRoomModel({
    required this.id,
    this.requestId,
    required this.title,
    this.provider,
    this.latestMessage,
  });

  factory ChatRoomModel.fromJson(Map<String, dynamic> json) {
    final providerJson = _mapFromAny(
      json['provider'] ?? json['serviceProvider'],
    );
    final requestJson = _mapFromAny(json['request'] ?? json['serviceRequest']);
    final latestJson = _mapFromAny(json['latestMessage']);
    final provider = providerJson == null
        ? null
        : ProviderSummary.fromJson(providerJson);
    return ChatRoomModel(
      id: _intOf(json['id'] ?? json['chatRoomId']),
      requestId: _nullableIntOf(json['requestId'] ?? requestJson?['id']),
      title: _stringOf(
        json['title'] ?? requestJson?['title'] ?? provider?.name,
      ),
      provider: provider,
      latestMessage: latestJson == null
          ? null
          : ChatMessageModel.fromJson(latestJson),
    );
  }
}

class ChatMessageModel {
  final int id;
  final String message;
  final String type;
  final String senderName;
  final int? senderId;
  final bool fromCustomer;
  final double? latitude;
  final double? longitude;
  final String photoUrl;
  final DateTime? createdAt;

  const ChatMessageModel({
    required this.id,
    required this.message,
    required this.type,
    required this.senderName,
    this.senderId,
    required this.fromCustomer,
    this.latitude,
    this.longitude,
    required this.photoUrl,
    this.createdAt,
  });

  factory ChatMessageModel.fromJson(Map<String, dynamic> json) {
    final sender = _mapFromAny(json['sender']);
    final type = _stringOf(json['type'] ?? json['messageType']).toUpperCase();
    final role = _stringOf(sender?['role'] ?? json['senderRole']).toUpperCase();
    return ChatMessageModel(
      id: _intOf(json['id'] ?? json['messageId']),
      message: _stringOf(json['message'] ?? json['content'] ?? json['text']),
      type: type.isEmpty ? 'TEXT' : type,
      senderName: _stringOf(sender?['name'] ?? json['senderName']),
      senderId: _nullableIntOf(sender?['id'] ?? json['senderId']),
      fromCustomer:
          role.contains('CUSTOMER') ||
          _stringOf(json['senderType']).contains('CUSTOMER'),
      latitude: _doubleOf(json['latitude']),
      longitude: _doubleOf(json['longitude']),
      photoUrl: _stringOf(json['photoUrl'] ?? json['imageUrl']),
      createdAt: _dateOf(json['createdAt'] ?? json['sentAt']),
    );
  }
}

List<Map<String, dynamic>> listPayload(dynamic decoded) {
  if (decoded is List) {
    return decoded
        .whereType<Map>()
        .map((item) => Map<String, dynamic>.from(item))
        .toList();
  }
  if (decoded is Map<String, dynamic>) {
    final data = decoded['data'];
    if (data is List) return listPayload(data);
    if (data is Map<String, dynamic>) {
      final content = data['content'] ?? data['items'] ?? data['data'];
      if (content is List) return listPayload(content);
      return [data];
    }
    final content = decoded['content'] ?? decoded['items'];
    if (content is List) return listPayload(content);
  }
  return const [];
}

Map<String, dynamic>? mapPayload(dynamic decoded) {
  if (decoded is Map<String, dynamic>) {
    final data = decoded['data'];
    if (data is Map<String, dynamic>) return data;
    return decoded;
  }
  return null;
}

Map<String, dynamic>? _mapFromAny(dynamic value) {
  if (value is Map<String, dynamic>) return value;
  if (value is Map) return Map<String, dynamic>.from(value);
  return null;
}

String _stringOf(dynamic value) => value?.toString() ?? '';

int _intOf(dynamic value) => _nullableIntOf(value) ?? 0;

int? _nullableIntOf(dynamic value) {
  if (value is int) return value;
  if (value is num) return value.toInt();
  return int.tryParse(value?.toString() ?? '');
}

double? _doubleOf(dynamic value) {
  if (value is num) return value.toDouble();
  return double.tryParse(value?.toString() ?? '');
}

DateTime? _dateOf(dynamic value) => DateTime.tryParse(value?.toString() ?? '');
