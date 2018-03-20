/**
 * @Title: SuspDaoImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.dao.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.yiqiniu.db4j.ctx.DB;
import com.yiqiniu.mktinfo.persist.po.HkStkSusp;
import com.yiqiniu.mktinfo.persist.tables.StaticImport;
import com.yiqiniu.mktserver.dao.SuspDao;

/**
 * @description 停牌DAO实现
 *
 * @author 余俊斌
 * @date 2015年11月19日 下午8:26:11
 * @version v1.0
 */
@Repository
public class SuspDaoImpl extends StaticImport implements SuspDao {
	
	@Resource
	private DB db;

	@Override
	public void saveSusp(HkStkSusp susp) {
		db.save(susp);
	}

	@Override
	public void updateSusp(HkStkSusp susp) {
		db.update(susp);
	}

	@Override
	public void deleteByAssetIdAndSuspendDate(String assetId, Date suspDate) {
		db.delete(HkStkSusp.class, db.where(tHkStkSusp.assetId.eq(assetId).and(tHkStkSusp.suspDate.eq(suspDate))));
	}

}
