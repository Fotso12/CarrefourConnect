import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart' hide Path;
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
  double _radius = 10.0; // km par défaut

  @override
  void initState() {
    super.initState();
    _determinePosition().then((pos) {
      if (pos != null) {
        setState(() => _currentPosition = pos);
        _mapController.move(LatLng(pos.latitude, pos.longitude), 13.0);
      }
      _loadCommerces();
    });
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
      rayon: _radius,
    );
    if (mounted) {
      setState(() {
        _commerces = results.where((c) => c.latitude != null && c.longitude != null).toList();
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
                urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                userAgentPackageName: 'CarrefourConnectMobileApp/1.0 (contact@carrefourconnect.com)',
              ),
              MarkerLayer(
                markers: [
                  // Position utilisateur
                  if (_currentPosition != null)
                    Marker(
                      point: LatLng(_currentPosition!.latitude, _currentPosition!.longitude),
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
                  
                  // Commerces
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
                                border: Border.all(color: Colors.white, width: 2),
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
                boxShadow: [BoxShadow(color: Colors.black.withAlpha(25), blurRadius: 10)],
              ),
              child: Row(
                children: [
                  const Icon(Icons.radar_rounded, color: primaryBlue, size: 20),
                  Expanded(
                    child: Slider(
                      value: _radius,
                      min: 1,
                      max: 50,
                      divisions: 10,
                      activeColor: primaryBlue,
                      inactiveColor: Colors.grey[200],
                      label: '${_radius.round()} km',
                      onChanged: (value) {
                        setState(() => _radius = value);
                      },
                      onChangeEnd: (_) => _loadCommerces(),
                    ),
                  ),
                  SizedBox(
                    width: 45,
                    child: Text(
                      '${_radius.round()} km',
                      textAlign: TextAlign.end,
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: primaryBlue),
                    ),
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
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(30),
                    boxShadow: [BoxShadow(color: Colors.black.withAlpha(25), blurRadius: 10)],
                  ),
                  child: const Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      SizedBox(
                        width: 15,
                        height: 15,
                        child: CircularProgressIndicator(strokeWidth: 2, color: accentOrange),
                      ),
                      SizedBox(width: 12),
                      Text(
                        'Actualisation des commerces...',
                        style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: Colors.grey),
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
                  Text('Explorer les commerces...', style: TextStyle(color: Colors.grey)),
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
                  builder: (context) => CommerceDetailsScreen(commerce: commerce),
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
