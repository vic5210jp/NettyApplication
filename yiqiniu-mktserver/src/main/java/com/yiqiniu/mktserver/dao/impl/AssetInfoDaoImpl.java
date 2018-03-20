package com.yiqiniu.mktserver.dao.impl;

/**
 * @Title: AssetInfoDaoImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.yiqiniu.api.mktinfo.vo.StkDailyVO;
import com.yiqiniu.common.utils.QuoteUtil;
import com.yiqiniu.common.utils.RowMapperUtils;
import com.yiqiniu.mktinfo.persist.po.HkAssetInfo;
import com.yiqiniu.mktinfo.persist.po.StkDayHk;
import com.yiqiniu.mktserver.dao.AssetInfoDao;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;

/**
 * @description 
 *
 * @author Jimmy
 * @date 2015-6-4 下午2:10:54
 * @version v1.0
 */

@Repository
public class AssetInfoDaoImpl extends MktInfoBeanRepository implements AssetInfoDao {
	
	@Override
	public Map<String, StkDailyVO> findLastStkDailyBeforeTime(String date) {
		List<StkDayHk> lstSh = findLastHkStkDailyBeforeTime(date);
		
		Map<String, StkDailyVO> mapRet = new HashMap<String, StkDailyVO>(lstSh.size());
		List<StkDailyVO> convertShTo = QuoteUtil.convertHkTo(lstSh);
		if (CollectionUtils.isNotEmpty(convertShTo)) {
			for (StkDailyVO dailyVO : convertShTo) {
				mapRet.put(dailyVO.getAssetId(), dailyVO);
			}
		}
		
		return mapRet;
	}
	
	@Override
	public List<StkDayHk> findLastHkStkDailyBeforeTime(String date) {
		List<Map<String, Object>> lstResult = db.findRecords("SELECT target.* "
				+ "FROM stk_day_hk target, "
				+ "(SELECT asset_id,MAX(date) AS date FROM stk_day_hk WHERE date <= ? GROUP BY asset_id) t "
				+ "WHERE target.asset_id=t.asset_id AND target.date=t.date"
			, date);
		return RowMapperUtils.map2List(lstResult, StkDayHk.class);
	}

	@Override
	public List<HkAssetInfo> findAllAssetInfo() {
		return db.selectFrom(tHkAssetInfo).queryObjectsForList(HkAssetInfo.class);
	}
}
