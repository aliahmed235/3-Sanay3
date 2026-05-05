import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/models/customer_models.dart';
import 'package:talat_sanaye3/providers/auth_session_provider.dart';
import 'package:talat_sanaye3/screens/user/chat_screens.dart';
import 'package:talat_sanaye3/screens/user/offers_screen.dart';
import 'package:talat_sanaye3/services/request_service.dart';

const _navy = Color(0xFF121A34);
const _yellow = Color(0xFFFFB703);
const _pageBg = Color(0xFFF7F8FC);

class BookingsScreen extends StatefulWidget {
  const BookingsScreen({super.key});

  @override
  State<BookingsScreen> createState() => _BookingsScreenState();
}

class _BookingsScreenState extends State<BookingsScreen> {
  late Future<List<CustomerRequestModel>> _future;

  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  Future<List<CustomerRequestModel>> _load() {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    return ServiceRequestApi().getMyRequests(token: token);
  }

  Future<void> _refresh() async {
    setState(() => _future = _load());
    await _future;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _pageBg,
      appBar: AppBar(
        backgroundColor: _pageBg,
        elevation: 0,
        title: const Text(
          'Bookings',
          style: TextStyle(color: Colors.black, fontWeight: FontWeight.w900),
        ),
      ),
      body: RefreshIndicator(
        color: _yellow,
        onRefresh: _refresh,
        child: FutureBuilder<List<CustomerRequestModel>>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.waiting) {
              return const Center(
                child: CircularProgressIndicator(color: _yellow),
              );
            }
            if (snapshot.hasError) {
              return _EmptyState(
                icon: Icons.error_outline,
                title: 'Could not load requests',
                subtitle: snapshot.error.toString(),
              );
            }
            final requests = snapshot.data ?? const [];
            if (requests.isEmpty) {
              return const _EmptyState(
                icon: Icons.assignment_outlined,
                title: 'No requests yet',
                subtitle: 'Requests you create will appear here.',
              );
            }
            return ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: requests.length,
              separatorBuilder: (_, _) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                return _RequestCard(
                  request: requests[index],
                  onChanged: _refresh,
                );
              },
            );
          },
        ),
      ),
    );
  }
}

class RequestDetailsScreen extends StatefulWidget {
  final int requestId;

  const RequestDetailsScreen({super.key, required this.requestId});

  @override
  State<RequestDetailsScreen> createState() => _RequestDetailsScreenState();
}

class _RequestDetailsScreenState extends State<RequestDetailsScreen> {
  late Future<CustomerRequestModel> _future;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  Future<CustomerRequestModel> _load() {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    return ServiceRequestApi().getRequestDetails(
      token: token,
      requestId: widget.requestId,
    );
  }

  Future<void> _run(Future<void> Function(String token) action) async {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    setState(() => _busy = true);
    try {
      await action(token);
      setState(() => _future = _load());
      await _future;
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Action failed: $e')));
    } finally {
      if (mounted) setState(() => _busy = false);
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
          '#REQ${widget.requestId}',
          style: const TextStyle(
            color: Colors.black,
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: FutureBuilder<CustomerRequestModel>(
        future: _future,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(
              child: CircularProgressIndicator(color: _yellow),
            );
          }
          if (snapshot.hasError || !snapshot.hasData) {
            return _EmptyState(
              icon: Icons.error_outline,
              title: 'Could not load details',
              subtitle: snapshot.error?.toString() ?? 'No data',
            );
          }
          final request = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _DetailsHeader(request: request),
              const SizedBox(height: 14),
              _InfoTile(
                icon: Icons.location_on_outlined,
                title: 'Location',
                value: request.address,
              ),
              _InfoTile(
                icon: Icons.notes_outlined,
                title: 'Description',
                value: request.description,
              ),
              if (request.acceptedProvider != null)
                _InfoTile(
                  icon: Icons.handyman_outlined,
                  title: 'Professional',
                  value: request.acceptedProvider!.name,
                ),
              const SizedBox(height: 14),
              _Actions(
                request: request,
                busy: _busy,
                onCancel: () => _run(
                  (token) => ServiceRequestApi().cancelRequest(
                    token: token,
                    requestId: request.id,
                  ),
                ),
                onStart: () => _run(
                  (token) => ServiceRequestApi().startService(
                    token: token,
                    requestId: request.id,
                  ),
                ),
                onComplete: () => _run(
                  (token) => ServiceRequestApi().completeService(
                    token: token,
                    requestId: request.id,
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _RequestCard extends StatelessWidget {
  final CustomerRequestModel request;
  final Future<void> Function() onChanged;

  const _RequestCard({required this.request, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => RequestDetailsScreen(requestId: request.id),
          ),
        ).then((_) => onChanged());
      },
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    request.title.isEmpty
                        ? 'Request #${request.id}'
                        : request.title,
                    style: const TextStyle(
                      fontWeight: FontWeight.w900,
                      fontSize: 16,
                    ),
                  ),
                ),
                _StatusChip(status: request.status),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              request.address,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(color: Colors.black54),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(
                  Icons.local_offer_outlined,
                  size: 18,
                  color: request.offerCount > 0 ? _yellow : Colors.black38,
                ),
                const SizedBox(width: 5),
                Text('${request.offerCount} offers'),
                const Spacer(),
                if (request.acceptedProvider != null)
                  TextButton(
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) =>
                              ChatByRequestScreen(requestId: request.id),
                        ),
                      );
                    },
                    child: const Text('Chat'),
                  )
                else if (request.offerCount > 0)
                  TextButton(
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => OffersScreen(requestId: request.id),
                        ),
                      ).then((_) => onChanged());
                    },
                    child: const Text('View offers'),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _DetailsHeader extends StatelessWidget {
  final CustomerRequestModel request;

  const _DetailsHeader({required this.request});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  request.title,
                  style: const TextStyle(
                    color: _navy,
                    fontSize: 20,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
              _StatusChip(status: request.status),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            request.serviceType,
            style: const TextStyle(color: Colors.black54),
          ),
          if (request.rating != null) ...[
            const SizedBox(height: 10),
            Text(
              'Rating: ${request.rating}',
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
          ],
        ],
      ),
    );
  }
}

