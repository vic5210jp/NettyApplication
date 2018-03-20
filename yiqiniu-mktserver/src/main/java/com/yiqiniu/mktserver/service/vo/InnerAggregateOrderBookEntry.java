/**
 * @Title: AggregateOrderBookEntry.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.vo;

import com.yiqiniu.api.mktinfo.util.AggregateOrderBookEntry;
import com.yiqiniu.mktserver.util.Constants.EHkAggregateOrderSide;
import com.yiqiniu.mktserver.util.Constants.EHkAggregateOrderUpdateAction;

/**
 * @description inner entry for aggregate order book management<br>
 *              fields are refered from HKEX document
 *
 * @author 余俊斌
 * @date 2015年11月19日 下午9:14:41
 * @version v1.0
 */

public class InnerAggregateOrderBookEntry extends AggregateOrderBookEntry {

	private EHkAggregateOrderSide side;
	private EHkAggregateOrderUpdateAction updateAction;

	public EHkAggregateOrderSide getSide() {
		return side;
	}

	public void setSide(EHkAggregateOrderSide side) {
		this.side = side;
	}

	public EHkAggregateOrderUpdateAction getUpdateAction() {
		return updateAction;
	}

	public void setUpdateAction(EHkAggregateOrderUpdateAction updateAction) {
		this.updateAction = updateAction;
	}

}
