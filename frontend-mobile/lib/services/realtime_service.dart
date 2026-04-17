import 'dart:async';
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';

class RealtimeService {
  static RealtimeService? _instance;
  static RealtimeService get instance => _instance ??= RealtimeService._();

  WebSocketChannel? _channel;
  final _controller = StreamController<Map<String, dynamic>>.broadcast();
  Stream<Map<String, dynamic>> get stream => _controller.stream;

  RealtimeService._();

  void connect({String url = 'ws://127.0.0.1:8084/ws'}) {
    try {
      _channel = WebSocketChannel.connect(Uri.parse(url));
      _channel!.stream.listen(
        (message) {
          try {
            final decoded =
                jsonDecode(message as String) as Map<String, dynamic>;
            _controller.add(decoded);
          } catch (e) {
            // ignore malformed message
          }
        },
        onDone: _reconnect,
        onError: (_) => _reconnect(),
      );
    } catch (e) {
      _scheduleReconnect();
    }
  }

  void _reconnect() {
    _channel = null;
    _scheduleReconnect();
  }

  void _scheduleReconnect() {
    Future.delayed(const Duration(seconds: 3), () {
      if (_channel == null) connect();
    });
  }

  void dispose() {
    _channel?.sink.close();
    _controller.close();
  }
}
