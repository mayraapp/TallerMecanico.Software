package com.taller.m01.service;

import com.taller.m01.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ClientePhotoStorage {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private final Path storageDirectory;

    public ClientePhotoStorage(@Value("${app.client-photo.storage-directory:./uploads/clientes}") String configuredDirectory) {
        storageDirectory = Paths.get(configuredDirectory).toAbsolutePath().normalize();
    }

    public FotografiaValidada validar(MultipartFile fotografia) {
        if (fotografia == null || fotografia.isEmpty()) return null;
        if (fotografia.getSize() > MAX_BYTES) throw new ApiException(HttpStatus.BAD_REQUEST, "FOTOGRAFIA_DEMASIADO_GRANDE", "La fotografía no debe superar 5 MB.");
        try {
            byte[] content = fotografia.getBytes();
            TipoFotografia tipo = detectarTipo(content);
            if (tipo == null) throw new ApiException(HttpStatus.BAD_REQUEST, "FOTOGRAFIA_INVALIDA", "La fotografía debe ser JPEG, PNG o WebP.");
            return new FotografiaValidada(content, tipo.extension());
        } catch (ApiException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FOTOGRAFIA_INVALIDA", "No fue posible leer la fotografía proporcionada.");
        }
    }

    public String guardar(FotografiaValidada fotografia) {
        if (fotografia == null) return null;
        try {
            Files.createDirectories(storageDirectory);
            String reference = UUID.randomUUID() + fotografia.extension();
            Path target = storageDirectory.resolve(reference).normalize();
            if (!target.startsWith(storageDirectory)) throw new ApiException(HttpStatus.BAD_REQUEST, "FOTOGRAFIA_INVALIDA", "La referencia de fotografía es inválida.");
            Files.write(target, fotografia.contenido(), StandardOpenOption.CREATE_NEW);
            return reference;
        } catch (ApiException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FOTOGRAFIA_ALMACENAMIENTO", "No fue posible almacenar la fotografía.");
        }
    }

    public FotoLeida leer(String referencia) {
        if (referencia == null || !referencia.matches("[0-9a-f-]{36}\\.(jpg|png|webp)")) throw noEncontrada();
        try {
            Path target = storageDirectory.resolve(referencia).normalize();
            if (!target.startsWith(storageDirectory) || !Files.isRegularFile(target)) throw noEncontrada();
            Resource resource = new UrlResource(target.toUri());
            if (!resource.isReadable()) throw noEncontrada();
            return new FotoLeida(resource, tipoPorReferencia(referencia).mediaType());
        } catch (IOException ex) {
            throw noEncontrada();
        }
    }

    public void eliminarSiExiste(String referencia) {
        if (referencia == null || !referencia.matches("[0-9a-f-]{36}\\.(jpg|png|webp)")) return;
        try {
            Path target = storageDirectory.resolve(referencia).normalize();
            if (target.startsWith(storageDirectory)) Files.deleteIfExists(target);
        } catch (IOException ignored) { }
    }

    public record FotografiaValidada(byte[] contenido, String extension) { }
    public record FotoLeida(Resource resource, String mediaType) { }
    private ApiException noEncontrada() { return new ApiException(HttpStatus.NOT_FOUND, "FOTOGRAFIA_NO_ENCONTRADA", "La fotografía solicitada no existe."); }
    private TipoFotografia tipoPorReferencia(String reference) { return reference.endsWith(".jpg") ? TipoFotografia.JPEG : reference.endsWith(".png") ? TipoFotografia.PNG : TipoFotografia.WEBP; }
    private TipoFotografia detectarTipo(byte[] content) {
        if (content.length >= 3 && (content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8 && (content[2] & 0xFF) == 0xFF) return TipoFotografia.JPEG;
        if (content.length >= 8 && (content[0] & 0xFF) == 0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47 && content[4] == 0x0D && content[5] == 0x0A && content[6] == 0x1A && content[7] == 0x0A) return TipoFotografia.PNG;
        if (content.length >= 12 && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F' && content[8] == 'W' && content[9] == 'E' && content[10] == 'B' && content[11] == 'P') return TipoFotografia.WEBP;
        return null;
    }
    private enum TipoFotografia {
        JPEG(".jpg", "image/jpeg"), PNG(".png", "image/png"), WEBP(".webp", "image/webp");
        private final String extension; private final String mediaType;
        TipoFotografia(String extension, String mediaType) { this.extension = extension; this.mediaType = mediaType; }
        String extension() { return extension; } String mediaType() { return mediaType; }
    }
}
