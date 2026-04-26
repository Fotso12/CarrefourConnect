import 'package:flutter/widgets.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:cached_network_image/cached_network_image.dart';

/// A TileProvider that uses the cached_network_image package so map tiles
/// are cached on disk and reused when possible.
class CachedTileProvider extends TileProvider {
  const CachedTileProvider();

  @override
  ImageProvider getImage(TileCoordinates coords, TileLayer options) {
    final template = options.urlTemplate;
    if (template == null) {
      // Fallback or empty image if template is missing
      return const NetworkImage('');
    }

    String url = template
        .replaceAll('{z}', coords.z.toString())
        .replaceAll('{x}', coords.x.toString())
        .replaceAll('{y}', coords.y.toString());

    // Support subdomains replacement if present
    if (options.subdomains.isNotEmpty) {
      // rotate subdomain based on x+y to spread requests
      final idx = (coords.x + coords.y) % options.subdomains.length;
      url = url.replaceAll('{s}', options.subdomains[idx]);
    }

    return CachedNetworkImageProvider(url);
  }
}
