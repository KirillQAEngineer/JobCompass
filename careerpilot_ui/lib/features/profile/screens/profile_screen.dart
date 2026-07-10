import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../providers/auth_provider.dart';
import '../../../providers/profile_provider.dart';
import '../../../providers/resume_upload_provider.dart';
import '../../settings/screens/settings_screen.dart';
import 'edit_profile_screen.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profileAsync = ref.watch(profileProvider);
    final uploadState = ref.watch(resumeUploadProvider);
    final isUploading = uploadState.isLoading;

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Profile',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        actions: [
          profileAsync.maybeWhen(
            data: (profile) {
              if (profile == null) {
                return const SizedBox.shrink();
              }

              return IconButton(
                tooltip: 'Edit Profile',
                icon: const Icon(Icons.edit_outlined),
                onPressed: isUploading
                    ? null
                    : () async {
                        final updated = await Navigator.push<bool>(
                          context,
                          MaterialPageRoute(
                            builder: (_) => EditProfileScreen(profile: profile),
                          ),
                        );

                        if (updated == true) {
                          ref.invalidate(profileProvider);
                        }
                      },
              );
            },
            orElse: () => const SizedBox.shrink(),
          ),
          IconButton(
            tooltip: 'Settings',
            icon: const Icon(Icons.settings_outlined),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const SettingsScreen()),
              );
            },
          ),
        ],
      ),
      body: profileAsync.when(
        loading: () {
          return const Center(child: CircularProgressIndicator());
        },
        error: (error, stackTrace) {
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.error_outline, size: 48),
                  const SizedBox(height: 16),
                  Text(error.toString(), textAlign: TextAlign.center),
                  const SizedBox(height: 20),
                  FilledButton.icon(
                    onPressed: () {
                      ref.invalidate(profileProvider);
                    },
                    icon: const Icon(Icons.refresh),
                    label: const Text('Retry'),
                  ),
                ],
              ),
            ),
          );
        },
        data: (profile) {
          if (profile == null) {
            return _EmptyProfileState(
              isUploading: isUploading,
              onUpload: () async {
                final uploaded = await ref
                    .read(resumeUploadProvider.notifier)
                    .pickAndUploadResume();

                if (!context.mounted) {
                  return;
                }

                if (uploaded) {
                  ref.invalidate(profileProvider);

                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Resume uploaded and profile created'),
                    ),
                  );

                  return;
                }

                final state = ref.read(resumeUploadProvider);

                if (state.hasError) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Failed to upload resume')),
                  );
                }
              },
              onLogout: () async {
                await ref.read(authProvider.notifier).logout();
              },
            );
          }

          final hasResume = profile.resumeText.trim().isNotEmpty;

          return RefreshIndicator(
            onRefresh: () async {
              ref.invalidate(profileProvider);
              await ref.read(profileProvider.future);
            },
            child: ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(24),
              children: [
                CircleAvatar(
                  radius: 46,
                  child: Text(
                    _getProfileInitial(profile.profession),
                    style: const TextStyle(
                      fontSize: 34,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                Text(
                  profile.profession.isEmpty
                      ? 'Profession not specified'
                      : profile.profession,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  profile.level.isEmpty ? 'Level not specified' : profile.level,
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 16,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(height: 30),
                _ProfileSection(
                  icon: Icons.badge_outlined,
                  title: 'Preferred Roles',
                  value: profile.preferredRoles,
                  emptyValue: 'No preferred roles specified',
                ),
                _ProfileSection(
                  icon: Icons.psychology_outlined,
                  title: 'Skills',
                  value: profile.skills,
                  emptyValue: 'No skills specified',
                ),
                _ProfileSection(
                  icon: Icons.code,
                  title: 'Technologies',
                  value: profile.technologies,
                  emptyValue: 'No technologies specified',
                ),
                _ProfileSection(
                  icon: Icons.language,
                  title: 'English Level',
                  value: profile.englishLevel,
                  emptyValue: 'English level not specified',
                ),
                Card(
                  child: Column(
                    children: [
                      ListTile(
                        leading: const Icon(Icons.description_outlined),
                        title: const Text('Resume'),
                        subtitle: Text(
                          hasResume ? 'Resume uploaded' : 'No resume uploaded',
                        ),
                        trailing: hasResume
                            ? const Icon(Icons.chevron_right)
                            : null,
                        onTap: hasResume
                            ? () {
                                _showResume(context, profile.resumeText);
                              }
                            : null,
                      ),
                      const Divider(height: 1),
                      Padding(
                        padding: const EdgeInsets.all(12),
                        child: SizedBox(
                          width: double.infinity,
                          child: FilledButton.icon(
                            onPressed: isUploading
                                ? null
                                : () async {
                                    final uploaded = await ref
                                        .read(resumeUploadProvider.notifier)
                                        .pickAndUploadResume();

                                    if (!context.mounted) {
                                      return;
                                    }

                                    if (uploaded) {
                                      ScaffoldMessenger.of(
                                        context,
                                      ).showSnackBar(
                                        SnackBar(
                                          content: Text(
                                            hasResume
                                                ? 'Resume replaced and profile updated'
                                                : 'Resume uploaded and profile updated',
                                          ),
                                        ),
                                      );
                                      return;
                                    }

                                    final state = ref.read(
                                      resumeUploadProvider,
                                    );

                                    if (state.hasError) {
                                      ScaffoldMessenger.of(
                                        context,
                                      ).showSnackBar(
                                        const SnackBar(
                                          content: Text(
                                            'Failed to upload resume',
                                          ),
                                        ),
                                      );
                                    }
                                  },
                            icon: isUploading
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  )
                                : Icon(
                                    hasResume
                                        ? Icons.sync_outlined
                                        : Icons.upload_file_outlined,
                                  ),
                            label: Padding(
                              padding: const EdgeInsets.symmetric(vertical: 12),
                              child: Text(
                                isUploading
                                    ? 'Analyzing Resume...'
                                    : hasResume
                                    ? 'Replace Resume'
                                    : 'Upload Resume',
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 32),
                OutlinedButton.icon(
                  onPressed: isUploading
                      ? null
                      : () async {
                          final confirmed = await showDialog<bool>(
                            context: context,
                            builder: (dialogContext) {
                              return AlertDialog(
                                title: const Text('Delete Resume'),
                                content: const Text(
                                  'This will permanently delete your '
                                  'resume and profile data. Continue?',
                                ),
                                actions: [
                                  TextButton(
                                    onPressed: () {
                                      Navigator.pop(dialogContext, false);
                                    },
                                    child: const Text('Cancel'),
                                  ),
                                  FilledButton(
                                    onPressed: () {
                                      Navigator.pop(dialogContext, true);
                                    },
                                    child: const Text('Delete'),
                                  ),
                                ],
                              );
                            },
                          );

                          if (confirmed != true || !context.mounted) {
                            return;
                          }

                          final deleted = await ref
                              .read(profileDeleteProvider.notifier)
                              .deleteProfile();

                          if (!context.mounted) {
                            return;
                          }

                          if (deleted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Resume deleted successfully'),
                              ),
                            );
                            return;
                          }

                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Failed to delete resume'),
                            ),
                          );
                        },
                  icon: const Icon(Icons.delete_outline),
                  label: const Padding(
                    padding: EdgeInsets.symmetric(vertical: 14),
                    child: Text('Delete Resume'),
                  ),
                ),
                const SizedBox(height: 12),
                OutlinedButton.icon(
                  onPressed: isUploading
                      ? null
                      : () async {
                          await ref.read(authProvider.notifier).logout();
                        },
                  icon: const Icon(Icons.logout),
                  label: const Padding(
                    padding: EdgeInsets.symmetric(vertical: 14),
                    child: Text('Logout'),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  String _getProfileInitial(String profession) {
    final value = profession.trim();

    if (value.isEmpty) {
      return '?';
    }

    return value[0].toUpperCase();
  }

  void _showResume(BuildContext context, String resumeText) {
    showDialog<void>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Resume'),
          content: SizedBox(
            width: 600,
            child: SingleChildScrollView(child: SelectableText(resumeText)),
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(dialogContext);
              },
              child: const Text('Close'),
            ),
          ],
        );
      },
    );
  }
}

class _ProfileSection extends StatelessWidget {
  final IconData icon;
  final String title;
  final String value;
  final String emptyValue;

  const _ProfileSection({
    required this.icon,
    required this.title,
    required this.value,
    required this.emptyValue,
  });

  @override
  Widget build(BuildContext context) {
    final displayValue = value.trim().isEmpty ? emptyValue : value;

    return Card(
      child: ListTile(
        leading: Icon(icon),
        title: Text(title),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 6),
          child: Text(displayValue),
        ),
      ),
    );
  }
}

class _EmptyProfileState extends StatelessWidget {
  final bool isUploading;
  final Future<void> Function() onUpload;
  final Future<void> Function() onLogout;

  const _EmptyProfileState({
    required this.isUploading,
    required this.onUpload,
    required this.onLogout,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(32),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 520),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.description_outlined, size: 72),
              const SizedBox(height: 24),
              const Text(
                'Create Your Profile',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 12),
              Text(
                'Upload your resume to automatically create '
                'your CareerPilot profile and fill in your '
                'skills, technologies, experience level, '
                'and preferred roles.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 28),
              SizedBox(
                width: double.infinity,
                child: FilledButton.icon(
                  onPressed: isUploading
                      ? null
                      : () async {
                          await onUpload();
                        },
                  icon: isUploading
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.upload_file_outlined),
                  label: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    child: Text(
                      isUploading ? 'Analyzing Resume...' : 'Upload Resume',
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              TextButton.icon(
                onPressed: isUploading
                    ? null
                    : () async {
                        await onLogout();
                      },
                icon: const Icon(Icons.logout),
                label: const Text('Logout'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
