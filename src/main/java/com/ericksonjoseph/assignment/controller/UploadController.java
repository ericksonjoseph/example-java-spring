
package com.ericksonjoseph.assignment.controller;

import com.ericksonjoseph.assignment.fs.StorageFileNotFoundException;
import com.ericksonjoseph.assignment.fs.StorageService;
import com.ericksonjoseph.assignment.video.VideoValidationService;
import com.ericksonjoseph.assignment.video.VideoValidationException;
import com.ericksonjoseph.assignment.fs.StorageException;
import com.ericksonjoseph.assignment.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class UploadController {

    private final StorageService storageService;
    private final VideoValidationService videoValidationService;

    private String uploadDir;
    private String path;

    @Autowired
    public UploadController(StorageService storageService, VideoValidationService videoValidationService) {
        this.uploadDir = Config.get("app.video.uploadDir");
        this.storageService = storageService;
        this.videoValidationService = videoValidationService
            .setMaxDuration(Config.getFloat("app.video.max-duration"))
            .setFormat(Config.get("app.video.required-format"));
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, 
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) throws IOException {

        String response = "You successfully uploaded " + file.getOriginalFilename() + ". A backup has been created in S3";
        String videoInfo = "";

        try {
            // Upload the video
            path = storageService.store(file);

            //Validate video
            videoInfo = videoValidationService.validate(path);

            // Upload to S3 @TODO New Thread
            storageService.backup(path);

        } catch (StorageException e) {
            response = "Failed to upload a file";

        } catch (VideoValidationException o) {
            response = "Failed to validate the video file. Error: " + o.getMessage();
            Files.delete(Paths.get(path));
        }

        redirectAttributes.addFlashAttribute("message", response);
        redirectAttributes.addFlashAttribute("videoInfo", videoInfo);

        return "redirect:/upload";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/upload")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService
                .loadAll()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(UploadController.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList()));

        return "upload";
    }
}
