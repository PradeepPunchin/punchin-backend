package com.punchin.dto;

import com.punchin.entity.ClaimsData;
import com.punchin.enums.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
public class AgentUploadDocumentDTO {

    private ClaimsData claimsData;

    private CauseOfDeathEnum causeOfDeath;

    private boolean isMinor;

    private MultipartFile SignedForm;

    private MultipartFile deathCertificate;

    private KycOrAddressDocType borrowerIdDocType;
    private MultipartFile borrowerIdDoc;
    private KycOrAddressDocType borrowerAddressDocType;
    private MultipartFile borrowerAddressDoc;
    private KycOrAddressDocType nomineeIdDocType;
    private MultipartFile nomineeIdDoc;
    private KycOrAddressDocType nomineeAddressDocType;
    private MultipartFile nomineeAddressDoc;
    private BankAccountDocType bankAccountDocType;
    private MultipartFile bankAccountDoc;
    private MultipartFile FirOrPostmortemReport;
    private AdditionalDocType additionalDocType;
    private MultipartFile additionalDoc;
    private Map<String, MultipartFile> isMinorDoc;
    private String agentRemark;
}
