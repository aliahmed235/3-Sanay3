import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/models/customer_models.dart';
import 'package:talat_sanaye3/models/request_model.dart';
import 'package:talat_sanaye3/providers/auth_session_provider.dart';
import 'package:talat_sanaye3/screens/user/chat_screens.dart';
import 'package:talat_sanaye3/screens/user/user_home_screen.dart';
import 'package:talat_sanaye3/services/request_service.dart';

const _navy = Color(0xFF121A34);
const _yellow = Color(0xFFFFB703);
const _pageBg = Color(0xFFF7F8FC);

class OffersScreen extends StatefulWidget {
  final int requestId;
  final CreatedServiceRequest? request;

  const OffersScreen({super.key, required this.requestId, this.request});

  @override
  State<OffersScreen> createState() => _OffersScreenState();
}

class _OffersScreenState extends State<OffersScreen> {
  late Future<List<OfferModel>> _future;
  bool _accepting = false;

  @override
  void initState() {
    super.initState();
    _future = _loadOffers();
  }

  Future<List<OfferModel>> _loadOffers() {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    return ServiceRequestApi().getOffersForRequest(
      token: token,
      requestId: widget.requestId,
    );
  }

  Future<void> _refresh() async {
    setState(() => _future = _loadOffers());
    await _future;
  }

  Future<void> _accept(OfferModel offer) async {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    setState(() => _accepting = true);
    try {
      await ServiceRequestApi().acceptOffer(
        token: token,
        requestId: widget.requestId,
        offerId: offer.id,
      );
      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) =>
              OfferAcceptedScreen(requestId: widget.requestId, offer: offer),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Could not accept offer: $e')));
    } finally {
      if (mounted) setState(() => _accepting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _pageBg,
      appBar: AppBar(
        backgroundColor: _pageBg,
        elevation: 0,
        title: Text(
          'Offers for #REQ${widget.requestId}',
          style: const TextStyle(
            color: Colors.black,
            fontWeight: FontWeight.w800,
          ),
        ),
        actions: [
          IconButton(
            onPressed: _refresh,
            icon: const Icon(Icons.refresh, color: Colors.black87),
          ),
        ],
      ),
      body: RefreshIndicator(
        color: _yellow,
        onRefresh: _refresh,
        child: FutureBuilder<List<OfferModel>>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.waiting) {
              return const Center(
                child: CircularProgressIndicator(color: _yellow),
              );
            }
            if (snapshot.hasError) {
              return _StateView(
                icon: Icons.error_outline,
                title: 'Could not load offers',
                subtitle: snapshot.error.toString(),
                actionLabel: 'Try again',
                onAction: _refresh,
              );
            }

            final offers = snapshot.data ?? const [];
            if (offers.isEmpty) {
              return ListView(
                padding: const EdgeInsets.all(20),
                children: const [
                  SizedBox(height: 80),
                  _StateView(
                    icon: Icons.hourglass_empty,
                    title: 'Waiting for offers',
                    subtitle:
                        'Professionals will appear here as soon as they send offers.',
                  ),
                ],
              );
            }

            return ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: offers.length,
              separatorBuilder: (_, _) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final offer = offers[index];
                return _OfferCard(
                  offer: offer,
                  busy: _accepting,
                  onAccept: () => _accept(offer),
                );
              },
            );
          },
        ),
      ),
    );
  }
}

class OfferAcceptedScreen extends StatelessWidget {
  final int requestId;
  final OfferModel offer;

  const OfferAcceptedScreen({
    super.key,
    required this.requestId,
    required this.offer,
  });

  @override
  Widget build(BuildContext context) {
    final providerName = offer.provider?.name ?? 'The professional';
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(22),
          child: Column(
            children: [
              const Spacer(),
              const CircleAvatar(
                radius: 50,
                backgroundColor: Color(0xFFFFF4D0),
                child: Icon(Icons.check_rounded, size: 62, color: _yellow),
              ),
              const SizedBox(height: 24),
              const Text(
                'Request Confirmed',
                style: TextStyle(
                  color: _navy,
                  fontSize: 24,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                '$providerName accepted your request. You can now open the chat and coordinate the details.',
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.black54, height: 1.35),
              ),
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: _pageBg,
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Request ID',
                      style: TextStyle(color: Colors.black54),
                    ),
                    Text(
                      '#REQ$requestId',
                      style: const TextStyle(
                        color: _navy,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ],
                ),
              ),
              const Spacer(),
              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton.icon(
                  onPressed: () {
                    Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(
                        builder: (_) =>
                            ChatByRequestScreen(requestId: requestId),
                      ),
                    );
                  },
                  icon: const Icon(Icons.chat_bubble_outline),
                  label: const Text('Open Chat'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: _navy,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                ),
              ),
              TextButton(
                onPressed: () {
                  Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (_) => const UserHomeScreen()),
                    (_) => false,
                  );
                },
                child: const Text('Go to Home'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _OfferCard extends StatelessWidget {
  final OfferModel offer;
  final bool busy;
  final VoidCallback onAccept;

  const _OfferCard({
    required this.offer,
    required this.busy,
    required this.onAccept,
  });

  @override
  Widget build(BuildContext context) {
    final provider = offer.provider;
    final name = provider?.name.isNotEmpty == true
        ? provider!.name
        : 'Professional';
    final price = offer.price == null
        ? 'Price pending'
        : 'EGP ${offer.price!.toStringAsFixed(0)}';
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.black.withValues(alpha: 0.05)),
      ),
      child: Column(
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 24,
                backgroundColor: const Color(0xFFFFF4D0),
                child: Text(
                  name[0].toUpperCase(),
                  style: const TextStyle(
                    color: _navy,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      name,
                      style: const TextStyle(fontWeight: FontWeight.w900),
                    ),
                    const SizedBox(height: 3),
                    Text(
                      provider?.rating == null
                          ? 'New professional'
                          : '${provider!.rating!.toStringAsFixed(1)} rating',
                      style: const TextStyle(
                        color: Colors.black54,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              Text(
                price,
                style: const TextStyle(
                  color: _navy,
                  fontWeight: FontWeight.w900,
                  fontSize: 16,
                ),
              ),
            ],
          ),
          if (offer.note.isNotEmpty) ...[
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                offer.note,
                style: const TextStyle(color: Colors.black54, height: 1.35),
              ),
            ),
          ],
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            height: 46,
            child: ElevatedButton(
              onPressed: busy ? null : onAccept,
              style: ElevatedButton.styleFrom(
                backgroundColor: _navy,
                foregroundColor: Colors.white,
                elevation: 0,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              child: const Text(
                'Confirm this offer',
                style: TextStyle(fontWeight: FontWeight.w800),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _StateView extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final String? actionLabel;
  final VoidCallback? onAction;

  const _StateView({
    required this.icon,
    required this.title,
    required this.subtitle,
    this.actionLabel,
    this.onAction,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 58, color: Colors.black26),
            const SizedBox(height: 14),
            Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
            const SizedBox(height: 6),
            Text(
              subtitle,
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.black54),
            ),
            if (actionLabel != null) ...[
              const SizedBox(height: 14),
              TextButton(onPressed: onAction, child: Text(actionLabel!)),
            ],
          ],
        ),
      ),
    );
  }
}
