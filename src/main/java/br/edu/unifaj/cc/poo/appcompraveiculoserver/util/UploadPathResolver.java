package br.edu.unifaj.cc.poo.appcompraveiculoserver.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class UploadPathResolver {

    private UploadPathResolver() {}

    public static Path resolveDentroDeUploads(Path uploadDir, String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            throw new IllegalArgumentException("Nome de arquivo vazio");
        }
        Path base = uploadDir.normalize().toAbsolutePath();
        Path resolvido = base.resolve(nomeArquivo).normalize().toAbsolutePath();
        if (!resolvido.startsWith(base)) {
            throw new IllegalArgumentException("Nome de arquivo inválido: " + nomeArquivo);
        }
        return resolvido;
    }

    public static String gerarNomeSeguro(String nomeOriginal) {
        String extensao = "";
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
        }
        return UUID.randomUUID() + extensao;
    }

    public static void apagarSeExistir(Path uploadDir, String nomeArquivo) throws IOException {
        if (nomeArquivo == null || nomeArquivo.isBlank()) return;
        try {
            Files.deleteIfExists(resolveDentroDeUploads(uploadDir, nomeArquivo));
        } catch (IllegalArgumentException e) {
            // nome suspeito - ignora em vez de apagar algo fora de uploads
        }
    }
}