/**
 * @Title: StkHisQuoteDaoImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.dao.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.yiqiniu.api.mktserver.ClearTsKey;
import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.common.utils.DateUtils.TimeFormatter;
import com.yiqiniu.common.utils.RowMapperUtils;
import com.yiqiniu.db4j.transaction.Transaction;
import com.yiqiniu.mktinfo.persist.po.StkDayHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkTimesharingHk;
import com.yiqiniu.mktinfo.persist.po.StkWeekHk;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkYearHk;
import com.yiqiniu.mktinfo.persist.po.StkYearHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkYearHkFwd;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;
import com.yiqiniu.mktserver.dao.StkHisQuoteDao;

/**
 * @description 历史行情dao实现
 *
 * @author 余俊斌
 * @date 2015年7月16日 下午2:22:40
 * @version v1.0
 */
@Repository
public class StkHisQuoteDaoImpl extends MktInfoBeanRepository implements StkHisQuoteDao {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public List<StkWeekHk> findLastHkWeekQuotes() {
		return findLastHistQuot(StkWeekHk.class);
	}

	@Override
	public List<StkMonthHk> findLastHkMonthQuotes() {
		return findLastHistQuot(StkMonthHk.class);
	}

	@Override
	public List<StkYearHk> findLastHkYearQuotes() {
		return findLastHistQuot(StkYearHk.class);
	}
	
	@Override
	public List<StkWeekHkFwd> findLastHkWeekFwdQuotes() {
		return findLastHistQuot(StkWeekHkFwd.class);
	}

	@Override
	public List<StkWeekHkBkw> findLastHkWeekBkwQuotes() {
		return findLastHistQuot(StkWeekHkBkw.class);
	}

	@Override
	public List<StkMonthHkFwd> findLastHkMonthFwdQuotes() {
		return findLastHistQuot(StkMonthHkFwd.class);
	}

	@Override
	public List<StkMonthHkBkw> findLastHkMonthBkwQuotes() {
		return findLastHistQuot(StkMonthHkBkw.class);
	}

	@Override
	public List<StkYearHkFwd> findLastHkYearFwdQuotes() {
		return findLastHistQuot(StkYearHkFwd.class);
	}

	@Override
	public List<StkYearHkBkw> findLastHkYearBkwQuotes() {
		return findLastHistQuot(StkYearHkBkw.class);
	}

