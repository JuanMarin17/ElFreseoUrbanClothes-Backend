ALTER TABLE store_user DROP CONSTRAINT store_user_role_check;
ALTER TABLE store_user ADD CONSTRAINT store_user_role_check CHECK (role IN ('OWNER', 'ADMIN', 'STAFF'));
