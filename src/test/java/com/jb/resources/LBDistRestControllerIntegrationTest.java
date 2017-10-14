package com.jb.resources;


import com.jayway.jsonassert.JsonAssert;
import org.assertj.core.api.Assertions;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
    public void shouldGetForm() throws Exception {
        ResponseEntity<String> response = template.getForEntity(base.toString(),
                String.class);
        assertThat(response.getBody(), containsString("File format: CSV, columns"));
    }

    @Test
    public void shouldGenerateChart() throws Exception {
        ClassPathResource resource = new ClassPathResource("testFile.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<Resource> response = template.postForEntity("/", map,
                Resource.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isOfAnyClassIn(ByteArrayResource.class);
        Assertions.assertThat(response.getBody().contentLength()).isGreaterThan(10000);
    }

    @Test
    @Parameters
    public void shouldThrowBadRequestWhenMissingColumn() throws Exception {
        ClassPathResource resource = new ClassPathResource("testFile_missing_column.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/", map,
                String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }

    @Test
    @Parameters
    public void shouldThrowBadRequestWhenInvalidInputFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("image_invalid_format.png", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/", map,
                String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }

    @Test
    public void shouldGenerateJson() throws Exception {
        ClassPathResource resource = new ClassPathResource("testFile.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/json", map,
                String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().length()).isGreaterThan(1);
        JsonAssert.with(response.getBody()).assertThat("$.LESSTHAN100kB.1", equalTo(10.0));
        JsonAssert.with(response.getBody()).assertThat("$.LESSTHAN100kB.6", equalTo(20.0));
        JsonAssert.with(response.getBody()).assertThat("$.LESSTHAN100kB.41", equalTo(10.0));
        JsonAssert.with(response.getBody()).assertThat("$.LESSTHAN100kB.61", equalTo(10.0));
        JsonAssert.with(response.getBody()).assertThat("$.BETWEEN100kBAND1MB.769", equalTo(10.0));
        JsonAssert.with(response.getBody()).assertThat("$.BETWEEN100kBAND1MB.2055", equalTo(10.0));
        JsonAssert.with(response.getBody()).assertThat("$.BETWEEN100kBAND1MB.7931", equalTo(20.0));
        JsonAssert.with(response.getBody()).assertThat("$.BETWEEN100kBAND1MB.30229", equalTo(10.0));

    }

    @Test
    public void shouldThrowBadRequestGeneratingJsonWhenMissingColumn() throws Exception {
        ClassPathResource resource = new ClassPathResource("testFile_missing_column.csv", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/json", map,
                String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }

    @Test
    public void shouldThrowBadRequestGeneratingJsonWhenInvalidInputFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("image_invalid_format.png", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = template.postForEntity("/json", map,
                String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).containsIgnoringCase("invalid file format");
    }
}