import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'main_screen.dart';

class AboutScreen extends StatelessWidget {
  const AboutScreen({super.key});

  static const primaryBlue = Color(0xFF034D92);
  static const accentOrange = Color(0xFFF78F1E);
  static const secondaryBlue = Color(0xFF00ADEF);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SingleChildScrollView(
        child: Column(
          children: [
            _buildHero(context),
            _buildMission(),
            _buildStats(),
            _buildGuide(),
            _buildCTA(context),
          ],
        ),
      ),
    );
  }

  Widget _buildHero(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 40),
      width: double.infinity,
      color: const Color(0xFFF8FAFC),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          RichText(
            text: const TextSpan(
              style: TextStyle(
                fontSize: 32,
                fontFamily: 'Roboto',
                fontWeight: FontWeight.w900,
                color: primaryBlue,
                height: 1.2,
              ),
              children: [
                TextSpan(text: 'Votre pont vers\nl\'excellence '),
                TextSpan(
                  text: 'locale',
                  style: TextStyle(color: accentOrange),
                ),
                TextSpan(text: '.'),
              ],
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            'CarrefourConnect est bien plus qu\'un simple annuaire. C\'est une plateforme dynamique conçue pour mettre en lumière le savoir-faire de nos commerçants et faciliter votre quotidien.',
            style: TextStyle(
              fontSize: 16,
              color: Color(0xFF64748B),
              height: 1.6,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 30),
          Wrap(
            spacing: 12,
            children: [
              ElevatedButton(
                onPressed: () {
                  // Navigate to main screen home tab
                  Navigator.of(context).pushReplacement(
                    MaterialPageRoute(builder: (_) => const MainScreen()),
                  );
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: primaryBlue,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 16,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                  elevation: 0,
                ),
                child: const Text(
                  'Explorer les commerces',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
              OutlinedButton(
                onPressed: () => _showPartnerModal(context),
                style: OutlinedButton.styleFrom(
                  foregroundColor: primaryBlue,
                  side: const BorderSide(color: Color(0xFFE2E8F0), width: 2),
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 16,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
                child: const Text(
                  'Devenir partenaire',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMission() {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: accentOrange.withOpacity(0.1),
              borderRadius: BorderRadius.circular(20),
            ),
            child: const Text(
              'NOTRE MISSION',
              style: TextStyle(
                color: accentOrange,
                fontSize: 10,
                fontWeight: FontWeight.w900,
                letterSpacing: 1.2,
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text(
            'Connecter, Valoriser et Simplifier.',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.w900,
              color: primaryBlue,
            ),
          ),
          const SizedBox(height: 16),
          const Text(
            'Dans un monde de plus en plus numérique, nous croyons à la force de l\'économie locale. Notre mission est de donner à chaque commerce une vitrine digitale d\'exception, tout en offrant aux citoyens un outil simple et puissant pour trouver exactement ce dont ils ont besoin.',
            style: TextStyle(
              fontSize: 15,
              color: Color(0xFF64748B),
              height: 1.6,
            ),
          ),
          const SizedBox(height: 12),
          const Text(
            'Qualité, proximité et confiance sont les piliers sur lesquels nous bâtissons cette communauté d\'échange et de découverte.',
            style: TextStyle(
              fontSize: 15,
              color: Color(0xFF64748B),
              height: 1.6,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStats() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
      child: GridView.count(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        crossAxisCount: 2,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        childAspectRatio: 1.3,
        children: [
          _buildStatCard(
            '+1000',
            'Commerces',
            const Color(0xFFF8FAFC),
            primaryBlue,
          ),
          _buildStatCard(
            '98%',
            'Satisfaction',
            const Color(0xFFF0F9FF),
            secondaryBlue,
          ),
          _buildStatCard(
            '24/7',
            'Accessibilité',
            const Color(0xFFFFF7ED),
            accentOrange,
          ),
          _buildStatCard(
            '100%',
            'Local',
            const Color(0xFF0F172A),
            Colors.white,
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard(
    String value,
    String label,
    Color bgColor,
    Color textColor,
  ) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(32),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            value,
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.w900,
              color: textColor,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label.toUpperCase(),
            style: TextStyle(
              fontSize: 9,
              fontWeight: FontWeight.w900,
              color: textColor.withOpacity(0.6),
              letterSpacing: 1,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildGuide() {
    return Container(
      padding: const EdgeInsets.all(24),
      width: double.infinity,
      color: const Color(0xFFF8FAFC),
      child: Column(
        children: [
          const Text(
            'Guide d\'utilisation',
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.w900,
              color: primaryBlue,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            'Tout ce que vous devez savoir pour profiter au mieux de la plateforme.',
            textAlign: TextAlign.center,
            style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13),
          ),
          const SizedBox(height: 32),
          _buildStep(
            1,
            'Recherche & Filtres',
            'Utilisez la barre de recherche intelligente. Filtrez par catégorie ou proximité.',
            primaryBlue,
          ),
          _buildStep(
            2,
            'Détails & Avis',
            'Consultez les photos et les horaires avant de vous déplacer.',
            accentOrange,
          ),
          _buildStep(
            3,
            'Engagement',
            'Pour les pros, créez votre compte et publiez vos offres.',
            secondaryBlue,
          ),
        ],
      ),
    );
  }

  Widget _buildStep(int number, String title, String desc, Color color) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 24),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            height: 48,
            width: 48,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 10,
                ),
              ],
            ),
            child: Center(
              child: Text(
                '$number',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w900,
                  color: color,
                ),
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w900,
                    color: primaryBlue,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  desc,
                  style: const TextStyle(
                    fontSize: 13,
                    color: Color(0xFF64748B),
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCTA(BuildContext context) {
    return Container(
      margin: const EdgeInsets.all(24),
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [primaryBlue, secondaryBlue],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(40),
        boxShadow: [
          BoxShadow(
            color: primaryBlue.withOpacity(0.3),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        children: [
          const Text(
            'Prêt à rejoindre l\'aventure ?',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.w900,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'CarrefourConnect est votre meilleur allié pour booster votre commerce.',
            textAlign: TextAlign.center,
            style: TextStyle(
              color: Colors.white.withOpacity(0.9),
              fontSize: 14,
            ),
          ),
          const SizedBox(height: 32),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () => _showPartnerModal(context),
              style: ElevatedButton.styleFrom(
                backgroundColor: accentOrange,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
              ),
              child: const Text(
                'Inscrire un commerce',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _showPartnerModal(BuildContext context) {
    const webUrl = 'https://carrefourconnect.com';
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text(
          'Devenir partenaire',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        content: const Text(
          'Pour créer un compte professionnel et inscrire un commerce, veuillez utiliser la version web depuis un ordinateur.\n\nRendez-vous sur notre site pour vous inscrire et soumettre votre commerce.\n\nLe processus permet de vérifier votre identité et d\'ajouter les informations administratives nécessaires.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Fermer'),
          ),
          ElevatedButton(
            onPressed: () async {
              final uri = Uri.parse(webUrl);
              if (!await launchUrl(uri, mode: LaunchMode.externalApplication)) {
                // ignore: avoid_print
                print('Impossible d\'ouvrir $webUrl');
              }
              Navigator.pop(ctx);
            },
            child: const Text('Ouvrir le site'),
          ),
        ],
      ),
    );
  }
}
