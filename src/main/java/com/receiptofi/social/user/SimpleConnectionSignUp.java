package com.receiptofi.social.user;

import com.receiptofi.social.annotation.Social;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;

@Social
public final class SimpleConnectionSignUp implements ConnectionSignUp {

	private final AtomicLong userIdSequence = new AtomicLong();
	
	public String execute(Connection<?> connection) {
		return Long.toString(userIdSequence.incrementAndGet());
	}
}
