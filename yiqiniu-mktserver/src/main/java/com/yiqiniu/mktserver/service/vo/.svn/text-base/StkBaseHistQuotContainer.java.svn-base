/**
 * @Title: StkHistQuotVO.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.vo;

import java.util.Map;

/**
 * @description 历史K线存储结构，由不复权、前复权、后复权的资产Map组成，key为资产id
 * 
 * @author 余俊斌
 * @date 2015年8月1日 下午4:38:59
 * @version v1.0
 */
public class StkBaseHistQuotContainer<N, F, B> {
	private Map<Object, N> nonAdjQuotMap;
	private Map<Object, F> forwardAdjQuotMap;
	private Map<Object, B> backwardAdjQuotMap;

	public StkBaseHistQuotContainer(Map<Object, N> nonAdjQuotMap, Map<Object, F> forwardAdjQuotMap,
			Map<Object, B> backwardAdjQuotMap) {
		super();
		this.nonAdjQuotMap = nonAdjQuotMap;
		this.forwardAdjQuotMap = forwardAdjQuotMap;
		this.backwardAdjQuotMap = backwardAdjQuotMap;
	}

	public Map<Object, N> getNonAdjQuotMap() {
		return nonAdjQuotMap;
	}

	public Map<Object, F> getForwardAdjQuotMap() {
		return forwardAdjQuotMap;
	}

	public Map<Object, B> getBackwardAdjQuotMap() {
		return backwardAdjQuotMap;
	}
}
