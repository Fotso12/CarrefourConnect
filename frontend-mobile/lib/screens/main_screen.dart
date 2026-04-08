import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'home_screen.dart';
import 'map_screen.dart';
import 'about_screen.dart';
import '../widgets/app_drawer.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _selectedIndex = 0;
  
  static const List<Widget> _screens = [
    HomeScreen(),
    MapScreen(),
    AboutScreen(),
  ];

  static const primaryBlue = Color(0xFF034D92);
  static const accentOrange = Color(0xFFF78F1E);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: false,
        title: RichText(
          text: const TextSpan(
            children: [
              TextSpan(
                text: 'Carrefour',
                style: TextStyle(
                  color: primaryBlue,
                  fontSize: 22,
                  fontWeight: FontWeight.w900,
                ),
              ),
              TextSpan(
                text: 'Connect',
                style: TextStyle(
                  color: accentOrange,
                  fontSize: 22,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ],
          ),
        ),
        actions: [
          Builder(
            builder: (context) => IconButton(
              onPressed: () => Scaffold.of(context).openEndDrawer(),
              icon: const Icon(FontAwesomeIcons.bars, color: primaryBlue, size: 20),
            ),
          ),
          const SizedBox(width: 8),
        ],
      ),
      endDrawer: const AppDrawer(),
      body: _screens[_selectedIndex],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, -2),
            ),
          ],
        ),
        child: BottomNavigationBar(
          currentIndex: _selectedIndex,
          onTap: (index) {
            setState(() {
              _selectedIndex = index;
            });
          },
          backgroundColor: Colors.white,
          selectedItemColor: primaryBlue,
          unselectedItemColor: Colors.grey[400],
          selectedFontSize: 12,
          unselectedFontSize: 12,
          type: BottomNavigationBarType.fixed,
          items: const [
            BottomNavigationBarItem(
              icon: Icon(FontAwesomeIcons.house, size: 18),
              activeIcon: Icon(FontAwesomeIcons.houseUser, size: 18),
              label: 'Accueil',
            ),
            BottomNavigationBarItem(
              icon: Icon(FontAwesomeIcons.mapLocationDot, size: 18),
              activeIcon: Icon(FontAwesomeIcons.mapLocation, size: 18),
              label: 'Carte',
            ),
            BottomNavigationBarItem(
              icon: Icon(FontAwesomeIcons.circleInfo, size: 18),
              activeIcon: Icon(FontAwesomeIcons.info, size: 18),
              label: 'À propos',
            ),
          ],
        ),
      ),
    );
  }
}
