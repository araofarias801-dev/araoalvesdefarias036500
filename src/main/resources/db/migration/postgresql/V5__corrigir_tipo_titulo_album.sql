DO $$
BEGIN
	IF EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_name = 'album'
		  AND column_name = 'titulo'
		  AND udt_name = 'bytea'
	) THEN
		ALTER TABLE album
			ALTER COLUMN titulo TYPE VARCHAR(200)
			USING convert_from(titulo, 'UTF8');
	ELSE
		ALTER TABLE album
			ALTER COLUMN titulo TYPE VARCHAR(200);
	END IF;
END $$;
