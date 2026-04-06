import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/commerce.dart';

class ApiService {
  // Remplacez par l'IP de votre machine si vous testez sur un téléphone physique
  // Sur émulateur Android, utilisez 10.0.2.2
  static const String baseUrl = 'http://192.168.1.100:8084/api';

  Future<List<Commerce>> getCommerces({String? nom, String? idCategorie}) async {
    try {
      final queryParams = {
        if (nom != null && nom.isNotEmpty) 'nom': nom,
        if (idCategorie != null && idCategorie != 'null') 'idCategorie': idCategorie,
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
}
