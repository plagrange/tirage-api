package com.lagrange.tirage.tirageapi.exceptions;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.mail.MessagingException;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class SendMessageException extends MessagingException {

    public String errorCode;
    public String errorMessage;
}
