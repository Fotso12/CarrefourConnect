class Categorie {
  final String? idcategorie;
  final String nom;

  Categorie({this.idcategorie, required this.nom});

  factory Categorie.fromJson(Map<String, dynamic> json) {
    return Categorie(
      idcategorie: json['idcategorie']?.toString(),
      nom: json['nom'] ?? '',
    );
  }
}

class Media {
  final String? idmedia;
  final String url;
  final bool estPrincipale;

  Media({this.idmedia, required this.url, required this.estPrincipale});

  factory Media.fromJson(Map<String, dynamic> json) {
    return Media(
      idmedia: json['idmedia']?.toString(),
      url: (json['url'] ?? '').replaceAll('localhost', '192.168.6.44').replaceAll('192.168.6.95', '192.168.6.44'),
      estPrincipale: json['estPrincipale'] ?? false,
    );
  }
}

class Commerce {
  final String? idcommerce;
  final String nom;
  final String? description;
  final String? adresse;
  final String? ville;
  final String? region;
  final double? latitude;
  final double? longitude;
  final String? telephone;
  final Categorie? categorie;
  final List<Media> images;
  final String? imagePrincipale;
  final double? noteGlobale;

  Commerce({
    this.idcommerce,
    required this.nom,
    this.description,
    this.adresse,
    this.ville,
    this.region,
    this.latitude,
    this.longitude,
    this.telephone,
    this.categorie,
    required this.images,
    this.imagePrincipale,
    this.noteGlobale,
  });

  factory Commerce.fromJson(Map<String, dynamic> json) {
    double? lat;
    double? lon;
    String? addr;
    String? city;
    String? rgn;

    if (json['localisations'] != null && (json['localisations'] as List).isNotEmpty) {
      final loc = json['localisations'][0];
      lat = loc['lat'] != null ? double.tryParse(loc['lat'].toString()) : null;
      lon = loc['lon'] != null ? double.tryParse(loc['lon'].toString()) : null;
      addr = loc['adresse'];
      city = loc['ville'];
      rgn = loc['region'];
    }

    return Commerce(
      idcommerce: json['idcommerce']?.toString(),
      nom: json['nom'] ?? '',
      description: json['description'],
      adresse: addr ?? json['adresse'],
      ville: city ?? json['ville'],
      region: rgn ?? json['region'],
      latitude: lat ?? (json['latitude'] != null ? double.tryParse(json['latitude'].toString()) : null),
      longitude: lon ?? (json['longitude'] != null ? double.tryParse(json['longitude'].toString()) : null),
      telephone: json['telephone1'] ?? json['telephone'],
      categorie: json['categorie'] != null ? Categorie.fromJson(json['categorie']) : null,
      images: (json['images'] as List? ?? [])
          .map((img) => Media.fromJson(img))
          .toList(),
      imagePrincipale: json['imagePrincipale']?.replaceAll('localhost', '192.168.6.44').replaceAll('192.168.6.95', '192.168.6.44'),
      noteGlobale: json['noteGlobale'] != null ? double.tryParse(json['noteGlobale'].toString()) : null,
    );
  }

  String get mainImageUrl {
    if (images.isEmpty) {
      return 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=400';
    }
    final principal = images.firstWhere((img) => img.estPrincipale, orElse: () => images.first);
    return principal.url;
  }
}
