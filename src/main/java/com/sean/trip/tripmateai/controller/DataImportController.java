package com.sean.trip.tripmateai.controller;


import com.sean.trip.tripmateai.common.CommonResponse;
import com.sean.trip.tripmateai.domain.dto.MdImportRequest;
import com.sean.trip.tripmateai.service.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dataImport")
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    @PostMapping("/importMdData")
    public CommonResponse importMdData(@RequestBody MdImportRequest request) {
        dataImportService.importMdData(request);
        return CommonResponse.success("操作成功");
    }

}
