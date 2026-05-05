import 'dart:developer';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/models/location_model.dart';
import 'package:talat_sanaye3/models/request_model.dart';
import 'package:talat_sanaye3/providers/auth_session_provider.dart';
import 'package:talat_sanaye3/providers/location_provider.dart';
import 'package:talat_sanaye3/screens/user/location_setup_screen.dart';
import 'package:talat_sanaye3/screens/user/offers_screen.dart';
import 'package:talat_sanaye3/screens/user/user_home_screen.dart';
import 'package:talat_sanaye3/services/request_service.dart';

const _navy = Color(0xFF121A34);
const _yellow = Color(0xFFFFB703);
const _pageBg = Color(0xFFF7F8FC);

class ServiceRequestDraft {
  String categoryId;
  String serviceTitle;
  Location? location;
  String title = '';
  String description = '';
  final List<XFile> images = [];
  bool isNow = true;
  final List<DateTime> selectedDates = [];
  TimeOfDay? selectedTime;

  ServiceRequestDraft({
    required this.categoryId,
    required this.serviceTitle,
    this.location,
    String? initialTitle,
    String? initialDescription,
  }) {
    title = initialTitle ?? '';
    description = initialDescription ?? '';
  }

  // ✅ FIX: only WATER, ELECTRICITY, CARPENTER are accepted by the API
  String get apiServiceType {
    switch (categoryId.toLowerCase()) {
      case 'plumber':
      case 'water':
      case 'leak_repair':
        return 'WATER';
      case 'electrician':
      case 'electricity':
      case 'light_fixing':
      case 'socket_repair':
        return 'ELECTRICITY';
      case 'carpenter':
      case 'door_fixing':
        return 'CARPENTER';
      default:
        return 'WATER'; // safe fallback
    }
  }

  String get timeSummary {
    if (isNow) return 'Now';
    final dates = selectedDates.map(_formatDate).join(', ');
    final time = selectedTime == null ? 'Any time' : _formatTime(selectedTime!);
    return dates.isEmpty ? time : '$dates at $time';
  }
}

class ConfirmServiceLocationScreen extends StatelessWidget {
  final String categoryId;
  final String title;
  final String? initialProblemTitle;
  final String? initialProblemDescription;

  const ConfirmServiceLocationScreen({
    super.key,
    required this.categoryId,
    required this.title,
    this.initialProblemTitle,
    this.initialProblemDescription,
  });

  @override
  Widget build(BuildContext context) {
    return Consumer<LocationProvider>(
      builder: (context, provider, _) {
        final location = provider.currentLocation;
        return _FlowScaffold(
          title: 'Confirm location',
          subtitle: 'Make sure the craftsman goes to the right place',
          body: Column(
            children: [
              _LocationCard(location: location),
              const SizedBox(height: 14),
              _OutlineAction(
                icon: Icons.edit_location_alt_outlined,
                title: 'Change location',
                subtitle: 'Use current location, search, or choose on map',
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (_) =>
                          const LocationSetupScreen(replaceStack: false),
                    ),
                  );
                },
              ),
              const Spacer(),
              _PrimaryButton(
                label: 'Continue',
                onPressed: location == null
                    ? null
                    : () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => DescribeProblemScreen(
                              draft: ServiceRequestDraft(
                                categoryId: categoryId,
                                serviceTitle: title,
                                location: location,
                                initialTitle: initialProblemTitle,
                                initialDescription: initialProblemDescription,
                              ),
                            ),
                          ),
                        );
                      },
              ),
            ],
          ),
        );
      },
    );
  }
}

class DescribeProblemScreen extends StatefulWidget {
  final ServiceRequestDraft draft;
  final bool returnToPrevious;

  const DescribeProblemScreen({
    super.key,
    required this.draft,
    this.returnToPrevious = false,
  });

  @override
  State<DescribeProblemScreen> createState() => _DescribeProblemScreenState();
}

