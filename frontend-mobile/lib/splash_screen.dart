import 'dart:math';
import 'package:flutter/material.dart';

class SplashScreen extends StatefulWidget {
  final Widget nextScreen;
  const SplashScreen({super.key, required this.nextScreen});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with TickerProviderStateMixin {
  static const Color primaryBlue = Color(0xFF034D92);
  static const Color darkBlue = Color(0xFF021E3F);
  static const Color accentOrange = Color(0xFFF78F1E);
  static const Color lightBlue = Color(0xFF00ADEF);

  // Controllers
  late AnimationController _convergenceController; // icons converging
  late AnimationController _logoController;         // logo appears
  late AnimationController _textController;         // text appears
  late AnimationController _pulseController;        // logo pulsation
  late AnimationController _exitController;         // fade out

  // Animations
  late Animation<double> _logoScale;
  late Animation<double> _logoOpacity;
  late Animation<double> _textOpacity;
  late Animation<Offset> _textSlide;

  @override
  void initState() {
    super.initState();

    _convergenceController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1800),
    );
    _logoController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );
    _textController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 700),
    );
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    )..repeat(reverse: true);
    _exitController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    );

    _logoScale = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _logoController, curve: Curves.elasticOut),
    );
    _logoOpacity = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _logoController, curve: const Interval(0.0, 0.4)),
    );
    _textSlide = Tween<Offset>(
      begin: const Offset(0, 0.6),
      end: Offset.zero,
    ).animate(
      CurvedAnimation(parent: _textController, curve: Curves.easeOutCubic),
    );
    _textOpacity = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _textController, curve: Curves.easeIn),
    );

    _startSequence();
  }

  Future<void> _startSequence() async {
    await Future.delayed(const Duration(milliseconds: 400));
    // Phase 1: icons converge to center
    _convergenceController.forward();
    // Phase 2: logo appears at the center
    await Future.delayed(const Duration(milliseconds: 1400));
    _logoController.forward();
    // Phase 3: text appears below
    await Future.delayed(const Duration(milliseconds: 600));
    _textController.forward();
    // Wait 4 seconds before transitioning
    await Future.delayed(const Duration(milliseconds: 4000));
    if (mounted) {
      Navigator.of(context).pushReplacement(
        PageRouteBuilder(
          pageBuilder: (_, _, _) => widget.nextScreen,
          transitionsBuilder: (_, anim, _, child) =>
              FadeTransition(opacity: anim, child: child),
          transitionDuration: const Duration(milliseconds: 600),
        ),
      );
    }
  }

  @override
  void dispose() {
    _convergenceController.dispose();
    _logoController.dispose();
    _textController.dispose();
    _pulseController.dispose();
    _exitController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final center = Offset(size.width / 2, size.height / 2 - 60);

    // 8 elements converging from edges: shops/people/pins from all 4 roads
    final elements = [
      // North road (top)
      _ConvElement(
        icon: Icons.store_rounded,
        color: accentOrange,
        start: Offset(center.dx, 40),
        end: center + const Offset(0, -20),
        delay: 0.0,
      ),
      _ConvElement(
        icon: Icons.person_rounded,
        color: lightBlue,
        start: Offset(center.dx - 30, 70),
        end: center + const Offset(-14, -14),
        delay: 0.08,
      ),
      // South road (bottom)
      _ConvElement(
        icon: Icons.restaurant_rounded,
        color: accentOrange,
        start: Offset(center.dx, size.height - 100),
        end: center + const Offset(0, 20),
        delay: 0.05,
      ),
      _ConvElement(
        icon: Icons.person_2_rounded,
        color: lightBlue,
        start: Offset(center.dx + 30, size.height - 130),
        end: center + const Offset(14, 14),
        delay: 0.12,
      ),
      // West road (left)
      _ConvElement(
        icon: Icons.local_grocery_store_rounded,
        color: accentOrange,
        start: Offset(20, center.dy),
        end: center + const Offset(-20, 0),
        delay: 0.02,
      ),
      _ConvElement(
        icon: Icons.person_3_rounded,
        color: lightBlue,
        start: Offset(40, center.dy - 30),
        end: center + const Offset(-14, -14),
        delay: 0.1,
      ),
      // East road (right)
      _ConvElement(
        icon: Icons.local_cafe_rounded,
        color: accentOrange,
        start: Offset(size.width - 20, center.dy),
        end: center + const Offset(20, 0),
        delay: 0.03,
      ),
      _ConvElement(
        icon: Icons.person_4_rounded,
        color: lightBlue,
        start: Offset(size.width - 40, center.dy + 30),
        end: center + const Offset(14, 14),
        delay: 0.07,
      ),
    ];

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [darkBlue, primaryBlue, Color(0xFF0271C7)],
          ),
        ),
        child: Stack(
          children: [
            // Road network background
            CustomPaint(
              size: Size(size.width, size.height),
              painter: _RoadPainter(center: center),
            ),

            // Background glow circles
            Positioned(
              top: -60,
              right: -60,
              child: _GlowCircle(size: 240, color: lightBlue, opacity: 0.07),
            ),
            Positioned(
              bottom: -80,
              left: -40,
              child: _GlowCircle(size: 280, color: accentOrange, opacity: 0.06),
            ),

            // Converging icons
            AnimatedBuilder(
              animation: _convergenceController,
              builder: (context, _) {
                return Stack(
                  children: elements.map((el) {
                    final t = (((_convergenceController.value - el.delay) /
                                (1.0 - el.delay))
                            .clamp(0.0, 1.0));
                    final curved =
                        Curves.easeInCubic.transform(t); // accelerate inward
                    final pos = Offset(
                      el.start.dx + (el.end.dx - el.start.dx) * curved,
                      el.start.dy + (el.end.dy - el.start.dy) * curved,
                    );
                    final opacity = (1.0 - curved * 0.85).clamp(0.0, 1.0);

                    return Positioned(
                      left: pos.dx - 18,
                      top: pos.dy - 18,
                      child: Opacity(
                        opacity: opacity,
                        child: Container(
                          width: 36,
                          height: 36,
                          decoration: BoxDecoration(
                            color: el.color.withValues(alpha: 0.15),
                            shape: BoxShape.circle,
                          ),
                          child: Icon(el.icon, color: el.color, size: 22),
                        ),
                      ),
                    );
                  }).toList(),
                );
              },
            ),

            // Center: animated "explosion" ring when icons arrive
            AnimatedBuilder(
              animation: _logoController,
              builder: (context, _) {
                final t = _logoController.value;
                return Positioned(
                  left: center.dx - 90 * t,
                  top: center.dy - 90 * t,
                  child: Opacity(
                    opacity: (t * 2).clamp(0.0, 1.0) * (1.0 - t).clamp(0.0, 1.0) * 2,
                    child: Container(
                      width: 180 * t,
                      height: 180 * t,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: accentOrange,
                          width: 2,
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),

            // Logo at center
            AnimatedBuilder(
              animation: Listenable.merge([_logoController, _pulseController]),
              builder: (context, _) {
                final pulse = 0.97 + 0.03 * _pulseController.value;
                return Positioned(
                  left: center.dx - 65,
                  top: center.dy - 65,
                  child: FadeTransition(
                    opacity: _logoOpacity,
                    child: ScaleTransition(
                      scale: _logoScale,
                      child: Transform.scale(
                        scale: pulse,
                        child: Container(
                          width: 130,
                          height: 130,
                          decoration: BoxDecoration(
                            color: Colors.white,
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: accentOrange.withValues(alpha: 0.5),
                                blurRadius: 35,
                                spreadRadius: 8,
                              ),
                              BoxShadow(
                                color: Colors.black.withValues(alpha: 0.3),
                                blurRadius: 20,
                                spreadRadius: 2,
                              ),
                            ],
                          ),
                          padding: const EdgeInsets.all(14),
                          child: Image.asset(
                            'assets/icon.png',
                            fit: BoxFit.contain,
                          ),
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),

            // Text below center
            Positioned(
              left: 0,
              right: 0,
              top: center.dy + 90,
              child: SlideTransition(
                position: _textSlide,
                child: FadeTransition(
                  opacity: _textOpacity,
                  child: Column(
                    children: [
                      RichText(
                        textAlign: TextAlign.center,
                        text: const TextSpan(
                          children: [
                            TextSpan(
                              text: 'Carrefour',
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 32,
                                fontWeight: FontWeight.w800,
                                letterSpacing: 0.5,
                              ),
                            ),
                            TextSpan(
                              text: 'Connect',
                              style: TextStyle(
                                color: accentOrange,
                                fontSize: 32,
                                fontWeight: FontWeight.w800,
                                letterSpacing: 0.5,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Commerces • Personnes • Proximité',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Colors.white.withValues(alpha: 0.65),
                          fontSize: 13,
                          letterSpacing: 1.2,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      const SizedBox(height: 32),
                      _PulsingDots(),
                    ],
                  ),
                ),
              ),
            ),

            // Version
            Positioned(
              bottom: 20,
              left: 0,
              right: 0,
              child: FadeTransition(
                opacity: _textOpacity,
                child: Text(
                  'v1.0.0',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    color: Colors.white.withValues(alpha: 0.3),
                    fontSize: 11,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ---------- Data class ----------
class _ConvElement {
  final IconData icon;
  final Color color;
  final Offset start;
  final Offset end;
  final double delay; // 0.0–1.0

  const _ConvElement({
    required this.icon,
    required this.color,
    required this.start,
    required this.end,
    required this.delay,
  });
}

// ---------- Road Painter ----------
class _RoadPainter extends CustomPainter {
  final Offset center;
  _RoadPainter({required this.center});

  @override
  void paint(Canvas canvas, Size size) {
    final roadPaint = Paint()
      ..color = Colors.white.withValues(alpha: 0.04)
      ..strokeWidth = 28
      ..strokeCap = StrokeCap.round;

    final linePaint = Paint()
      ..color = Colors.white.withValues(alpha: 0.07)
      ..strokeWidth = 1.5
      ..strokeCap = StrokeCap.round;

    // 4 roads converging to center
    // North
    canvas.drawLine(Offset(center.dx, 0), center, roadPaint);
    // South
    canvas.drawLine(Offset(center.dx, size.height), center, roadPaint);
    // West
    canvas.drawLine(Offset(0, center.dy), center, roadPaint);
    // East
    canvas.drawLine(Offset(size.width, center.dy), center, roadPaint);

    // Dashed center lines
    _drawDashedLine(canvas, linePaint, Offset(center.dx, 0), center);
    _drawDashedLine(canvas, linePaint, Offset(center.dx, size.height), center);
    _drawDashedLine(canvas, linePaint, Offset(0, center.dy), center);
    _drawDashedLine(canvas, linePaint, Offset(size.width, center.dy), center);

    // Central roundabout circle
    final circlePaint = Paint()
      ..color = Colors.white.withValues(alpha: 0.06)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 20;
    canvas.drawCircle(center, 78, circlePaint);
  }

  void _drawDashedLine(Canvas canvas, Paint paint, Offset from, Offset to) {
    const dashLen = 10.0;
    const gapLen = 8.0;
    final total = (to - from).distance;
    final dir = (to - from) / total;
    double d = 0;
    bool drawing = true;
    while (d < total) {
      final segLen = drawing ? dashLen : gapLen;
      final end = d + segLen > total ? total : d + segLen;
      if (drawing) {
        canvas.drawLine(from + dir * d, from + dir * end, paint);
      }
      d = end;
      drawing = !drawing;
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

// ---------- Glow Circle ----------
class _GlowCircle extends StatelessWidget {
  final double size;
  final Color color;
  final double opacity;
  const _GlowCircle(
      {required this.size, required this.color, required this.opacity});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: color.withValues(alpha: opacity),
      ),
    );
  }
}

// ---------- Pulsing dots ----------
class _PulsingDots extends StatefulWidget {
  @override
  State<_PulsingDots> createState() => _PulsingDotsState();
}

class _PulsingDotsState extends State<_PulsingDots>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat();
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _ctrl,
      builder: (context, _) {
        return Row(
          mainAxisSize: MainAxisSize.min,
          children: List.generate(3, (i) {
            final t = ((_ctrl.value - i * 0.25) % 1.0).clamp(0.0, 1.0);
            final opacity = sin(t * pi).clamp(0.15, 1.0);
            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4),
              child: Container(
                width: 8,
                height: 8,
                decoration: BoxDecoration(
                  color: (i == 1
                          ? const Color(0xFFF78F1E)
                          : Colors.white)
                      .withValues(alpha: opacity),
                  shape: BoxShape.circle,
                ),
              ),
            );
          }),
        );
      },
    );
  }
}
