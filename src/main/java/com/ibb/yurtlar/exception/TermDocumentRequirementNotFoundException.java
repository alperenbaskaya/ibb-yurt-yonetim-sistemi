package com.ibb.yurtlar.exception;

public class TermDocumentRequirementNotFoundException
        extends RuntimeException {

    public TermDocumentRequirementNotFoundException(Long id) {
        super(
                id
                        + " ID değerine sahip dönem belge gereksinimi bulunamadı."
        );
    }
}