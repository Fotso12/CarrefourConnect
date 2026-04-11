import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/commerce.dart';

class ApiService {
  // Remplacez par l'IP de votre machine si vous testez sur un téléphone physique
  // Sur émulateur Android, utilisez 10.0.2.2
  static const String baseUrl = 'http://192.168.6.44:8084/api';

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
      
      final response = await http.get(uri);

      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(utf8.decode(response.bodyBytes));
        return data.map((json) => Commerce.fromJson(json)).toList();
      } else {
        throw Exception('Erreur lors du chargement des commerces : ${response.statusCode}');
      }
    } catch (e) {
      print('Erreur ApiService.getCommerces: $e');
      return [];
    }
  }

  Future<List<Commerce>> getAllCommerces() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/commerces'));
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(utf8.decode(response.bodyBytes));
        return data.map((json) => Commerce.fromJson(json)).toList();
      }
      return [];
    } catch (e) {
      print('Erreur ApiService.getAllCommerces: $e');
      return [];
    }
  }

  Future<List<Categorie>> getCategories() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/categories'));

      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(utf8.decode(response.bodyBytes));
        return data.map((json) => Categorie.fromJson(json)).toList();
      } else {
        throw Exception('Erreur lors du chargement des catégories : ${response.statusCode}');
      }
    } catch (e) {
      print('Erreur ApiService.getCategories: $e');
      return [];
    }
  }

  // --- Avis ---
  Future<List<dynamic>> getAvisByCommerce(String commerceId) async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/avis/commerce/$commerceId'));
      if (response.statusCode == 200) {
        return json.decode(utf8.decode(response.bodyBytes));
      }
      return [];
    } catch (e) {
      print('Erreur getAvisByCommerce: $e');
      return [];
    }
  }

  Future<bool> createAvis(Map<String, dynamic> avisData, String? token) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/avis'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
        body: json.encode(avisData),
      );
      return response.statusCode == 200 || response.statusCode == 201;
    } catch (e) {
      print('Erreur createAvis: $e');
      return false;
    }
  }

  // --- Favoris ---
  Future<bool> toggleFavorite(String userId, String commerceId, bool isFavorite, String? token) async {
    try {
      final url = '$baseUrl/utilisateurs/$userId/favoris/$commerceId';
      final headers = {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token'
      };
      
      final response = isFavorite 
        ? await http.delete(Uri.parse(url), headers: headers)
        : await http.post(Uri.parse(url), headers: headers, body: json.encode({}));
      
      if (response.statusCode != 200 && response.statusCode != 201) {
        print('Erreur toggleFavorite API response: ${response.statusCode} - ${response.body}');
      }
      return response.statusCode == 200 || response.statusCode == 201;
    } catch (e) {
      print('Erreur toggleFavorite: $e');
      return false;
    }
  }

  Future<List<String>> getFavorites(String userId, String? token) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/utilisateurs/$userId/favoris'),
        headers: {if (token != null) 'Authorization': 'Bearer $token'},
      );
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(response.body);
        return data.map((id) => id.toString()).toList();
      }
      return [];
    } catch (e) {
      print('Erreur getFavorites: $e');
      return [];
    }
  }

  // --- Authentification ---
  Future<Map<String, dynamic>?> login(String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({'email': email, 'password': password}),
      );
      if (response.statusCode == 200) {
        return json.decode(utf8.decode(response.bodyBytes));
      } else {
        print('Erreur Login: ${response.statusCode} - ${response.body}');
        return null;
      }
    } catch (e) {
      print('Erreur login: $e');
      return null;
    }
  }

  Future<bool> register(String nom, String prenom, String email, String password, String telephone) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/utilisateurs/inscription/visiteur'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'nom': nom,
          'prenom': prenom,
          'email': email,
          'password': password,
          'telephone': telephone,
        }),
      );
      
      if (response.statusCode == 201 || response.statusCode == 200) {
        return true;
      } else {
        print('Erreur Inscription: ${response.statusCode} - ${response.body}');
        return false;
      }
    } catch (e) {
      print('Erreur register: $e');
      return false;
    }
  }
}

