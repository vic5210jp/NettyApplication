/**
 * @Title: TrdDayInfoDaoImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.dao.impl;

import org.springframework.stereotype.Repository;

import com.yiqiniu.mktinfo.persist.po.StkTrdCale;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;
import com.yiqiniu.mktserver.dao.TrdDayInfoDao;

/**
 * <code>TrdDayInfoDaoImpl</code>
 *
 * @author Jimmy
 * @date 2015-7-14 下午5:18:53
 * @version v1.0
 */
@Repository
public class TrdDayInfoDaoImpl extends MktInfoBeanRepository implements TrdDayInfoDao{

	@Override
	public StkTrdCale findTrdByNormalDay(String normalDate, String regionCode) {
		StkTrdCale trdInfo = db.select(tStkTrdCale.all).from(tStkTrdCale) // 
				.where(tStkTrdCale.normalDate.eq(normalDate)//
						.and(tStkTrdCale.regionCode.eq(regionCode))//
				).queryObject(StkTrdCale.class);
		return trdInfo;
	}

}
