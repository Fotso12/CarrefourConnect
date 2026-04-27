import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import 'main_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> with SingleTickerProviderStateMixin {
  final _authService = AuthService();
  final _apiService = ApiService();
  
  Map<String, dynamic>? _user;
  bool _isLoading = true;
  bool _isEditing = false;
  bool _isSaving = false;

  final _nomController = TextEditingController();
  final _prenomController = TextEditingController();
  final _emailController = TextEditingController();
  final _phoneController = TextEditingController();

  static const primaryBlue = Color(0xFF034D92);
  static const secondaryBlue = Color(0xFF003B71);
  static const accentOrange = Color(0xFFF78F1E);

  late AnimationController _animController;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(vsync: this, duration: const Duration(milliseconds: 600));
    _fadeAnimation = CurvedAnimation(parent: _animController, curve: Curves.easeIn);
    _loadUser();
  }

  @override
  void dispose() {
    _animController.dispose();
    _nomController.dispose();
    _prenomController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  Future<void> _loadUser() async {
    final data = await _authService.getUserData();
    if (mounted) {
      if (data != null) {
        setState(() {
          _user = data;
          _nomController.text = data['nom'] ?? '';
          _prenomController.text = data['prenom'] ?? '';
          _emailController.text = data['email'] ?? '';
          _phoneController.text = data['telephone'] ?? '';
          _isLoading = false;
        });
        _animController.forward();
      } else {
        setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _handleSave() async {
    setState(() => _isSaving = true);
    final token = await _authService.getToken();
    final userId = _user?['id']?.toString();

    if (userId != null) {
      final updatedData = {
        'id': userId,
        'nom': _nomController.text,
        'prenom': _prenomController.text,
        'email': _emailController.text,
        'telephone': _phoneController.text,
        'role': _user?['role'],
      };

      final result = await _apiService.updateProfile(userId, updatedData, token);
      
      if (result != null) {
        await _authService.saveUserData(result);
        if (mounted) {
          setState(() {
            _user = result;
            _isEditing = false;
            _isSaving = false;
          });
          _showSuccessSnackBar('Profil mis à jour avec succès');
        }
      } else {
        if (mounted) {
          setState(() => _isSaving = false);
          _showErrorSnackBar('Erreur lors de la mise à jour');
        }
      }
    }
  }

  void _showSuccessSnackBar(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(children: [const Icon(Icons.check_circle, color: Colors.white), const SizedBox(width: 12), Text(msg)]),
        backgroundColor: Colors.green,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    );
  }

  void _showErrorSnackBar(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(children: [const Icon(Icons.error, color: Colors.white), const SizedBox(width: 12), Text(msg)]),
        backgroundColor: Colors.red,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    );
  }

  void _showChangePasswordDialog() {
    final oldPassController = TextEditingController();
    final newPassController = TextEditingController();
    final confirmPassController = TextEditingController();
    bool isChanging = false;

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
          backgroundColor: Colors.white,
          surfaceTintColor: Colors.white,
          title: const Text('Sécurité', style: TextStyle(fontWeight: FontWeight.w900, color: primaryBlue)),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text('Changez votre mot de passe pour sécuriser votre accès.', style: TextStyle(fontSize: 13, color: Colors.grey)),
              const SizedBox(height: 20),
              _buildDialogField(oldPassController, 'Ancien mot de passe', true, Icons.lock_outline),
              const SizedBox(height: 12),
              _buildDialogField(newPassController, 'Nouveau mot de passe', true, Icons.vpn_key_outlined),
              const SizedBox(height: 12),
              _buildDialogField(confirmPassController, 'Confirmer le nouveau', true, Icons.check_circle_outline),
            ],
          ),
          actions: [
            TextButton(
              onPressed: isChanging ? null : () => Navigator.pop(context),
              child: const Text('ANNULER', style: TextStyle(color: Colors.grey, fontWeight: FontWeight.bold)),
            ),
            ElevatedButton(
              onPressed: isChanging ? null : () async {
                if (newPassController.text.isEmpty || newPassController.text != confirmPassController.text) {
                  _showErrorSnackBar('Les mots de passe ne correspondent pas');
                  return;
                }
                
                setDialogState(() => isChanging = true);
                final token = await _authService.getToken();
                final success = await _apiService.changeProfilePassword(
                  _user?['id']?.toString() ?? '',
                  oldPassController.text,
                  newPassController.text,
                  token
                );
                
                if (mounted) {
                  setDialogState(() => isChanging = false);
                  Navigator.pop(context);
                  if (success) {
                    _showSuccessSnackBar('Mot de passe mis à jour !');
                  } else {
                    _showErrorSnackBar('Ancien mot de passe incorrect');
                  }
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: primaryBlue,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
              ),
              child: isChanging 
                ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                : const Text('CONFIRMER', style: TextStyle(fontWeight: FontWeight.bold)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDialogField(TextEditingController ctrl, String hint, bool isPass, IconData icon) {
    return TextField(
      controller: ctrl,
      obscureText: isPass,
      decoration: InputDecoration(
        prefixIcon: Icon(icon, size: 18, color: primaryBlue),
        hintText: hint,
        hintStyle: const TextStyle(fontSize: 14),
        filled: true,
        fillColor: const Color(0xFFF1F5F9),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide.none),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator(color: primaryBlue)));
    }

    if (_user == null) {
      return Scaffold(
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.no_accounts_rounded, size: 60, color: Colors.grey),
              const SizedBox(height: 16),
              const Text('Profil introuvable', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: () => Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const MainScreen())),
                child: const Text('Retourner à l\'accueil'),
              ),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      body: FadeTransition(
        opacity: _fadeAnimation,
        child: CustomScrollView(
          physics: const BouncingScrollPhysics(),
          slivers: [
            _buildSliverHeader(),
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildSectionHeader('INFORMATIONS DU COMPTE', action: IconButton(
                      onPressed: () => setState(() => _isEditing = !_isEditing),
                      icon: Icon(_isEditing ? Icons.close_rounded : Icons.edit_note_rounded, color: primaryBlue, size: 28),
                    )),
                    const SizedBox(height: 8),
                    _buildFieldGroup([
                      _buildModernField('Prénom', _prenomController, Icons.person_outline_rounded),
                      _buildModernField('Nom', _nomController, Icons.badge_outlined),
                      _buildModernField('Contact', _phoneController, Icons.phone_android_rounded),
                    ]),
                    
                    if (_isEditing) ...[
                      const SizedBox(height: 24),
                      _buildSaveButton(),
                    ],

                    const SizedBox(height: 40),
                    _buildSectionHeader('SÉCURITÉ'),
                    const SizedBox(height: 16),
                    _buildMenuCard(
                      FontAwesomeIcons.shieldHalved,
                      'Protection du compte',
                      'Modifier votre mot de passe',
                      onTap: _showChangePasswordDialog
                    ),
                    const SizedBox(height: 12),
                    _buildMenuCard(
                      FontAwesomeIcons.arrowRightFromBracket,
                      'Déconnexion',
                      'Quitter votre session actuelle',
                      color: Colors.red,
                      onTap: () => _confirmLogout(context)
                    ),
                    const SizedBox(height: 60),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSliverHeader() {
    final String initial = (_user?['prenom'] ?? 'U').toString().substring(0, 1).toUpperCase();
    
    return SliverAppBar(
      expandedHeight: 220,
      pinned: true,
      backgroundColor: secondaryBlue,
      elevation: 0,
      flexibleSpace: FlexibleSpaceBar(
        background: Stack(
          children: [
            Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [primaryBlue, secondaryBlue],
                ),
              ),
            ),
            Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const SizedBox(height: 40),
                  Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(color: Colors.white, shape: BoxShape.circle),
                    child: CircleAvatar(
                      radius: 45,
                      backgroundColor: primaryBlue.withOpacity(0.1),
                      child: Text(initial, style: const TextStyle(fontSize: 40, fontWeight: FontWeight.w900, color: primaryBlue)),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    '${_user?['prenom']} ${_user?['nom']}'.toUpperCase(),
                    style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w900, letterSpacing: 1.1),
                  ),
                  const SizedBox(height: 4),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                    decoration: BoxDecoration(color: Colors.white.withOpacity(0.2), borderRadius: BorderRadius.circular(20)),
                    child: Text(
                      _user?['email'] ?? '',
                      style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w500),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title, {Widget? action}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(title, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w900, color: Color(0xFF64748B), letterSpacing: 1.5)),
        if (action != null) action,
      ],
    );
  }

  Widget _buildFieldGroup(List<Widget> children) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.04), blurRadius: 20, offset: const Offset(0, 10))],
      ),
      child: Column(children: children),
    );
  }

  Widget _buildModernField(String label, TextEditingController ctrl, IconData icon) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(color: const Color(0xFFF1F5F9), borderRadius: BorderRadius.circular(12)),
            child: Icon(icon, color: primaryBlue, size: 20),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: const TextStyle(color: Colors.grey, fontSize: 11, fontWeight: FontWeight.bold)),
                TextField(
                  controller: ctrl,
                  enabled: _isEditing,
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Color(0xFF1E293B)),
                  decoration: const InputDecoration(isDense: true, border: InputBorder.none, contentPadding: EdgeInsets.zero),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSaveButton() {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton(
        onPressed: _isSaving ? null : _handleSave,
        style: ElevatedButton.styleFrom(
          backgroundColor: accentOrange,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(vertical: 18),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          elevation: 4,
          shadowColor: accentOrange.withOpacity(0.4),
        ),
        child: _isSaving 
          ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
          : const Text('ENREGISTRER LES CHANGEMENTS', style: TextStyle(fontWeight: FontWeight.w900, letterSpacing: 1.1)),
      ),
    );
  }

  Widget _buildMenuCard(IconData icon, String title, String subtitle, {Color? color, required VoidCallback onTap}) {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(24),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: const Color(0xFFF1F5F9), width: 1),
          ),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(color: (color ?? primaryBlue).withOpacity(0.08), shape: BoxShape.circle),
                child: FaIcon(icon, color: color ?? primaryBlue, size: 18),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 15, color: Color(0xFF1E293B))),
                    Text(subtitle, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12, fontWeight: FontWeight.w500)),
                  ],
                ),
              ),
              const Icon(Icons.arrow_forward_ios_rounded, color: Color(0xFFCBD5E1), size: 16),
            ],
          ),
        ),
      ),
    );
  }

  void _confirmLogout(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
        title: const Text('Déconnexion', style: TextStyle(fontWeight: FontWeight.w900)),
        content: const Text('Souhaitez-vous fermer votre session ?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('A REVOIR', style: TextStyle(color: Colors.grey))),
          ElevatedButton(
            onPressed: () async {
              await _authService.logout();
              if (mounted) {
                Navigator.of(context).pushAndRemoveUntil(MaterialPageRoute(builder: (_) => const MainScreen()), (route) => false);
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red, foregroundColor: Colors.white, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
            child: const Text('QUITTER', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}