class _DescribeProblemScreenState extends State<DescribeProblemScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _titleController.text = widget.draft.title;
    _descriptionController.text = widget.draft.description;
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _pickImage(ImageSource source) async {
    final image = await _picker.pickImage(
      source: source,
      imageQuality: 82,
      maxWidth: 1600,
    );
    if (image != null) {
      setState(() => widget.draft.images.add(image));
    }
  }

  void _continue() {
    if (!_formKey.currentState!.validate()) return;
    widget.draft.title = _titleController.text.trim();
    widget.draft.description = _descriptionController.text.trim();
    if (widget.returnToPrevious) {
      Navigator.pop(context, true);
      return;
    }
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ServiceScheduleScreen(draft: widget.draft),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _FlowScaffold(
      title: 'Describe your problem',
      subtitle: 'Provide details about the issue',
      body: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: SingleChildScrollView(
                keyboardDismissBehavior:
                    ScrollViewKeyboardDismissBehavior.onDrag,
                padding: const EdgeInsets.only(bottom: 18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _Label('Problem title'),
                    TextFormField(
                      controller: _titleController,
                      textInputAction: TextInputAction.next,
                      decoration: _inputDecoration(
                        'Example: Kitchen pipe leaking',
                      ),
                      validator: (value) =>
                          value == null || value.trim().isEmpty
                          ? 'Add a title'
                          : null,
                    ),
                    const SizedBox(height: 16),
                    _Label('Upload Image (Optional)'),
                    Row(
                      children: [
                        Expanded(
                          child: _ImagePickButton(
                            icon: Icons.image_outlined,
                            label: 'Upload Image',
                            onTap: () => _pickImage(ImageSource.gallery),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: _ImagePickButton(
                            icon: Icons.camera_alt_rounded,
                            label: 'Take Photo',
                            onTap: () => _pickImage(ImageSource.camera),
                          ),
                        ),
                      ],
                    ),
                    if (widget.draft.images.isNotEmpty) ...[
                      const SizedBox(height: 12),
                      SizedBox(
                        height: 92,
                        child: ListView.separated(
                          scrollDirection: Axis.horizontal,
                          itemCount: widget.draft.images.length,
                          separatorBuilder: (_, _) => const SizedBox(width: 10),
                          itemBuilder: (context, index) {
                            return _ImagePreview(
                              image: widget.draft.images[index],
                              onRemove: () {
                                setState(
                                  () => widget.draft.images.removeAt(index),
                                );
                              },
                            );
                          },
                        ),
                      ),
                    ],
                    const SizedBox(height: 16),
                    _Label('Description'),
                    TextFormField(
                      controller: _descriptionController,
                      maxLength: 300,
                      minLines: 5,
                      maxLines: 7,
                      decoration: _inputDecoration(
                        'Example: Water leaking under the sink. It happens when I open the faucet.',
                      ),
                      validator: (value) =>
                          value == null || value.trim().isEmpty
                          ? 'Add a short description'
                          : null,
                    ),
                  ],
                ),
              ),
            ),
            _PrimaryButton(label: 'Continue', onPressed: _continue),
          ],
        ),
      ),
    );
  }
}

class ServiceScheduleScreen extends StatefulWidget {
  final ServiceRequestDraft draft;
  final bool returnToPrevious;

  const ServiceScheduleScreen({
    super.key,
    required this.draft,
    this.returnToPrevious = false,
  });

  @override
  State<ServiceScheduleScreen> createState() => _ServiceScheduleScreenState();
}

