import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/auth_session_provider.dart';
import 'package:talat_sanaye3/providers/location_provider.dart';
import 'package:talat_sanaye3/screens/user/account_screen.dart';
import 'package:talat_sanaye3/screens/user/bookings_screen.dart';
import 'package:talat_sanaye3/screens/user/chat_screens.dart';
import 'package:talat_sanaye3/screens/user/location_setup_screen.dart';
import 'package:talat_sanaye3/screens/user/offers_screen.dart';
import 'package:talat_sanaye3/screens/user/service_request_flow.dart';
import 'package:talat_sanaye3/services/request_service.dart';

class UserHomeScreen extends StatefulWidget {
  const UserHomeScreen({super.key});

  @override
  State<UserHomeScreen> createState() => _UserHomeScreenState();
}

class _UserHomeScreenState extends State<UserHomeScreen> {
  int _currentIndex = 0;

  final List<Widget> _pages = [
    const _HomeBody(),
    const BookingsScreen(),
    const ChatRoomsScreen(),
    const AccountScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF6F7FB),
      body: _pages[_currentIndex],
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _currentIndex,
        onTap: (index) => setState(() => _currentIndex = index),
        selectedItemColor: const Color(0xFFFFB703),
        unselectedItemColor: Colors.black38,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home_filled), label: "Home"),
          BottomNavigationBarItem(
            icon: Icon(Icons.calendar_month_outlined),
            label: "Bookings",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.chat_bubble_outline),
            label: "Messages",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person_outline),
            label: "Account",
          ),
        ],
      ),
    );
  }
}

// ─── Home Body ────────────────────────────────────────────────────────────────

class _HomeBody extends StatelessWidget {
  const _HomeBody();

