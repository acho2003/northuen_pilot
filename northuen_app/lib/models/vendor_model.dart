class Vendor {
  Vendor({
    required this.id,
    required this.name,
    required this.category,
    required this.description,
    required this.address,
    this.imageUrl,
    this.latitude,
    this.longitude,
    required this.open,
  });

  final String id;
  final String name;
  final String category;
  final String description;
  final String address;
  final String? imageUrl;
  final num? latitude;
  final num? longitude;
  final bool open;

  factory Vendor.fromJson(Map<String, dynamic> json) => Vendor(
        id: json['id'],
        name: json['name'],
        category: json['category'],
        description: json['description'],
        address: json['address'],
        imageUrl: json['imageUrl'],
        latitude: json['latitude'],
        longitude: json['longitude'],
        open: json['open'] ?? true,
      );
}
