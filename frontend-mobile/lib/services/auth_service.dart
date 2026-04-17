import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  static const String _tokenKey = 'auth_token';
  static const String _userKey = 'user_data';
  static const String _allowAutoLoginKey = 'allow_auto_login';
  static final AuthService _instance = AuthService._internal();

  // in-memory token/user used when user doesn't want "remember me"
  String? _inMemoryToken;
  Map<String, dynamic>? _inMemoryUser;

  // Broadcast stream to notify UI about login/logout events
  final StreamController<bool> _authController =
      StreamController<bool>.broadcast();
  // ValueNotifier holds current auth state so new listeners get current value immediately
  final ValueNotifier<bool> authNotifier = ValueNotifier<bool>(false);

  factory AuthService() => _instance;

  AuthService._internal();

  // Stream that emits the current auth state immediately when listened to,
  // then forwards subsequent events from the broadcast controller.
  Stream<bool> get authStateChanges async* {
    yield authNotifier.value;
    yield* _authController.stream;
  }

  /// Initialize service and emit initial auth state (useful at app start)
  Future<void> init() async {
    // Force-disable auto-login on startup to respect user's request
    final prefs = await SharedPreferences.getInstance();
    // Clear any previous allow flag and persisted token/user to avoid unwanted auto-login
    await prefs.setBool(_allowAutoLoginKey, false);
    await prefs.remove(_tokenKey);
    await prefs.remove(_userKey);
    _inMemoryToken = null;
    _inMemoryUser = null;
    _authController.add(false);
    authNotifier.value = false;
  }

  Future<void> saveToken(String token, {bool remember = true}) async {
    if (remember) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_tokenKey, token);
      await prefs.setBool(_allowAutoLoginKey, true);
    } else {
      _inMemoryToken = token;
      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool(_allowAutoLoginKey, false);
    }
    // notify listeners
    _authController.add(true);
    authNotifier.value = true;
  }

  Future<void> saveUserData(
    Map<String, dynamic> userData, {
    bool remember = true,
  }) async {
    if (remember) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_userKey, json.encode(userData));
      await prefs.setBool(_allowAutoLoginKey, true);
    } else {
      _inMemoryUser = Map<String, dynamic>.from(userData);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool(_allowAutoLoginKey, false);
    }
    // notify listeners
    _authController.add(true);
    authNotifier.value = true;
  }

  Future<Map<String, dynamic>?> getUserData() async {
    if (_inMemoryUser != null) return _inMemoryUser;
    final prefs = await SharedPreferences.getInstance();
    final String? userStr = prefs.getString(_userKey);
    if (userStr == null) return null;
    return json.decode(userStr);
  }

  Future<String?> getToken() async {
    if (_inMemoryToken != null) return _inMemoryToken;
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await prefs.remove(_userKey);
    await prefs.setBool(_allowAutoLoginKey, false);
    _inMemoryToken = null;
    _inMemoryUser = null;
    _authController.add(false);
    authNotifier.value = false;
  }

  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }

  void dispose() {
    _authController.close();
  }
}
