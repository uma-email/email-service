-- DDL
DROP SEQUENCE IF EXISTS message_timeline_id;
CREATE SEQUENCE IF NOT EXISTS message_timeline_id;
DROP SEQUENCE IF EXISTS message_history_id;
CREATE SEQUENCE IF NOT EXISTS message_history_id;
DROP SEQUENCE IF EXISTS label_history_id;
CREATE SEQUENCE IF NOT EXISTS label_history_id;
DROP SEQUENCE IF EXISTS filter_history_id;
CREATE SEQUENCE IF NOT EXISTS filter_history_id;
DROP SEQUENCE IF EXISTS contact_history_id;
CREATE SEQUENCE IF NOT EXISTS contact_history_id;
DROP SEQUENCE IF EXISTS resource_server_history_id;
CREATE SEQUENCE IF NOT EXISTS resource_server_history_id;


ALTER TABLE public.message ALTER COLUMN timeline_id SET NOT NULL;
ALTER TABLE public.message ALTER COLUMN timeline_id SET DEFAULT NEXTVAL('message_timeline_id');
ALTER TABLE public.message ALTER COLUMN history_id SET NOT NULL;
ALTER TABLE public.message ALTER COLUMN history_id SET DEFAULT NEXTVAL('message_history_id');
ALTER TABLE public.label ALTER COLUMN history_id SET NOT NULL;
ALTER TABLE public.label ALTER COLUMN history_id SET DEFAULT NEXTVAL('label_history_id');
ALTER TABLE public.filter ALTER COLUMN history_id SET NOT NULL;
ALTER TABLE public.filter ALTER COLUMN history_id SET DEFAULT NEXTVAL('filter_history_id');
ALTER TABLE public.contact ALTER COLUMN history_id SET NOT NULL;
ALTER TABLE public.contact ALTER COLUMN history_id SET DEFAULT NEXTVAL('contact_history_id');
ALTER TABLE public.resource_server ALTER COLUMN history_id SET NOT NULL;
ALTER TABLE public.resource_server ALTER COLUMN history_id SET DEFAULT NEXTVAL('resource_server_history_id');

-- Data
INSERT INTO user_(username) VALUES ('jdoe@acme.org'); -- John Doe
INSERT INTO user_(username) VALUES ('jroe@acme.org'); -- John Roe

INSERT INTO account(user_id, email_address, display_name, username) VALUES (1, 'izboran@gmail.com', 'Igor Zboran (Gmail-1)', 'izboran@gmail.com');
INSERT INTO account(user_id, email_address, display_name, username) VALUES (1, 'izboran@hotmail.com', 'Igor Zboran (Hotmail)', 'izboran@hotmail.com');
INSERT INTO account(user_id, email_address, display_name, username) VALUES (2, 'igor.zboran@gmail.com', 'Igor Zboran (Gmail-2)', 'igor.zboran@gmail.com');
INSERT INTO account(user_id, email_address, username) VALUES (2, 'theo32@protonmail.com', 'theo32@protonmail.com');

INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (1, 'rnd1VLFLDzBJD66ePDc3', 'Hello again!', now());
INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (1, 'YPRYxyEQtkGdwM4e9pvR', 'Hello!', now());
INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (2, 'HydCfgcpHxB0dgcNOH8r', 'Hi!', now());
INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (2, 'cw3rK7wGvQlvUFKuU6h3', 'Hi again!', now());
INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (3, 'OGBeDkLxQwj6BcrDKJrr', 'Holla!', now());
INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (3, 'bhxe7KY2jIuNx9BDUfL0', 'Hola de nuevo!', now());
INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (4, '7S6EY42zD3WgqtXPmSAF', 'Bonjour!', now());
INSERT INTO message(account_id, message_id, subject, sent_at) VALUES (4, 'Vg3iHbhbZa93uK1mtzNi', 'Rebonjour!', now());

INSERT INTO msg_tag(message_id, name, attributes) VALUES (1, 'Invoice', '{"roles":["INVOICE"]}');
INSERT INTO msg_tag(message_id, name, attributes) VALUES (1, 'Project', '{"roles":["PROJECT"]}');
INSERT INTO msg_tag(message_id, name, attributes) VALUES (1, 'Contract', '{"roles":["INVOICE", "TELCO"]}');