class _ServiceScheduleScreenState extends State<ServiceScheduleScreen> {
  Future<void> _addDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 90)),
    );
    if (picked == null) return;
    final normalized = DateTime(picked.year, picked.month, picked.day);
    if (!widget.draft.selectedDates.any((date) => _sameDay(date, normalized))) {
      setState(() => widget.draft.selectedDates.add(normalized));
    }
  }

  Future<void> _pickTime() async {
    final picked = await showTimePicker(
      context: context,
      initialTime: widget.draft.selectedTime ?? TimeOfDay.now(),
    );
    if (picked != null) setState(() => widget.draft.selectedTime = picked);
  }

  void _continue() {
    if (!widget.draft.isNow &&
        (widget.draft.selectedDates.isEmpty ||
            widget.draft.selectedTime == null)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Choose at least one date and a time')),
      );
      return;
    }
    if (widget.returnToPrevious) {
      Navigator.pop(context, true);
      return;
    }
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ReviewRequestScreen(draft: widget.draft),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _FlowScaffold(
      title: 'When do you need the service?',
      subtitle: 'Choose a time that works for you',
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _ScheduleOption(
            selected: widget.draft.isNow,
            icon: Icons.flash_on_rounded,
            title: 'Now',
            subtitle: 'Find a professional asap',
            onTap: () => setState(() => widget.draft.isNow = true),
          ),
          const SizedBox(height: 12),
          _ScheduleOption(
            selected: !widget.draft.isNow,
            icon: Icons.calendar_month_outlined,
            title: 'Later',
            subtitle: 'Choose date and time',
            onTap: () => setState(() => widget.draft.isNow = false),
          ),
          if (!widget.draft.isNow) ...[
            const SizedBox(height: 22),
            _Label('Select Date & Time'),
            _PickerTile(
              icon: Icons.calendar_today_outlined,
              label: widget.draft.selectedDates.isEmpty
                  ? 'Add service dates'
                  : '${widget.draft.selectedDates.length} date(s) selected',
              onTap: _addDate,
            ),
            if (widget.draft.selectedDates.isNotEmpty) ...[
              const SizedBox(height: 10),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: widget.draft.selectedDates.map((date) {
                  return InputChip(
                    label: Text(_formatDate(date)),
                    deleteIcon: const Icon(Icons.close, size: 16),
                    onDeleted: () {
                      setState(() => widget.draft.selectedDates.remove(date));
                    },
                    backgroundColor: const Color(0xFFFFF4D0),
                    side: BorderSide.none,
                  );
                }).toList(),
              ),
            ],
            const SizedBox(height: 10),
            _PickerTile(
              icon: Icons.schedule_outlined,
              label: widget.draft.selectedTime == null
                  ? 'Choose time'
                  : _formatTime(widget.draft.selectedTime!),
              onTap: _pickTime,
            ),
          ],
          const Spacer(),
          _PrimaryButton(label: 'Continue', onPressed: _continue),
        ],
      ),
    );
  }
}

class ReviewRequestScreen extends StatefulWidget {
  final ServiceRequestDraft draft;

  const ReviewRequestScreen({super.key, required this.draft});

  @override
  State<ReviewRequestScreen> createState() => _ReviewRequestScreenState();
}

class _ReviewRequestScreenState extends State<ReviewRequestScreen> {
  bool _isSubmitting = false;

