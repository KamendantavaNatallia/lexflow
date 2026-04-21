create table cases (
                       id bigserial primary key,
                       title varchar(255),
                       client varchar(255),
                       type varchar(255),
                       status varchar(255)
);

create table deadlines (
                           id bigserial primary key,
                           title varchar(255),
                           due_date date,
                           priority varchar(255),
                           completed boolean not null default false,
                           case_id bigint
);

create table notes (
                       id bigserial primary key,
                       content varchar(2000),
                       case_id bigint
);

create table documents (
                           id bigserial primary key,
                           original_file_name varchar(255) not null,
                           stored_file_name varchar(255) not null,
                           content_type varchar(255) not null,
                           file_size bigint not null,
                           uploaded_at timestamp(6) not null,
                           case_id bigint
);

alter table if exists deadlines
    add constraint fk_deadlines_case
    foreign key (case_id)
    references cases(id)
    on delete cascade;

alter table if exists notes
    add constraint fk_notes_case
    foreign key (case_id)
    references cases(id)
    on delete cascade;

alter table if exists documents
    add constraint fk_documents_case
    foreign key (case_id)
    references cases(id)
    on delete cascade;

alter table if exists documents
    add constraint uk_documents_stored_file_name
    unique (stored_file_name);

create index idx_deadlines_case_id on deadlines(case_id);
create index idx_notes_case_id on notes(case_id);
create index idx_documents_case_id on documents(case_id);

create index idx_cases_status on cases(status);
create index idx_cases_client on cases(client);
create index idx_deadlines_due_date on deadlines(due_date);