import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/models/customer_models.dart';
import 'package:talat_sanaye3/providers/auth_session_provider.dart';
import 'package:talat_sanaye3/providers/location_provider.dart';
import 'package:talat_sanaye3/services/chat_service.dart';
import 'package:url_launcher/url_launcher.dart';

const _navy = Color(0xFF121A34);
const _yellow = Color(0xFFFFB703);
const _pageBg = Color(0xFFF7F8FC);

class ChatRoomsScreen extends StatefulWidget {
  const ChatRoomsScreen({super.key});

  @override
  State<ChatRoomsScreen> createState() => _ChatRoomsScreenState();
}

class _ChatRoomsScreenState extends State<ChatRoomsScreen> {
  late Future<List<ChatRoomModel>> _future;

  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  Future<List<ChatRoomModel>> _load() {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    return ChatApi().getMyChatRooms(token: token);
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
          'Messages',
          style: TextStyle(color: Colors.black, fontWeight: FontWeight.w900),
        ),
      ),
      body: RefreshIndicator(
        color: _yellow,
        onRefresh: _refresh,
        child: FutureBuilder<List<ChatRoomModel>>(
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
                title: 'Could not load chats',
                subtitle: snapshot.error.toString(),
              );
            }
            final rooms = snapshot.data ?? const [];
            if (rooms.isEmpty) {
              return const _EmptyState(
                icon: Icons.chat_bubble_outline,
                title: 'No chats yet',
                subtitle:
                    'Your conversations with professionals will appear here.',
              );
            }
            return ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: rooms.length,
              separatorBuilder: (_, _) => const SizedBox(height: 10),
              itemBuilder: (context, index) {
                final room = rooms[index];
                return _RoomTile(room: room);
              },
            );
          },
        ),
      ),
    );
  }
}

class ChatByRequestScreen extends StatelessWidget {
  final int requestId;

  const ChatByRequestScreen({super.key, required this.requestId});

  @override
  Widget build(BuildContext context) {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    return FutureBuilder<ChatRoomModel>(
      future: ChatApi().getChatRoomByRequest(
        token: token,
        requestId: requestId,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Scaffold(
            backgroundColor: _pageBg,
            body: Center(child: CircularProgressIndicator(color: _yellow)),
          );
        }
        if (snapshot.hasError || !snapshot.hasData) {
          return Scaffold(
            backgroundColor: _pageBg,
            appBar: AppBar(backgroundColor: _pageBg, elevation: 0),
            body: _EmptyState(
              icon: Icons.error_outline,
              title: 'Chat is not ready',
              subtitle: snapshot.error?.toString() ?? 'No chat room found.',
            ),
          );
        }
        return ChatDetailScreen(room: snapshot.data!);
      },
    );
  }
}

class ChatDetailScreen extends StatefulWidget {
  final ChatRoomModel room;

  const ChatDetailScreen({super.key, required this.room});

  @override
  State<ChatDetailScreen> createState() => _ChatDetailScreenState();
}

class _ChatDetailScreenState extends State<ChatDetailScreen> {
  final _controller = TextEditingController();
  late Future<List<ChatMessageModel>> _future;
  bool _sending = false;

  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<List<ChatMessageModel>> _load() {
    final token = context.read<AuthSessionProvider>().accessToken ?? '';
    return ChatApi().getMessages(token: token, chatRoomId: widget.room.id);
  }

  Future<void> _refresh() async {
    setState(() => _future = _load());
    await _future;
  }

  Future<void> _sendText() async {
    final text = _controller.text.trim();
    if (text.isEmpty) return;
    _controller.clear();
    await _send(() {
      final token = context.read<AuthSessionProvider>().accessToken ?? '';
      return ChatApi().sendTextMessage(
        token: token,
        chatRoomId: widget.room.id,
        message: text,
      );
    });
  }

  Future<void> _sendLocation() async {
    final provider = context.read<LocationProvider>();
    if (provider.currentLocation == null) {
      await provider.getCurrentLocation();
    }
    final location = provider.currentLocation;
    if (location == null || !mounted) return;
    await _send(() {
      final token = context.read<AuthSessionProvider>().accessToken ?? '';
      return ChatApi().sendLocationMessage(
        token: token,
        chatRoomId: widget.room.id,
        latitude: location.latitude,
        longitude: location.longitude,
      );
    });
  }

