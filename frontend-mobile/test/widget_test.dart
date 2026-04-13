// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:carrefourconnect_mobile/main.dart';

import 'test_helpers.dart';

void main() {
  testWidgets('App initialization smoke test', (WidgetTester tester) async {
    // Provide a fake asset bundle so Image.asset calls during the splash
    // do not fail in the test environment. The bundle returns a 1x1
    // transparent PNG for any asset key.
    // Build our app wrapped with the shared fake asset bundle helper so
    // Image.asset calls during the splash do not fail in the test.
    await tester.pumpWidget(wrapWithFakeAssets(const MyApp()));

    // La SplashScreen a plusieurs phases d'animation et des délais.
    // On attend un peu pour que le premier frame soit rendu correctement.
    // On pompe plusieurs fois pour avancer les timers. Après chaque pump
    // on vérifie s'il y a des exceptions capturées par l'environnement de test.
    await tester.pump(const Duration(seconds: 1));
    var ex = tester.takeException();
    if (ex != null) fail('Exception after first pump: $ex');

    await tester.pump(const Duration(seconds: 1));
    ex = tester.takeException();
    if (ex != null) fail('Exception after second pump: $ex');

    await tester.pump(const Duration(seconds: 1));
    ex = tester.takeException();
    if (ex != null) fail('Exception after third pump: $ex');

    // Verify that our app starts and pumps the MaterialApp.
    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
