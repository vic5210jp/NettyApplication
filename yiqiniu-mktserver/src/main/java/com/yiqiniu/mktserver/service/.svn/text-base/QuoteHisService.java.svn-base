/**
 * @Title: QuoteService.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description 收盘K线相关服务
 *
 * @author 余俊斌
 * @date 2015年7月16日 下午7:43:34
 * @version v1.0
 */

public interface QuoteHisService {

	/**
	 * 保存列表
	 * @param targetList
	 * @param batchSize
	 */
	<T extends Serializable> void saveList(List<T> targetList, int batchSize);
	
	/**
	 * 使用列表的数据进行更新
	 * @param targetList
	 */
	<T extends Serializable> void updateList(List<T> targetList);
	
	/**
	 * 使用列表的数据进行更新，每隔一段提交一次
	 * @param targetList
	 */
	<T extends Serializable> void updateListWithinTrans(List<T> targetList);	
	
	/**
	 * 保存列表
	 * @param targetList
	 * @param batchSize
	 */
	<T extends Serializable> void storeList(List<T> targetList, int batchSize);
	
	/**
	 * 清理多余的分时数据
	 * @param assetId
	 * @param date
	 */
	void clearTimeSharingData(String assetId, Date date);
}
