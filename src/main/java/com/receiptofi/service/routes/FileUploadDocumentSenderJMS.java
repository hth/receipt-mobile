/**
 *
 */
package com.receiptofi.service.routes;

import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.UserProfileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * @author hitender
 * @since Mar 30, 2013 2:42:21 AM
 *
 */
public final class FileUploadDocumentSenderJMS {
	private static final Logger log = LoggerFactory.getLogger(FileUploadDocumentSenderJMS.class);

	@Autowired private JmsTemplate jmsSenderTemplate;

    @Value("${queue-name}")
    private String queueName;

	public void send(final DocumentEntity documentEntity, final UserProfileEntity userProfile) {
        jmsSenderTemplate.send(queueName,
				new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						MapMessage mapMessage = session.createMapMessage();
						mapMessage.setString("id", documentEntity.getId());
						mapMessage.setString("level", userProfile.getLevel().getDescription());
                        mapMessage.setInt("status", documentEntity.getDocumentStatus().ordinal());

						//This does not work since this values has to be set after sending the message. It will always default to 4.
						mapMessage.setJMSPriority(userProfile.getLevel().getMessagePriorityJMS());

						mapMessage.setJMSTimestamp(documentEntity.getUpdated().getTime());
						return mapMessage;
					}
				}
				);
		log.info("Message sent ReceiptOCR={}, level={}", documentEntity.getId(), userProfile.getLevel().getDescription());
    }
}
