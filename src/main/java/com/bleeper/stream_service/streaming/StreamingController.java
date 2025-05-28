package com.bleeper.stream_service.streaming;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stream")
public class StreamingController {
    private static final Logger logger = LoggerFactory.getLogger(StreamingController.class);
    private static final String UPLOAD_DIR = "/home/bas/dev/bleeper/upload-service/uploads/";
    private static final String UPLOAD_DIR_DOCKER = "/home/bas/dev/bleeper/upload-service/uploads/";

    private final SupabaseS3Service supabaseS3Service;

//    @GetMapping("/public/{id}")
//    public ResponseEntity<Resource> streamPublicVideo(
//            @RequestHeader(value = "Range", required = false) String rangeHeader,
//            @PathVariable Long id
//    ) throws IOException {
//        String videoPath = UPLOAD_DIR + "recorded-video-" + id + ".mp4";
//        File videoFile = new File(videoPath);
//        long fileSize = videoFile.length();
//        Path path = Paths.get(videoPath);
//        String mimeType = Files.probeContentType(path);
//        if (mimeType == null) mimeType = "application/octet-stream";
//
//        long start = 0;
//        long end = fileSize - 1;
//
//        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
//            String[] ranges = rangeHeader.substring(6).split("-");
//            start = Long.parseLong(ranges[0]);
//            if (ranges.length > 1 && !ranges[1].isEmpty()) {
//                end = Long.parseLong(ranges[1]);
//            }
//        }
//
//        if (start > end) start = end;
//
//        long contentLength = end - start + 1;
//        InputStream inputStream = new BufferedInputStream(new FileInputStream(videoFile));
//        inputStream.skip(start);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Type", mimeType);
//        headers.set("Accept-Ranges", "bytes");
//        headers.set("Content-Length", String.valueOf(contentLength));
//        headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
//
//        logger.info("Streaming video: " + path.toAbsolutePath() + " - " + rangeHeader);
//        return new ResponseEntity<>(
//                new InputStreamResource(inputStream),
//                headers,
//                rangeHeader == null ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT
//        );
//    }

    @GetMapping("/public/{filename}")
    public ResponseEntity<Resource> streamPublicVideoByFilename(
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            @PathVariable String filename
    ) throws IOException {

        ResponseInputStream<GetObjectResponse> s3Object = supabaseS3Service.getObject("public-videos", filename);
        GetObjectResponse metadata = s3Object.response();

        byte[] data = s3Object.readAllBytes();
        long contentLength = metadata.contentLength();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM));

        if (rangeHeader != null && metadata.contentRange() != null) {
            headers.set(HttpHeaders.CONTENT_RANGE, metadata.contentRange());
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.setContentLength(data.length);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(new ByteArrayResource(data));
        } else {
            headers.setContentLength(contentLength);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(data));
        }
    }

    @GetMapping("/private/{filename}")
    public ResponseEntity<Resource> streamPrivateVideoByFilename(
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            @AuthenticationPrincipal Jwt jwt,
            //@PathVariable String bucket,
            @PathVariable String filename
    ) throws IOException {
        // 1. Get the user ID from the JWT
        Long userId = Long.parseLong(jwt.getSubject());

        // 2. Construct path: /{userId}/{filename}
        String filePath = userId + "/" + filename;

        ResponseInputStream<GetObjectResponse> s3Object = supabaseS3Service.getObject("private-videos", filePath);
        GetObjectResponse metadata = s3Object.response();

        byte[] data = s3Object.readAllBytes();
        long contentLength = metadata.contentLength();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM));

        if (rangeHeader != null && metadata.contentRange() != null) {
            headers.set(HttpHeaders.CONTENT_RANGE, metadata.contentRange());
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.setContentLength(data.length);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(new ByteArrayResource(data));
        } else {
            headers.setContentLength(contentLength);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(data));
        }
    }
}
