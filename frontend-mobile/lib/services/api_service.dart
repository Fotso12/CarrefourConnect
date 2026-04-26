import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/commerce.dart';

class ApiService {
  static const String baseUrl = 'http://127.0.0.1:8084/api';

  // --- Helpers pour réduire la duplication ---

  Map<String, String> _headers(String? token) => {
    'Content-Type': 'application/json',
    if (token != null) 'Authorization': 'Bearer $token',
  };

  Future<http.Response> _handleRequest(Future<http.Response> Function() request, String methodName) async {
    try {
      final response = await request();
      if (response.statusCode >= 200 && response.statusCode < 300) {
        return response;
      } else {
        print('Erreur $methodName: ${response.statusCode} - ${response.body}');
        return response;
      }
    } catch (e) {
      print('Exception dans $methodName: $e');
      rethrow;
    }
  }

  // --- Commerces ---

  Future<List<Commerce>> getCommerces({
    String? nom,
    String? idCategorie,
    double? lat,
    double? lon,
    double? rayon,
  }) async {
    try {
      final queryParams = {
        if (nom != null && nom.isNotEmpty) 'nom': nom,
        if (idCategorie != null && idCategorie != 'null') 'idCategorie': idCategorie,
        if (lat != null) 'lat': lat.toString(),
        if (lon != null) 'lon': lon.toString(),
        if (rayon != null) 'rayon': rayon.toString(),
      };

      final uri = Uri.parse('$baseUrl/commerces/rechercher').replace(queryParameters: queryParams);
      final response = await _handleRequest(() => http.get(uri), 'getCommerces');
      
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(utf8.decode(response.bodyBytes));
        return data.map((json) => Commerce.fromJson(json)).toList();
      }
    } catch (_) {}
    return [];
  }

  Future<List<Commerce>> getAllCommerces() async {
    try {
      final response = await _handleRequest(() => http.get(Uri.parse('$baseUrl/commerces')), 'getAllCommerces');
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(utf8.decode(response.bodyBytes));
        return data.map((json) => Commerce.fromJson(json)).toList();
      }
    } catch (_) {}
    return [];
  }

  Future<List<Categorie>> getCategories() async {
    try {
      final response = await _handleRequest(() => http.get(Uri.parse('$baseUrl/categories')), 'getCategories');
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(utf8.decode(response.bodyBytes));
        return data.map((json) => Categorie.fromJson(json)).toList();
      }
    } catch (_) {}
    return [];
  }

  // --- Avis ---

  Future<List<dynamic>> getAvisByCommerce(String commerceId) async {
    try {
      final response = await _handleRequest(() => http.get(Uri.parse('$baseUrl/avis/commerce/$commerceId')), 'getAvisByCommerce');
      if (response.statusCode == 200) {
        return json.decode(utf8.decode(response.bodyBytes));
      }
    } catch (_) {}
    return [];
  }

  Future<bool> createAvis(Map<String, dynamic> avisData, String? token) async {
    final response = await _handleRequest(
      () => http.post(Uri.parse('$baseUrl/avis'), headers: _headers(token), body: json.encode(avisData)), 
      'createAvis'
    );
    return response.statusCode == 200 || response.statusCode == 201;
  }

  // --- Favoris ---

  Future<bool> toggleFavorite(String userId, String commerceId, bool isFavorite, String? token) async {
    final url = Uri.parse('$baseUrl/utilisateurs/$userId/favoris/$commerceId');
    final response = isFavorite
        ? await _handleRequest(() => http.delete(url, headers: _headers(token)), 'toggleFavorite (delete)')
        : await _handleRequest(() => http.post(url, headers: _headers(token), body: json.encode({})), 'toggleFavorite (post)');
    return response.statusCode == 200 || response.statusCode == 201;
  }

  Future<List<String>> getFavorites(String userId, String? token) async {
    try {
      final response = await _handleRequest(() => http.get(Uri.parse('$baseUrl/utilisateurs/$userId/favoris'), headers: _headers(token)), 'getFavorites');
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(response.body);
        return data.map((id) => id.toString()).toList();
      }
    } catch (_) {}
    return [];
  }

  // --- Authentification ---

  Future<Map<String, dynamic>?> login(String email, String password) async {
    final response = await _handleRequest(
      () => http.post(Uri.parse('$baseUrl/auth/login'), headers: _headers(null), body: json.encode({'email': email, 'password': password})), 
      'login'
    );
    return response.statusCode == 200 ? json.decode(utf8.decode(response.bodyBytes)) : null;
  }

  Future<bool> requestPasswordReset(String email) async {
    final response = await _handleRequest(
      () => http.post(Uri.parse('$baseUrl/auth/mot-de-passe-oublie'), headers: _headers(null), body: json.encode({'email': email})), 
      'requestPasswordReset'
    );
    return response.statusCode == 200;
  }

  Future<String?> verifyResetCode(String email, String code) async {
    final response = await _handleRequest(
      () => http.post(Uri.parse('$baseUrl/auth/verifier-code'), headers: _headers(null), body: json.encode({'email': email, 'code': code})), 
      'verifyResetCode'
    );
    if (response.statusCode == 200) {
      return json.decode(utf8.decode(response.bodyBytes))['token']?.toString();
    }
    return null;
  }

  Future<bool> resetPassword(String email, String token, String nouveauMotDePasse) async {
    final response = await _handleRequest(
      () => http.post(Uri.parse('$baseUrl/auth/reinitialiser-mot-de-passe'), headers: _headers(null), body: json.encode({
        'email': email, 'token': token, 'nouveauMotDePasse': nouveauMotDePasse,
      })), 
      'resetPassword'
    );
    return response.statusCode == 200;
  }

  Future<bool> register(String nom, String prenom, String email, String password, String telephone) async {
    final response = await _handleRequest(
      () => http.post(Uri.parse('$baseUrl/utilisateurs/inscription/visiteur'), headers: _headers(null), body: json.encode({
        'nom': nom, 'prenom': prenom, 'email': email, 'password': password, 'telephone': telephone,
      })), 
      'register'
    );
    return response.statusCode == 201 || response.statusCode == 200;
  }
}
