INSERT INTO artista (nome)
SELECT 'Serj Tankian'
WHERE NOT EXISTS (
	SELECT 1 FROM artista WHERE nome = 'Serj Tankian'
);

INSERT INTO artista (nome)
SELECT 'Mike Shinoda'
WHERE NOT EXISTS (
	SELECT 1 FROM artista WHERE nome = 'Mike Shinoda'
);

INSERT INTO artista (nome)
SELECT 'Michel Teló'
WHERE NOT EXISTS (
	SELECT 1 FROM artista WHERE nome = 'Michel Teló'
);

INSERT INTO artista (nome)
SELECT 'Guns N'' Roses'
WHERE NOT EXISTS (
	SELECT 1 FROM artista WHERE nome = 'Guns N'' Roses'
);

INSERT INTO album (titulo)
SELECT 'Harakiri'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Harakiri'
);

INSERT INTO album (titulo)
SELECT 'Black Blooms'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Black Blooms'
);

INSERT INTO album (titulo)
SELECT 'The Rough Dog'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'The Rough Dog'
);

INSERT INTO album (titulo)
SELECT 'The Rising Tied'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'The Rising Tied'
);

INSERT INTO album (titulo)
SELECT 'Post Traumatic'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Post Traumatic'
);

INSERT INTO album (titulo)
SELECT 'Post Traumatic EP'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Post Traumatic EP'
);

INSERT INTO album (titulo)
SELECT 'Where''d You Go'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Where''d You Go'
);

INSERT INTO album (titulo)
SELECT 'Bem Sertanejo'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Bem Sertanejo'
);

INSERT INTO album (titulo)
SELECT 'Bem Sertanejo - O Show (Ao Vivo)'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Bem Sertanejo - O Show (Ao Vivo)'
);

INSERT INTO album (titulo)
SELECT 'Bem Sertanejo - (1ª Temporada) - EP'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Bem Sertanejo - (1ª Temporada) - EP'
);

INSERT INTO album (titulo)
SELECT 'Use Your Illusion I'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Use Your Illusion I'
);

INSERT INTO album (titulo)
SELECT 'Use Your Illusion II'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Use Your Illusion II'
);

INSERT INTO album (titulo)
SELECT 'Greatest Hits'
WHERE NOT EXISTS (
	SELECT 1 FROM album WHERE titulo = 'Greatest Hits'
);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Harakiri'
WHERE a.nome = 'Serj Tankian'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Black Blooms'
WHERE a.nome = 'Serj Tankian'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'The Rough Dog'
WHERE a.nome = 'Serj Tankian'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'The Rising Tied'
WHERE a.nome = 'Mike Shinoda'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Post Traumatic'
WHERE a.nome = 'Mike Shinoda'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Post Traumatic EP'
WHERE a.nome = 'Mike Shinoda'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Where''d You Go'
WHERE a.nome = 'Mike Shinoda'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Bem Sertanejo'
WHERE a.nome = 'Michel Teló'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Bem Sertanejo - O Show (Ao Vivo)'
WHERE a.nome = 'Michel Teló'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Bem Sertanejo - (1ª Temporada) - EP'
WHERE a.nome = 'Michel Teló'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Use Your Illusion I'
WHERE a.nome = 'Guns N'' Roses'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Use Your Illusion II'
WHERE a.nome = 'Guns N'' Roses'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id
FROM artista a
JOIN album al ON al.titulo = 'Greatest Hits'
WHERE a.nome = 'Guns N'' Roses'
	AND NOT EXISTS (
		SELECT 1
		FROM artista_album aa
		WHERE aa.artista_id = a.id
			AND aa.album_id = al.id
	);
