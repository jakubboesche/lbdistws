package com.jb.resources;


import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LBDistRestControllerIT {
    private static final String INPUT_FILE_NAME = "src/test/resources/testFile.csv";
    private Path path;

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/");

        path = FileSystems.getDefault().getPath(INPUT_FILE_NAME);

    }

    @Test
    public void shouldGetForm() throws Exception {
        ResponseEntity<String> response = template.getForEntity(base.toString(),
                String.class);
        assertThat(response.getBody(), containsString("File format: CSV, columns"));
    }

    @Test
    public void shouldGenerateChart() throws Exception {
        ClassPathResource resource = new ClassPathResource("testFile.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", resource);
        ResponseEntity<Resource> response = template.postForEntity("/", map,
                Resource.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isOfAnyClassIn(ByteArrayResource.class);
        Assertions.assertThat(response.getBody().contentLength()).isGreaterThan(10000);
    }

    @Test
    public void shouldThrowBadRequestWhenMissingColumn() throws Exception {
        ClassPathResource resource = new ClassPathResource("testFile_missing_column.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", resource);
        ResponseEntity<Resource> response = template.postForEntity("/", map,
                Resource.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
    }
}