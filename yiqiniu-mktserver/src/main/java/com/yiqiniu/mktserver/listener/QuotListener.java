/**
 * @Title: QuotListener.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.listener;

import com.yiqiniu.mktserver.listener.event.QuotEvent;



/**
 * <code>QuotListener</code> 行情监听器.
 *
 * @author Jimmy
 * @date 2015-7-7 下午4:44:54
 * @version v1.0
 */

public interface QuotListener {
	
	/**
	 * @param quotEvent
	 */
	public void updated(QuotEvent quotEvent);
}
