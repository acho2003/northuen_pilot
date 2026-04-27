class Driver {
  Driver({
    required this.id,
    required this.userId,
    required this.fullName,
    required this.phone,
    required this.vehicleType,
    this.licenseNumber,
    required this.available,
    this.currentLatitude,
    this.currentLongitude,
  });

  final String id;
  final String userId;
  final String fullName;
  final String phone;
  final String vehicleType;
  final String? licenseNumber;
  final bool available;
  final num? currentLatitude;
  final num? currentLongitude;

  factory Driver.fromJson(Map<String, dynamic> json) => Driver(
        id: json['id'],
        userId: json['userId'],
        fullName: json['fullName'],
        phone: json['phone'],
        vehicleType: json['vehicleType'],
        licenseNumber: json['licenseNumber'],
        available: json['available'] ?? false,
        currentLatitude: json['currentLatitude'],
        currentLongitude: json['currentLongitude'],
      );
}
