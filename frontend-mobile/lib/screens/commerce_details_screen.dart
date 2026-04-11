import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart' hide Path;
import 'package:url_launcher/url_launcher.dart';
import 'package:geolocator/geolocator.dart';
import '../models/commerce.dart';
import '../services/api_service.dart';
import '../services/auth_service.dart';
import 'full_map_screen.dart';
import 'login_screen.dart';

class CommerceDetailsScreen extends StatefulWidget {
  final Commerce commerce;

  const CommerceDetailsScreen({super.key, required this.commerce});

  @override
  State<CommerceDetailsScreen> createState() => _CommerceDetailsScreenState();
}

class _CommerceDetailsScreenState extends State<CommerceDetailsScreen> {
  int _currentImageIndex = 0;
  final ApiService _apiService = ApiService();
  final AuthService _authService = AuthService();
  List<dynamic> _avis = [];
  bool _isLoadingAvis = true;
  double _moyenne = 0;
  List<String> _displayImages = [];
  final MapController _miniMapController = MapController();
  final PageController _pageController = PageController();

  static const primaryBlue = Color(0xFF034D92);
  static const accentOrange = Color(0xFFF78F1E);

  @override
  void initState() {
    super.initState();
    _moyenne = widget.commerce.noteGlobale ?? 0.0;
    _loadAvis();
    _prepareImages();
  }

  void _prepareImages() {
    _displayImages = widget.commerce.images.map((img) => img.url).toList();
    if (widget.commerce.imagePrincipale != null && !_displayImages.contains(widget.commerce.imagePrincipale)) {
      _displayImages.insert(0, widget.commerce.imagePrincipale!);
    }
    
    // Images préparées
  }

  Future<void> _loadAvis() async {
    if (widget.commerce.idcommerce == null) return;
    final results = await _apiService.getAvisByCommerce(widget.commerce.idcommerce!);
    if (mounted) {
      setState(() {
        _avis = results;
        _isLoadingAvis = false;
        if (_avis.isNotEmpty) {
          double total = 0;
          for (var a in _avis) {
            total += (a['note'] ?? 0).toDouble();
          }
          _moyenne = total / _avis.length;
        }
      });
    }
  }

  void _checkAuthAndRate() async {
    bool isConnected = await _authService.isLoggedIn();

    if (!isConnected) {
      _showLoginRequiredModal();
    } else {
      _showRateDialog();
    }
  }

