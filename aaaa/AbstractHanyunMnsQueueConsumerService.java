package com.laipao8.hanyun.mnsmiddleware.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONPOJOBuilder;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.model.Message;
import com.laipao8.hanyun.mnsmiddleware.Encrypt;

public abstract class AbstractHanyunMnsQueueConsumerService {

	private CloseableHttpClient client = HttpClients.createDefault();
	private static Logger logger = LoggerFactory.getLogger(AbstractHanyunMnsQueueConsumerService.class);
	Encrypt ec = new Encrypt();
	@Value("${laipao8.uc.api.url}")
	protected String apiUrl;

	@Value("${aliyun.mns.check.interval}")
	private int interval;
	@Autowired
	private AliyunMnsClientService aliyunMnsClientService;

	private ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();

	private ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(10);

	private long lastUpdateTime;

	protected String queueName = "";

	protected boolean isBase64 = false;

	@PostConstruct
	private void init() {
		logger.info("init:" + queueName);
		lastUpdateTime = System.currentTimeMillis();
		newSingleThreadExecutor.submit(new Runnable() {
			@Override
			public void run() {
				receive();
			}
		});

		newScheduledThreadPool.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() - interval * 1000 > lastUpdateTime) {
					newSingleThreadExecutor.submit(new Runnable() {
						@Override
						public void run() {
							receive();
						}
					});
					logger.warn("RESTART  " + lastUpdateTime);
				} else {
					logger.info("RUNNING  " + lastUpdateTime);
				}
			}
		}, interval, interval, TimeUnit.SECONDS);
	}

	private void receive() {
		logger.info("receive:" + queueName);
		try {
			CloudQueue queue = aliyunMnsClientService.getClient().getQueueRef(queueName);
			Message popMsg = queue.popMessage();
			if (popMsg != null) {
				if (isBase64) {
					String msg = popMsg.getMessageBodyAsBase64();
					JSONObject msgObj = (JSONObject) JSON.parse(msg);
					String message = msgObj.getString("Message");
					byte[] b = message.getBytes();
					Base64 base64 = new Base64();
					b = base64.decode(b);
					String s = new String(b);
					execute(s);
				} else {
					String msg = popMsg.getMessageBodyAsString();
					execute(msg);
				}
				// 删除已经取出消费的消息
				queue.deleteMessage(popMsg.getReceiptHandle());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lastUpdateTime = System.currentTimeMillis();
			newSingleThreadExecutor.submit(new Runnable() {

				@Override
				public void run() {
					if ("serp-hanyun-msg".equals(queueName)) {
						receive();
					}
				}
			});
		}
	}

	protected String post(String url, JSONObject o) {
		StringEntity se = new StringEntity(o.toString(), "UTF-8");
		se.setContentEncoding("UTF-8");
		se.setContentType("application/json");
		HttpUriRequest post = RequestBuilder.post(url).setCharset(Charset.forName("UTF-8")).setEntity(se).build();
		CloseableHttpResponse res = null;
		try {
			res = client.execute(post);
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = res.getEntity();
				String s = EntityUtils.toString(entity, "UTF-8");
				return s;
			}
		} catch (IOException e) {
			try {
				client.close();
			} catch (Exception e1) {
			} finally {
				client = HttpClients.createDefault();
			}

		} finally {
			try {
				res.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected String signature(JSONObject jData) {
		jData.put("appid", "lp8f50f1f30eceb662a");
		String secret = "b0c363714888e0682e885017d91f1fda";
		jData.put("timestamp", System.currentTimeMillis());
		List<String> keys = (List<String>) jData.keySet().stream().collect(Collectors.toList());
		Collections.sort(keys);
		// 验证
		StringBuffer tempBuffer = new StringBuffer(
				keys.stream().map(key -> key + "=" + jData.get(key)).reduce((a, b) -> a + "&" + b).get());
		tempBuffer.append(secret);

		return ec.encrypt(tempBuffer.toString());
	}

	protected JSONObject post2laipao8(String url, JSONObject body) {
		body.put("signature", signature(body));
		String result = post(url, body);
		if (result != null) {
			return JSONObject.parseObject(result);
		} else {
			return new JSONObject();
		}
	}

	protected JSONObject postHanyun(String url, JSONObject body) {

		return new JSONObject();
	}

	public abstract void execute(String msg);
}
