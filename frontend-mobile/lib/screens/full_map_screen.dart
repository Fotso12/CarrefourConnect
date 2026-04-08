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
  String _travelMode = 'driving'; // driving, moto, walking
  final Map<String, dynamic> _routeCache = {};

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
    // 1. Vérifier si on a déjà ce trajet en cache
    if (_routeCache.containsKey(_travelMode)) {
      final cached = _routeCache[_travelMode]!;
      setState(() {
        _routePoints = cached['points'];
        _distance = cached['distance'];
        _duration = cached['duration'];
        _isLoading = false;
      });
      return;
    }

    final destLat = widget.commerce.latitude;
    final destLon = widget.commerce.longitude;

    if (destLat == null || destLon == null) return;

    final profile = _travelMode == 'walking' ? 'walking' : 'driving';
    final url = 'https://router.project-osrm.org/route/v1/$profile/${widget.userLon},${widget.userLat};$destLon,$destLat?overview=full&geometries=geojson';

    try {
      final response = await http.get(Uri.parse(url));
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        if (data['routes'] != null && data['routes'].isNotEmpty) {
          final route = data['routes'][0];
          final List<dynamic> coords = route['geometry']['coordinates'];
          
          setState(() {
            final points = coords.map((c) => LatLng(c[1].toDouble(), c[0].toDouble())).toList();
            double distanceKm = (route['distance'] / 1000);
            final distanceStr = distanceKm.toStringAsFixed(1);
            String durationStr = '';
            
            if (_travelMode == 'walking') {
              durationStr = (distanceKm * 13.1).toStringAsFixed(0);
            } else {
              double baseDurationSec = (route['duration']).toDouble();
              final traffic = _getTrafficInfo();
              
              double factorMin = _travelMode == 'moto' ? 1.2 : 1.8;
              double factorMax = _travelMode == 'moto' ? 2.5 : 4.0;
              
              if (traffic['intensity'] == 'dense') {
                factorMin *= 1.5; factorMax *= 1.5;
              } else if (traffic['intensity'] == 'fluide') {
                factorMax *= 0.8;
              }

              int min = (baseDurationSec * factorMin / 60).round();
              int max = (baseDurationSec * factorMax / 60).round();
              durationStr = '$min - $max';
            }
            
            _routePoints = points;
            _distance = distanceStr;
            _duration = durationStr;
            
            // Mettre en cache pour la prochaine fois
            _routeCache[_travelMode] = {
              'points': points,
              'distance': distanceStr,
              'duration': durationStr,
            };
            
            _isLoading = false;
          });
        }
      }
    } catch (e) {
      print('Erreur OSRM: $e');
      setState(() => _isLoading = false);
    }
  }

  Map<String, String> _getTrafficInfo() {
    final hour = DateTime.now().hour;
    final minute = DateTime.now().minute;
    final time = hour + (minute / 60.0);

    if ((time >= 7.5 && time <= 9.5) || (time >= 16.5 && time <= 19.5)) {
      return {'intensity': 'dense', 'label': 'Trafic dense', 'color': 'red'};
    } else if (time >= 10.0 && time <= 16.0) {
      return {'intensity': 'modere', 'label': 'Trafic modéré', 'color': 'orange'};
    } else {
      return {'intensity': 'fluide', 'label': 'Trafic fluide', 'color': 'green'};
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
          
          // Mode Selection
          Positioned(
            top: 10,
            left: 20,
            right: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(30),
                boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 10)],
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  _buildModeTab('driving', Icons.directions_car_rounded, 'Voiture'),
                  _buildModeTab('moto', Icons.moped_rounded, 'Moto'),
                  _buildModeTab('walking', Icons.directions_walk_rounded, 'Pied'),
                ],
              ),
            ),
          ),
          
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
                    _buildRouteInfo(
                      _travelMode == 'driving' ? Icons.directions_car : 
                      (_travelMode == 'moto' ? Icons.moped : Icons.directions_walk), 
                      'Distance', 
                      '$_distance km'
                    ),
                    Container(width: 1, height: 40, color: Colors.grey[200]),
                    _buildRouteInfo(Icons.access_time_filled, 'Temps', '$_duration min'),
                  ],
                ),
              ),
            ),
          
          // Traffic Status Badge
          if (_travelMode != 'walking' && _distance.isNotEmpty)
            Positioned(
              bottom: 125,
              left: 0,
              right: 0,
              child: Center(
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                  decoration: BoxDecoration(
                    color: _getTrafficInfo()['color'] == 'red' ? Colors.red[50] :
                           (_getTrafficInfo()['color'] == 'orange' ? Colors.orange[50] : Colors.green[50]),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(
                      color: _getTrafficInfo()['color'] == 'red' ? Colors.red[200]! :
                             (_getTrafficInfo()['color'] == 'orange' ? Colors.orange[200]! : Colors.green[200]!),
                    ),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        width: 8,
                        height: 8,
                        decoration: BoxDecoration(
                          color: _getTrafficInfo()['color'] == 'red' ? Colors.red :
                                 (_getTrafficInfo()['color'] == 'orange' ? Colors.orange : Colors.green),
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        _getTrafficInfo()['label']!,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: _getTrafficInfo()['color'] == 'red' ? Colors.red[900] :
                                 (_getTrafficInfo()['color'] == 'orange' ? Colors.orange[900] : Colors.green[900]),
                        ),
                      ),
                    ],
                  ),
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

  Widget _buildModeTab(String mode, IconData icon, String label) {
    final bool isSelected = _travelMode == mode;
    return GestureDetector(
      onTap: () {
        setState(() {
          _travelMode = mode;
          _isLoading = true;
        });
        _fetchRoute();
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? accentOrange : Colors.transparent,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Row(
          children: [
            Icon(icon, color: isSelected ? Colors.white : Colors.grey, size: 20),
            if (isSelected) const SizedBox(width: 8),
            if (isSelected)
              Text(
                label,
                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12),
              ),
          ],
        ),
      ),
    );
  }
}
