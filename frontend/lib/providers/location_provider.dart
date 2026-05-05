// lib/providers/location_provider.dart
import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:geocoding/geocoding.dart' as geo;
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../models/location_model.dart';

class LocationProvider extends ChangeNotifier {
  Location? _currentLocation;
  bool _isLoadingLocation = false;
  String? _errorMessage;

  Location? get currentLocation => _currentLocation;
  bool get isLoadingLocation => _isLoadingLocation;
  String? get errorMessage => _errorMessage;

  Future<void> getCurrentLocation() async {
    _isLoadingLocation = true;
    _errorMessage = null;
    notifyListeners();

    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        _errorMessage = 'Location services are disabled.';
        _isLoadingLocation = false;
        notifyListeners();
        return;
      }

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          _errorMessage = 'Location permissions are denied';
          _isLoadingLocation = false;
          notifyListeners();
          return;
        }
      }

      Position? lastPosition = await Geolocator.getLastKnownPosition();

      if (lastPosition != null) {
        _currentLocation = Location(
          latitude: lastPosition.latitude,
          longitude: lastPosition.longitude,
          address: 'Current Location',
          placeName:
              '${lastPosition.latitude.toStringAsFixed(4)}, ${lastPosition.longitude.toStringAsFixed(4)}',
        );
        _isLoadingLocation = false;
        notifyListeners();

        _updateLocationInBackground();
      } else {
        Position position =
            await Geolocator.getCurrentPosition(
              desiredAccuracy: LocationAccuracy.high,
              timeLimit: const Duration(seconds: 20),
            ).timeout(
              const Duration(seconds: 25),
              onTimeout: () {
                throw Exception('Location request timed out.');
              },
            );

        _currentLocation = Location(
          latitude: position.latitude,
          longitude: position.longitude,
          address: 'Current Location',
          placeName:
              '${position.latitude.toStringAsFixed(4)}, ${position.longitude.toStringAsFixed(4)}',
        );
        _isLoadingLocation = false;
        notifyListeners();

        _tryGetPlaceName(position.latitude, position.longitude);
      }
    } catch (e) {
      _errorMessage = 'Error: ${e.toString()}';
      print('Location error: $e');
      _isLoadingLocation = false;
      notifyListeners();
    }
  }

  Future<void> _updateLocationInBackground() async {
    try {
      Position position =
          await Geolocator.getCurrentPosition(
            desiredAccuracy: LocationAccuracy.high,
            timeLimit: const Duration(seconds: 20),
          ).timeout(
            const Duration(seconds: 25),
            onTimeout: () => throw Exception('Location update timed out.'),
          );

      _currentLocation = Location(
        latitude: position.latitude,
        longitude: position.longitude,
        address: 'Current Location',
        placeName:
            '${position.latitude.toStringAsFixed(4)}, ${position.longitude.toStringAsFixed(4)}',
      );
      notifyListeners();

      _tryGetPlaceName(position.latitude, position.longitude);
    } catch (e) {
      print('Background location update failed: $e');
    }
  }

  Future<void> _tryGetPlaceName(double lat, double lng) async {
    try {
      List<geo.Placemark> placemarks = await geo
          .placemarkFromCoordinates(lat, lng)
          .timeout(const Duration(seconds: 5));

      if (placemarks.isNotEmpty) {
        final place = placemarks.first;
        final placeName = place.locality ?? place.country ?? 'Unknown Location';

        _currentLocation = Location(
          latitude: lat,
          longitude: lng,
          address: '${place.street ?? ''}, ${place.locality ?? place.country}',
          placeName: placeName,
        );
        notifyListeners();
      }
    } catch (e) {
      print('Reverse geocoding failed (non-critical): $e');
    }
  }

  Future<void> selectLocationFromCoordinates(double lat, double lng) async {
    _isLoadingLocation = true;
    _errorMessage = null;
    notifyListeners();

    _currentLocation = Location(
      latitude: lat,
      longitude: lng,
      address: 'Selected Location',
      placeName: '${lat.toStringAsFixed(4)}, ${lng.toStringAsFixed(4)}',
    );
    _isLoadingLocation = false;
    notifyListeners();

    await _tryGetPlaceName(lat, lng);
  }

  // ⭐ البحث مع دعم العربية
  Future<void> searchLocationByName(String placeName) async {
    _isLoadingLocation = true;
    _errorMessage = null;
    notifyListeners();

    try {
      // ⭐ ترميز النص العربي
      final encodedPlace = Uri.encodeComponent(placeName);

      final response = await http
          .get(
            Uri.parse(
              'https://nominatim.openstreetmap.org/search?q=$encodedPlace&format=json&limit=1&countrycodes=eg&accept-language=ar',
            ),
            headers: {
              'User-Agent':
                  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            },
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> results = json.decode(response.body);

        if (results.isNotEmpty) {
          final result = results.first;
          final lat = double.parse(result['lat']);
          final lng = double.parse(result['lon']);
          final displayName = result['display_name'] ?? placeName;

          _currentLocation = Location(
            latitude: lat,
            longitude: lng,
            address: displayName,
            placeName: displayName.split(',').first,
          );

          _errorMessage = null;
        } else {
          _errorMessage = 'لم يتم العثور على هذا المكان';
        }
      } else {
        _errorMessage = 'خطأ في البحث';
      }
    } catch (e) {
      _errorMessage = 'خطأ: $e';
      print('Search error: $e');
    }
    _isLoadingLocation = false;
    notifyListeners();
  }

  void setLocation(Location location) {
    _currentLocation = location;
    _errorMessage = null;
    _isLoadingLocation = false;
    notifyListeners();
  }
}
