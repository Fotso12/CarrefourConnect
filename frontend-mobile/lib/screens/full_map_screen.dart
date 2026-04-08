import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart' hide Path;
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../models/commerce.dart';

class FullScreenMapScreen extends StatefulWidget {
  final Commerce commerce;
  final double userLat;
  final double userLon;

  const FullScreenMapScreen({
    super.key,
    required this.commerce,
    required this.userLat,
    required this.userLon,
  });

  @override
  State<FullScreenMapScreen> createState() => _FullScreenMapScreenState();
}

class _FullScreenMapScreenState extends State<FullScreenMapScreen> {
  List<LatLng> _routePoints = [];
  String _distance = '';
  String _duration = '';
  bool _isLoading = true;

  static const accentOrange = Color(0xFFF78F1E);
  final MapController _mapController = MapController();
  late LatLng _userPos;

  @override
  void initState() {
    super.initState();
    _userPos = LatLng(widget.userLat, widget.userLon);
    _fetchRoute();
  }

  Future<void> _fetchRoute() async {
    final destLat = widget.commerce.latitude;
    final destLon = widget.commerce.longitude;

    if (destLat == null || destLon == null) return;

    final url = 'https://router.project-osrm.org/route/v1/driving/${widget.userLon},${widget.userLat};$destLon,$destLat?overview=full&geometries=geojson';

    try {
      final response = await http.get(Uri.parse(url));
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        if (data['routes'] != null && data['routes'].isNotEmpty) {
          final route = data['routes'][0];
          final List<dynamic> coords = route['geometry']['coordinates'];
          
          setState(() {
            _routePoints = coords.map((c) => LatLng(c[1].toDouble(), c[0].toDouble())).toList();
            _distance = (route['distance'] / 1000).toStringAsFixed(1);
            _duration = (route['duration'] / 60).toStringAsFixed(0);
            _isLoading = false;
          });
        }
      }
    } catch (e) {
      print('Erreur OSRM: $e');
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final commercePos = LatLng(widget.commerce.latitude!, widget.commerce.longitude!);
    final userPos = LatLng(widget.userLat, widget.userLon);

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.commerce.nom, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: Stack(
        children: [
          FlutterMap(
            mapController: _mapController,
            options: MapOptions(
              initialCenter: commercePos,
              initialZoom: 14.0,
            ),
            children: [
              TileLayer(
                urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                userAgentPackageName: 'CarrefourConnectMobile/1.0 (contact@carrefourconnect.com)',
              ),
              if (_routePoints.isNotEmpty)
                PolylineLayer(
                  polylines: [
                    Polyline(
                      points: _routePoints,
                      color: accentOrange,
                      strokeWidth: 5.0,
                    ),
                  ],
                ),
              MarkerLayer(
                markers: [
                  Marker(
                    point: userPos,
                    width: 40,
                    height: 40,
                    child: const Icon(Icons.my_location, color: Colors.blue, size: 30),
                  ),
                  Marker(
                    point: commercePos,
                    width: 50,
                    height: 50,
                    child: const Icon(Icons.location_on, color: Colors.red, size: 40),
                  ),
                ],
              ),
            ],
          ),
          if (_isLoading)
            const Center(child: CircularProgressIndicator(color: accentOrange)),
          
          // Info Overlay
          if (_distance.isNotEmpty)
            Positioned(
              bottom: 40,
              left: 20,
              right: 20,
              child: Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 20)],
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _buildRouteInfo(Icons.directions_car, 'Distance', '$_distance km'),
                    Container(width: 1, height: 40, color: Colors.grey[200]),
                    _buildRouteInfo(Icons.access_time_filled, 'Temps', '$_duration min'),
                  ],
                ),
              ),
            ),
          
          // Floating Action Button to center user
          Positioned(
            bottom: 130,
            right: 20,
            child: FloatingActionButton(
              onPressed: () => _mapController.move(_userPos, 16.0),
              backgroundColor: Colors.white,
              mini: true,
              child: const Icon(Icons.my_location, color: Color(0xFF034D92)),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRouteInfo(IconData icon, String label, String value) {
    return Column(
      children: [
        Icon(icon, color: accentOrange, size: 24),
        const SizedBox(height: 4),
        Text(label, style: TextStyle(color: Colors.grey[400], fontSize: 10, fontWeight: FontWeight.bold)),
        Text(value, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18, color: Color(0xFF1E293B))),
      ],
    );
  }
}
