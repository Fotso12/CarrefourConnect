// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:carrefourconnect_mobile/splash_screen.dart';

import 'test_helpers.dart';

class TestApp extends StatelessWidget {
  const TestApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CarrefourConnect',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF034D92)),
        useMaterial3: true,
        fontFamily: 'Roboto',
      ),
      home: const SplashScreen(nextScreen: SizedBox(), testMode: true),
    );
  }
}

void main() {
  testWidgets('App initialization smoke test', (WidgetTester tester) async {
    // Capture framework errors with stack traces to make failures actionable
    FlutterErrorDetails? frameworkErrorDetails;
    final oldOnError = FlutterError.onError;
    FlutterError.onError = (FlutterErrorDetails details) {
      frameworkErrorDetails = details;
    };
    try {
      // Provide a fake asset bundle so Image.asset calls during the splash
      // do not fail in the test environment. The bundle returns a 1x1
      // transparent PNG for any asset key.
      // Build a small test app that uses the SplashScreen in testMode to
      // disable repeating animations.
      await tester.pumpWidget(wrapWithFakeAssets(const TestApp()));

      // La SplashScreen a plusieurs phases d'animation et des délais.
      // On attend un peu pour que le premier frame soit rendu correctement.
      // On pompe plusieurs fois pour avancer les timers. Après chaque pump
      // on vérifie s'il y a des exceptions capturées par l'environnement de test.
      // Advance enough time for the full splash sequence (≈6.4s) and then
      // settle animations. This prevents pending timers from remaining after
      // the test finishes.
      await tester.pump(const Duration(milliseconds: 7000));
      await tester.pumpAndSettle();
      var ex = tester.takeException();
      if (ex != null) {
        if (frameworkErrorDetails != null) {
          fail(
            'Exception after advancing timers: $ex\nFramework stack:\n${frameworkErrorDetails!.stack}',
          );
        }
        fail('Exception after advancing timers: $ex');
      }
    } finally {
      FlutterError.onError = oldOnError;
    }

    // Verify that our app starts and pumps the MaterialApp.
    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
