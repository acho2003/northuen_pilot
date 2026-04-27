import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class MoneyText extends StatelessWidget {
  MoneyText(this.amount, {super.key, this.style});

  final num amount;
  final TextStyle? style;
  final _format = NumberFormat.currency(symbol: 'Nu. ', decimalDigits: 2);

  @override
  Widget build(BuildContext context) => Text(_format.format(amount), style: style);
}
