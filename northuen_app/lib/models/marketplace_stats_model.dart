class MarketplaceStats {
  MarketplaceStats({required this.openVendors, required this.availableProducts, required this.activeOrders});

  final int openVendors;
  final int availableProducts;
  final int activeOrders;

  factory MarketplaceStats.fromJson(Map<String, dynamic> json) => MarketplaceStats(
        openVendors: json['openVendors'],
        availableProducts: json['availableProducts'],
        activeOrders: json['activeOrders'],
      );
}
