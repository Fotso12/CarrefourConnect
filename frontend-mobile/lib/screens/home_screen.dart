import 'dart:async';
import 'package:flutter/material.dart';
import '../models/commerce.dart';
import '../services/api_service.dart';
import '../widgets/commerce_card.dart';
import 'commerce_details_screen.dart';
import '../services/auth_service.dart';
import 'package:geolocator/geolocator.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();

  List<Commerce> _commerces = [];
  List<Categorie> _categories = [];
  Set<String> _favoriteIds = {};
  String? _selectedCategoryId;
  bool _isLoading = true;
  Position? _currentPosition;
  double _radius = 10.0; // km par défaut
  String? _userName;

  final AuthService _authService = AuthService();
  VoidCallback? _authListener;

  @override
  void initState() {
    super.initState();
    _initLocation().then((_) => _loadData());
    // Listen to auth changes to refresh UI automatically after login/logout
    _authListener = () {
      _loadFavorites();
      _loadData();
    };
    _authService.authNotifier.addListener(_authListener!);
  }

  @override
  void dispose() {
    if (_authListener != null)
      _authService.authNotifier.removeListener(_authListener!);
    super.dispose();
  }

  Future<void> _loadFavorites() async {
    final token = await _authService.getToken();
    final userData = await _authService.getUserData();
    if (token != null && userData != null) {
      final list = await _apiService.getFavorites(
        userData['id'].toString(),
        token,
      );
      if (mounted) {
        setState(() {
          _favoriteIds = list.toSet();
          _userName = userData['prenom'] ?? userData['nom'];
        });
      }
    } else {
      if (mounted) {
        setState(() {
          _userName = null;
        });
      }
    }
  }

  Future<void> _initLocation() async {
    try {
      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.whileInUse ||
          permission == LocationPermission.always) {
        final pos = await Geolocator.getCurrentPosition();
        setState(() => _currentPosition = pos);
      }
    } catch (e) {
      print('Erreur localisation: $e');
    }
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    _loadFavorites();
    final results = await Future.wait([
      _apiService.getCategories(),
      _apiService.getCommerces(
        lat: _currentPosition?.latitude,
        lon: _currentPosition?.longitude,
        rayon: _radius,
      ),
    ]);

    if (mounted) {
      setState(() {
        _categories = results[0] as List<Categorie>;
        _commerces = results[1] as List<Commerce>;
        _isLoading = false;
      });
    }
  }

  Future<void> _search() async {
    setState(() => _isLoading = true);
    final results = await _apiService.getCommerces(
      nom: _searchController.text,
      idCategorie: _selectedCategoryId,
      lat: _currentPosition?.latitude,
      lon: _currentPosition?.longitude,
      rayon: _radius,
    );
    if (mounted) {
      setState(() {
        _commerces = results;
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    const primaryBlue = Color(0xFF034D92);
    const accentOrange = Color(0xFFF78F1E);

    return RefreshIndicator(
      onRefresh: _loadData,
      color: accentOrange,
      child: CustomScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
              child: Row(
                children: [
                  const CircleAvatar(
                    backgroundColor: accentOrange,
                    radius: 16,
                    child: Icon(Icons.person, size: 20, color: Colors.white),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Bonjour, ${_userName ?? "Visiteur"} !',
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                      color: primaryBlue,
                    ),
                  ),
                  const Spacer(),
                  if (_userName != null)
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 4,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.green.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: Colors.green),
                      ),
                      child: const Row(
                        children: [
                          Icon(
                            Icons.check_circle,
                            color: Colors.green,
                            size: 14,
                          ),
                          SizedBox(width: 4),
                          Text(
                            'Connecté',
                            style: TextStyle(
                              color: Colors.green,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                ],
              ),
            ),
          ),
          // Search Bar
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.03),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: TextField(
                  controller: _searchController,
                  onSubmitted: (_) => _search(),
                  decoration: InputDecoration(
                    hintText: 'Quel commerce recherchez-vous ?',
                    hintStyle: TextStyle(color: Colors.grey[400], fontSize: 14),
                    border: InputBorder.none,
                    icon: const Icon(Icons.search_rounded, color: Colors.grey),
                  ),
                ),
              ),
            ),
          ),

          // Categories
          SliverToBoxAdapter(
            child: SizedBox(
              height: 60,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 12,
                ),
                itemCount: _categories.length + 1,
                itemBuilder: (context, index) {
                  final isAll = index == 0;
                  final cat = isAll ? null : _categories[index - 1];
                  final isSelected = isAll
                      ? _selectedCategoryId == null
                      : _selectedCategoryId == cat?.idcategorie;

                  return Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 4),
                    child: ChoiceChip(
                      label: Text(isAll ? 'Tous' : cat!.nom),
                      selected: isSelected,
                      onSelected: (selected) {
                        setState(() {
                          _selectedCategoryId = isAll ? null : cat?.idcategorie;
                        });
                        _search();
                      },
                      selectedColor: primaryBlue,
                      labelStyle: TextStyle(
                        color: isSelected ? Colors.white : Colors.grey[600],
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                      backgroundColor: Colors.white,
                      showCheckmark: false,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                        side: BorderSide(
                          color: isSelected ? primaryBlue : Colors.grey[200]!,
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
          ),

          // Distance Slider
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 4,
                ),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.02),
                      blurRadius: 10,
                    ),
                  ],
                ),
                child: Row(
                  children: [
                    const Icon(
                      Icons.radar_rounded,
                      color: primaryBlue,
                      size: 20,
                    ),
                    const SizedBox(width: 8),
                    const Text(
                      'Rayon:',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Expanded(
                      child: Slider(
                        value: _radius,
                        min: 1,
                        max: 500,
                        divisions: 100,
                        activeColor: primaryBlue,
                        label: '${_radius.round()} km',
                        onChanged: (value) {
                          setState(() => _radius = value);
                        },
                        onChangeEnd: (_) => _search(),
                      ),
                    ),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Text(
                          '${_radius.round()} km',
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        IconButton(
                          onPressed: () async {
                            final text = await showDialog<String>(
                              context: context,
                              builder: (context) {
                                final ctrl = TextEditingController(
                                  text: _radius.round().toString(),
                                );
                                return AlertDialog(
                                  title: const Text('Entrer la distance (km)'),
                                  content: TextField(
                                    controller: ctrl,
                                    keyboardType:
                                        TextInputType.numberWithOptions(
                                          decimal: false,
                                        ),
                                    decoration: const InputDecoration(
                                      hintText: 'Ex: 120',
                                    ),
                                  ),
                                  actions: [
                                    TextButton(
                                      onPressed: () => Navigator.pop(context),
                                      child: const Text('Annuler'),
                                    ),
                                    ElevatedButton(
                                      onPressed: () =>
                                          Navigator.pop(context, ctrl.text),
                                      child: const Text('OK'),
                                    ),
                                  ],
                                );
                              },
                            );
                            if (text != null && text.isNotEmpty) {
                              final val = double.tryParse(text);
                              if (val != null && val > 0) {
                                setState(() => _radius = val.clamp(1, 500));
                                _search();
                              }
                            }
                          },
                          icon: const Icon(Icons.edit, size: 18),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),

          // Grid Header
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Row(
                children: [
                  const Icon(
                    Icons.storefront_rounded,
                    size: 16,
                    color: primaryBlue,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    '${_commerces.length} COMMERCES À PROXIMITÉ',
                    style: const TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w900,
                      letterSpacing: 2.0,
                      color: Colors.grey,
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Grid Content
          _isLoading
              ? const SliverFillRemaining(
                  child: Center(
                    child: CircularProgressIndicator(color: accentOrange),
                  ),
                )
              : _commerces.isEmpty
              ? SliverFillRemaining(
                  child: Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.search_off_rounded,
                          size: 64,
                          color: Colors.grey[300],
                        ),
                        const SizedBox(height: 16),
                        const Text(
                          'Aucun commerce trouvé',
                          style: TextStyle(color: Colors.grey),
                        ),
                      ],
                    ),
                  ),
                )
              : SliverPadding(
                  padding: const EdgeInsets.all(16),
                  sliver: SliverGrid(
                    gridDelegate:
                        const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          mainAxisSpacing: 16,
                          crossAxisSpacing: 16,
                          childAspectRatio: 0.65,
                        ),
                    delegate: SliverChildBuilderDelegate((context, index) {
                      final commerce = _commerces[index];
                      return CommerceCard(
                        commerce: commerce,
                        isFavorite: _favoriteIds.contains(commerce.idcommerce),
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) =>
                                  CommerceDetailsScreen(commerce: commerce),
                            ),
                          );
                        },
                      );
                    }, childCount: _commerces.length),
                  ),
                ),

          // Bottom spacing
          const SliverToBoxAdapter(child: SizedBox(height: 32)),
        ],
      ),
    );
  }
}
