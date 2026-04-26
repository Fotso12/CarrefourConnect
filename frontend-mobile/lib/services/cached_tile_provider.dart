import 'package:flutter/widgets.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:cached_network_image/cached_network_image.dart';

/// A TileProvider that uses the cached_network_image package so map tiles
/// are cached on disk and reused when possible.
class CachedTileProvider extends TileProvider {
  CachedTileProvider();

  @override
  ImageProvider getImage(TileCoordinates coordinates, TileLayer options) {
    final template = options.urlTemplate;
    if (template == null) {
      // Fallback or empty image if template is missing
      return const NetworkImage('');
    }

    String url = template
        .replaceAll('{z}', coordinates.z.toString())
        .replaceAll('{x}', coordinates.x.toString())
        .replaceAll('{y}', coordinates.y.toString());

    // Support subdomains replacement if present
    if (options.subdomains.isNotEmpty) {
      // rotate subdomain based on x+y to spread requests
      final idx = (coordinates.x + coordinates.y) % options.subdomains.length;
      url = url.replaceAll('{s}', options.subdomains[idx]);
    }

    return CachedNetworkImageProvider(url);
  }
}
