import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:talat_sanaye3/providers/auth_session_provider.dart';
import 'package:talat_sanaye3/screens/SignIn_page.dart';
import 'package:talat_sanaye3/services/Auth_services.dart';

class AccountScreen extends StatelessWidget {
  const AccountScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF6F7FB),
      appBar: AppBar(
        elevation: 0,
        backgroundColor: const Color(0xFFF6F7FB),
        title: const Text(
          "My Account",
          style: TextStyle(color: Colors.black, fontWeight: FontWeight.w700),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: Stack(
              children: [
                const Icon(Icons.notifications_none, color: Colors.black),
                Positioned(
                  right: 0,
                  top: 0,
                  child: Container(
                    width: 8,
                    height: 8,
                    decoration: const BoxDecoration(
                      color: Colors.orange,
                      shape: BoxShape.circle,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _profileCard(),
          const SizedBox(height: 16),
          _premiumCard(),
          const SizedBox(height: 20),

          const Text("Account", style: TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),

          _section([
            _item(
              Icons.person_outline,
              "Personal Information",
              "Update your personal details",
              () {},
            ),
            _item(
              Icons.location_on_outlined,
              "Saved Addresses",
              "Manage your saved addresses",
              () {},
            ),
            _item(
              Icons.payment_outlined,
              "Payment Methods",
              "Manage your payment options",
              () {},
            ),
            _item(
              Icons.notifications_none,
              "Notifications",
              "Manage your notification preferences",
              () {},
            ),
            _item(
              Icons.security_outlined,
              "Privacy & Security",
              "Manage your privacy settings",
              () {},
            ),
            _item(
              Icons.help_outline,
              "Help & Support",
              "Get help and support",
              () {},
              isLast: true,
            ),
          ]),

          const SizedBox(height: 20),

          const Text("More", style: TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),

          _section([
            _item(Icons.info_outline, "About Us", "Learn more about us", () {}),
            _item(
              Icons.logout,
              "Logout",
              "Sign out from your account",
              () => _showLogoutDialog(context),
              isLast: true,
            ),
          ]),
        ],
      ),
    );
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text(
          'Logout',
          style: TextStyle(fontWeight: FontWeight.w700),
        ),
        content: const Text('Are you sure you want to sign out?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(dialogContext);
              _performLogout(context);
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF121A34),
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
              ),
            ),
            child: const Text('Logout'),
          ),
        ],
      ),
    );
  }

  Future<void> _performLogout(BuildContext context) async {
    final authProvider = context.read<AuthSessionProvider>();
    final token = authProvider.accessToken;

    if (token == null || token.isEmpty) {
      if (context.mounted) {
        _goToLogin(context);
      }
      return;
    }

    final navigator = Navigator.of(context);
    final scaffoldMessenger = ScaffoldMessenger.of(context);

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => const Center(child: CircularProgressIndicator()),
    );

    try {
      final response = await LogoutServise().logout(token: token);

      navigator.pop();

      if (response.success) {
        await authProvider.clearSession();

        scaffoldMessenger.showSnackBar(
          SnackBar(content: Text(response.message)),
        );

        navigator.pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) =>  SignInPage()),
          (_) => false,
        );
      } else {
        throw Exception(response.message);
      }
    } catch (e) {
      try {
        navigator.pop();
      } catch (_) {}

      scaffoldMessenger.showSnackBar(
        SnackBar(content: Text('Logout failed: $e')),
      );
    }
  }

  void _goToLogin(BuildContext context) {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) =>  SignInPage()),
      (_) => false,
    );
  }

  Widget _profileCard() {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          const CircleAvatar(
            radius: 28,
            backgroundColor: Colors.black12,
            child: Icon(Icons.person, color: Colors.black54),
          ),
          const SizedBox(width: 12),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Ahmed Mohamed",
                  style: TextStyle(fontWeight: FontWeight.w700),
                ),
                SizedBox(height: 4),
                Text(
                  "+20 123 456 7890",
                  style: TextStyle(color: Colors.black54, fontSize: 12),
                ),
                Text(
                  "ahmed.mohamed@gmail.com",
                  style: TextStyle(color: Colors.black54, fontSize: 12),
                ),
              ],
            ),
          ),
          Container(
            decoration: BoxDecoration(
              color: Colors.grey.shade200,
              borderRadius: BorderRadius.circular(10),
            ),
            child: IconButton(
              onPressed: () {},
              icon: const Icon(Icons.edit_outlined, size: 18),
            ),
          ),
        ],
      ),
    );
  }

  Widget _premiumCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        gradient: const LinearGradient(
          colors: [Color(0xFF0D1B2A), Color(0xFF1B263B)],
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(
              Icons.workspace_premium,
              color: Color(0xFFFFC65A),
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Become a Premium Member",
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                SizedBox(height: 4),
                Text(
                  "Get priority service and exclusive benefits",
                  style: TextStyle(color: Colors.white70, fontSize: 12),
                ),
              ],
            ),
          ),
          ElevatedButton(
            onPressed: () {},
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFFFC65A),
              foregroundColor: Colors.black,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            child: const Text("Upgrade"),
          ),
        ],
      ),
    );
  }

  Widget _section(List<Widget> children) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(children: children),
    );
  }

  Widget _item(
    IconData icon,
    String title,
    String subtitle,
    VoidCallback onTap, {
    bool isLast = false,
  }) {
    return Column(
      children: [
        ListTile(
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 12,
            vertical: 4,
          ),
          leading: Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              color: Colors.grey.shade200,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, size: 20, color: const Color(0xFFFFC65A)),
          ),
          title: Text(
            title,
            style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
          ),
          subtitle: Text(
            subtitle,
            style: const TextStyle(
              fontSize: 11.5,
              color: Colors.black54,
              height: 1.2,
            ),
          ),
          trailing: const Icon(
            Icons.arrow_forward_ios,
            size: 16,
            color: Colors.black38,
          ),
          onTap: onTap,
        ),
        if (!isLast) const Divider(height: 1, thickness: 0.8),
      ],
    );
  }
}