  Future<void> _editLocation() async {
    await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => const LocationSetupScreen(replaceStack: false),
      ),
    );
    if (!mounted) return;
    final location = context.read<LocationProvider>().currentLocation;
    if (location != null) {
      setState(() => widget.draft.location = location);
    }
  }

  Future<void> _editProblem() async {
    final updated = await Navigator.push<bool>(
      context,
      MaterialPageRoute(
        builder: (_) =>
            DescribeProblemScreen(draft: widget.draft, returnToPrevious: true),
      ),
    );
    if (updated == true && mounted) setState(() {});
  }

  Future<void> _editSchedule() async {
    final updated = await Navigator.push<bool>(
      context,
      MaterialPageRoute(
        builder: (_) =>
            ServiceScheduleScreen(draft: widget.draft, returnToPrevious: true),
      ),
    );
    if (updated == true && mounted) setState(() {});
  }

  Future<void> _editService() async {
    // ✅ FIX: ids match the switch cases in apiServiceType exactly
    const choices = [
      _ServiceChoice('water', 'Plumber', Icons.plumbing),
      _ServiceChoice(
        'electricity',
        'Electrician',
        Icons.electrical_services_outlined,
      ),
      _ServiceChoice('carpenter', 'Carpenter', Icons.handyman_outlined),
    ];

    final service = await showModalBottomSheet<_ServiceChoice>(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(22)),
      ),
      builder: (context) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Choose service',
                  style: TextStyle(
                    color: _navy,
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 14),
                ...choices.map(
                  (choice) => ListTile(
                    onTap: () => Navigator.pop(context, choice),
                    leading: Icon(choice.icon, color: _yellow),
                    title: Text(
                      choice.title,
                      style: const TextStyle(fontWeight: FontWeight.w700),
                    ),
                    trailing: widget.draft.categoryId == choice.id
                        ? const Icon(Icons.check_circle, color: _yellow)
                        : null,
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );

    if (service == null || !mounted) return;
    setState(() {
      widget.draft.categoryId = service.id;
      widget.draft.serviceTitle = service.title;
    });
  }

  Future<void> _confirm() async {
    final token = context.read<AuthSessionProvider>().accessToken;
    final location = widget.draft.location;

    log('DEBUG token: "$token"');
    if (token == null || token.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please sign in again to continue')),
      );
      return;
    }
    if (location == null) return;

    setState(() => _isSubmitting = true);
    try {
      log('Sending serviceType: ${widget.draft.apiServiceType}');
      final request = await ServiceRequestApi().createRequest(
        token: token,
        serviceType: widget.draft.apiServiceType,
        title: widget.draft.title,
        description: widget.draft.description,
        address: location.address,
        latitude: location.latitude,
        longitude: location.longitude,
      );
      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => OffersScreen(requestId: request.id, request: request),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Could not create request: $e')));
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final draft = widget.draft;
    return _FlowScaffold(
      title: 'Review your request',
      subtitle: 'Please review your request details before continuing',
      body: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                children: [
                  _ReviewRow(
                    icon: Icons.home_repair_service_outlined,
                    label: 'Service',
                    value: draft.serviceTitle,
                    onEdit: _editService,
                  ),
                  _ReviewRow(
                    icon: Icons.location_on_outlined,
                    label: 'Location',
                    value: draft.location?.address ?? 'No location',
                    onEdit: _editLocation,
                  ),
                  _ReviewRow(
                    icon: Icons.chat_bubble_outline,
                    label: 'Problem',
                    value: '${draft.title}\n${draft.description}',
                    onEdit: _editProblem,
                  ),
                  _ReviewRow(
                    icon: Icons.image_outlined,
                    label: 'Image',
                    image: draft.images.isEmpty ? null : draft.images.first,
                    value: draft.images.isEmpty
                        ? 'No image attached'
                        : '${draft.images.length} image(s)',
                    onEdit: _editProblem,
                  ),
                  _ReviewRow(
                    icon: Icons.schedule_outlined,
                    label: 'Time',
                    value: draft.timeSummary,
                    onEdit: _editSchedule,
                  ),
                  const _ReviewRow(
                    icon: Icons.wallet_outlined,
                    label: 'Estimated Price',
                    value: 'EGP 150 - 200\nYou will pay after the service',
                  ),
                  const SizedBox(height: 12),
                ],
              ),
            ),
          ),
          _PrimaryButton(
            label: 'Confirm Request',
            isLoading: _isSubmitting,
            onPressed: _confirm,
          ),
        ],
      ),
    );
  }
}

class RequestSubmittedScreen extends StatelessWidget {
  final ServiceRequestDraft draft;
  final CreatedServiceRequest request;

