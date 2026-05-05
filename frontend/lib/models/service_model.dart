// lib/models/service_model.dart
class Service {
  final String id;
  final String name;
  final String icon;
  final String description;

  Service({
    required this.id,
    required this.name,
    required this.icon,
    required this.description,
  });
}

class ServiceProvider {
  final String id;
  final String name;
  final String profileImage;
  final double rating;
  final int reviews;
  final String category;
  final double latitude;
  final double longitude;

  ServiceProvider({
    required this.id,
    required this.name,
    required this.profileImage,
    required this.rating,
    required this.reviews,
    required this.category,
    required this.latitude,
    required this.longitude,
  });
}