	@Override
	public List<StkDayHk> findStkDayHkBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkDayHk.class, assetId, date, limit);
	}

	@Override
	public List<StkWeekHk> findStkWeekHkBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkWeekHk.class, assetId, date, limit);
	}
	
	
	@Override
	public List<StkWeekHkFwd> findStkWeekHkFwdBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkWeekHkFwd.class, assetId, date, limit);
	}
	
	@Override
	public List<StkWeekHkBkw> findStkWeekHkBkwBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkWeekHkBkw.class, assetId, date, limit);
	}
	
	@Override
	public List<StkMonthHk> findStkMonthHkBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkMonthHk.class, assetId, date, limit);
	}
	
	@Override
	public List<StkMonthHkFwd> findStkMonthHkFwdBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkMonthHkFwd.class, assetId, date, limit);
	}
	
	@Override
	public List<StkMonthHkBkw> findStkMonthHkBkwBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkMonthHkBkw.class, assetId, date, limit);
	}
	
	@Override
	public List<StkYearHk> findStkYearHkBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkYearHk.class, assetId, date, limit);
	}
	
	@Override
	public List<StkYearHkFwd> findStkYearHkFwdBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkYearHkFwd.class, assetId, date, limit);
	}
	
	@Override
	public List<StkYearHkBkw> findStkYearHkBkwBeforeTime(String assetId, String date, Integer limit) {
		return findHistQuotBeforeTime(StkYearHkBkw.class, assetId, date, limit);
	}
	
	@Override
	public List<ClearTsKey> findTsKeysToClear(int expireDay) {
		String sql = "SELECT asset_id,MIN(date) date FROM stk_timesharing_hk GROUP BY asset_id HAVING COUNT(DISTINCT date) > ?";
		return db.findObjectsForList(ClearTsKey.class, sql, expireDay);
	}

	@Override
	public void clearTimeSharingData(String assetId, Date date) {
		Transaction transaction = new Transaction(db);
		transaction.beginTrans();
		try {
			java.sql.Date sqlDate = java.sql.Date.valueOf(DateUtils.dateToString(date, TimeFormatter.YYYY_MM_DD));
			int iDelCount = db.delete(StkTimesharingHk.class, 
					db.where(tStkTimesharingHk.assetId.eq(assetId).and(tStkTimesharingHk.date.eq(sqlDate))));
			transaction.commit();
			LOG.debug("{} stk_timesharing deleted", iDelCount);
		} catch (Exception e) {
			transaction.rollback();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void refreshFowardAdjust(String assetId, double af) {
		// 总体的思路是，从后复权表中取数据替换前复权数据，价格=后复权价格/最新复权因子
		// TODO:调整表结构时，需要修改此方法
		String strMkt = assetId.substring(assetId.lastIndexOf('.') + 1).toLowerCase();

		StringBuilder deleteBuilder = new StringBuilder();
		deleteBuilder.append("DELETE FROM stk_%s_%s_fwd WHERE asset_id = ?");
		String deleteTemplate = deleteBuilder.toString();
		
		String weekDeleteSql = String.format(deleteTemplate, "week", strMkt);
		String monthDeleteSql = String.format(deleteTemplate, "month", strMkt);
		String yearDeleteSql = String.format(deleteTemplate, "year", strMkt);
		
		LOG.debug("week delete sql:[{}]", weekDeleteSql);
		LOG.debug("month delete sql:[{}]", monthDeleteSql);
		LOG.debug("year delete sql:[{}]", yearDeleteSql);

		StringBuilder insertBuilder = new StringBuilder();
		insertBuilder.append("INSERT INTO stk_%1$s_%2$s_fwd(asset_id, date, open, high, low, close, prev_close, volume, turnover, "
				+ "turn_rate, k_val, d_val, j_val, ema1, ema2, dea, "
				+ "up_seq1, down_seq1, up_seq2, down_seq2, up_seq3, down_seq3 )");
		insertBuilder.append(" SELECT asset_id, date, open / ?, high / ?, low / ?, close / ?, prev_close / ?, volume, turnover, "
				+ "turn_rate, k_val, d_val, j_val, ema1, ema2, dea, "
				+ "up_seq1, down_seq1, up_seq2, down_seq2, up_seq3, down_seq3");
		insertBuilder.append(" FROM stk_%1$s_%2$s_bkw ");
		insertBuilder.append(" WHERE asset_id = ? ");
		String insertTemplate = insertBuilder.toString();
		
		String weekInsertSql = String.format(insertTemplate, "week", strMkt);		
		String monthInsertSql = String.format(insertTemplate, "month", strMkt);
		String yearInsertSql = String.format(insertTemplate, "year", strMkt);
		
		LOG.debug("week insert sql:[{}]", weekInsertSql);
		LOG.debug("month insert sql:[{}]", monthInsertSql);
		LOG.debug("year insert sql:[{}]", yearInsertSql);
		
		Transaction transaction = new Transaction(db);
		transaction.beginTrans();
		try {
			// 参数个数为复权因子参数个数加1（最后一个参数是assetId），有需要时要注意修改
			Object[] paramsForInsert = new Object[5 + 1];
			for (int i = 0; i < paramsForInsert.length - 1; i++) {
				paramsForInsert[i] = af;
			}
			paramsForInsert[paramsForInsert.length - 1] = assetId;
			
			db.update(weekDeleteSql, assetId);
			db.update(weekInsertSql, paramsForInsert);
			db.update(monthDeleteSql, assetId);
			db.update(monthInsertSql, paramsForInsert);
			db.update(yearDeleteSql, assetId);
			db.update(yearInsertSql, paramsForInsert);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T extends Serializable> void saveList(List<T> targetList, int batchSize) {
		db.saveBatch(targetList, batchSize);
	}

	@Override
	public <T extends Serializable> void updateList(List<T> targetList) {
		for (T t : targetList) {
			db.update(t);
		}
	}

	@Override
	public <T extends Serializable> void updateListWithinTrans(List<T> targetList) {
		int recCnt = 0;
		try {
			Transaction transaction = new Transaction(db);
			transaction.beginTrans();
			for (T t : targetList) {
				db.update(t);
				recCnt++;
				if (recCnt % 1000 == 0) {
					transaction.commit();
					
					transaction = new Transaction(db);
					transaction.beginTrans();
				}
			}
			transaction.commit();
		} catch (Exception e) {
			throw new RuntimeException("批量更新出错，已更新" + recCnt + "条", e);
		}
	}

	@Override
	public <T extends Serializable> void saveTsList(List<T> targetList, int batchSize) {
		Transaction transaction = new Transaction(db);
		transaction.beginTrans();
		try {
			db.saveBatch(targetList, batchSize);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 查找给定类型最近的历史K线
	 * @param targetCls K线类型
	 * @return
	 */
	private <T> List<T> findLastHistQuot(Class<T> targetCls) {
		String tableName = getTableNameFromClassName(targetCls.getSimpleName());
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT target.* ");
		sql.append("FROM ").append(tableName).append(" target, ");
		sql.append("(SELECT asset_id,MAX(date) AS date FROM ").append(tableName).append(" WHERE turnover <> 0 GROUP BY asset_id) tmp ");
		sql.append("WHERE target.asset_id=tmp.asset_id AND target.date=tmp.date");
		List<Map<String, Object>> lstResult = db.findRecords(sql.toString());
		return RowMapperUtils.map2List(lstResult, targetCls);
	}
	
	/**
	 * 查找给定类型在给定时间前的历史K线，返回不超过给定条数
	 * @param targetCls K线类型
	 * @param date 时间
	 * @param limit 记录数限制，空则不限制
	 * @return
	 */
	private <T> List<T> findHistQuotBeforeTime(Class<T> targetCls, String assetId, String date, Integer limit) {
		String tableName = getTableNameFromClassName(targetCls.getSimpleName());
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT target.* ");
		sql.append("FROM ").append(tableName).append(" target ");
		sql.append("WHERE turnover <> 0 AND target.asset_id = ? AND target.date <= ? ");
		sql.append("ORDER BY target.date DESC ");
		if (limit != null) {
			sql.append("LIMIT ").append(limit);
		}
		
		List<Map<String, Object>> lstResult = db.findRecords(sql.toString(), assetId, date);
		return RowMapperUtils.map2List(lstResult, targetCls);
	}
	
	/**
	 * 从java类名转化为表名
	 * @param clsName java类名
	 * @return
	 */
	private String getTableNameFromClassName(String clsName) {
		char[] chars = clsName.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if ((i > 0) && (Character.isUpperCase(c)))
				sb.append('_').append(Character.toLowerCase(c));
			else {
				sb.append(Character.toLowerCase(c));
			}
		}
		return new String(sb);
	}
}
