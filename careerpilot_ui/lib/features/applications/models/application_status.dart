const applicationStatuses = <String>[
  'applied',
  'screening',
  'interview',
  'technical_interview',
  'offer',
  'rejected',
];

String applicationStatusLabel(String status) {
  final normalizedStatus = status.trim().toLowerCase();

  return switch (normalizedStatus) {
    'applied' => 'Applied',
    'screening' => 'Screening',
    'interview' => 'Interview',
    'technical_interview' => 'Technical Interview',
    'offer' => 'Offer',
    'rejected' => 'Rejected',
    _ => status.isEmpty ? 'Applied' : status,
  };
}
