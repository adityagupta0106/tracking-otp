package com.serviceplus.UserManagement.util;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileEncryptionUtil {

    private FileEncryptionUtil() {
    }

    public static byte[] encryptAndRead(Path sourceFile, String password, boolean deleteSourceFile) throws Exception {

        Path encryptedFile = Files.createTempFile("encrypted_", ".tmp");

        try {

            try (POIFSFileSystem fs = new POIFSFileSystem()) {

                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);

                Encryptor encryptor = info.getEncryptor();

                encryptor.confirmPassword(password);

                try (OPCPackage opc = OPCPackage.open(sourceFile.toFile());
                     OutputStream os = encryptor.getDataStream(fs)) {

                    opc.save(os);
                }

                try (FileOutputStream fos = new FileOutputStream(encryptedFile.toFile())) {
                    fs.writeFilesystem(fos);
                }
            }

            return Files.readAllBytes(encryptedFile);

        } finally {

            Files.deleteIfExists(encryptedFile);

            if (deleteSourceFile) {
                Files.deleteIfExists(sourceFile);
            }
        }
    }
}