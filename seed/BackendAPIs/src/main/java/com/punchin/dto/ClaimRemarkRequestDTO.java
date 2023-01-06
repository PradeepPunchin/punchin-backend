package com.punchin.dto;

import com.punchin.enums.RemarkForEnum;
import lombok.Data;

@Data
public class ClaimRemarkRequestDTO {
    private String remark;
    private RemarkForEnum remarkFor;
}
