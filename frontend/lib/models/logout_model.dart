class LogoutResponse {
  final bool data;
  final String message;
  final bool success;
  final String timestamp;

  const LogoutResponse({
    required this.data,
    required this.message,
    required this.success,
    required this.timestamp,
  });

  factory LogoutResponse.fromJson(Map<String, dynamic> json) {
    return LogoutResponse(
      data: json['data'] ?? false,
      message: json['message'] ?? '',
      success: json['success'] ?? false,
      timestamp: json['timestamp'] ?? '',
    );
  }
}