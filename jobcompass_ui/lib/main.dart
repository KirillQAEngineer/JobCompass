import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/semantics.dart';

import 'app/app.dart';

SemanticsHandle? e2eSemanticsHandle;

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  const enableE2eSemantics = bool.fromEnvironment('E2E_SEMANTICS_ENABLED');
  if (enableE2eSemantics) {
    e2eSemanticsHandle = SemanticsBinding.instance.ensureSemantics();
  }
  runApp(const ProviderScope(child: JobCompassApp()));
}
