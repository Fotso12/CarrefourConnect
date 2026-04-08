import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import '../models/commerce.dart';
import '../services/api_service.dart';
import '../widgets/commerce_card.dart';

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

  @override
  void initState() {
    super.initState();
    _loadCommerces();
  }

  Future<void> _loadCommerces() async {
    setState(() => _isLoading = true);
    final results = await _apiService.getCommerces();
    if (mounted) {
      setState(() {
        _commerces = results.where((c) => c.latitude != null && c.longitude != null).toList();
        _isLoading = false;
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
              onTap: (_, __) {
                // Fermer les popups si nécessaire
              },
            ),
            children: [
              TileLayer(
                urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                userAgentPackageName: 'com.carrefourconnect.app',
              ),
              MarkerLayer(
                markers: _commerces.map((commerce) {
                  return Marker(
                    point: LatLng(commerce.latitude!, commerce.longitude!),
                    width: 40,
                    height: 40,
                    child: GestureDetector(
                      onTap: () => _showCommerceDetails(commerce),
                      child: Container(
                        decoration: BoxDecoration(
                          color: primaryBlue,
                          shape: BoxShape.circle,
                          border: Border.all(color: Colors.white, width: 2),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(0.2),
                              blurRadius: 10,
                              offset: const Offset(0, 5),
                            ),
                          ],
                        ),
                        child: const Center(
                          child: Icon(
                            FontAwesomeIcons.store,
                            color: Colors.white,
                            size: 16,
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
            ],
          ),
          
          // Loading Indicator
          if (_isLoading)
            const Center(child: CircularProgressIndicator(color: accentOrange)),

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
                  BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 10),
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
        onPressed: _loadCommerces,
        backgroundColor: Colors.white,
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
              // Aller aux détails
            },
          ),
        );
      },
    );
  }
}
