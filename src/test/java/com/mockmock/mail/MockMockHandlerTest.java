package com.mockmock.mail;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RunWith(MockitoJUnitRunner.class)
public class MockMockHandlerTest {

    private static final String EMAIL_HEADERS = "Received: from localhost ([127.0.0.1])\n" +
            "        by smtpMock\n" +
            "        with SMTP (MockMock SMTP Server version 1.0) id I2YKW4NU\n" +
            "        for me@mail.com;\n" +
            "        Wed, 26 Nov 2014 10:57:39 +0000 (GMT)\n" +
            "Date: Wed, 26 Nov 2014 11:57:39 +0100 (CET)\n" +
            "From: no-reply@mail.com\n" +
            "To: me@mail.com\n" +
            "Message-ID: <1777238524.0.1416999459723.JavaMail.somebody@mail.com>\n" +
            "Subject: Email subject\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/plain; charset=UTF-8\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n";

    @Mock
    private MessageContext context;

    @InjectMocks
    private MockMockHandler handler;

    @Test
    public void shouldSetBodyForPlainTextData() throws IOException {
        // given
        String expectedContent = "some email content\n";
        String rawMessageString = EMAIL_HEADERS + expectedContent;
        InputStream stream = new ByteArrayInputStream(rawMessageString.getBytes(StandardCharsets.UTF_8));

        // when
        handler.data(stream);

        // then
        MockMail mockMail = (MockMail) Whitebox.getInternalState(handler, "mockMail");
        Assert.assertEquals(expectedContent, mockMail.getBody());
    }
}