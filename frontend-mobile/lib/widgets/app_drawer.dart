import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:url_launcher/url_launcher.dart';

class AppDrawer extends StatelessWidget {
  const AppDrawer({super.key});

  static const primaryBlue = Color(0xFF034D92);
  static const accentOrange = Color(0xFFF78F1E);

  @override
  Widget build(BuildContext context) {
    return Drawer(
      backgroundColor: Colors.white,
      child: Column(
        children: [
          _buildHeader(),
          Expanded(
            child: ListView(
              padding: EdgeInsets.zero,
              children: [
                _buildDrawerItem(
                  icon: FontAwesomeIcons.rightToBracket,
                  title: 'Connexion',
                  onTap: () {
                    Navigator.pop(context);
                    // TODO: Navigation vers Connexion
                  },
                ),
                _buildDrawerItem(
                  icon: FontAwesomeIcons.userPlus,
                  title: 'Inscription',
                  onTap: () {
                    Navigator.pop(context);
                    // TODO: Navigation vers Inscription
                  },
                ),
                const Divider(indent: 20, endIndent: 20, color: Color(0xFFF1F5F9)),
                _buildDrawerItem(
                  icon: FontAwesomeIcons.plusCircle,
                  title: 'Ajouter un commerce',
                  onTap: () {
                    Navigator.pop(context);
                    _launchUrl('https://great-rats-love.loca.lt/inscription-commercant');
                  },
                ),
                _buildDrawerItem(
                  icon: FontAwesomeIcons.headset,
                  title: 'Support client',
                  onTap: () {
                    Navigator.pop(context);
                  },
                ),
              ],
            ),
          ),
          _buildFooter(),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return DrawerHeader(
      decoration: const BoxDecoration(
        color: primaryBlue,
      ),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
              ),
              child: const Icon(FontAwesomeIcons.store, color: primaryBlue, size: 40),
            ),
            const SizedBox(height: 12),
            const Text(
              'CarrefourConnect',
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w900,
              ),
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
      visualDensity: const VisualDensity(vertical: -1),
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

  Future<void> _launchUrl(String url) async {
    final uri = Uri.parse(url);
    if (!await launchUrl(uri)) {
      throw Exception('Could not launch $url');
    }
  }
}
