import 'package:flutter/material.dart';

class StatusChip extends StatelessWidget {
  const StatusChip(this.label, {super.key, this.color});

  final String label;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final chipColor = color ?? Theme.of(context).colorScheme.primary;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(color: chipColor.withValues(alpha: .12), borderRadius: BorderRadius.circular(20)),
      child: Text(label.replaceAll('_', ' '), style: TextStyle(color: chipColor, fontWeight: FontWeight.w700, fontSize: 12)),
    );
  }
}
