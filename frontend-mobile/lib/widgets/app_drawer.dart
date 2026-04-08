import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:url_launcher/url_launcher.dart';
import '../screens/login_screen.dart';
import '../screens/signup_screen.dart';
import '../screens/profile_screen.dart';
import '../screens/favorites_screen.dart';
import '../services/auth_service.dart';

class AppDrawer extends StatelessWidget {
  const AppDrawer({super.key});

  static const primaryBlue = Color(0xFF034D92);
  static const accentOrange = Color(0xFFF78F1E);

  @override
  Widget build(BuildContext context) {
    final authService = AuthService();

    return Drawer(
      backgroundColor: Colors.white,
      child: FutureBuilder<Map<String, dynamic>?>(
        future: authService.getUserData(),
        builder: (context, snapshot) {
          final userData = snapshot.data;
          final isLoggedIn = userData != null;

          return Column(
            children: [
              _buildHeader(userData),
              Expanded(
                child: ListView(
                  padding: EdgeInsets.zero,
                  children: [
                    if (!isLoggedIn) ...[
                      _buildDrawerItem(
                        icon: FontAwesomeIcons.rightToBracket,
                        title: 'Connexion',
                        onTap: () {
                          Navigator.pop(context);
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => const LoginScreen()),
                          );
                        },
                      ),
                      _buildDrawerItem(
                        icon: FontAwesomeIcons.userPlus,
                        title: 'Inscription',
                        onTap: () {
                          Navigator.pop(context);
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => const SignupScreen()),
                          );
                        },
                      ),
                    ] else ...[
                      _buildDrawerItem(
                        icon: FontAwesomeIcons.userLarge,
                        title: 'Mon Profil',
                        onTap: () {
                          Navigator.pop(context);
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => const ProfileScreen()),
                          );
                        },
                      ),
                      _buildDrawerItem(
                        icon: FontAwesomeIcons.heartCircleCheck,
                        title: 'Mes Favoris',
                        onTap: () {
                          Navigator.pop(context);
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => const FavoritesScreen()),
                          );
                        },
                      ),
                      const Divider(height: 32),
                      _buildDrawerItem(
                        icon: FontAwesomeIcons.rightFromBracket,
                        title: 'Déconnexion',
                        onTap: () async {
                          showDialog(
                            context: context,
                            builder: (context) => AlertDialog(
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                              title: const Text('Déconnexion', style: TextStyle(fontWeight: FontWeight.w900)),
                              content: const Text('Voulez-vous vraiment vous déconnecter ?'),
                              actions: [
                                TextButton(onPressed: () => Navigator.pop(context), child: const Text('Annuler')),
                                ElevatedButton(
                                  onPressed: () async {
                                    await authService.logout();
                                    if (context.mounted) {
                                      Navigator.of(context).pushNamedAndRemoveUntil('/', (route) => false);
                                    }
                                  },
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: Colors.red,
                                    foregroundColor: Colors.white,
                                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                                  ),
                                  child: const Text('Déconnexion'),
                                ),
                              ],
                            ),
                          );
                        },
                      ),
                    ],
                  ],
                ),
              ),
              _buildFooter(),
            ],
          );
        },
      ),
    );
  }

  Widget _buildHeader(Map<String, dynamic>? user) {
    final String fullName = user != null ? '${user['prenom']} ${user['nom']}' : 'CarrefourConnect';
    final String initial = user != null 
        ? (user['nom'] ?? 'U').toString().substring(0, 1).toUpperCase()
        : '';

    return DrawerHeader(
      decoration: const BoxDecoration(
        color: primaryBlue,
      ),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 70,
              height: 70,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(24),
                boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 10)],
              ),
              child: Center(
                child: user != null 
                  ? Text(initial, style: const TextStyle(fontSize: 32, fontWeight: FontWeight.w900, color: accentOrange))
                  : ClipRRect(
                      borderRadius: BorderRadius.circular(16),
                      child: Image.asset('assets/icon.png', width: 50, height: 50, fit: BoxFit.cover, 
                        errorBuilder: (c, e, s) => const Icon(Icons.person, color: accentOrange, size: 40)),
                    ),
              ),
            ),
            const SizedBox(height: 12),
            Text(
              fullName,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w900,
              ),
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDrawerItem({
    required IconData icon,
    required String title,
    required VoidCallback onTap,
  }) {
    return ListTile(
      leading: Icon(icon, color: primaryBlue, size: 18),
      title: Text(
        title,
        style: const TextStyle(
          color: Color(0xFF1E293B),
          fontSize: 15,
          fontWeight: FontWeight.w600,
        ),
      ),
      onTap: onTap,
      horizontalTitleGap: 0,
    );
  }

  Widget _buildFooter() {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          const Text(
            'Version 1.0.0',
            style: TextStyle(color: Colors.grey, fontSize: 12),
          ),
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(
                onPressed: () {},
                icon: const Icon(FontAwesomeIcons.facebook, size: 20, color: Colors.grey),
              ),
              IconButton(
                onPressed: () {},
                icon: const Icon(FontAwesomeIcons.instagram, size: 20, color: Colors.grey),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
