import 'package:flutter/material.dart';
// 1. Déplace l'import ici (en haut)
import 'verify_code_screen.dart';
// 2. Utilise de préférence l'import 'package' pour éviter les erreurs de chemin relatif
import 'package:carrefourconnect_mobile/services/api_service.dart'; 

class RequestCodeScreen extends StatefulWidget {
  const RequestCodeScreen({super.key});

  @override
  State<RequestCodeScreen> createState() => _RequestCodeScreenState();
}

class _RequestCodeScreenState extends State<RequestCodeScreen> {
  final TextEditingController _emailCtrl = TextEditingController();
  final ApiService _api = ApiService();
  bool _loading = false;

  void _submit() async {
    final email = _emailCtrl.text.trim();
    if (email.isEmpty) return;
    
    setState(() => _loading = true);
    final ok = await _api.requestPasswordReset(email);
    setState(() => _loading = false);

    if (ok) {
      if (mounted) { // Utilisation simplifiée de context.mounted
        Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => VerifyCodeScreen(email: email)),
        );
      }
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Erreur lors de la demande.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mot de passe oublié')),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Entrez votre adresse email. Nous vous enverrons un code à 5 chiffres.'),
            const SizedBox(height: 16),
            TextField(
              controller: _emailCtrl, 
              keyboardType: TextInputType.emailAddress, 
              decoration: const InputDecoration(labelText: 'Email'),
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _loading ? null : _submit,
                child: _loading 
                  ? const SizedBox(
                      width: 18, 
                      height: 18, 
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                    ) 
                  : const Text('Envoyer le code'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
// SUPPRIME L'IMPORT QUI ÉTAIT ICI