INSERT INTO label(user_id, name, role) VALUES (1, 'Inbox', 'INBOX');
INSERT INTO label(user_id, name, role) VALUES (1, 'Snoozed', 'SNOOZED');
INSERT INTO label(user_id, name, role) VALUES (1, 'Sent', 'SENT');
INSERT INTO label(user_id, name, role) VALUES (1, 'Drafts', 'DRAFTS');
INSERT INTO label(user_id, name, role) VALUES (2, 'Inbox', 'INBOX');
INSERT INTO label(user_id, name, role) VALUES (2, 'Snoozed', 'SNOOZED');
INSERT INTO label(user_id, name, role) VALUES (2, 'Sent', 'SENT');
INSERT INTO label(user_id, name, role) VALUES (2, 'Drafts', 'DRAFTS');
INSERT INTO label(user_id, name, role) VALUES (1, 'Invoices', 'INVOICE');
INSERT INTO label(user_id, parent_id, name) VALUES (1, 9, 'O2');
INSERT INTO label(user_id, parent_id, name) VALUES (1, 9, 'Orange');
INSERT INTO label(user_id, name, role) VALUES (1, 'Personal', 'CATEGORY_PERSONAL');
INSERT INTO label(user_id, name, role) VALUES (1, 'Personal', 'CATEGORY_SOCIAL');
INSERT INTO label(user_id, name, role) VALUES (1, 'Personal', 'CATEGORY_PROMOTIONS');
INSERT INTO label(user_id, name, role) VALUES (1, 'Personal', 'CATEGORY_UPDATES');
INSERT INTO label(user_id, name, role) VALUES (1, 'Personal', 'CATEGORY_FORUMS');
INSERT INTO label(user_id, name, role) VALUES (1, 'Personal', 'CATEGORY_BUSINESS');
INSERT INTO label(user_id, name, role) VALUES (1, 'Personal', 'CATEGORY_HEALTHCARE');
INSERT INTO label(user_id, name, role) VALUES (2, 'Personal', 'CATEGORY_PERSONAL');
INSERT INTO label(user_id, name, role) VALUES (2, 'Personal', 'CATEGORY_SOCIAL');
INSERT INTO label(user_id, name, role) VALUES (2, 'Personal', 'CATEGORY_PROMOTIONS');
INSERT INTO label(user_id, name, role) VALUES (2, 'Personal', 'CATEGORY_UPDATES');
INSERT INTO label(user_id, name, role) VALUES (2, 'Personal', 'CATEGORY_FORUMS');
INSERT INTO label(user_id, name, role) VALUES (2, 'Personal', 'CATEGORY_BUSINESS');
INSERT INTO label(user_id, name, role) VALUES (2, 'Personal', 'CATEGORY_HEALTHCARE');



INSERT INTO message_label(message_id, label_id) VALUES (1, 1);
INSERT INTO message_label(message_id, label_id) VALUES (1, 2);
INSERT INTO message_label(message_id, label_id) VALUES (2, 1);
INSERT INTO message_label(message_id, label_id) VALUES (3, 3);
INSERT INTO message_label(message_id, label_id) VALUES (4, 3);
INSERT INTO message_label(message_id, label_id) VALUES (5, 5);
INSERT INTO message_label(message_id, label_id) VALUES (6, 5);
INSERT INTO message_label(message_id, label_id) VALUES (7, 7);
INSERT INTO message_label(message_id, label_id) VALUES (8, 7);

INSERT INTO msg_recipient_to(message_id, display_name, email_address) VALUES (1, 'John Doe', 'jdoe@acme.org');
INSERT INTO msg_recipient_to(message_id, display_name, email_address) VALUES (1, 'Mary Doe', 'mdoe@acme.org');
INSERT INTO msg_recipient_to(message_id, display_name, email_address) VALUES (2, 'Theo Brown', 'tbrown@acme.org');
INSERT INTO msg_recipient_to(message_id, display_name, email_address) VALUES (5, 'Alice Williams', 'awilliams@acme.org');
INSERT INTO msg_recipient_cc(message_id, display_name, email_address) VALUES (1, 'Theo Brown', 'tbrown@acme.org');
INSERT INTO msg_recipient_bcc(message_id, display_name, email_address) VALUES (2, 'Alice Williams', 'awilliams@acme.org');

INSERT INTO msg_attachment(message_id, filename, mimetype, resource_url) VALUES (1, 'Contract.pdf', 'application/pdf', 'http://localhost:7000/fEuVMDQ7ch5YEM8MKFWE');
INSERT INTO msg_attachment(message_id, filename, mimetype, resource_url) VALUES (1, 'Template.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'http://localhost:7000/XyLDEnBSYV04hTxNJDN2');
INSERT INTO msg_attachment(message_id, filename, mimetype, resource_url) VALUES (2, 'Invoice.pdf', 'application/pdf', 'http://localhost:7000/5FHOuZbh2eoCHjfqpIz9');
INSERT INTO msg_attachment(message_id, filename, mimetype, resource_url) VALUES (5, 'Forecast.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'http://localhost:7000/GCn1P02tHIXf4j0NlGmL');

INSERT INTO filter(user_id, name) VALUES (1, 'O2 Invoice');
INSERT INTO filter(user_id, name) VALUES (1, 'Orange Invoice');

INSERT INTO filter_label(filter_id, label_id) VALUES (1, 10);
INSERT INTO filter_label(filter_id, label_id) VALUES (2, 11);

INSERT INTO contact(user_id, first_name, last_name, email_address) VALUES (1, 'Mary', 'Doe', 'jdoe@acme.org');
INSERT INTO contact(user_id, first_name, last_name, email_address) VALUES (1, 'John', 'Doe', 'mdoe@acme.org');
INSERT INTO contact(user_id, first_name, last_name, email_address) VALUES (1, 'Theo', 'Brown', 'tbrown@acme.org');
INSERT INTO contact(user_id, first_name, last_name, email_address) VALUES (2, 'Alice', '', 'awilliams@acme.org');

INSERT INTO resource_server(account_id, name, role, url) VALUES (1, 'Personal', 'CATEGORY_PERSONAL', 'http://localhost:7000');
INSERT INTO resource_server(account_id, name, role, url) VALUES (1, 'Business', 'CATEGORY_BUSINESS', 'http://localhost:7000');
INSERT INTO resource_server(account_id, name, role, url) VALUES (1, 'My healthcare', 'CATEGORY_HEALTHCARE', 'http://localhost:8000');
