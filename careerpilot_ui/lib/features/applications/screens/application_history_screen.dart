import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/utils/url_launcher_utils.dart';
import '../../../models/application.dart';
import '../../../providers/application_provider.dart';
import '../models/application_status.dart';

class ApplicationHistoryScreen extends ConsumerStatefulWidget {
  const ApplicationHistoryScreen({super.key});

  @override
  ConsumerState<ApplicationHistoryScreen> createState() =>
      _ApplicationHistoryScreenState();
}

class _ApplicationHistoryScreenState
    extends ConsumerState<ApplicationHistoryScreen> {
  final Set<int> _updatingApplicationIds = <int>{};

  Future<void> _openJob(Application application) async {
    final opened = await openExternalUrl(application.jobUrl);

    if (!mounted) {
      return;
    }

    if (!opened) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Failed to open vacancy')));
    }
  }

  Future<void> _refreshApplications() async {
    await ref.read(applicationProvider.notifier).refresh();
  }

  Future<void> _updateApplicationStatus(
    Application application,
    String status,
  ) async {
    if (_updatingApplicationIds.contains(application.id) ||
        application.status == status) {
      return;
    }

    setState(() {
      _updatingApplicationIds.add(application.id);
    });

    final success = await ref
        .read(applicationProvider.notifier)
        .updateStatus(applicationId: application.id, status: status);

    if (!mounted) {
      return;
    }

    setState(() {
      _updatingApplicationIds.remove(application.id);
    });

    if (!success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Failed to update application status')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final applicationsAsync = ref.watch(applicationProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('CRM', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: applicationsAsync.when(
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
                    onPressed: _refreshApplications,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Retry'),
                  ),
                ],
              ),
            ),
          );
        },
        data: (applications) {
          if (applications.isEmpty) {
            return RefreshIndicator(
              onRefresh: _refreshApplications,
              child: ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                children: const [
                  SizedBox(height: 180),
                  Icon(Icons.send_outlined, size: 64),
                  SizedBox(height: 16),
                  Center(
                    child: Text(
                      'No applications yet',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  SizedBox(height: 8),
                  Center(
                    child: Padding(
                      padding: EdgeInsets.symmetric(horizontal: 32),
                      child: Text(
                        'Jobs you apply to will appear here.',
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                ],
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: _refreshApplications,
            child: ListView.builder(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(16),
              itemCount: applications.length,
              itemBuilder: (context, index) {
                final application = applications[index];

                return _ApplicationCard(
                  application: application,
                  isUpdating: _updatingApplicationIds.contains(application.id),
                  onStatusSelected: (status) {
                    _updateApplicationStatus(application, status);
                  },
                  onOpen: () {
                    _openJob(application);
                  },
                );
              },
            ),
          );
        },
      ),
    );
  }
}

class _ApplicationCard extends StatelessWidget {
  final Application application;
  final bool isUpdating;
  final ValueChanged<String> onStatusSelected;
  final VoidCallback onOpen;

  const _ApplicationCard({
    required this.application,
    required this.isUpdating,
    required this.onStatusSelected,
    required this.onOpen,
  });

  @override
  Widget build(BuildContext context) {
    final location = application.jobLocation?.trim() ?? '';
    final workFormat = application.jobWorkFormat?.trim() ?? '';

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              application.jobTitle.isEmpty
                  ? 'Untitled vacancy'
                  : application.jobTitle,
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Text(
              application.jobCompany.isEmpty
                  ? 'Company not specified'
                  : application.jobCompany,
              style: const TextStyle(fontSize: 16),
            ),
            if (location.isNotEmpty) ...[
              const SizedBox(height: 14),
              Row(
                children: [
                  const Icon(Icons.location_on_outlined, size: 18),
                  const SizedBox(width: 6),
                  Expanded(child: Text(location)),
                ],
              ),
            ],
            if (workFormat.isNotEmpty) ...[
              const SizedBox(height: 10),
              Row(
                children: [
                  const Icon(Icons.work_outline, size: 18),
                  const SizedBox(width: 6),
                  Text(workFormat),
                ],
              ),
            ],
            const SizedBox(height: 10),
            Row(
              children: [
                const Icon(Icons.schedule, size: 18),
                const SizedBox(width: 6),
                Text('Applied ${_formatDate(application.createdAt)}'),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                if (isUpdating)
                  const SizedBox(
                    key: ValueKey('application-status-progress'),
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                else
                  PopupMenuButton<String>(
                    key: ValueKey('application-status-menu-${application.id}'),
                    tooltip: 'Change application status',
                    onSelected: onStatusSelected,
                    itemBuilder: (context) {
                      return applicationStatuses.map((status) {
                        return CheckedPopupMenuItem<String>(
                          value: status,
                          checked: application.status == status,
                          child: Text(applicationStatusLabel(status)),
                        );
                      }).toList();
                    },
                    child: Chip(
                      label: Text(applicationStatusLabel(application.status)),
                      avatar: const Icon(Icons.arrow_drop_down, size: 18),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: FilledButton.icon(
                onPressed: application.jobUrl.isEmpty ? null : onOpen,
                icon: const Icon(Icons.open_in_new),
                label: const Text('Open Vacancy'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _formatDate(DateTime date) {
    final localDate = date.toLocal();

    final day = localDate.day.toString().padLeft(2, '0');
    final month = localDate.month.toString().padLeft(2, '0');
    final year = localDate.year.toString();

    return '$day.$month.$year';
  }
}
