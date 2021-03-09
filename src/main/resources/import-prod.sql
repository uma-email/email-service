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
