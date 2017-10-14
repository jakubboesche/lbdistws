package com.jb.resources;

import com.jb.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
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

    @PostMapping
    public ResponseEntity<Resource> generateHistogram(@RequestBody MultipartFile file) throws IOException {
        ByteArrayOutputStream chartStream = LBChartCreator
                .createChartStream(calculateHistogram(file));
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new ByteArrayResource(chartStream.toByteArray()));
    }

    @PostMapping(path = "/json")
    @ResponseBody
    public Map<LBClass, Map<Long, Double>> generateHistogramJson(@RequestBody MultipartFile file) throws IOException {
        return calculateHistogram(file);
    }

    private Map<LBClass, Map<Long, Double>> calculateHistogram(@RequestBody MultipartFile file) throws IOException {
        try {
            LastByteDistributionProcessor processor = new LastByteDistributionProcessor();
            return processor.calculateStatistics(
                    processor.parse(new BufferedReader(new InputStreamReader(file.getInputStream())).lines()));
        } catch (InvalidFileFormatException e) {
            throw new InvalidFileFormatWSException();
        }
    }
}
