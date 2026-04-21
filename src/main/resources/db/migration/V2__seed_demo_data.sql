insert into cases (title, client, type, status)
values
    ('Contract Review for ABC Ltd', 'ABC Ltd', 'CONTRACT', 'OPEN'),
    ('Employment Dispute - John Smith', 'John Smith', 'LITIGATION', 'IN_PROGRESS'),
    ('Trademark Compliance Review for Nova', 'Nova LLC', 'COMPLIANCE', 'ON_HOLD'),
    ('Corporate Filing for BrightWave', 'BrightWave Inc', 'CORPORATE', 'CLOSED');

insert into deadlines (title, due_date, priority, completed, case_id)
values
    ('Submit revised contract draft', current_date + 3, 'HIGH', false, 1),
    ('Prepare court filing package', current_date + 7, 'URGENT', false, 2),
    ('Respond to compliance request', current_date + 5, 'MEDIUM', false, 3),
    ('Archive final corporate documents', current_date - 2, 'LOW', true, 4);

insert into notes (content, case_id)
values
    ('Client requested a focused review of liability and termination clauses.', 1),
    ('Need to confirm timeline and collect supporting communication records.', 2),
    ('Waiting for regulator clarification before proceeding.', 3),
    ('Case completed successfully; documents should be archived.', 4);