  const RequestSubmittedScreen({
    super.key,
    required this.draft,
    required this.request,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(22),
          child: Column(
            children: [
              const Spacer(),
              Container(
                width: 132,
                height: 132,
                decoration: BoxDecoration(
                  color: const Color(0xFFFFF4D0),
                  borderRadius: BorderRadius.circular(36),
                ),
                child: const Icon(
                  Icons.assignment_turned_in,
                  size: 76,
                  color: _yellow,
                ),
              ),
              const SizedBox(height: 26),
              const Text(
                'Request Submitted!',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.w800,
                  color: _navy,
                ),
              ),
              const SizedBox(height: 10),
              const Text(
                "We've received your request and will connect you with the best professional shortly.",
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.black54, height: 1.35),
              ),
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: _pageBg,
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Column(
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Request ID',
                          style: TextStyle(color: Colors.black54),
                        ),
                        Text(
                          '#REQ${request.id}',
                          style: const TextStyle(
                            fontWeight: FontWeight.w800,
                            color: _navy,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 18),
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        'We will notify you soon',
                        style: TextStyle(fontWeight: FontWeight.w800),
                      ),
                    ),
                    const SizedBox(height: 6),
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        'Track offers and choose the professional that fits you.',
                        style: TextStyle(color: Colors.black54, height: 1.35),
                      ),
                    ),
                  ],
                ),
              ),
              const Spacer(),
              _PrimaryButton(
                label: 'View Offers',
                onPressed: () {
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(
                      builder: (_) =>
                          WaitingOffersScreen(draft: draft, request: request),
                    ),
                  );
                },
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

class WaitingOffersScreen extends StatefulWidget {
  final ServiceRequestDraft draft;
  final CreatedServiceRequest request;

  const WaitingOffersScreen({
    super.key,
    required this.draft,
    required this.request,
  });

  @override
  State<WaitingOffersScreen> createState() => _WaitingOffersScreenState();
}

class _WaitingOffersScreenState extends State<WaitingOffersScreen> {
  bool _isAccepting = false;

  final _offers = const [
    _Offer(
      id: 1,
      name: 'Ahmed Hassan',
      price: 'EGP 180',
      eta: '20 min',
      rating: 4.8,
      jobs: 126,
    ),
    _Offer(
      id: 2,
      name: 'Mohamed Ali',
      price: 'EGP 165',
      eta: '35 min',
      rating: 4.6,
      jobs: 88,
    ),
    _Offer(
      id: 3,
      name: 'Karim Samir',
      price: 'EGP 200',
      eta: '15 min',
      rating: 4.9,
      jobs: 204,
    ),
  ];

