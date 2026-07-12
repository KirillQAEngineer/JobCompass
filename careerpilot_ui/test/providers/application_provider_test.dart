import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:careerpilot_ui/features/applications/services/application_api.dart';
import 'package:careerpilot_ui/models/application.dart';
import 'package:careerpilot_ui/providers/application_provider.dart';

class FakeApplicationApi extends ApplicationApi {
  FakeApplicationApi({
    required this.applications,
    this.updatedApplication,
    this.updateError,
  }) : super(Dio());

  final List<Application> applications;
  final Application? updatedApplication;
  final Object? updateError;

  int updateStatusCallCount = 0;
  int? receivedApplicationId;
  String? receivedStatus;

  @override
  Future<List<Application>> fetchApplications() async {
    return applications;
  }

  @override
  Future<Application> updateStatus({
    required int applicationId,
    required String status,
  }) async {
    updateStatusCallCount++;
    receivedApplicationId = applicationId;
    receivedStatus = status;

    if (updateError != null) {
      throw updateError!;
    }

    return updatedApplication!;
  }
}

Application makeApplication({
  required int id,
  required String status,
  required DateTime updatedAt,
}) {
  return Application(
    id: id,
    userId: 1,
    jobTitle: 'QA Engineer $id',
    jobCompany: 'Company $id',
    jobUrl: 'https://example.com/jobs/$id',
    jobLocation: 'Remote',
    jobWorkFormat: 'Remote',
    jobPublishedAt: '2026-07-10T10:00:00Z',
    jobSource: 'test',
    jobExternalId: '$id',
    status: status,
    createdAt: DateTime.utc(2026, 7, 10),
    updatedAt: updatedAt,
  );
}

void main() {
  test(
    'updateStatus replaces application with backend response and sorts state',
    () async {
      final first = makeApplication(
        id: 1,
        status: 'applied',
        updatedAt: DateTime.utc(2026, 7, 10),
      );

      final second = makeApplication(
        id: 2,
        status: 'screening',
        updatedAt: DateTime.utc(2026, 7, 11),
      );

      final updatedFirst = makeApplication(
        id: 1,
        status: 'interview',
        updatedAt: DateTime.utc(2026, 7, 12),
      );

      final api = FakeApplicationApi(
        applications: [second, first],
        updatedApplication: updatedFirst,
      );

      final container = ProviderContainer(
        overrides: [applicationApiProvider.overrideWithValue(api)],
      );

      addTearDown(container.dispose);

      await container.read(applicationProvider.future);

      final result = await container
          .read(applicationProvider.notifier)
          .updateStatus(applicationId: first.id, status: 'interview');

      final state = container.read(applicationProvider).requireValue;

      expect(result, isTrue);
      expect(api.updateStatusCallCount, 1);
      expect(api.receivedApplicationId, first.id);
      expect(api.receivedStatus, 'interview');
      expect(state.map((application) => application.id), [1, 2]);
      expect(state.first.status, 'interview');
      expect(state.first.updatedAt, DateTime.utc(2026, 7, 12));
    },
  );

  test('updateStatus keeps current state when API request fails', () async {
    final application = makeApplication(
      id: 1,
      status: 'applied',
      updatedAt: DateTime.utc(2026, 7, 10),
    );

    final api = FakeApplicationApi(
      applications: [application],
      updateError: StateError('request failed'),
    );

    final container = ProviderContainer(
      overrides: [applicationApiProvider.overrideWithValue(api)],
    );

    addTearDown(container.dispose);

    await container.read(applicationProvider.future);

    final result = await container
        .read(applicationProvider.notifier)
        .updateStatus(applicationId: application.id, status: 'offer');

    final state = container.read(applicationProvider).requireValue;

    expect(result, isFalse);
    expect(api.updateStatusCallCount, 1);
    expect(state, same(api.applications));
    expect(state.single.status, 'applied');
  });
}
