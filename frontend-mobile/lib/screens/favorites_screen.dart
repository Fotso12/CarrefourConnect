import 'package:flutter/material.dart';
import '../models/commerce.dart';
import '../services/api_service.dart';
import '../services/auth_service.dart';
import '../widgets/commerce_card.dart';
import 'commerce_details_screen.dart';

class FavoritesScreen extends StatefulWidget {
  const FavoritesScreen({super.key});

  @override
  State<FavoritesScreen> createState() => _FavoritesScreenState();
}

class _FavoritesScreenState extends State<FavoritesScreen> {
  final ApiService _apiService = ApiService();
  final AuthService _authService = AuthService();
  List<Commerce> _favoriteCommerces = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadFavorites();
  }

  Future<void> _loadFavorites() async {
    setState(() => _isLoading = true);
    final token = await _authService.getToken();
    final userData = await _authService.getUserData();

    if (token != null && userData != null) {
      final favoriteIds = await _apiService.getFavorites(userData['id'].toString(), token);
      final allCommerces = await _apiService.getAllCommerces(); // Simplified: fetch all and filter
      
      setState(() {
        _favoriteCommerces = allCommerces.where((c) => favoriteIds.contains(c.idcommerce)).toList();
        _isLoading = false;
      });
    } else {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    const primaryBlue = Color(0xFF034D92);
    const accentOrange = Color(0xFFF78F1E);

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Mes Favoris', style: TextStyle(fontWeight: FontWeight.w900, color: primaryBlue)),
        centerTitle: true,
        backgroundColor: Colors.white,
        elevation: 0,
        foregroundColor: primaryBlue,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: accentOrange))
          : _favoriteCommerces.isEmpty
              ? _buildEmptyState()
              : GridView.builder(
                  padding: const EdgeInsets.all(16),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    mainAxisSpacing: 16,
                    crossAxisSpacing: 16,
                    childAspectRatio: 0.65,
                  ),
                  itemCount: _favoriteCommerces.length,
                  itemBuilder: (context, index) {
                    final commerce = _favoriteCommerces[index];
                    return CommerceCard(
                      commerce: commerce,
                      isFavorite: true,
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => CommerceDetailsScreen(commerce: commerce),
                          ),
                        ).then((_) => _loadFavorites()); // Refresh on return
                      },
                    );
                  },
                ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.favorite_border_rounded, size: 64, color: Colors.grey[300]),
          const SizedBox(height: 16),
          const Text('Vous n\'avez pas encore de favoris', style: TextStyle(color: Colors.grey)),
        ],
      ),
    );
  }
}
