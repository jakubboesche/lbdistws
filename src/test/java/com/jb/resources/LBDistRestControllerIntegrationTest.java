package com.jb.resources;


import com.jayway.jsonassert.JsonAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LBDistRestControllerIntegrationTest {
    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/");
    }

    @Test
    public void shouldGetForm() {
        ResponseEntity<String> response = template.getForEntity(base.toString(), String.class);
        assertThat(response.getBody()).contains("File format: CSV, columns");
    }

    @Test
    public void shouldGenerateChart() throws Exception {
        ClassPathResource resource = new ClassPathResource("testFile.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<Resource> response = template.postForEntity("/", map,
                Resource.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody()).isOfAnyClassIn(ByteArrayResource.class);
        assertThat(response.getBody().contentLength()).isGreaterThan(10000);
    }

    @Test
    @Parameters
    public void shouldThrowBadRequestWhenMissingColumn() {
        ClassPathResource resource = new ClassPathResource("testFile_missing_column.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/", map, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }

    @Test
    @Parameters
    public void shouldThrowBadRequestWhenInvalidInputFile() {
        ClassPathResource resource = new ClassPathResource("image_invalid_format.png", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/", map, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }

    @Test
    public void shouldGenerateJson() {
        ClassPathResource resource = new ClassPathResource("testFile.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/json", map, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody().length()).isGreaterThan(1);
        JsonAssert.with(response.getBody()).assertThat("$.BETWEEN100kBAND1MB.7931", equalTo(20.0));
    }

    @Test
    public void shouldThrowBadRequestGeneratingJsonWhenMissingColumn() {
        ClassPathResource resource = new ClassPathResource("testFile_missing_column.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/json", map, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }

    @Test
    public void shouldThrowBadRequestGeneratingJsonWhenInvalidInputFile() {
        ClassPathResource resource = new ClassPathResource("image_invalid_format.png", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/json", map, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }
}