import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'dart:io';
import 'package:latlong2/latlong.dart' hide Path;
// using default tile provider to avoid custom provider compatibility issues
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:geolocator/geolocator.dart';
import '../models/commerce.dart';
import '../services/api_service.dart';
import '../widgets/commerce_card.dart';
import 'commerce_details_screen.dart';

class MapScreen extends StatefulWidget {
  const MapScreen({super.key});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  final ApiService _apiService = ApiService();
  final MapController _mapController = MapController();

  List<Commerce> _commerces = [];
  bool _isLoading = true;
  Position? _currentPosition;
  double _radius = 10.0; // displayed slider (represents max radius)
  double _maxRadius = 10.0; // effective max radius sent to API (km)
  bool _hasNetwork = true;

  @override
  void initState() {
    super.initState();
    _determinePosition().then((pos) {
      if (pos != null) {
        setState(() => _currentPosition = pos);
        _mapController.move(LatLng(pos.latitude, pos.longitude), 13.0);
      }
      _checkNetwork();
      _loadCommerces();
    });
  }

  Future<void> _checkNetwork() async {
    try {
      // quick HEAD request to an OSM tile to verify network (short timeout)
      final uri = Uri.parse('https://tile.openstreetmap.org/0/0/0.png');
      final resp = await Future.any([
        // use http package via ApiService import is not desired here; use Dart HttpClient
        // small request to check connectivity
        HttpClient().getUrl(uri).then((r) => r.close()),
        Future.delayed(const Duration(seconds: 3), () => null),
      ]);
      setState(() => _hasNetwork = resp != null);
    } catch (_) {
      setState(() => _hasNetwork = false);
    }
  }

  Future<Position?> _determinePosition() async {
    bool serviceEnabled;
    LocationPermission permission;

    serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) return null;

    permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) return null;
    }

    if (permission == LocationPermission.deniedForever) return null;

    return await Geolocator.getCurrentPosition();
  }

  Future<void> _loadCommerces() async {
    setState(() => _isLoading = true);
    final results = await _apiService.getCommerces(
      lat: _currentPosition?.latitude,
      lon: _currentPosition?.longitude,
      rayon: _maxRadius,
    );
    if (mounted) {
      // compute distance to each commerce and apply client-side min/max filter
      final Distance distance = Distance();
      final filtered = <Commerce>[];
      for (final c in results) {
        if (c.latitude == null || c.longitude == null) continue;
        if (_currentPosition == null) {
          filtered.add(c);
          continue;
        }
        final dKm = distance.as(
          LengthUnit.Kilometer,
          LatLng(_currentPosition!.latitude, _currentPosition!.longitude),
          LatLng(c.latitude!, c.longitude!),
        );
        if (dKm <= _maxRadius) filtered.add(c);
      }

      setState(() {
        _commerces = filtered;
        _isLoading = false;
      });
    }
  }

  void _centerOnUser() {
    if (_currentPosition != null) {
      _mapController.move(
        LatLng(_currentPosition!.latitude, _currentPosition!.longitude),
        15.0,
      );
    } else {
      _determinePosition().then((pos) {
        if (pos != null) {
          setState(() => _currentPosition = pos);
          _mapController.move(LatLng(pos.latitude, pos.longitude), 15.0);
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    const primaryBlue = Color(0xFF034D92);
    const accentOrange = Color(0xFFF78F1E);

    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
            mapController: _mapController,
            options: MapOptions(
              initialCenter: const LatLng(4.0511, 9.7679), // Douala par défaut
              initialZoom: 13.0,
              onTap: (_, _) {
                // Fermer les popups si nécessaire
              },
            ),
            children: [
              TileLayer(
                urlTemplate:
                    'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                subdomains: const ['a', 'b', 'c'],
                userAgentPackageName:
                    'CarrefourConnectMobileApp/1.0 (contact@carrefourconnect.com)',
              ),
              MarkerLayer(
                markers: [
                  if (_currentPosition != null)
                    Marker(
                      point: LatLng(
                        _currentPosition!.latitude,
                        _currentPosition!.longitude,
                      ),
                      width: 60,
                      height: 60,
                      child: Stack(
                        alignment: Alignment.center,
                        children: [
                          Container(
                            width: 20,
                            height: 20,
                            decoration: BoxDecoration(
                              color: Colors.blue.withAlpha(51),
                              shape: BoxShape.circle,
                            ),
                          ),
                          Container(
                            width: 12,
                            height: 12,
                            decoration: BoxDecoration(
                              color: Colors.blue,
                              shape: BoxShape.circle,
                              border: Border.all(color: Colors.white, width: 2),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ..._commerces.map((commerce) {
                    return Marker(
                      point: LatLng(commerce.latitude!, commerce.longitude!),
                      width: 50,
                      height: 50,
                      child: GestureDetector(
                        onTap: () => _showCommerceDetails(commerce),
                        child: Column(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: primaryBlue,
                                borderRadius: BorderRadius.circular(12),
                                border: Border.all(
                                  color: Colors.white,
                                  width: 2,
                                ),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withAlpha(51),
                                    blurRadius: 8,
                                    offset: const Offset(0, 4),
                                  ),
                                ],
                              ),
                              child: const Icon(
                                FontAwesomeIcons.store,
                                color: Colors.white,
                                size: 14,
                              ),
                            ),
                            CustomPaint(
                              painter: TrianglePainter(color: primaryBlue),
                              size: const Size(10, 5),
                            ),
                          ],
                        ),
                      ),
                    );
                  }),
                ],
              ),
              if (!_hasNetwork)
                Positioned.fill(
                  child: Container(
                    color: Colors.white.withOpacity(0.9),
                    child: Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(
                            Icons.cloud_off,
                            size: 64,
                            color: Colors.grey,
                          ),
                          const SizedBox(height: 12),
                          const Text(
                            'Aucune connexion internet pour charger la carte',
                            style: TextStyle(color: Colors.grey),
                          ),
                          const SizedBox(height: 12),
                          ElevatedButton(
                            onPressed: () async {
                              await _checkNetwork();
                              if (_hasNetwork) _loadCommerces();
                            },
                            child: const Text('Réessayer'),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
            ],
          ),

          // Slider de distance
          Positioned(
            bottom: 30,
            left: 20,
            right: 80,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                boxShadow: [
                  BoxShadow(color: Colors.black.withAlpha(25), blurRadius: 10),
                ],
              ),
              child: Row(
                children: [
                  const Icon(Icons.radar_rounded, color: primaryBlue, size: 20),
                  Expanded(
                    child: Slider(
                      value: _radius,
                      min: 0,
                      max: 500,
                      divisions: 50,
                      activeColor: primaryBlue,
                      inactiveColor: Colors.grey[200],
                      label: '${_radius.round()} km',
                      onChanged: (value) {
                        setState(() => _radius = value);
                      },
                      onChangeEnd: (value) {
                        setState(() {
                          _maxRadius = value;
                        });
                        _loadCommerces();
                      },
                    ),
                  ),
                  SizedBox(
                    width: 45,
                    child: Text(
                      '${_radius.round()} km',
                      textAlign: TextAlign.end,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                        color: primaryBlue,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    onPressed: () async {
                      // open dialog for manual min/max entry
                      final result = await showDialog<Map<String, double>>(
                        context: context,
                        builder: (context) {
                          final maxCtrl = TextEditingController(
                            text: _maxRadius.toStringAsFixed(0),
                          );
                          return AlertDialog(
                            title: const Text(
                              'Saisir la distance maximale (km)',
                            ),
                            content: TextField(
                              controller: maxCtrl,
                              keyboardType: TextInputType.number,
                              decoration: const InputDecoration(
                                labelText: 'Distance maximale (km)',
                              ),
                            ),
                            actions: [
                              TextButton(
                                onPressed: () => Navigator.pop(context),
                                child: const Text('Annuler'),
                              ),
                              ElevatedButton(
                                onPressed: () {
                                  final max =
                                      double.tryParse(maxCtrl.text) ??
                                      _maxRadius;
                                  Navigator.pop(context, {'max': max});
                                },
                                child: const Text('OK'),
                              ),
                            ],
                          );
                        },
                      );

                      if (result != null) {
                        setState(() {
                          _maxRadius = (result['max'] as double).clamp(
                            0.0,
                            10000.0,
                          );
                          _radius = _maxRadius;
                        });
                        _loadCommerces();
                      }
                    },
                    icon: const Icon(Icons.edit, size: 18),
                    color: primaryBlue,
                  ),
                ],
              ),
            ),
          ),

          // Loading Overlay (Intelligent)
          if (_isLoading)
            Positioned(
              top: 100,
              left: 0,
              right: 0,
              child: Center(
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 10,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(30),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withAlpha(25),
                        blurRadius: 10,
                      ),
                    ],
                  ),
                  child: const Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      SizedBox(
                        width: 15,
                        height: 15,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: accentOrange,
                        ),
                      ),
                      SizedBox(width: 12),
                      Text(
                        'Actualisation des commerces...',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                          color: Colors.grey,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),

          // Search / Filter overlay (optionnel)
          Positioned(
            top: 40,
            left: 20,
            right: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(30),
                boxShadow: [
                  BoxShadow(color: Colors.black.withAlpha(25), blurRadius: 10),
                ],
              ),
              child: const Row(
                children: [
                  Icon(Icons.search_rounded, color: Colors.grey),
                  SizedBox(width: 12),
                  Text(
                    'Explorer les commerces...',
                    style: TextStyle(color: Colors.grey),
                  ),
                  Spacer(),
                  Icon(Icons.tune_rounded, color: accentOrange),
                ],
              ),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _centerOnUser,
        backgroundColor: Colors.white,
        elevation: 4,
        child: const Icon(Icons.my_location_rounded, color: primaryBlue),
      ),
    );
  }

  void _showCommerceDetails(Commerce commerce) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) {
        return Container(
          margin: const EdgeInsets.all(16),
          height: 300,
          child: CommerceCard(
            commerce: commerce,
            onTap: () {
              Navigator.pop(context);
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) =>
                      CommerceDetailsScreen(commerce: commerce),
                ),
              );
            },
          ),
        );
      },
    );
  }
}

class TrianglePainter extends CustomPainter {
  final Color color;
  TrianglePainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()..color = color;
    final path = Path();
    path.moveTo(0, 0);
    path.lineTo(size.width, 0);
    path.lineTo(size.width / 2, size.height);
    path.close();
    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) => false;
}
