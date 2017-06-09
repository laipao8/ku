package com.laipao8.hanyun.mnsmiddleware.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.MNSClient;

@Service
public class AliyunMnsClientService {
	private CloudAccount account ;
	private MNSClient client;

	@Value("${aliyun.mns.endpoint}")
	private String endpoint;
	@Value("${aliyun.mns.accessId}")
	private String accessId;
	@Value("${aliyun.mns.accessKey}")
	private String accessKey;

	@PostConstruct
	private void init() {
		account = new CloudAccount(accessId, accessKey, endpoint);
		MNSClient = account.getMNSClient(); //
		//宋江苏不要脸！
	}

	public MNSClient getClient() {
		return MNSClient;
	}
}
