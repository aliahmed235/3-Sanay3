class LoginModel {
  final String accessToken;
  final String refreshToken;
  final int expiresIn;
  final User user;

  LoginModel({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
    required this.user,
  });

  factory LoginModel.fromJson(Map<String, dynamic> json) {
    return LoginModel(
      accessToken: json['accessToken'] ?? '',
      refreshToken: json['refreshToken'] ?? '',
      expiresIn: json['expiresIn'] ?? 0,
      user: json['user'] != null
          ? User.fromJson(json['user'])
          : User(
              id: 0,
              name: '',
              email: '',
              phone: '',
              roles: [],
              isActive: false,
            ),
    );
  }
}

class User {
  final int id;
  final String name;
  final String email;
  final String phone;
  final List<String> roles;
  final bool isActive;

  User({
    required this.id,
    required this.name,
    required this.email,
    required this.phone,
    required this.roles,
    required this.isActive,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      email: json['email'] ?? '',
      phone: json['phone'] ?? '',
      roles: json['roles'] != null ? List<String>.from(json['roles']) : [],
      isActive: json['isActive'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'phone': phone,
      'roles': roles,
      'isActive': isActive,
    };
  }
}