  Future<void> _sendPhotoUrl() async {
    final controller = TextEditingController();
    final url = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Send photo URL'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(
            hintText: 'https://example.com/photo.jpg',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, controller.text.trim()),
            child: const Text('Send'),
          ),
        ],
      ),
    );
    controller.dispose();
    if (url == null || url.isEmpty) return;
    await _send(() {
      final token = context.read<AuthSessionProvider>().accessToken ?? '';
      return ChatApi().sendPhotoMessage(
        token: token,
        chatRoomId: widget.room.id,
        photoUrl: url,
      );
    });
  }

  Future<void> _send(Future<void> Function() action) async {
    setState(() => _sending = true);
    try {
      await action();
      await _refresh();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Message failed: $e')));
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final title = widget.room.provider?.name.isNotEmpty == true
        ? widget.room.provider!.name
        : widget.room.title;
    return Scaffold(
      backgroundColor: _pageBg,
      appBar: AppBar(
        backgroundColor: _pageBg,
        elevation: 0,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title.isEmpty ? 'Chat' : title,
              style: const TextStyle(
                color: Colors.black,
                fontWeight: FontWeight.w900,
                fontSize: 17,
              ),
            ),
            if (widget.room.requestId != null)
              Text(
                '#REQ${widget.room.requestId}',
                style: const TextStyle(color: Colors.black45, fontSize: 12),
              ),
          ],
        ),
      ),
      body: Column(
        children: [
          Expanded(
            child: FutureBuilder<List<ChatMessageModel>>(
              future: _future,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(
                    child: CircularProgressIndicator(color: _yellow),
                  );
                }
                final messages = snapshot.data ?? const [];
                if (messages.isEmpty) {
                  return const _EmptyState(
                    icon: Icons.forum_outlined,
                    title: 'Start the conversation',
                    subtitle: 'Send a message, location, or photo URL.',
                  );
                }
                return RefreshIndicator(
                  color: _yellow,
                  onRefresh: _refresh,
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: messages.length,
                    itemBuilder: (context, index) {
                      return _MessageBubble(message: messages[index]);
                    },
                  ),
                );
              },
            ),
          ),
          Container(
            padding: const EdgeInsets.fromLTRB(12, 8, 12, 14),
            color: Colors.white,
            child: SafeArea(
              top: false,
              child: Row(
                children: [
                  IconButton(
                    onPressed: _sending ? null : _sendLocation,
                    icon: const Icon(
                      Icons.location_on_outlined,
                      color: _yellow,
                    ),
                  ),
                  IconButton(
                    onPressed: _sending ? null : _sendPhotoUrl,
                    icon: const Icon(Icons.image_outlined, color: _navy),
                  ),
                  Expanded(
                    child: TextField(
                      controller: _controller,
                      minLines: 1,
                      maxLines: 4,
                      decoration: InputDecoration(
                        hintText: 'Type a message',
                        filled: true,
                        fillColor: _pageBg,
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(24),
                          borderSide: BorderSide.none,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  CircleAvatar(
                    backgroundColor: _navy,
                    child: IconButton(
                      onPressed: _sending ? null : _sendText,
                      icon: const Icon(Icons.send_rounded, color: Colors.white),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _RoomTile extends StatelessWidget {
  final ChatRoomModel room;

  const _RoomTile({required this.room});

  @override
  Widget build(BuildContext context) {
    final name = room.provider?.name.isNotEmpty == true
        ? room.provider!.name
        : room.title;
    final latest = room.latestMessage?.message ?? 'Open chat';
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(16),
      child: ListTile(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => ChatDetailScreen(room: room)),
          );
        },
        leading: CircleAvatar(
          backgroundColor: const Color(0xFFFFF4D0),
          child: Text(
            name.isEmpty ? '?' : name[0].toUpperCase(),
            style: const TextStyle(color: _navy, fontWeight: FontWeight.w900),
          ),
        ),
        title: Text(
          name.isEmpty ? 'Chat' : name,
          style: const TextStyle(fontWeight: FontWeight.w900),
        ),
        subtitle: Text(latest, maxLines: 1, overflow: TextOverflow.ellipsis),
        trailing: const Icon(Icons.chevron_right, color: Colors.black38),
      ),
    );
  }
}

class _MessageBubble extends StatelessWidget {
  final ChatMessageModel message;

  const _MessageBubble({required this.message});

  @override
  Widget build(BuildContext context) {
    final isMe = message.fromCustomer;
    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        constraints: const BoxConstraints(maxWidth: 280),
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isMe ? _navy : Colors.white,
          borderRadius: BorderRadius.circular(16),
        ),
        child: _MessageContent(message: message, isMe: isMe),
      ),
    );
  }
}

class _MessageContent extends StatelessWidget {
  final ChatMessageModel message;
  final bool isMe;

  const _MessageContent({required this.message, required this.isMe});

  @override
  Widget build(BuildContext context) {
    final color = isMe ? Colors.white : Colors.black87;
    if (message.type.contains('LOCATION') &&
        message.latitude != null &&
        message.longitude != null) {
      return InkWell(
        onTap: () {
          final uri = Uri.parse(
            'https://www.google.com/maps/search/?api=1&query=${message.latitude},${message.longitude}',
          );
          launchUrl(uri, mode: LaunchMode.externalApplication);
        },
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.map_outlined, color: color),
            const SizedBox(width: 8),
            Text('Open location', style: TextStyle(color: color)),
          ],
        ),
      );
    }
    if (message.type.contains('PHOTO') && message.photoUrl.isNotEmpty) {
      return ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child: Image.network(
          message.photoUrl,
          width: 220,
          height: 150,
          fit: BoxFit.cover,
          errorBuilder: (_, _, _) =>
              Text(message.photoUrl, style: TextStyle(color: color)),
        ),
      );
    }
    return Text(
      message.message.isEmpty ? 'Message' : message.message,
      style: TextStyle(color: color, height: 1.3),
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
