package com.ibb.yurtlar.exception;

import com.ibb.yurtlar.enums.AdmissionStatus;

public class InvalidAdmissionStatusTransitionException
        extends RuntimeException {

    public InvalidAdmissionStatusTransitionException(
            Long admissionId,
            AdmissionStatus currentStatus,
            AdmissionStatus requestedStatus
    ) {
        super(
                "Kabul durumu geçişi geçersizdir. Kabul ID: "
                        + admissionId
                        + ", mevcut durum: "
                        + currentStatus
                        + ", istenen durum: "
                        + requestedStatus
        );
    }
}
