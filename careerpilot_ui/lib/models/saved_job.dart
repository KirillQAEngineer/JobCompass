import 'package:careerpilot_ui/features/feed/models/filterable_job.dart';

class SavedJob implements FilterableJob {
  final int id;
  final int userId;
  @override
  final String title;
  @override
  final String company;
  final String url;
  @override
  final String location;
  @override
  final String? workFormat;
  @override
  final DateTime? publishedAt;
  final String action;
  final DateTime? createdAt;

  const SavedJob({
    required this.id,
    required this.userId,
    required this.title,
    required this.company,
    required this.url,
    required this.location,
    required this.workFormat,
    required this.publishedAt,
    required this.action,
    required this.createdAt,
  });

  @override
  String get stableKey {
    return id > 0 ? 'saved::$id' : url;
  }

  factory SavedJob.fromJson(Map<String, dynamic> json) {
    return SavedJob(
      id: json['id'] as int? ?? 0,
      userId: json['user_id'] as int? ?? 0,
      title: json['job_title'] as String? ?? '',
      company: json['job_company'] as String? ?? '',
      url: json['job_url'] as String? ?? '',
      location: json['job_location']?.toString() ?? '',
      workFormat: json['job_work_format']?.toString(),
      publishedAt: json['job_published_at'] == null
          ? null
          : DateTime.tryParse(json['job_published_at'].toString()),
      action: json['action'] as String? ?? '',
      createdAt: json['created_at'] == null
          ? null
          : DateTime.tryParse(json['created_at'].toString()),
    );
  }
}
