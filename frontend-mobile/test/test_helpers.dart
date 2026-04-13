import 'dart:typed_data';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// Fournit un [AssetBundle] factice retournant un PNG 1x1 pour tout asset.
class FakeAssetBundle extends CachingAssetBundle {
  final Uint8List _bytes;
  FakeAssetBundle(List<int> bytes) : _bytes = Uint8List.fromList(bytes);

  @override
  Future<ByteData> load(String key) async {
    return ByteData.view(_bytes.buffer);
  }

  @override
  Future<String> loadString(String key, {bool cache = true}) async {
    return '';
  }
}

/// Retourne un PNG 1x1 transparent sous forme de bytes.
Uint8List onePixelPng() {
  // Standard 1x1 transparent PNG used in many examples/tests
  // Use a canonical base64 1x1 transparent PNG to avoid formatting issues.
  const b64 =
      'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8Xw8A'
      'An8B9Q1f2wAAAABJRU5ErkJggg==';
  return base64.decode(b64);
}

/// Enveloppe [child] avec un [DefaultAssetBundle] utilisant le bundle factice.
Widget wrapWithFakeAssets(Widget child) {
  final bundle = FakeAssetBundle(onePixelPng());
  return DefaultAssetBundle(bundle: bundle, child: child);
}
