package com.jb.resources;

import com.jb.InvalidFileFormatException;
import com.jb.LBChartCreator;
import com.jb.LBClass;
import com.jb.LastByteDistributionProcessor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Controller
public class LBDistRestController {
    @GetMapping("/")
    public String uploadForm(Model model) {
        return "uploadForm";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> generateHistogram(@RequestBody MultipartFile file) {
        LastByteDistributionProcessor processor = new LastByteDistributionProcessor();
        Map<LBClass, Map<Long, Double>> statistics;
        try {
            statistics = processor.calculateStatistics(
                    processor.parse(new BufferedReader(new InputStreamReader(file.getInputStream())).lines()));
            ByteArrayOutputStream chartStream = LBChartCreator.createChartStream(statistics);
            return ResponseEntity.ok()
                    .body(new ByteArrayResource(chartStream.toByteArray()));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ByteArrayResource(e.getMessage().getBytes()));
        }
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<?> handleInvalidFileFormat(InvalidFileFormatException exc) {
        return ResponseEntity.badRequest().build();
    }
}
