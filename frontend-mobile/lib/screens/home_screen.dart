import 'package:flutter/material.dart';
import '../models/commerce.dart';
import '../services/api_service.dart';
import '../widgets/commerce_card.dart';
import 'commerce_details_screen.dart';
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
  String? _selectedCategoryId;
  bool _isLoading = true;
  Position? _currentPosition;
  double _radius = 10.0; // km par défaut

  @override
  void initState() {
    super.initState();
    _initLocation().then((_) => _loadData());
  }

  Future<void> _initLocation() async {
    try {
      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.whileInUse || permission == LocationPermission.always) {
        final pos = await Geolocator.getCurrentPosition();
        setState(() => _currentPosition = pos);
      }
    } catch (e) {
      print('Erreur localisation: $e');
    }
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
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
                      suffixIcon: IconButton(
                        icon: const Icon(Icons.tune_rounded, color: accentOrange),
                        onPressed: _search,
                      ),
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
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
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
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(20),
                    boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.02), blurRadius: 10)],
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.radar_rounded, color: primaryBlue, size: 20),
                      const SizedBox(width: 8),
                      const Text('Rayon:', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                      Expanded(
                        child: Slider(
                          value: _radius,
                          min: 1,
                          max: 50,
                          divisions: 10,
                          activeColor: primaryBlue,
                          label: '${_radius.round()} km',
                          onChanged: (value) {
                            setState(() => _radius = value);
                          },
                          onChangeEnd: (_) => _search(),
                        ),
                      ),
                      Text('${_radius.round()} km', style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
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
                    const Icon(Icons.storefront_rounded, size: 16, color: primaryBlue),
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
                    child: Center(child: CircularProgressIndicator(color: accentOrange)),
                  )
                : _commerces.isEmpty
                    ? SliverFillRemaining(
                        child: Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(Icons.search_off_rounded, size: 64, color: Colors.grey[300]),
                              const SizedBox(height: 16),
                              const Text('Aucun commerce trouvé', style: TextStyle(color: Colors.grey)),
                            ],
                          ),
                        ),
                      )
                    : SliverPadding(
                        padding: const EdgeInsets.all(16),
                        sliver: SliverGrid(
                          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                            crossAxisCount: 2,
                            mainAxisSpacing: 16,
                            crossAxisSpacing: 16,
                            childAspectRatio: 0.65,
                          ),
                          delegate: SliverChildBuilderDelegate(
                            (context, index) => CommerceCard(
                              commerce: _commerces[index],
                              onTap: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => CommerceDetailsScreen(commerce: _commerces[index]),
                                  ),
                                );
                              },
                            ),
                            childCount: _commerces.length,
                          ),
                        ),
                      ),
            
            // Bottom spacing
            const SliverToBoxAdapter(child: SizedBox(height: 32)),
          ],
        ),
      );
  }
}
