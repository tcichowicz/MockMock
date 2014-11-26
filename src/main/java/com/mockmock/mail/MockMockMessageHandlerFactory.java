package com.mockmock.mail;

import com.google.common.eventbus.EventBus;
import com.mockmock.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

@Service
public class MockMockMessageHandlerFactory implements MessageHandlerFactory
{
    private EventBus eventBus;
	private Settings settings;

    @Autowired
    public MockMockMessageHandlerFactory(EventBus eventBus)
    {
        this.eventBus = eventBus;
    }

	@Autowired
	public void setSettings(Settings settings)
	{
		this.settings = settings;
	}

	@Override
    public MessageHandler create(MessageContext messageContext)
    {
        return new MockMockHandler(eventBus, settings);
    }

}
