package com.laipao8.hanyun.mnsmiddleware.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.model.Base64TopicMessage;
import com.aliyun.mns.model.TopicMessage;


@Service
public class HanyunMnsTopicPushService {
	
	
	@Autowired
	private AliyunMnsClientService aliyunMnsClientService;
	private static String DEFAULT_MNS_TAG = "hy-order";
	private static String DEFAULT_TOPIC = "laipao8-topic-record";

	@PostConstruct
	private void init() {

	}

	public TopicMessage push(String msg, String tag) {
		CloudTopic topic = aliyunMnsClientService.getClient().getTopicRef(DEFAULT_TOPIC);
		TopicMessage topicMsg = new Base64TopicMessage();
		try {
			// 可以使用TopicMessage结构，选择不进行Base64加密
			topicMsg.setMessageBody(msg);
			topicMsg.setMessageTag(tag);
			topicMsg = topic.publishMessage(topicMsg);
		} catch (Exception e) {
		}
		return topicMsg;
	}

	public TopicMessage push(String msg) {
		return push(msg, DEFAULT_MNS_TAG);
	}
}
