import 'package:flutter/material.dart';
// import '../services/api_service.dart';
import 'package:carrefourconnect_mobile/services/api_service.dart';
import 'reset_password_screen.dart';

class VerifyCodeScreen extends StatefulWidget {
  final String email;
  const VerifyCodeScreen({super.key, required this.email});

  @override
  State<VerifyCodeScreen> createState() => _VerifyCodeScreenState();
}

class _VerifyCodeScreenState extends State<VerifyCodeScreen> {
  final TextEditingController _codeCtrl = TextEditingController();
  final ApiService _api = ApiService();
  bool _loading = false;

  void _submit() async {
    final code = _codeCtrl.text.trim();
    if (code.isEmpty) return;
    setState(() => _loading = true);
    final token = await _api.verifyResetCode(widget.email, code);
    setState(() => _loading = false);
    if (token != null) {
      if (context.mounted) {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => ResetPasswordScreen(email: widget.email, token: token)),
        );
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Code invalide ou expiré.')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Vérifier le code')),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Nous avons envoyé un code à ${widget.email}'),
            const SizedBox(height: 16),
            TextField(controller: _codeCtrl, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Code (5 chiffres)')),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _loading ? null : _submit,
                child: _loading ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white)) : const Text('Vérifier'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}


