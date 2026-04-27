class Product {
  Product({
    required this.id,
    required this.vendorId,
    required this.name,
    required this.description,
    required this.price,
    required this.category,
    required this.available,
    this.imageUrl,
  });

  final String id;
  final String vendorId;
  final String name;
  final String description;
  final num price;
  final String category;
  final bool available;
  final String? imageUrl;

  factory Product.fromJson(Map<String, dynamic> json) => Product(
        id: json['id'],
        vendorId: json['vendorId'],
        name: json['name'],
        description: json['description'],
        price: json['price'],
        category: json['category'],
        available: json['available'] ?? true,
        imageUrl: json['imageUrl'],
      );
}
