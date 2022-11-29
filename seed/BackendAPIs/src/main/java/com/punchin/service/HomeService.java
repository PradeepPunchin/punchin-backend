package com.punchin.service;

import com.punchin.dto.ClaimDataDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimData;
import com.punchin.repository.ClaimDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HomeService {

    @Autowired
    ClaimDataRepository claimDataRepository;
   @Autowired
   private ModelMapperService modelMapperService;

   @Autowired
   private CommonUtilService commonUtilService;


    public Object saveUploadExcelData(MultipartFile file) throws IOException {
        if (!checkExcelFormat(file)) {
            return null;
        }
            List<ClaimData> products = commonUtilService.convertExcelToListOfProduct(file.getInputStream());
            claimDataRepository.saveAll(products);
            return "File uploaded and Data Saved successfully";
    }

    public boolean checkExcelFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return false;
    }

    public PageDTO getAllProducts(Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<ClaimData> allClaimData = claimDataRepository.findAllClaimData(pageable);
        if (allClaimData.isEmpty()) {
            return null;
        }
        List<ClaimDataDTO> claimDataDTOList = new ArrayList<>();
        for (ClaimData claimData:allClaimData) {
            ClaimDataDTO map = modelMapperService.map(claimData, ClaimDataDTO.class);
            claimDataDTOList.add(map);
        }
        return commonUtilService.getDetailsPage(claimDataDTOList,allClaimData.getContent().size(),allClaimData.getTotalPages(),allClaimData.getTotalElements());
    }
}
