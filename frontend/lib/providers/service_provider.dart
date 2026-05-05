// lib/providers/service_provider.dart
// ignore_for_file: prefer_final_fields

import 'package:flutter/material.dart';
import '../models/service_model.dart';

class ServiceProviderManager extends ChangeNotifier {
  List<Service> _services = [
    Service(
      id: '1',
      name: 'نجار',
      icon: '🪵',
      description: 'خدمات النجارة والأثاث',
    ),
    Service(
      id: '2',
      name: 'سباك',
      icon: '🚿',
      description: 'خدمات السباكة والتمديدات',
    ),
    Service(
      id: '3',
      name: 'كهربائي',
      icon: '⚡',
      description: 'خدمات الكهرباء والتركيبات',
    ),
  ];

  List<ServiceProvider> _serviceProviders = [
    ServiceProvider(
      id: '1',
      name: 'أحمد النجار',
      profileImage: 'assets/images/logo.png',
      rating: 4.8,
      reviews: 125,
      category: 'نجار',
      latitude: 24.7136,
      longitude: 46.6753,
    ),
    ServiceProvider(
      id: '2',
      name: 'محمد السباك',
      profileImage: 'assets/images/logo.png',
      rating: 4.5,
      reviews: 89,
      category: 'سباك',
      latitude: 24.7145,
      longitude: 46.6760,
    ),
    ServiceProvider(
      id: '3',
      name: 'علي الكهربائي',
      profileImage: 'assets/images/logo.png',
      rating: 4.9,
      reviews: 150,
      category: 'كهربائي',
      latitude: 24.7130,
      longitude: 46.6745,
    ),
  ];

  String? _selectedCategory;

  List<Service> get services => _services;
  List<ServiceProvider> get serviceProviders => _serviceProviders;
  String? get selectedCategory => _selectedCategory;

  List<ServiceProvider> getProvidersByCategory(String category) {
    return _serviceProviders
        .where((provider) => provider.category == category)
        .toList();
  }

  void selectCategory(String category) {
    _selectedCategory = category;
    notifyListeners();
  }

  void clearCategory() {
    _selectedCategory = null;
    notifyListeners();
  }
}
