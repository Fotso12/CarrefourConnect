import 'dart:typed_data';
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
  return Uint8List.fromList([
    137,
    80,
    78,
    71,
    13,
    10,
    26,
    10,
    0,
    0,
    0,
    13,
    73,
    72,
    68,
    82,
    0,
    0,
    0,
    1,
    0,
    0,
    0,
    1,
    8,
    6,
    0,
    0,
    0,
    31,
    21,
    196,
    137,
    0,
    0,
    0,
    12,
    73,
    68,
    65,
    84,
    8,
    153,
    99,
    0,
    1,
    0,
    0,
    5,
    0,
    1,
    14,
    190,
    83,
    0,
    0,
    0,
    0,
    73,
    69,
    78,
    68,
    174,
    66,
    96,
    130,
  ]);
}

/// Enveloppe [child] avec un [DefaultAssetBundle] utilisant le bundle factice.
Widget wrapWithFakeAssets(Widget child) {
  final bundle = FakeAssetBundle(onePixelPng());
  return DefaultAssetBundle(bundle: bundle, child: child);
}
