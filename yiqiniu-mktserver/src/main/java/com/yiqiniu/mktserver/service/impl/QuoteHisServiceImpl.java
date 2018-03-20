/**
 * @Title: QuoteHisServiceImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yiqiniu.mktserver.dao.StkHisQuoteDao;
import com.yiqiniu.mktserver.service.QuoteHisService;

/**
 * @description 
 *
 * @author 余俊斌
 * @date 2015年7月16日 下午7:45:29
 * @version v1.0
 */
@Service
public class QuoteHisServiceImpl implements QuoteHisService {

	@Resource
	private StkHisQuoteDao hisQuoteDao;
	
	@Override
	public <T extends Serializable> void saveList(List<T> targetList, int batchSize) {
		hisQuoteDao.saveList(targetList, batchSize);
	}
	
	@Override
	public <T extends Serializable> void updateList(List<T> targetList) {
		hisQuoteDao.updateList(targetList);
	}

	@Override
	public <T extends Serializable> void updateListWithinTrans(List<T> targetList) {
		hisQuoteDao.updateListWithinTrans(targetList);
	}

	@Override
	public <T extends Serializable> void storeList(List<T> targetList, int batchSize) {
		hisQuoteDao.saveTsList(targetList, batchSize);
	}
	
	@Override
	public void clearTimeSharingData(String assetId, Date date) {
		hisQuoteDao.clearTimeSharingData(assetId, date);
	}

}
