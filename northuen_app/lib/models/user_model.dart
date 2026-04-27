class AppUser {
  AppUser({
    required this.id,
    required this.fullName,
    required this.email,
    required this.phone,
    required this.role,
    required this.active,
  });

  final String id;
  final String fullName;
  final String email;
  final String phone;
  final String role;
  final bool active;

  factory AppUser.fromJson(Map<String, dynamic> json) => AppUser(
        id: json['id'],
        fullName: json['fullName'],
        email: json['email'],
        phone: json['phone'],
        role: json['role'],
        active: json['active'] ?? true,
      );
}
