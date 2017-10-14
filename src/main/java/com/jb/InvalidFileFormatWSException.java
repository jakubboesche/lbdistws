package com.jb;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid file format")
public class InvalidFileFormatWSException extends RuntimeException {
}
