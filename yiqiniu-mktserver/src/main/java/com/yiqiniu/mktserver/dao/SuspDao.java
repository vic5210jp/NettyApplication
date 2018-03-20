/**
 * @Title: SuspDao.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.dao;

import java.util.Date;

import com.yiqiniu.mktinfo.persist.po.HkStkSusp;

/**
 * @description 停牌DAO
 *
 * @author 余俊斌
 * @date 2015年11月19日 下午8:20:07
 * @version v1.0
 */

public interface SuspDao {

	/**
	 * 保存停牌对象
	 * @param susp
	 */
	void saveSusp(HkStkSusp susp);
	
	/**
	 * 更新停牌对象
	 * @param susp
	 */
	void updateSusp(HkStkSusp susp);
	
	/**
	 * 按照资产id和停牌日期删除对象
	 * @param assetId
	 * @param suspDate
	 * @return
	 */
	void deleteByAssetIdAndSuspendDate(String assetId, Date suspDate);
}
