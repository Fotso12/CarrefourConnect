import 'package:flutter/widgets.dart';
import 'package:flutter_map/flutter_map.dart';
// Import BaseTileProvider implementation to extend and avoid implementing
// the full TileProvider interface manually.
import 'package:flutter_map/src/layer/tile_layer/tile_provider/base_tile_provider.dart';
import 'package:cached_network_image/cached_network_image.dart';

/// A TileProvider that uses the cached_network_image package so map tiles
/// are cached on disk and reused when possible. This reduces network load
/// and improves perceived map loading performance when tiles are already cached.
class CachedTileProvider extends BaseTileProvider {
  const CachedTileProvider();

  @override
  ImageProvider getImage(TileCoordinates coords, TileLayer options) {
    // Build URL from the template
    String url = options.urlTemplate
        .replaceAll('{z}', coords.z.toString())
        .replaceAll('{x}', coords.x.toString())
        .replaceAll('{y}', coords.y.toString());

    // Support subdomains replacement if present
    if (options.subdomains != null && options.subdomains!.isNotEmpty) {
      // rotate subdomain based on x+y to spread requests
      final idx = (coords.x + coords.y) % options.subdomains!.length;
      url = url.replaceAll('{s}', options.subdomains![idx]);
    }

    return CachedNetworkImageProvider(url);
  }
}
