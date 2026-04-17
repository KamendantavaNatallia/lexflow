DELETE FROM notes;
DELETE FROM deadlines;
DELETE FROM documents;
DELETE FROM cases;

INSERT INTO cases (id, title, client, type, status) VALUES
                                                        (1, 'Contract Review for Acme Corp', 'Acme Corp', 'CONTRACT', 'OPEN'),
                                                        (2, 'Compliance Audit for Beta Ltd', 'Beta Ltd', 'COMPLIANCE', 'IN_PROGRESS'),
                                                        (3, 'Corporate Restructuring for Nova Group', 'Nova Group', 'CORPORATE', 'ON_HOLD'),
                                                        (4, 'Litigation Support for Horizon LLC', 'Horizon LLC', 'LITIGATION', 'CLOSED');

INSERT INTO deadlines (title, due_date, priority, completed, case_id) VALUES
                                                                          ('Review master service agreement', CURRENT_DATE + 3, 'HIGH', false, 1),
                                                                          ('Send contract revisions to client', CURRENT_DATE + 7, 'MEDIUM', false, 1),
                                                                          ('Prepare compliance checklist', CURRENT_DATE + 5, 'URGENT', false, 2),
                                                                          ('Submit audit summary', CURRENT_DATE - 2, 'HIGH', false, 2),
                                                                          ('Draft board resolution', CURRENT_DATE + 10, 'MEDIUM', false, 3),
                                                                          ('File court response', CURRENT_DATE - 5, 'URGENT', true, 4);

INSERT INTO notes (content, case_id) VALUES
                                         ('Client requested a faster turnaround on the first contract draft.', 1),
                                         ('Need to verify indemnity and termination clauses before final review.', 1),
                                         ('Compliance team is waiting for updated internal policy documents.', 2),
                                         ('Audit preparation is blocked until vendor risk data is confirmed.', 2),
                                         ('Restructuring timeline depends on board approval next week.', 3),
                                         ('Litigation matter is closed, but documents should remain available for reference.', 4);