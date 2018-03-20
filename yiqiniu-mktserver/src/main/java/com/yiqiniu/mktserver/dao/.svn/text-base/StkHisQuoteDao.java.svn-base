/**
 * @Title: StkHisQuoteDao.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.yiqiniu.api.mktserver.ClearTsKey;
import com.yiqiniu.mktinfo.persist.po.StkDayHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkWeekHk;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkYearHk;
import com.yiqiniu.mktinfo.persist.po.StkYearHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkYearHkFwd;

/**
 * @description 历史行情dao
 *
 * @author 余俊斌
 * @date 2015年7月16日 下午2:17:08
 * @version v1.0
 */

public interface StkHisQuoteDao {


	/**
	 * 查找港交所所有个股最近一条周K数据（不复权）
	 * @return
	 */
	List<StkWeekHk> findLastHkWeekQuotes();
	
	/**
	 * 查找港交所所有个股最近一条周K数据（前复权）
	 * @return
	 */
	List<StkWeekHkFwd> findLastHkWeekFwdQuotes();
	
	/**
	 * 查找港交所所有个股最近一条周K数据（后复权）
	 * @return
	 */
	List<StkWeekHkBkw> findLastHkWeekBkwQuotes();

	
	/**
	 * 查找港交所所有个股最近一条月K数据（不复权）
	 * @return
	 */
	List<StkMonthHk> findLastHkMonthQuotes();
	
	/**
	 * 查找港交所所有个股最近一条月K数据（前复权）
	 * @return
	 */
	List<StkMonthHkFwd> findLastHkMonthFwdQuotes();
	
	/**
	 * 查找港交所所有个股最近一条月K数据（后复权）
	 * @return
	 */
	List<StkMonthHkBkw> findLastHkMonthBkwQuotes();

	
	/**
	 * 查找港交所所有个股最近一条年K数据（不复权）
	 * @return
	 */
	List<StkYearHk> findLastHkYearQuotes();
	
	/**
	 * 查找港交所所有个股最近一条年K数据（前复权）
	 * @return
	 */
	List<StkYearHkFwd> findLastHkYearFwdQuotes();
	
	/**
	 * 查找港交所所有个股最近一条年K数据（后复权）
	 * @return
	 */
	List<StkYearHkBkw> findLastHkYearBkwQuotes();
	
	/**
	 * 查找港交所给定时间之前最近的股票日K，按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkDayHk> findStkDayHkBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票周K（不复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkWeekHk> findStkWeekHkBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票周K（前复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkWeekHkFwd> findStkWeekHkFwdBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票周K（后复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkWeekHkBkw> findStkWeekHkBkwBeforeTime(String assetId, String date, Integer limit);

	/**
	 * 查找港交所给定时间之前最近的股票月K（不复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkMonthHk> findStkMonthHkBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票月K（前复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkMonthHkFwd> findStkMonthHkFwdBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票月K（后复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkMonthHkBkw> findStkMonthHkBkwBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票年K（不复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkYearHk> findStkYearHkBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票年K（前复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkYearHkFwd> findStkYearHkFwdBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找港交所给定时间之前最近的股票年K（后复权），按时间倒序排，返回不超过给定的条数
	 * @param assetId
	 * @param date
	 * @param limit
	 * @return
	 */
	List<StkYearHkBkw> findStkYearHkBkwBeforeTime(String assetId, String date, Integer limit);
	
	/**
	 * 查找需要删除的分时数据
	 * @param expireDay 超过此天数则需要删除
	 * @return
	 */
	List<ClearTsKey> findTsKeysToClear(int expireDay);
	
	/**
	 * 清理多余的分时数据
	 * @param assetId
	 * @param date
	 */
	void clearTimeSharingData(String assetId, Date date);
	
	/**
	 * 刷新前复权K线
	 * @param assetId
	 * @param af
	 */
	void refreshFowardAdjust(String assetId, double af);
	
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
	 * 使用列表的数据进行更新，每隔一部分提交一次
	 * @param targetList
	 */
	<T extends Serializable> void updateListWithinTrans(List<T> targetList);
	
	/**
	 * 保存列表，每次调用都在单独的事务中
	 * @param targetList
	 * @param batchSize
	 */
	<T extends Serializable> void saveTsList(List<T> targetList, int batchSize);
}
