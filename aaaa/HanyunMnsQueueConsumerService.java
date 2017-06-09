package com.laipao8.hanyun.mnsmiddleware.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HanyunMnsQueueConsumerService extends AbstractHanyunMnsQueueConsumerService {

	@Autowired
	private HanyunMnsTopicDispathService dispathService;

	public HanyunMnsQueueConsumerService() {
		this.queueName = "laipao8-hanyun-record";
	}

	@Override
	public void execute(String msg) {
		dispathService.dispath(msg);
	}

}
