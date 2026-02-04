ALTER TABLE artista
	ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'CANTOR';

UPDATE artista
SET tipo = 'BANDA'
WHERE upper(nome) IN (upper('Guns N'' Roses'));