  @override
  Widget build(BuildContext context) {
    final categories = const [
      _Category(
        id: "plumber",
        title: "Plumber",
        subtitle: "Fix leaks and\nwater problems",
        imageAsset: "assets/images/plumber.jpeg",
      ),
      _Category(
        id: "electrician",
        title: "Electrician",
        subtitle: "Fix wiring and\nelectrical issues",
        imageAsset: "assets/images/electrician.jpeg",
      ),
      _Category(
        id: "carpenter",
        title: "Carpenter",
        subtitle: "Woodwork and\nfurniture repair",
        imageAsset: "assets/images/carpenter.jpeg",
      ),
    ];

    final popularServices = const [
      _PopularService(
        id: "leak_repair",
        title: "Leak Repair",
        icon: Icons.plumbing,
      ),
      _PopularService(
        id: "light_fixing",
        title: "Light Fixing",
        icon: Icons.lightbulb_outline,
      ),
      _PopularService(
        id: "socket_repair",
        title: "Socket Repair",
        icon: Icons.electrical_services_outlined,
      ),
      _PopularService(
        id: "door_fixing",
        title: "Door Fixing",
        icon: Icons.door_front_door_outlined,
      ),
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF6F7FB),
      appBar: AppBar(
        elevation: 0,
        backgroundColor: const Color(0xFFF6F7FB),
        titleSpacing: 16,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: const [
            Text(
              "Hello!",
              style: TextStyle(
                color: Colors.black,
                fontWeight: FontWeight.w700,
                fontSize: 20,
              ),
            ),
            SizedBox(height: 2),
            Text(
              "How can we help you today?",
              style: TextStyle(color: Colors.black54, fontSize: 13),
            ),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(14),
              ),
              child: IconButton(
                onPressed: () => _showOfferNotifications(context),
                icon: const Icon(
                  Icons.notifications_none,
                  color: Colors.black87,
                ),
              ),
            ),
          ),
        ],
      ),
      body: ListView(
        children: [
          const SizedBox(height: 10),
          Consumer<LocationProvider>(
            builder: (context, locationProvider, _) {
              final location = locationProvider.currentLocation;

              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: InkWell(
                  borderRadius: BorderRadius.circular(16),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) =>
                            const LocationSetupScreen(replaceStack: false),
                      ),
                    );
                  },
                  child: Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: Row(
                      children: [
                        const Icon(
                          Icons.location_on_outlined,
                          color: Color(0xFFFFB703),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            location?.placeName ?? "Choose service location",
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontWeight: FontWeight.w700,
                              color: Colors.black87,
                            ),
                          ),
                        ),
                        const Icon(
                          Icons.keyboard_arrow_down,
                          color: Colors.black45,
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),

          const SizedBox(height: 18),

          // ── Choose a Service ──
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              "Choose a Service",
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
            ),
          ),
          const SizedBox(height: 12),

          // ✅ Categories ثابتة في Row
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: categories.asMap().entries.map((entry) {
                final i = entry.key;
                final c = entry.value;
                return Expanded(
                  child: Padding(
                    padding: EdgeInsets.only(
                      right: i < categories.length - 1 ? 8 : 0,
                    ),
                    child: _CategoryCard(
                      category: c,
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => ConfirmServiceLocationScreen(
                              categoryId: c.id,
                              title: c.title,
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                );
              }).toList(),
            ),
          ),

          const SizedBox(height: 18),

          // ── Popular Services ──
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  "Popular Services",
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                ),
                TextButton(
                  onPressed: () {},
                  child: const Text(
                    "View all  >",
                    style: TextStyle(color: Colors.black54),
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 8),

          // ✅ Popular - بيودي صفحة عند الضغط
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: popularServices.map((s) {
                return _PopularServiceChip(
                  service: s,
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => ConfirmServiceLocationScreen(
                          categoryId: _categoryIdForPopularService(s.id),
                          title: s.title,
                          initialProblemTitle: s.title,
                          initialProblemDescription:
                              _descriptionForPopularService(s.id),
                        ),
                      ),
                    );
                  },
                );
              }).toList(),
            ),
          ),

          const SizedBox(height: 18),

          // ✅ QuickReliableCard حجمها الأصلي بدون Expanded
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
            child: _QuickReliableCard(
              onBookNow: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => const ConfirmServiceLocationScreen(
                      categoryId: 'plumber',
                      title: 'Plumber',
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

Future<void> _showOfferNotifications(BuildContext context) async {
  final token = context.read<AuthSessionProvider>().accessToken ?? '';
  showModalBottomSheet(
    context: context,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(22)),
    ),
    builder: (context) {
      return FutureBuilder(
        future: ServiceRequestApi().getMyRequests(token: token),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const SizedBox(
              height: 220,
              child: Center(
                child: CircularProgressIndicator(color: Color(0xFFFFB703)),
              ),
            );
          }
          final requests = (snapshot.data ?? [])
              .where((request) => request.offerCount > 0)
              .toList();
          return SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Notifications',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.w900,
                      color: Color(0xFF121A34),
                    ),
                  ),
                  const SizedBox(height: 12),
                  if (requests.isEmpty)
                    const Padding(
                      padding: EdgeInsets.symmetric(vertical: 32),
                      child: Center(
                        child: Text(
                          'No new offers yet',
                          style: TextStyle(color: Colors.black54),
                        ),
                      ),
                    )
                  else
                    ...requests.map(
                      (request) => ListTile(
                        contentPadding: EdgeInsets.zero,
                        leading: const CircleAvatar(
                          backgroundColor: Color(0xFFFFF4D0),
                          child: Icon(
                            Icons.local_offer_outlined,
                            color: Color(0xFFFFB703),
                          ),
                        ),
                        title: Text(
                          '${request.offerCount} offer(s) for ${request.title}',
                          style: const TextStyle(fontWeight: FontWeight.w800),
                        ),
                        subtitle: Text('#REQ${request.id}'),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () {
                          Navigator.pop(context);
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) =>
                                  OffersScreen(requestId: request.id),
                            ),
                          );
                        },
                      ),
                    ),
                ],
              ),
            ),
          );
        },
      );
    },
  );
}

