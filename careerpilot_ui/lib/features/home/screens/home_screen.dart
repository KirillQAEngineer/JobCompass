import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../providers/profile_provider.dart';

class HomeScreen extends ConsumerWidget {
  final VoidCallback onOpenFeed;
  final VoidCallback onOpenProfile;
  final VoidCallback onOpenSaved;

  const HomeScreen({
    super.key,
    required this.onOpenFeed,
    required this.onOpenProfile,
    required this.onOpenSaved,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profileAsync = ref.watch(profileProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'CareerPilot',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      body: profileAsync.when(
        loading: () {
          return const Center(child: CircularProgressIndicator());
        },
        error: (error, stackTrace) {
          return _HomeMessage(
            icon: Icons.cloud_off_outlined,
            title: 'Could not load your profile',
            description: 'CareerPilot could not check your profile right now.',
            buttonLabel: 'Retry',
            onPressed: () {
              ref.invalidate(profileProvider);
            },
          );
        },
        data: (profile) {
          if (profile == null) {
            return _NewUserHome(onOpenProfile: onOpenProfile);
          }

          return _ReadyUserHome(
            profession: profile.profession,
            level: profile.level,
            onOpenFeed: onOpenFeed,
            onOpenSaved: onOpenSaved,
            onOpenProfile: onOpenProfile,
          );
        },
      ),
    );
  }
}

class _NewUserHome extends StatelessWidget {
  final VoidCallback onOpenProfile;

  const _NewUserHome({required this.onOpenProfile});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        const SizedBox(height: 20),
        const Icon(Icons.flight_takeoff_outlined, size: 72),
        const SizedBox(height: 24),
        const Text(
          'Start Your Job Search',
          textAlign: TextAlign.center,
          style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        Text(
          'CareerPilot helps you turn your resume into a '
          'personalized job search workspace.',
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 16,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 32),
        const _FeatureCard(
          icon: Icons.upload_file_outlined,
          title: '1. Upload your resume',
          description:
              'CareerPilot analyzes your resume and creates '
              'your professional profile.',
        ),
        const _FeatureCard(
          icon: Icons.person_outline,
          title: '2. Review your profile',
          description:
              'Check your skills, technologies, experience '
              'level, and preferred roles.',
        ),
        const _FeatureCard(
          icon: Icons.work_outline,
          title: '3. Explore relevant jobs',
          description:
              'Open Feed to discover vacancies based on '
              'your professional profile.',
        ),
        const _FeatureCard(
          icon: Icons.bookmark_outline,
          title: '4. Save and track opportunities',
          description:
              'Save interesting jobs and manage your '
              'applications in one place.',
        ),
        const SizedBox(height: 16),
        FilledButton.icon(
          onPressed: onOpenProfile,
          icon: const Icon(Icons.upload_file_outlined),
          label: const Padding(
            padding: EdgeInsets.symmetric(vertical: 14),
            child: Text('Upload Resume'),
          ),
        ),
      ],
    );
  }
}

class _ReadyUserHome extends StatelessWidget {
  final String profession;
  final String level;
  final VoidCallback onOpenFeed;
  final VoidCallback onOpenSaved;
  final VoidCallback onOpenProfile;

  const _ReadyUserHome({
    required this.profession,
    required this.level,
    required this.onOpenFeed,
    required this.onOpenSaved,
    required this.onOpenProfile,
  });

  @override
  Widget build(BuildContext context) {
    final professionText = profession.trim().isEmpty
        ? 'Profession not specified'
        : profession;

    final levelText = level.trim().isEmpty ? 'Level not specified' : level;

    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        const SizedBox(height: 12),
        Text(
          'Welcome back',
          style: Theme.of(
            context,
          ).textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 8),
        Text(
          'Your CareerPilot workspace is ready.',
          style: TextStyle(
            fontSize: 16,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 28),
        Card(
          child: ListTile(
            leading: const CircleAvatar(child: Icon(Icons.person_outline)),
            title: Text(professionText),
            subtitle: Text(levelText),
            trailing: const Icon(Icons.chevron_right),
            onTap: onOpenProfile,
          ),
        ),
        const SizedBox(height: 20),
        Text(
          'Continue your job search',
          style: Theme.of(
            context,
          ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        _ActionCard(
          icon: Icons.work_outline,
          title: 'Explore Job Feed',
          description: 'Discover vacancies selected for your profile.',
          onTap: onOpenFeed,
        ),
        _ActionCard(
          icon: Icons.bookmark_outline,
          title: 'Saved Jobs',
          description: 'Review opportunities you saved for later.',
          onTap: onOpenSaved,
        ),
        _ActionCard(
          icon: Icons.person_outline,
          title: 'Manage Profile',
          description: 'Update your resume and professional profile.',
          onTap: onOpenProfile,
        ),
      ],
    );
  }
}

class _FeatureCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;

  const _FeatureCard({
    required this.icon,
    required this.title,
    required this.description,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: Icon(icon),
        title: Text(title),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 6),
          child: Text(description),
        ),
      ),
    );
  }
}

class _ActionCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final VoidCallback onTap;

  const _ActionCard({
    required this.icon,
    required this.title,
    required this.description,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: Icon(icon),
        title: Text(title),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 6),
          child: Text(description),
        ),
        trailing: const Icon(Icons.chevron_right),
        onTap: onTap,
      ),
    );
  }
}

class _HomeMessage extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final String buttonLabel;
  final VoidCallback onPressed;

  const _HomeMessage({
    required this.icon,
    required this.title,
    required this.description,
    required this.buttonLabel,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 64),
            const SizedBox(height: 20),
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Text(description, textAlign: TextAlign.center),
            const SizedBox(height: 24),
            FilledButton(onPressed: onPressed, child: Text(buttonLabel)),
          ],
        ),
      ),
    );
  }
}
