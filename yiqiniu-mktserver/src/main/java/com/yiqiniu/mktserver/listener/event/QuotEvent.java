/**
 * @Title: QuotEvent.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.listener.event;

import java.util.Date;

import com.yiqiniu.api.mktinfo.vo.enums.EAction;

/**
 * <code>QuotEvent</code>
 * 
 * @author Jimmy
 * @date 2015-7-7 下午4:45:23
 * @version v1.0
 */

public class QuotEvent {
	// 事件类型
	private EAction action;
	// 市场代码
	private String mktCode;
	// 行情时间
	private Date quotTime;

	public EAction getAction() {
		return action;
	}

	public void setAction(EAction action) {
		this.action = action;
	}

	public String getMktCode() {
		return mktCode;
	}

	public void setMktCode(String mktCode) {
		this.mktCode = mktCode;
	}

	public Date getQuotTime() {
		return quotTime;
	}

	public void setQuotTime(Date quotTime) {
		this.quotTime = quotTime;
	}

}