// ─── Category ─────────────────────────────────────────────────────────────────

class _Category {
  final String id;
  final String title;
  final String subtitle;
  final String imageAsset;
  const _Category({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.imageAsset,
  });
}

class _CategoryCard extends StatelessWidget {
  final _Category category;
  final VoidCallback onTap;

  const _CategoryCard({required this.category, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            AspectRatio(
              aspectRatio: 1,
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.asset(
                  category.imageAsset,
                  fit: BoxFit.cover,
                  errorBuilder: (_, _, _) => Container(
                    decoration: BoxDecoration(
                      color: const Color(0xFFF0F0F0),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(
                      Icons.image_not_supported_outlined,
                      size: 36,
                      color: Colors.black26,
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              category.title,
              style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
            ),
            const SizedBox(height: 2),
            Text(
              category.subtitle,
              style: const TextStyle(
                color: Colors.black54,
                fontSize: 10,
                height: 1.2,
              ),
            ),
            const SizedBox(height: 8),
            Align(
              alignment: Alignment.centerRight,
              child: Container(
                width: 30,
                height: 30,
                decoration: BoxDecoration(
                  color: const Color(0xFFFFC65A),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Icon(
                  Icons.arrow_forward,
                  size: 16,
                  color: Colors.black87,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ─── Popular Service ──────────────────────────────────────────────────────────

class _PopularService {
  final String id;
  final String title;
  final IconData icon;
  const _PopularService({
    required this.id,
    required this.title,
    required this.icon,
  });
}

class _PopularServiceChip extends StatelessWidget {
  final _PopularService service;
  final VoidCallback onTap;

  const _PopularServiceChip({required this.service, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(16),
      onTap: onTap,
      child: Container(
        width: 78,
        padding: const EdgeInsets.symmetric(vertical: 10),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          children: [
            Icon(service.icon, size: 26, color: Colors.black87),
            const SizedBox(height: 8),
            Text(
              service.title,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 11, color: Colors.black87),
            ),
          ],
        ),
      ),
    );
  }
}

String _categoryIdForPopularService(String id) {
  switch (id) {
    case 'light_fixing':
    case 'socket_repair':
      return 'electrician';
    case 'door_fixing':
      return 'carpenter';
    case 'leak_repair':
    default:
      return 'plumber';
  }
}

String _descriptionForPopularService(String id) {
  switch (id) {
    case 'light_fixing':
      return 'Need help checking or installing a light fixture safely.';
    case 'socket_repair':
      return 'Need an electrician to inspect and repair a faulty socket.';
    case 'door_fixing':
      return 'Need help fixing a door, hinge, lock, or wooden frame issue.';
    case 'leak_repair':
    default:
      return 'Need help finding and repairing a water leak.';
  }
}

// ─── Quick Reliable Card ──────────────────────────────────────────────────────

class _QuickReliableCard extends StatelessWidget {
  final VoidCallback onBookNow;
  const _QuickReliableCard({required this.onBookNow});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(18),
        gradient: const LinearGradient(
          colors: [Color(0xFF0D1B2A), Color(0xFF1B263B)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Icon(Icons.access_time, color: Color(0xFFFFC65A)),
          ),
          const SizedBox(width: 12),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Quick & Reliable Service",
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                SizedBox(height: 4),
                Text(
                  "Book trusted professionals\nin just a few taps.",
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 12,
                    height: 1.2,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          ElevatedButton(
            onPressed: onBookNow,
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFFFC65A),
              foregroundColor: Colors.black87,
              shape: const StadiumBorder(),
              elevation: 0,
            ),
            child: const Text("Book Now"),
          ),
        ],
      ),
    );
  }
}

// ─── Placeholder Pages ────────────────────────────────────────────────────────
