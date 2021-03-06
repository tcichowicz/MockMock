package com.mockmock.mail;

import com.google.common.eventbus.EventBus;
import com.mockmock.Settings;
import org.joda.time.DateTime;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Properties;

public class MockMockHandler implements MessageHandler
{
    private EventBus eventBus;
    private Settings settings;
    private MockMail mockMail;

    /**
     * Constructor
     * @param context MessageContext
     * @param eventBus
     * @param settings
     */
    public MockMockHandler(EventBus eventBus, Settings settings)
    {
        this.eventBus = eventBus;
        this.settings = settings;
        this.mockMail = new MockMail();

        // give the mockmail a unique id (currently its just a timestamp in ms)
        this.mockMail.setId(DateTime.now().getMillis());
    }

    /**
     * Called first, after the MAIL FROM during a SMTP exchange.
     * @param from String
     * @throws org.subethamail.smtp.RejectException
     */
    @Override
    public void from(String from) throws RejectException
    {
        this.mockMail.setFrom(from);
        System.out.println("FROM:" + from);
    }

    /**
     * Called once for every RCPT TO during a SMTP exchange.
     * This will occur after a from() call.
     * @param recipient String
     * @throws RejectException
     */
    @Override
    public void recipient(String recipient) throws RejectException
    {
        this.mockMail.setTo(recipient);
        System.out.println("RECIPIENT:" + recipient);
    }

    /**
     * Called when the DATA part of the SMTP exchange begins.
     * @param data InputStream
     * @throws RejectException
     * @throws java.io.IOException
     */
    @Override
    public void data(InputStream data) throws RejectException, IOException
    {
        String rawMail = this.convertStreamToString(data);
        mockMail.setRawMail(rawMail);

        Session session = Session.getDefaultInstance(new Properties());
        InputStream is = new ByteArrayInputStream(rawMail.getBytes());

        try
        {
            MimeMessage message = new MimeMessage(session, is);
            mockMail.setSubject(message.getSubject());
            mockMail.setMimeMessage(message);

            Object messageContent = message.getContent();
            if(messageContent instanceof Multipart)
            {
                Multipart multipart = (Multipart) messageContent;
                for (int i = 0; i < multipart.getCount(); i++)
                {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    String contentType = bodyPart.getContentType();
                    if(contentType.matches("text/plain.*"))
                    {
                        mockMail.setBody(convertStreamToString(bodyPart.getInputStream()));
                    }
                    else if(contentType.matches("text/html.*"))
                    {
                        mockMail.setBodyHtml(convertStreamToString(bodyPart.getInputStream()));
                    }
                }
            }
            else if(messageContent instanceof InputStream)
            {
                InputStream mailContent = (InputStream) messageContent;
                mockMail.setBody(convertStreamToString(mailContent));
            }
            else if(messageContent instanceof String)
            {
                mockMail.setBody((String) messageContent);
            }
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }

        System.out.println("MAIL DATA");
        System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
        System.out.println(mockMail.getRawMail());
        System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
    }

    @Override
    public void done()
    {
        // check if this email's "from" address matches one in the filtered list
        if(settings.getFilterFromEmailAddresses().contains(mockMail.getFrom()))
        {
            System.out.println("Skipping email, because From address '" + mockMail.getFrom() + "' matches filter");
            return;
        }

        // check if this email's "to" address matches one in the filtered list
        if(settings.getFilterToEmailAddresses().contains(mockMail.getTo()))
        {
            System.out.println("Skipping email, because To address '" + mockMail.getTo() + "' matches filter");
            return;
        }

        // set the received date
        mockMail.setReceivedTime(DateTime.now().getMillis());

        System.out.println("Finished");
        eventBus.post(mockMail);
    }

    /**
     * Converts given input stream to String
     * @param is InputStream
     * @return String
     */
    protected String convertStreamToString(InputStream is)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}