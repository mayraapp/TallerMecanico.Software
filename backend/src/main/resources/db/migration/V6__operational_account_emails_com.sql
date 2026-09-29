-- Keeps operational-account identifiers under the requested .com domain without altering hashes.
-- The legacy fallback preserves an existing record if a .com account was created manually first.
UPDATE usuarios legacy
LEFT JOIN usuarios current_account ON LOWER(current_account.email) = 'administrador@taller.com'
SET legacy.email = CASE
    WHEN current_account.id IS NULL THEN 'administrador@taller.com'
    ELSE CONCAT('administrador+legacy-', legacy.id, '@taller.com')
END
WHERE LOWER(legacy.email) = 'administrador@taller.local';

UPDATE usuarios legacy
LEFT JOIN usuarios current_account ON LOWER(current_account.email) = 'recepcionista@taller.com'
SET legacy.email = CASE
    WHEN current_account.id IS NULL THEN 'recepcionista@taller.com'
    ELSE CONCAT('recepcionista+legacy-', legacy.id, '@taller.com')
END
WHERE LOWER(legacy.email) = 'recepcionista@taller.local';

-- Historical login attempts retain their referential data but use the same public account identifier.
UPDATE intentos_acceso
SET email = 'administrador@taller.com'
WHERE LOWER(email) = 'administrador@taller.local';

UPDATE intentos_acceso
SET email = 'recepcionista@taller.com'
WHERE LOWER(email) = 'recepcionista@taller.local';
