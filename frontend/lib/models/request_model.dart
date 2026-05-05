
class CreatedServiceRequest {
  final int id;
  final String serviceType;
  final String title;
  final String description;
  final String address;
  final String status;
  final DateTime? createdAt;

  const CreatedServiceRequest({
    required this.id,
    required this.serviceType,
    required this.title,
    required this.description,
    required this.address,
    required this.status,
    this.createdAt,
  });

  factory CreatedServiceRequest.fromJson(Map<String, dynamic> json) {
    return CreatedServiceRequest(
      id: json['id'] ?? 0,
      serviceType: json['serviceType'] ?? '',
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      address: json['address'] ?? '',
      status: json['status'] ?? '',
      createdAt: DateTime.tryParse(json['createdAt'] ?? ''),
    );
  }
}