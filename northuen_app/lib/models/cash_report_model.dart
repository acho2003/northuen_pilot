class DriverCashRow {
  DriverCashRow({required this.driverName, required this.collected, required this.pending});

  final String driverName;
  final num collected;
  final num pending;

  factory DriverCashRow.fromJson(Map<String, dynamic> json) => DriverCashRow(
        driverName: json['driverName'],
        collected: json['collected'],
        pending: json['pending'],
      );
}

class CashReport {
  CashReport({
    required this.totalCollected,
    required this.pendingPayments,
    required this.pendingDriverSettlement,
    required this.paidDriverSettlement,
    required this.paidCount,
    required this.pendingCount,
    required this.drivers,
  });

  final num totalCollected;
  final num pendingPayments;
  final num pendingDriverSettlement;
  final num paidDriverSettlement;
  final int paidCount;
  final int pendingCount;
  final List<DriverCashRow> drivers;

  factory CashReport.fromJson(Map<String, dynamic> json) => CashReport(
        totalCollected: json['totalCollected'],
        pendingPayments: json['pendingPayments'],
        pendingDriverSettlement: json['pendingDriverSettlement'] ?? 0,
        paidDriverSettlement: json['paidDriverSettlement'] ?? 0,
        paidCount: json['paidCount'],
        pendingCount: json['pendingCount'],
        drivers: ((json['drivers'] ?? []) as List).map((row) => DriverCashRow.fromJson(row)).toList(),
      );
}
