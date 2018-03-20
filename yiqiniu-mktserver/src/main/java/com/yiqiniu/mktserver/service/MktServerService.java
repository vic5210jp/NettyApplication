package com.yiqiniu.mktserver.service;

import com.yiqiniu.api.mktserver.HkQuotMsg;
import com.yiqiniu.mktserver.listener.QuotListener;



/**
 * @description 港股行情转码机服务
 *
 * @author 余俊斌
 * @date 2015年11月16日 下午3:40:44
 * @version v1.0
 */
public interface MktServerService {
	
	/**
	 * 处理收到的行情消息报文，消息串行处理
	 * @param receivedMsg
	 */
	void processReceivedQuotMsg(HkQuotMsg receivedMsg);
	
	/**
	 * 增加监听器
	 * @param quotListener
	 */
	public void addQuotListener(QuotListener quotListener);
	
	/**
	 * 删除监听器
	 * @param quotListener
	 */
	public void removeQuotListener(QuotListener quotListener);
}