  Future<void> _accept(_Offer offer) async {
    final token = context.read<AuthSessionProvider>().accessToken;
    setState(() => _isAccepting = true);
    try {
      if (token != null && token.isNotEmpty) {
        await ServiceRequestApi().acceptOffer(
          token: token,
          requestId: widget.request.id,
          offerId: offer.id,
        );
      }
    } catch (_) {
      // Offer endpoint not available yet — flow continues with mock data
    }

    if (!mounted) return;
    setState(() => _isAccepting = false);
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) =>
            _ProviderConfirmedScreen(request: widget.request, offer: offer),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _FlowScaffold(
      title: 'Choose an offer',
      subtitle: 'Your request is open. Compare offers and confirm one.',
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: const Color(0xFFFFF8E5),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: const Color(0xFFFFE2A4)),
            ),
            child: Row(
              children: [
                const Icon(Icons.hourglass_top_rounded, color: _yellow),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    '${_offers.length} offers received for #REQ${widget.request.id}',
                    style: const TextStyle(fontWeight: FontWeight.w700),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),
          Expanded(
            child: ListView.separated(
              itemCount: _offers.length,
              separatorBuilder: (_, _) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final offer = _offers[index];
                return _OfferCard(
                  offer: offer,
                  isLoading: _isAccepting,
                  onAccept: () => _accept(offer),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _ProviderConfirmedScreen extends StatelessWidget {
  final CreatedServiceRequest request;
  final _Offer offer;

  const _ProviderConfirmedScreen({required this.request, required this.offer});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(22),
          child: Column(
            children: [
              const Spacer(),
              const CircleAvatar(
                radius: 48,
                backgroundColor: Color(0xFFFFF4D0),
                child: Icon(Icons.check_rounded, size: 58, color: _yellow),
              ),
              const SizedBox(height: 24),
              const Text(
                'Request Confirmed',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.w800,
                  color: _navy,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                '${offer.name} accepted your request. You can now chat and coordinate service details.',
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.black54, height: 1.35),
              ),
              const SizedBox(height: 24),
              _RequestIdCard(requestId: request.id),
              const Spacer(),
              _PrimaryButton(
                label: 'Open Messages',
                onPressed: () {
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(
                      builder: (_) =>
                          _RequestChatScreen(request: request, offer: offer),
                    ),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _RequestChatScreen extends StatelessWidget {
  final CreatedServiceRequest request;
  final _Offer offer;

  const _RequestChatScreen({required this.request, required this.offer});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _pageBg,
      appBar: AppBar(
        backgroundColor: _pageBg,
        elevation: 0,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              offer.name,
              style: const TextStyle(
                color: Colors.black,
                fontWeight: FontWeight.w800,
                fontSize: 16,
              ),
            ),
            Text(
              '#REQ${request.id}',
              style: const TextStyle(color: Colors.black45, fontSize: 12),
            ),
          ],
        ),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: const [
                _Bubble(
                  text:
                      'Hi, I accepted your request. I can arrive at the selected time.',
                  isMe: false,
                ),
                _Bubble(
                  text: 'Great, please message me when you are on the way.',
                  isMe: true,
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.fromLTRB(16, 10, 16, 16),
            color: Colors.white,
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    decoration: InputDecoration(
                      hintText: 'Message ${offer.name.split(' ').first}',
                      filled: true,
                      fillColor: _pageBg,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(24),
                        borderSide: BorderSide.none,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                const CircleAvatar(
                  backgroundColor: _navy,
                  child: Icon(Icons.send_rounded, color: Colors.white),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

// ─────────────────────────────────────────────
// Shared private widgets & helpers
// ─────────────────────────────────────────────

class _FlowScaffold extends StatelessWidget {
  final String title;
  final String subtitle;
  final Widget body;

  const _FlowScaffold({
    required this.title,
    required this.subtitle,
    required this.body,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _pageBg,
      appBar: AppBar(
        backgroundColor: _pageBg,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.black87),
          onPressed: () => Navigator.maybePop(context),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(18, 0, 18, 18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: const TextStyle(
                  color: _navy,
                  fontSize: 24,
                  fontWeight: FontWeight.w800,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                subtitle,
                style: const TextStyle(color: Colors.black54, fontSize: 15),
              ),
              const SizedBox(height: 24),
              Expanded(child: body),
            ],
          ),
        ),
      ),
    );
  }
}

class _PrimaryButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final bool isLoading;

  const _PrimaryButton({
    required this.label,
    required this.onPressed,
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      height: 54,
      child: ElevatedButton(
        onPressed: isLoading ? null : onPressed,
        style: ElevatedButton.styleFrom(
          backgroundColor: _navy,
          disabledBackgroundColor: Colors.black12,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
          elevation: 0,
        ),
        child: isLoading
            ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(
                  color: Colors.white,
                  strokeWidth: 2,
                ),
              )
            : Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    label,
                    style: const TextStyle(
                      fontWeight: FontWeight.w800,
                      fontSize: 16,
                    ),
                  ),
                  const SizedBox(width: 10),
                  const Icon(Icons.arrow_forward_rounded, size: 20),
                ],
              ),
      ),
    );
  }
}

class _Label extends StatelessWidget {
  final String text;
  const _Label(this.text);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        text,
        style: const TextStyle(fontWeight: FontWeight.w800, color: _navy),
      ),
    );
  }
}

class _LocationCard extends StatelessWidget {
  final Location? location;