class _Actions extends StatelessWidget {
  final CustomerRequestModel request;
  final bool busy;
  final VoidCallback onCancel;
  final VoidCallback onStart;
  final VoidCallback onComplete;

  const _Actions({
    required this.request,
    required this.busy,
    required this.onCancel,
    required this.onStart,
    required this.onComplete,
  });

  @override
  Widget build(BuildContext context) {
    final status = request.status.toUpperCase();
    return Column(
      children: [
        if (request.offerCount > 0 && request.acceptedProvider == null)
          _ActionButton(
            label: 'View offers',
            icon: Icons.local_offer_outlined,
            onPressed: busy
                ? null
                : () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => OffersScreen(requestId: request.id),
                      ),
                    );
                  },
          ),
        if (request.acceptedProvider != null)
          _ActionButton(
            label: 'Open chat',
            icon: Icons.chat_bubble_outline,
            onPressed: busy
                ? null
                : () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) =>
                            ChatByRequestScreen(requestId: request.id),
                      ),
                    );
                  },
          ),
        if (status == 'OPEN' || status == 'ACCEPTED')
          _ActionButton(
            label: 'Start service',
            icon: Icons.play_arrow_rounded,
            onPressed: busy ? null : onStart,
          ),
        if (status == 'IN_PROGRESS')
          _ActionButton(
            label: 'Complete service',
            icon: Icons.check_rounded,
            onPressed: busy ? null : onComplete,
          ),
        if (status == 'OPEN')
          _ActionButton(
            label: 'Cancel request',
            icon: Icons.close_rounded,
            danger: true,
            onPressed: busy ? null : onCancel,
          ),
      ],
    );
  }
}

class _ActionButton extends StatelessWidget {
  final String label;
  final IconData icon;
  final VoidCallback? onPressed;
  final bool danger;

  const _ActionButton({
    required this.label,
    required this.icon,
    required this.onPressed,
    this.danger = false,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: SizedBox(
        width: double.infinity,
        height: 48,
        child: ElevatedButton.icon(
          onPressed: onPressed,
          icon: Icon(icon),
          label: Text(label),
          style: ElevatedButton.styleFrom(
            backgroundColor: danger ? Colors.redAccent : _navy,
            foregroundColor: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        ),
      ),
    );
  }
}

class _InfoTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String value;

  const _InfoTile({
    required this.icon,
    required this.title,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: _yellow),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 4),
                Text(value, style: const TextStyle(color: Colors.black54)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  final String status;

  const _StatusChip({required this.status});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF4D0),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        status.isEmpty ? 'OPEN' : status,
        style: const TextStyle(
          color: _navy,
          fontSize: 11,
          fontWeight: FontWeight.w900,
        ),
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;

  const _EmptyState({
    required this.icon,
    required this.title,
    required this.subtitle,
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
          ],
        ),
      ),
    );
  }
}
