package com.punchin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Scheduler {

    @Scheduled(cron="*/15 * * * *")
    public void cronJobForChangeInactiveClaimStatus() {
        try {

        }catch(Exception e) {
            log.error("Exception In Cron Job For Checking Status - ",e);
        }
    }
}
