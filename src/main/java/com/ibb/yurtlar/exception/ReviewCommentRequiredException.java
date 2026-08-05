package com.ibb.yurtlar.exception;

public class ReviewCommentRequiredException
        extends RuntimeException {

    public ReviewCommentRequiredException() {
        super(
                "Reddedilen veya yeniden yükleme istenen belgelerde açıklama zorunludur."
        );
    }
}