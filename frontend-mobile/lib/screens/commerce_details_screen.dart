import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:latlong2/latlong.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../models/commerce.dart';

class CommerceDetailsScreen extends StatefulWidget {
  final Commerce commerce;

  const CommerceDetailsScreen({super.key, required this.commerce});

  @override
  State<CommerceDetailsScreen> createState() => _CommerceDetailsScreenState();
}

class _CommerceDetailsScreenState extends State<CommerceDetailsScreen> {
  int _currentImageIndex = 0;
  static const primaryBlue = Color(0xFF034D92);
  static const accentOrange = Color(0xFFF78F1E);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: CustomScrollView(
        slivers: [
          _buildAppBar(),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildHeader(),
                  const SizedBox(height: 32),
                  _buildContactInfo(),
                  const SizedBox(height: 32),
                  _buildSectionTitle('À PROPOS'),
                  const SizedBox(height: 12),
                  Text(
                    widget.commerce.description ?? 'Aucune description disponible pour ce commerce.',
                    style: TextStyle(
                      color: Colors.grey[600],
                      fontSize: 16,
                      height: 1.6,
                    ),
                  ),
                  const SizedBox(height: 32),
                  _buildSectionTitle('LOCALISATION'),
                  const SizedBox(height: 16),
                  _buildMap(),
                  const SizedBox(height: 100), // Spacing for bottom button
                ],
              ),
            ),
          ),
        ],
      ),
      bottomSheet: _buildBottomAction(),
    );
  }

  Widget _buildAppBar() {
    return SliverAppBar(
      expandedHeight: 400,
      pinned: true,
      backgroundColor: primaryBlue,
      leading: Padding(
        padding: const EdgeInsets.all(8.0),
        child: CircleAvatar(
          backgroundColor: Colors.white.withOpacity(0.9),
          child: IconButton(
            icon: const Icon(Icons.arrow_back, color: primaryBlue),
            onPressed: () => Navigator.pop(context),
          ),
        ),
      ),
      flexibleSpace: FlexibleSpaceBar(
        background: Stack(
          children: [
            // Image Carousel
            if (widget.commerce.images.isNotEmpty)
              PageView.builder(
                itemCount: widget.commerce.images.length,
                onPageChanged: (index) => setState(() => _currentImageIndex = index),
                itemBuilder: (context, index) {
                  return CachedNetworkImage(
                    imageUrl: widget.commerce.images[index].url,
                    fit: BoxFit.cover,
                    placeholder: (context, url) => Container(color: Color(0xFFF1F5F9)),
                    errorWidget: (context, url, error) => const Icon(Icons.error),
                  );
                },
              )
            else
              Container(
                color: Colors.grey[200],
                child: const Icon(Icons.storefront_rounded, size: 80, color: Colors.grey),
              ),

            // Gradient Overlay
            Positioned.fill(
              child: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.black.withOpacity(0.3),
                      Colors.transparent,
                      Colors.black.withOpacity(0.5),
                    ],
                  ),
                ),
              ),
            ),

            // Image Indicators
            if (widget.commerce.images.length > 1)
              Positioned(
                bottom: 30,
                left: 0,
                right: 0,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(
                    widget.commerce.images.length,
                    (index) => AnimatedContainer(
                      duration: const Duration(milliseconds: 300),
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      width: _currentImageIndex == index ? 24 : 8,
                      height: 8,
                      decoration: BoxDecoration(
                        color: _currentImageIndex == index ? Colors.white : Colors.white.withOpacity(0.5),
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: accentOrange.withOpacity(0.1),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                widget.commerce.categorie?.nom.toUpperCase() ?? 'COMMERCE',
                style: const TextStyle(
                  color: accentOrange,
                  fontSize: 10,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.2,
                ),
              ),
            ),
            const Spacer(),
            const Icon(Icons.star_rounded, color: Color(0xFFFFB800), size: 18),
            const SizedBox(width: 4),
            const Text('4.8', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          ],
        ),
        const SizedBox(height: 16),
        Text(
          widget.commerce.nom,
          style: const TextStyle(
            fontSize: 32,
            fontWeight: FontWeight.w900,
            color: Color(0xFF1E293B),
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            const Icon(Icons.location_on_rounded, color: Colors.grey, size: 16),
            const SizedBox(width: 4),
            Text(
              '${widget.commerce.ville ?? "Littoral"}, Douala',
              style: TextStyle(color: Colors.grey[600], fontSize: 14),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildContactInfo() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _buildContactButton(
            icon: Icons.phone_rounded,
            label: 'Appeler',
            onTap: () => _launchURL('tel:${widget.commerce.telephone}'),
          ),
          Container(width: 1, height: 40, color: const Color(0xFFE2E8F0)),
          _buildContactButton(
            icon: Icons.message_rounded,
            label: 'Message',
            onTap: () => _launchURL('sms:${widget.commerce.telephone}'),
          ),
          Container(width: 1, height: 40, color: const Color(0xFFE2E8F0)),
          _buildContactButton(
            icon: Icons.language_rounded,
            label: 'Site Web',
            onTap: () => _launchURL('https://carrefourconnect.com'),
          ),
        ],
      ),
    );
  }

  Widget _buildContactButton({required IconData icon, required String label, required VoidCallback onTap}) {
    return InkWell(
      onTap: onTap,
      child: Column(
        children: [
          Icon(icon, color: primaryBlue),
          const SizedBox(height: 8),
          Text(label, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: primaryBlue)),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: const TextStyle(
        fontSize: 12,
        fontWeight: FontWeight.w900,
        letterSpacing: 2.0,
        color: Colors.grey,
      ),
    );
  }

  Widget _buildMap() {
    if (widget.commerce.latitude == null || widget.commerce.longitude == null) {
      return const SizedBox.shrink();
    }

    final latlng = LatLng(widget.commerce.latitude!, widget.commerce.longitude!);

    return Container(
      height: 200,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(24),
        child: FlutterMap(
          options: MapOptions(
            initialCenter: latlng,
            initialZoom: 15.0,
            interactionOptions: const InteractionOptions(flags: InteractiveFlag.none),
          ),
          children: [
            TileLayer(
              urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
            ),
            MarkerLayer(
              markers: [
                Marker(
                  point: latlng,
                  width: 40,
                  height: 40,
                  child: const Icon(Icons.location_on_rounded, color: Colors.red, size: 40),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBottomAction() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, -5),
          ),
        ],
      ),
      child: SafeArea(
        child: ElevatedButton(
          onPressed: () {}, // TODO: Itinéraire
          style: ElevatedButton.styleFrom(
            backgroundColor: primaryBlue,
            foregroundColor: Colors.white,
            minimumSize: const Size(double.infinity, 56),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            elevation: 8,
            shadowColor: primaryBlue.withOpacity(0.3),
          ),
          child: const Text('VOIR L\'ITINÉRAIRE', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        ),
      ),
    );
  }

  Future<void> _launchURL(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri);
    }
  }
}
