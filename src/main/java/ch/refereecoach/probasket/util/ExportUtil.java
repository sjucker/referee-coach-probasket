package ch.refereecoach.probasket.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class ExportUtil {

    public static ResponseEntity<Resource> export(byte[] bytes, String contentType) {
        try {
            var resource = new ByteArrayResource(bytes);

            return ResponseEntity.ok()
                                 .header(CONTENT_TYPE, contentType)
                                 .contentLength(bytes.length)
                                 .body(resource);

        } catch (RuntimeException e) {
            log.error("export failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public static ResponseEntity<Resource> export(File file) {
        var path = Paths.get(file.getAbsolutePath());
        try {
            return export(Files.readAllBytes(path), Files.probeContentType(path));
        } catch (IOException e) {
            log.error("export failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