  void _showLoginRequiredModal() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('Connexion requise', style: TextStyle(fontWeight: FontWeight.bold, color: primaryBlue)),
        content: const Text('Vous devez être connecté pour laisser un avis sur ce commerce.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler', style: TextStyle(color: Colors.grey)),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.push(context, MaterialPageRoute(builder: (context) => const LoginScreen()));
            },
            style: ElevatedButton.styleFrom(backgroundColor: accentOrange, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))),
            child: const Text('Se connecter'),
          ),
        ],
      ),
    );
  }

  void _showRateDialog() {
    double rating = 5.0;
    final commentController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => Dialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(32)),
          child: SingleChildScrollView(
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: accentOrange,
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: const Icon(Icons.star_rounded, color: Colors.white, size: 28),
                    ),
                    const SizedBox(width: 16),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Votre Expérience', style: TextStyle(fontSize: 24, fontWeight: FontWeight.w900, color: Color(0xFF1E293B))),
                          Text('Aidez la communauté en partageant votre avis', style: TextStyle(fontSize: 13, color: Colors.grey, fontWeight: FontWeight.w500)),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 32),
                const Text('QUELLE NOTE DONNERIEZ-VOUS ?', style: TextStyle(fontSize: 11, fontWeight: FontWeight.w900, color: Colors.grey, letterSpacing: 1.2)),
                const SizedBox(height: 8),
                Slider(
                  value: rating,
                  min: 1,
                  max: 5,
                  divisions: 4,
                  activeColor: accentOrange,
                  inactiveColor: Colors.grey[200],
                  onChanged: (value) => setDialogState(() => rating = value),
                ),
                Text(
                  '${rating.round()} / 5',
                  style: const TextStyle(fontSize: 32, fontWeight: FontWeight.w900, color: accentOrange),
                ),
                const SizedBox(height: 24),
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text('VOTRE COMMENTAIRE', style: TextStyle(fontSize: 11, fontWeight: FontWeight.w900, color: Colors.grey, letterSpacing: 1.2)),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: commentController,
                  maxLines: 4,
                  decoration: InputDecoration(
                    hintText: 'Racontez-nous ce que vous avez aimé (ou moins aimé)...',
                    hintStyle: const TextStyle(fontSize: 13, color: Colors.grey),
                    filled: true,
                    fillColor: const Color(0xFFF8FAFC),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide.none),
                    contentPadding: const EdgeInsets.all(16),
                  ),
                ),
                const SizedBox(height: 32),
                Row(
                  children: [
                    Expanded(
                      child: TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Annuler', style: TextStyle(color: primaryBlue, fontWeight: FontWeight.w900, fontSize: 16)),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      flex: 2,
                      child: Container(
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(16),
                          gradient: const LinearGradient(colors: [accentOrange, Color(0xFFE67E22)]),
                          boxShadow: [BoxShadow(color: accentOrange.withValues(alpha: 0.3), blurRadius: 15, offset: const Offset(0, 5))],
                        ),
                        child: ElevatedButton(
                          onPressed: () async {
                            final userData = await _authService.getUserData();
                            final token = await _authService.getToken();
                            if (userData == null || token == null) return;

                            final success = await _apiService.createAvis({
                              'idcommerce': widget.commerce.idcommerce,
                              'iduser': userData['id'], // Fix: sending the userId
                              'note': rating.round(),
                              'commentaire': commentController.text,
                              'status': 'PUBLIE', // Requirement for backend
                            }, token); 
                            
                            if (!mounted) return;

                            Navigator.pop(context); // Close rate dialog
                            if (success) {
                              _loadAvis();
                              showDialog(
                                context: context,
                                builder: (context) => AlertDialog(
                                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                                  title: const Icon(Icons.check_circle, color: Colors.green, size: 48),
                                  content: const Text('Merci ! Votre avis a bien été enregistré.', textAlign: TextAlign.center),
                                  actions: [
                                    TextButton(
                                      onPressed: () => Navigator.pop(context),
                                      child: const Text('OK', style: TextStyle(fontWeight: FontWeight.bold)),
                                    ),
                                  ],
                                ),
                              );
                            }
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.transparent,
                            shadowColor: Colors.transparent,
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                          ),
                          child: const Text('Publier mon avis', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 16)),
                        ),
                      ),
                    ),
                  ],
                ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

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
                  _buildSectionTitle('AVIS CLIENTS'),
                  const SizedBox(height: 12),
                  _buildAvisList(),
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
            if (_displayImages.isNotEmpty)
              PageView.builder(
                controller: _pageController,
                itemCount: _displayImages.length,
                onPageChanged: (index) => setState(() => _currentImageIndex = index),
                itemBuilder: (context, index) {
                  return Image.network(
                    _displayImages[index],
                    key: ValueKey(_displayImages[index]),
                    fit: BoxFit.cover,
                    loadingBuilder: (context, child, loadingProgress) {
                      if (loadingProgress == null) return child;
                      return Container(
                        color: const Color(0xFFF1F5F9),
                        child: const Center(child: CircularProgressIndicator(strokeWidth: 2)),
                      );
                    },
                    errorBuilder: (context, error, stackTrace) {
                      return Container(
                        color: const Color(0xFFF1F5F9),
                        child: const Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.broken_image_rounded, color: Colors.grey, size: 48),
                            SizedBox(height: 8),
                            Text('Image indisponible', style: TextStyle(color: Colors.grey, fontSize: 12)),
                          ],
                        ),
                      );
                    },
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
                      Colors.black.withValues(alpha: 0.3),
                      Colors.transparent,
                      Colors.black.withValues(alpha: 0.5),
                    ],
                  ),
                ),
              ),
            ),

            // Image Indicators
            if (_displayImages.length > 1) ...[
              Positioned(
                bottom: 30,
                left: 0,
                right: 0,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(
                    _displayImages.length,
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
              Positioned(
                left: 10,
                top: 150,
                child: CircleAvatar(
                  backgroundColor: Colors.black.withOpacity(0.4),
                  child: IconButton(
                    icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white, size: 20),
                    onPressed: _currentImageIndex > 0 ? () {
                      _pageController.previousPage(duration: const Duration(milliseconds: 300), curve: Curves.easeInOut);
                    } : null,
                  ),
                ),
              ),
              Positioned(
                right: 10,
                top: 150,
                child: CircleAvatar(
                  backgroundColor: Colors.black.withOpacity(0.4),
                  child: IconButton(
                    icon: const Icon(Icons.arrow_forward_ios, color: Colors.white, size: 20),
                    onPressed: _currentImageIndex < _displayImages.length - 1 ? () {
                      _pageController.nextPage(duration: const Duration(milliseconds: 300), curve: Curves.easeInOut);
                    } : null,
                  ),
                ),
              ),
            ]
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
            Text(
              widget.commerce.noteGlobale != null ? widget.commerce.noteGlobale!.toStringAsFixed(1) : '0.0', 
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
            ),
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
        child: Stack(
          children: [
            _buildFlutterMap(latlng),
            _buildMapOverlayButtons(latlng),
          ],
        ),
      ),
    );
  }

  Widget _buildFlutterMap(LatLng latlng) {
    return FlutterMap(
      mapController: _miniMapController,
      options: MapOptions(
        initialCenter: latlng,
        initialZoom: 15.0,
        interactionOptions: const InteractionOptions(flags: InteractiveFlag.all),
      ),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'CarrefourConnectMobile/1.0 (contact@carrefourconnect.com)',
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
    );
  }

  Widget _buildMapOverlayButtons(LatLng latlng) {
    return Positioned(
      bottom: 10,
      right: 10,
      child: Column(
        children: [
          FloatingActionButton.small(
            heroTag: 'btn_center_commerce',
            onPressed: () => _miniMapController.move(latlng, 15.0),
            backgroundColor: Colors.white,
            child: const Icon(Icons.storefront, color: primaryBlue),
          ),
          const SizedBox(height: 8),
          FloatingActionButton.small(
            heroTag: 'btn_center_user',
            onPressed: () async {
              Position pos = await Geolocator.getLastKnownPosition() ?? await Geolocator.getCurrentPosition(desiredAccuracy: LocationAccuracy.low);
              _miniMapController.move(LatLng(pos.latitude, pos.longitude), 15.0);
            },
            backgroundColor: Colors.white,
            child: const Icon(Icons.my_location, color: primaryBlue),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomAction() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
      color: Colors.white,
      child: SafeArea(
        child: ElevatedButton(
          onPressed: () async {
            Position? maybePos = await Geolocator.getLastKnownPosition();
            Position pos = maybePos ?? await Geolocator.getCurrentPosition(desiredAccuracy: LocationAccuracy.low);
            if (mounted && widget.commerce.latitude != null) {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => FullScreenMapScreen(
                    commerce: widget.commerce,
                    userLat: pos.latitude,
                    userLon: pos.longitude,
                  ),
                ),
              );
            }
          },
          style: ElevatedButton.styleFrom(
            backgroundColor: primaryBlue,
            foregroundColor: Colors.white,
            minimumSize: const Size(double.infinity, 56),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            elevation: 8,
            shadowColor: primaryBlue.withValues(alpha: 0.3),
          ),
          child: const Text('VOIR L\'ITINÉRAIRE', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        ),
      ),
    );
  }


  Widget _buildAvisList() {
    if (_isLoadingAvis) return const Center(child: CircularProgressIndicator());
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Text(
              _moyenne.toStringAsFixed(1),
              style: const TextStyle(fontSize: 48, fontWeight: FontWeight.w900, color: accentOrange),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: List.generate(5, (i) => Icon(
                    Icons.star_rounded, 
                    size: 18, 
                    color: i < _moyenne.round() ? accentOrange : Colors.grey[300])),
                ),
                Text('${_avis.length} avis', style: const TextStyle(color: Colors.grey, fontSize: 13, fontWeight: FontWeight.bold)),
              ],
            ),
            const Spacer(),
            ElevatedButton.icon(
              onPressed: _checkAuthAndRate,
              icon: const Icon(Icons.edit_rounded, size: 16),
              label: const Text('Rédiger', style: TextStyle(fontWeight: FontWeight.w900)),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF003B71),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              ),
            ),
          ],
        ),
        const SizedBox(height: 32),
        if (_avis.isEmpty)
          Center(
            child: Column(
              children: [
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(color: Colors.grey[50], shape: BoxShape.circle),
                  child: const Icon(Icons.chat_bubble_outline_rounded, color: Colors.grey, size: 30),
                ),
                const SizedBox(height: 12),
                const Text('Aucun avis pour le moment.', style: TextStyle(color: Colors.grey, fontWeight: FontWeight.w600)),
                const Text('Soyez le premier à partager votre expérience !', style: TextStyle(color: Colors.grey, fontSize: 12)),
              ],
            ),
          )
        else
          ..._avis.map((a) {
            final user = a['utilisateur'] ?? {};
            final String initial = (user['nom'] ?? 'U').toString().substring(0, 1).toUpperCase();
            return Container(
              margin: const EdgeInsets.only(bottom: 16),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  CircleAvatar(
                    backgroundColor: primaryBlue.withOpacity(0.05), 
                    child: Text(initial, style: const TextStyle(color: primaryBlue, fontWeight: FontWeight.w900))),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(user['nom'] ?? 'Anonyme', style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 14, color: Color(0xFF1E293B))),
                            Row(
                              children: List.generate(5, (i) => Icon(Icons.star_rounded, size: 12, color: i < (a['note'] ?? 0) ? accentOrange : Colors.grey[200])),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(a['commentaire'] ?? '', style: TextStyle(color: Colors.grey[600], fontSize: 13, height: 1.4)),
                      ],
                    ),
                  ),
                ],
              ),
            );
          }),
      ],
    );
  }

  Future<void> _launchURL(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri);
    }
  }
}
