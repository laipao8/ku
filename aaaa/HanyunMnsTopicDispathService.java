package com.laipao8.hanyun.mnsmiddleware.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.laipao8.hanyun.mnsmiddleware.constants.GlobalConstants;

@Service
public class HanyunMnsTopicDispathService {
	private static Logger logger = LoggerFactory.getLogger(HanyunMnsTopicDispathService.class);

	@Autowired
	private HanyunMnsTopicPushService topicPushService;

	public void dispath(String msg) {
		JSONObject msgObj = null;
		try {
			msgObj = JSONObject.parseObject(msg);
		} catch (Exception e) {

		} finally {
			logger.info(msg);
		}
		if (msgObj != null) {
			String messageType = "";
			if (msgObj.containsKey(GlobalConstants.KEY_MESSAGE_TYPE)) {
				messageType = msgObj.getString(GlobalConstants.KEY_MESSAGE_TYPE);
			}
			logger.info("type:" + messageType);
			if (GlobalConstants.MESSAGE_TYPE_GOODS.equals(messageType)) {
				topicPushService.push(msg, GlobalConstants.MNS_TAG_ORDER);
			} else if (GlobalConstants.MESSAGE_TYPE_MAKE_CARD.equals(messageType)) {
				topicPushService.push(msg, GlobalConstants.MNS_TAG_MAKE_CARD);
			} else if (GlobalConstants.MESSAGE_TYPE_MEMBER_INFO.equals(messageType)) {
				topicPushService.push(msg, GlobalConstants.MNS_TAG_MEMBER_INFO);
			} else if (GlobalConstants.MESSAGE_TYPE_CONSUME_CARD.equals(messageType)) {
				topicPushService.push(msg, GlobalConstants.MNS_TAG_CONSUME_CARD);
			}

		}
	}

}
