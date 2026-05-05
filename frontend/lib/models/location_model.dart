// lib/models/location_model.dart
class Location {
  final double latitude;
  final double longitude;
  final String address;
  final String placeName;

  Location({
    required this.latitude,
    required this.longitude,
    required this.address,
    required this.placeName,
  });

  Map<String, dynamic> toJson() {
    return {
      'latitude': latitude,
      'longitude': longitude,
      'address': address,
      'placeName': placeName,
    };
  }
}
