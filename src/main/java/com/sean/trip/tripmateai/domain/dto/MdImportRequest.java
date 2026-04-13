package com.sean.trip.tripmateai.domain.dto;

import lombok.Data;

@Data
public class MdImportRequest {

    private String mdFilePath;

    private String city;

    private boolean aiChunk;
}
