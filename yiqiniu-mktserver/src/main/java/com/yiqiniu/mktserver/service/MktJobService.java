package com.yiqiniu.mktserver.service;


import java.util.Map;

import com.yiqiniu.api.mktinfo.vo.StkDailyVO;
import com.yiqiniu.mktserver.service.vo.StkHkPersistContainer;

/**
 * <code>MktJobService<code> 开盘、收盘业务处理类
 *
 * @author Colin 
 * @since  Yiqiniu v0.0.1 (2014-12-26)
 *
 */
public interface MktJobService {
	/**
	 * 
	 * 开盘工作
	 */
	void openWork();
	/**
	 * 
	 * 收盘工作
	 */
	void closeWork();
	/**
	 * 
	 */
	StkHkPersistContainer hkCloseWork(Map<String, StkDailyVO> hStkDaily, String trdDate) throws Exception;
	/**
	 * 预加载数据
	 */
	void preLoadData();
	
	/**
	 * 保存分时，清理过时的分时数据
	 */
	void storeTimesharing();
	
	/**
	 * 计算技术指标
	 * @param HkContainer
	 * @param szContainer
	 * @param date
	 */
	void calcTechnicalIndicator(StkHkPersistContainer hkContainer, String date);
	
	/**
	 * 保存K线数据
	 * @param HkContainer
	 * @param szContainer
	 */
	void storeQuotData(StkHkPersistContainer hkContainer);
}
