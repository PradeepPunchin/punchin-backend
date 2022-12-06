
package com.punchin.dto;

import java.time.LocalDate;

public interface ClaimDataResponse {

    Long getId();

    LocalDate getRegistrationDate();

    String getBorrowerName();

    String getNomineeName();

    String getNomineeContactNumber();

    String getNomineeAddress();

}
