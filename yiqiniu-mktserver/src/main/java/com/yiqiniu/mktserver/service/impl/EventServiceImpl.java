/**
 * @Title: EventService.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiqiniu.api.mktinfo.vo.ChannelCommunicateVO;
import com.yiqiniu.common.utils.JSONUtil;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;
import com.yiqiniu.mktserver.listener.QuotListener;
import com.yiqiniu.mktserver.listener.event.QuotEvent;
import com.yiqiniu.mktserver.service.MktServerService;
import com.yiqiniu.mktserver.service.TimeShareGenerateService;

/**
 * <code>EventService</code>
 *
 * @author Jimmy
 * @date 2015-7-7 下午5:09:06
 * @version v1.0
 */

@Service
public class EventServiceImpl extends MktInfoBeanRepository implements QuotListener {
	final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private String quotUpdatedChannel;
	
	@Resource
	private MktServerService mktServerService;
	@Resource
	private TimeShareGenerateService timeShareGenerateService;
	
	@PostConstruct
	protected void preinit() {
		// 增加行情数据监听器, 如果行情刷新了会调用指定的回调方法
		mktServerService.addQuotListener(this);
		timeShareGenerateService.addQuotListener(this);
		quotUpdatedChannel = defaultConfig.getVal("quot.updated.channel");
	}
	
	 /**
	  * 回调方法, 行情数据刷新时会触发
	  * @param quotEvent
	  */
	@Override
	public void updated(QuotEvent quotEvent) {
		if (quotEvent == null) {
			return ;
		}
		ChannelCommunicateVO communicateVO = new ChannelCommunicateVO();
		communicateVO.setAction(quotEvent.getAction());
		communicateVO.setTime(quotEvent.getQuotTime());
		redisTpl.convertAndSend(quotUpdatedChannel, JSONUtil.toJson(communicateVO));
	}
	

}