  const _LocationCard({required this.location});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: const Color(0xFFFFF4D0),
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(Icons.location_on_outlined, color: _yellow),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  location?.placeName ?? 'No location selected',
                  style: const TextStyle(fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 4),
                Text(
                  location?.address ?? 'Choose a service address first',
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Colors.black54, fontSize: 13),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _OutlineAction extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _OutlineAction({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.black12),
        ),
        child: Row(
          children: [
            Icon(icon, color: _navy),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(fontWeight: FontWeight.w800),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    subtitle,
                    style: const TextStyle(color: Colors.black54, fontSize: 12),
                  ),
                ],
              ),
            ),
            const Icon(Icons.chevron_right, color: Colors.black38),
          ],
        ),
      ),
    );
  }
}

class _ImagePickButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _ImagePickButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Container(
        height: 86,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.black12),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: _navy),
            const SizedBox(height: 8),
            Text(label, style: const TextStyle(fontWeight: FontWeight.w700)),
          ],
        ),
      ),
    );
  }
}

class _ImagePreview extends StatelessWidget {
  final XFile image;
  final VoidCallback onRemove;

  const _ImagePreview({required this.image, required this.onRemove});

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        _PickedImageView(image: image, width: 132, height: 92),
        Positioned(
          top: 6,
          right: 6,
          child: InkWell(
            onTap: onRemove,
            child: const CircleAvatar(
              radius: 13,
              backgroundColor: Colors.white,
              child: Icon(Icons.close, size: 16, color: Colors.black87),
            ),
          ),
        ),
      ],
    );
  }
}

class _ScheduleOption extends StatelessWidget {
  final bool selected;
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _ScheduleOption({
    required this.selected,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: selected ? _yellow : Colors.black12,
            width: selected ? 1.5 : 1,
          ),
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: selected ? const Color(0xFFFFF4D0) : _pageBg,
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: selected ? _yellow : _navy),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(fontWeight: FontWeight.w800),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: const TextStyle(color: Colors.black54, height: 1.25),
                  ),
                ],
              ),
            ),
            Icon(
              selected ? Icons.check_circle : Icons.circle_outlined,
              color: selected ? _yellow : Colors.black26,
            ),
          ],
        ),
      ),
    );
  }
}

class _PickerTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _PickerTile({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(14),
      child: ListTile(
        onTap: onTap,
        leading: Icon(icon, color: _navy),
        title: Text(label, style: const TextStyle(fontWeight: FontWeight.w700)),
        trailing: const Icon(Icons.chevron_right, color: Colors.black38),
      ),
    );
  }
}

class _ReviewRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final XFile? image;
  final VoidCallback? onEdit;

  const _ReviewRow({
    required this.icon,
    required this.label,
    required this.value,
    this.image,
    this.onEdit,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 18),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 34,
            height: 34,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: _navy, size: 18),
          ),
          const SizedBox(width: 12),
          SizedBox(
            width: 86,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w800),
            ),
          ),
          Expanded(
            child: image == null
                ? Text(
                    value,
                    style: const TextStyle(color: Colors.black54, height: 1.35),
                  )
                : Row(
                    children: [
                      _PickedImageView(
                        image: image!,
                        width: 52,
                        height: 52,
                        radius: 8,
                      ),
                      const SizedBox(width: 10),
                      Expanded(child: Text(value)),
                    ],
                  ),
          ),
          const SizedBox(width: 4),
          if (onEdit != null)
            IconButton(
              onPressed: onEdit,
              icon: const Icon(
                Icons.edit_outlined,
                size: 18,
                color: Colors.black45,
              ),
              visualDensity: VisualDensity.compact,
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
            )
          else
            const SizedBox(width: 32),
        ],
      ),
    );
  }
}

class _ServiceChoice {
  final String id;
  final String title;
  final IconData icon;

  const _ServiceChoice(this.id, this.title, this.icon);
}

class _RequestIdCard extends StatelessWidget {
  final int requestId;

