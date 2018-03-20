/**
 * @Title: GenerateTimeShareService.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service;

import java.util.Set;

import com.yiqiniu.mktserver.listener.QuotListener;


/**
 * @description 分时生成服务
 *
 * @author 余俊斌
 * @date 2015年11月24日 下午7:56:20
 * @version v1.0
 */

public interface TimeShareGenerateService {

	/**
	 * 全量生成港股分时
	 * @param triggerTime 数据源触发的时间
	 * @param needPush 是否需要推送，true则推送分时，false不推送（异常恢复时触发）
	 */
	void generateTimeShare(long triggerTime, boolean needPush);
	
	/**
	 * 增加监听器
	 * @param quotListener
	 */
	void addQuotListener(QuotListener quotListener);
	
	/**
	 * 删除监听器
	 * @param quotListener
	 */
	void removeQuotListener(QuotListener quotListener);
	
	/**
	 * 获取可以生成分时的时间集合
	 */
	Set<String> getGenTsTime();
}
