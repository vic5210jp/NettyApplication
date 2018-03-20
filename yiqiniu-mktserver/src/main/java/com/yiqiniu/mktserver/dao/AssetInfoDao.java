package com.yiqiniu.mktserver.dao;

/**
 * @Title: AssetInfoDao.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */


import java.util.List;
import java.util.Map;

import com.yiqiniu.api.mktinfo.vo.StkDailyVO;
import com.yiqiniu.mktinfo.persist.po.HkAssetInfo;
import com.yiqiniu.mktinfo.persist.po.StkDayHk;

/**
 * @description 
 *
 * @author Jimmy
 * @date 2015-6-4 下午2:10:11
 * @version v1.0
 */

public interface AssetInfoDao {
	
	/**
	 * 查找小于date最后的一条历史行情数据
	 * @param date
	 * @return
	 */
	public Map<String, StkDailyVO> findLastStkDailyBeforeTime(String date) ;
	
	/**
	 * @param date
	 * @return
	 */
	public List<StkDayHk> findLastHkStkDailyBeforeTime(String date);
	
	/**
	 * 查找所有的资产信息，包括已退市
	 * @return
	 */
	List<HkAssetInfo> findAllAssetInfo();
}
