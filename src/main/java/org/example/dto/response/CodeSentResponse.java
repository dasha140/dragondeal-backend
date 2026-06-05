package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CodeSentResponse(
        String message,
        String devCode
) {
    public static CodeSentResponse sent() {
        return new CodeSentResponse("Verification code sent", null);
    }

    public static CodeSentResponse sentWithDevCode(String devCode) {
        return new CodeSentResponse("Verification code sent", devCode);
    }
}