  const _RequestIdCard({required this.requestId});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: _pageBg,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Text('Request ID', style: TextStyle(color: Colors.black54)),
          Text(
            '#REQ$requestId',
            style: const TextStyle(fontWeight: FontWeight.w800, color: _navy),
          ),
        ],
      ),
    );
  }
}

class _PickedImageView extends StatelessWidget {
  final XFile image;
  final double width;
  final double height;
  final double radius;

  const _PickedImageView({
    required this.image,
    required this.width,
    required this.height,
    this.radius = 14,
  });

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Uint8List>(
      future: image.readAsBytes(),
      builder: (context, snapshot) {
        final child = snapshot.hasData
            ? Image.memory(
                snapshot.data!,
                width: width,
                height: height,
                fit: BoxFit.cover,
              )
            : Container(
                width: width,
                height: height,
                color: const Color(0xFFEFF2F8),
                child: const Icon(Icons.image_outlined, color: Colors.black38),
              );

        return ClipRRect(
          borderRadius: BorderRadius.circular(radius),
          child: child,
        );
      },
    );
  }
}

class _Offer {
  final int id;
  final String name;
  final String price;
  final String eta;
  final double rating;
  final int jobs;

  const _Offer({
    required this.id,
    required this.name,
    required this.price,
    required this.eta,
    required this.rating,
    required this.jobs,
  });
}

class _OfferCard extends StatelessWidget {
  final _Offer offer;
  final bool isLoading;
  final VoidCallback onAccept;

  const _OfferCard({
    required this.offer,
    required this.isLoading,
    required this.onAccept,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        children: [
          Row(
            children: [
              CircleAvatar(
                backgroundColor: const Color(0xFFEFF2F8),
                child: Text(
                  offer.name.isEmpty ? '?' : offer.name[0],
                  style: const TextStyle(fontWeight: FontWeight.w800),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      offer.name,
                      style: const TextStyle(fontWeight: FontWeight.w800),
                    ),
                    const SizedBox(height: 3),
                    Text(
                      '${offer.rating} rating • ${offer.jobs} jobs',
                      style: const TextStyle(color: Colors.black54),
                    ),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    offer.price,
                    style: const TextStyle(
                      color: _navy,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  Text(
                    offer.eta,
                    style: const TextStyle(color: Colors.black54),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            height: 44,
            child: ElevatedButton(
              onPressed: isLoading ? null : onAccept,
              style: ElevatedButton.styleFrom(
                backgroundColor: _navy,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                elevation: 0,
              ),
              child: const Text(
                'Confirm this provider',
                style: TextStyle(fontWeight: FontWeight.w800),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Bubble extends StatelessWidget {
  final String text;
  final bool isMe;

  const _Bubble({required this.text, required this.isMe});

  @override
  Widget build(BuildContext context) {
    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        constraints: const BoxConstraints(maxWidth: 270),
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.all(13),
        decoration: BoxDecoration(
          color: isMe ? _navy : Colors.white,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Text(
          text,
          style: TextStyle(color: isMe ? Colors.white : Colors.black87),
        ),
      ),
    );
  }
}

InputDecoration _inputDecoration(String hint) {
  return InputDecoration(
    hintText: hint,
    hintStyle: const TextStyle(color: Colors.black38),
    filled: true,
    fillColor: Colors.white,
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(14),
      borderSide: BorderSide.none,
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(14),
      borderSide: const BorderSide(color: _yellow),
    ),
  );
}

String _formatDate(DateTime date) {
  const months = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec',
  ];
  return '${months[date.month - 1]} ${date.day}, ${date.year}';
}

String _formatTime(TimeOfDay time) {
  final hour = time.hourOfPeriod == 0 ? 12 : time.hourOfPeriod;
  final minute = time.minute.toString().padLeft(2, '0');
  final period = time.period == DayPeriod.am ? 'AM' : 'PM';
  return '$hour:$minute $period';
}

bool _sameDay(DateTime a, DateTime b) {
  return a.year == b.year && a.month == b.month && a.day == b.day;
}
