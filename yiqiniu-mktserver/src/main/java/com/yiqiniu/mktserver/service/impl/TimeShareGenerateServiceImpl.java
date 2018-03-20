/**
 * @Title: TimeShareGenerateServiceImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.impl;

import static com.yiqiniu.mktserver.util.Constants.CLOSE_TIME;
import static com.yiqiniu.mktserver.util.Constants.KEY_SEQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiqiniu.api.mktinfo.vo.QuotationVO;
import com.yiqiniu.api.mktinfo.vo.TimeSharingVO;
import com.yiqiniu.api.mktinfo.vo.enums.EAction;
import com.yiqiniu.api.mktserver.HkRtTime;
import com.yiqiniu.api.mktserver.HkTsData;
import com.yiqiniu.api.mktserver.HkTsHisData;
import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.common.utils.ArithmeticUtil;
import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.common.utils.DateUtils.TimeFormatter;
import com.yiqiniu.common.utils.QuoteUtil;
import com.yiqiniu.mktserver.listener.QuotListener;
import com.yiqiniu.mktserver.listener.event.QuotEvent;
import com.yiqiniu.mktserver.service.TimeShareGenerateService;
import com.yiqiniu.mktserver.util.Constants;
import com.yiqiniu.mktserver.util.LocalCache;
import com.yiqiniu.odps.po.Pair;
import com.yiqiniu.odps.service.RedisMapService;

/**
 * @description 分时生成服务实现
 *
 * @author 余俊斌
 * @date 2015年11月24日 下午8:04:22
 * @version v1.0
 */
@Service
public class TimeShareGenerateServiceImpl implements TimeShareGenerateService {

	private Logger LOG = LoggerFactory.getLogger(getClass());

	/**
	 * 需要生成分时的时间点的集合
	 */
	private Set<String> genTsTime;
	/**
	 * 行情本地缓存，用于缓存未保存到redis的分时数据
	 */
	private List<HkTsHisData> tsHisDataCache = new ArrayList<HkTsHisData>();
	
	/**
	 * 分时点本地缓存，用于缓存未保存到redis的分时点
	 */
	//private Map<String, HkRtTime> rtTimeCache = new HashMap<String, HkRtTime>();
	/**
	 * 用于注册和删除行情变更监听器
	 */
	private List<QuotListener> quotListeners = new ArrayList<QuotListener>();
	
	@Resource
	private DefaultConfig defaultConfig;
	@Resource
	private RedisMapService redisMapService;
	
	@PostConstruct
	protected void preInit() {
		// 9:30 - 12:00, 13:00-16:00
		String sTsAm = defaultConfig.getVal("ts.timeArea.am");
		String sTsPm = defaultConfig.getVal("ts.timeArea.pm");
		String sSeq = ",";
		TreeSet<String> treeSet = new TreeSet<String>();
		treeSet.addAll(QuoteUtil.times(sTsAm, sSeq, TimeFormatter.HH_MM_SS, Constants.HH_MM_00));
		treeSet.addAll(QuoteUtil.times(sTsPm, sSeq, TimeFormatter.HH_MM_SS, Constants.HH_MM_00));
		
		genTsTime = Collections.unmodifiableSortedSet(treeSet);
		
		LocalCache.HAS_GEN_TIME.putAll(redisMapService.findAllObject(HkRtTime.class));
	}

	@Override
	public void generateTimeShare(long triggerTime, boolean needPush) {
		String tsTime00 = DateUtils.dateToString(triggerTime, Constants.HH_MM_00);
		String strPrevTime00 = DateUtils.plusMins(new Date(triggerTime), -1, Constants.HH_MM_00);
		
		// 取本地缓存中获取已保存分时的时间点
		Map<String, HkRtTime>  mapRtTime = LocalCache.HAS_GEN_TIME;
		// 如果已经生成过，无需处理
		if (mapRtTime.containsKey(tsTime00)) {
			return;
		}

		// 不满足分时生成条件，无需处理
		if (!genTsTime.contains(strPrevTime00) && !CLOSE_TIME.equals(tsTime00)) {
			return;
		}
		
		// 取redis中最新的行情
		Map<String, HkTsData> mapTsPrevData = LocalCache.HK_TS_DATA;//redisMapService.findAllObject(HkTsData.class);
		
		List<HkTsHisData> lstTsNew = new ArrayList<HkTsHisData>(mapTsPrevData.size());
		//List<Pair> refreshPairList = new ArrayList<Pair>(mapTsPrevData.size());
		
		for (Entry<String, HkTsData> entry : mapTsPrevData.entrySet()) {
			// 上一分钟的时间
			HkTsData prevTsData = entry.getValue();
			// 把上一分钟的报价转变成分时数据
			HkTsHisData tsHisData = parseTimeShare(prevTsData, strPrevTime00);
			// 设置前成交量和前成交额，必须转换分时之后再设置，因为计算分时成交的时候需要用到上一成交和当前总成交
			prevTsData.setLastVol(prevTsData.getTotalVolume());
			prevTsData.setLastTurnover(prevTsData.getTurnOver());
			prevTsData.setDate(DateUtils.dateToString(triggerTime, TimeFormatter.YYYY_MM_DD));
			prevTsData.setTime(tsTime00);
			prevTsData.setUpdateTime(triggerTime);
			
			lstTsNew.add(tsHisData);
			// 存储对象
			/*Pair pair = new Pair();
			String sAssetId = prevTsData.getAssetId();
			pair.setKey(sAssetId);
			pair.setValue(prevTsData);
			refreshPairList.add(pair);*/
			//改为存储到本地缓存
			LocalCache.HK_TS_DATA.put(prevTsData.getAssetId(), prevTsData);
		}
		
		// 把最新的数据写到redis里面去
		/*try {
			redisMapService.saveUpdateBatch(HkTsData.class, refreshPairList);
		} catch (Exception e) {
			LOG.error("调用redisMapService进行保存港交所实时行情异常", e);
		}*/
		// 保存分时数据
		if (CollectionUtils.isNotEmpty(lstTsNew)) {
			boolean isSuccess = saveTimeShareToRedis(lstTsNew, HkTsHisData.class);
			// 保存不成功就放到本地内存
			if (!isSuccess) {
				tsHisDataCache.addAll(lstTsNew);
			} else {
				// 保存成功后， 检测内存是否有数据 , 如果有就同步到Redis
				if (CollectionUtils.isNotEmpty(tsHisDataCache)
						&& saveTimeShareToRedis(tsHisDataCache, HkTsHisData.class)) {
					// 成功把内存的同步到Redis服务器后， 把本地内存相关的数据清空
					tsHisDataCache.clear();
				}
				// 根据推送开关推送数据
				if (needPush) {
					// TODO Jimmy加上 notifyListener(triggerTime, Constants.MARKET_HK, EAction.HQUOTUPDATED);
				}
			}
		}
		// 保存分时点
		saveRtTimeToRedis(tsTime00);
	}


	@Override
	public void addQuotListener(QuotListener quotListener) {
		quotListeners.add(quotListener);
	}

	@Override
	public void removeQuotListener(QuotListener quotListener) {
		quotListeners.remove(quotListener);
	}
	
	/**
	 * 将行情转换成分时对象
	 * @param oQuote
	 * @param sTime00
	 * @return
	 */
	private HkTsHisData parseTimeShare(QuotationVO oQuote, String sTime00) {
		HkTsHisData ts = new HkTsHisData();
		// 资产ID
		ts.setAssetId(oQuote.getAssetId());
		// 日期
		String sDate = oQuote.getDate();
		ts.setDate(sDate);
		// 时间
		ts.setTime(sTime00);
		// 现价
		double price = oQuote.getPrice();
		ts.setLastPrice(price);
		// 昨收价
		ts.setPrevClose(oQuote.getPrevClose());
		// 开盘价
		ts.setOpen(oQuote.getOpen());
		// 最高
		ts.setHigh(oQuote.getHigh());
		// 最低
		ts.setLow(oQuote.getLow());
		double totalVolume = oQuote.getTotalVolume();
		// 分钟的成交量
		double dVol = ArithmeticUtil.sub(totalVolume, oQuote.getLastVol());
		ts.setVolume(dVol);
		// 分钟的成交额
		double dTurnover = ArithmeticUtil.sub(oQuote.getTurnOver(), oQuote.getLastTurnover());
		ts.setTurnOver(dTurnover);
		// 均价
		ts.setAvgPrice(oQuote.getAvgPrice());
		// 时间戳
		ts.setDateTime(DateUtils.stringToDate(sDate + " " + sTime00,
				TimeFormatter.YYYY_MM_DD_HH_MM_SS).getTime());
		return ts;
	}
	
	/**
	 * 追加历史分时数据
	 * 
	 * @param tsList
	 * 
	 * @return 保存成功 - true ， 否则 - false
	 */
	private <T extends TimeSharingVO> boolean saveTimeShareToRedis(List<T> tsList, Class<T> tsSaveClass) {
		try {
			List<Pair> updateList = new ArrayList<Pair>();
			// 给原来的key增量增加Key以"|"分开
			for (TimeSharingVO tsHisData : tsList) {
				String assetId = tsHisData.getAssetId();
				Pair pair = new Pair();
				pair.setKey(assetId + KEY_SEQ + tsHisData.getDate() + KEY_SEQ
						+ tsHisData.getTime());
				pair.setValue(tsHisData);
				updateList.add(pair);
			}
			if (updateList.size() > 0) {
				redisMapService.saveUpdateBatch(tsSaveClass, updateList);
			}
			LOG.info("港交所分时的数据的大小:{}", tsList.size());
		} catch (Exception e) {
			LOG.error("港交所实时行情分时更新异常", e);
			return false;
		}
		return true;
	}
	
	/**
	 * 保存分时点到redis
	 * @param tsTime
	 */
	private void saveRtTimeToRedis(String tsTime) {
		if (tsTime == null) {
			return;
		}
		//保存本地缓存
		HkRtTime rtTime = new HkRtTime();
		rtTime.setTime(tsTime);
		LocalCache.HAS_GEN_TIME.put(tsTime, rtTime);
		
		// 连同本地缓存一起保存到redis
		List<Pair> lstRtTimeToSave = new ArrayList<Pair>();
		if (MapUtils.isNotEmpty(LocalCache.HAS_GEN_TIME)) {
			for (Entry<String, HkRtTime> entry : LocalCache.HAS_GEN_TIME.entrySet()) {
				lstRtTimeToSave.add(new Pair(entry.getKey(), entry.getValue()));
			}
		}
		
		try {
			redisMapService.saveUpdateBatch(HkRtTime.class, lstRtTimeToSave);
		} catch (Exception e) {
			LOG.error("调用redisMapService进行保存分时点异常", e);
		}
	}
	
	/**
	 * 通知监听器
	 * @param triggerTime
	 * @param mktCode
	 * @param action
	 */
	private void notifyListener(long triggerTime, String mktCode, EAction action) {
		QuotEvent quotEvent = new QuotEvent();
		quotEvent.setQuotTime(new Date(triggerTime));
		quotEvent.setMktCode(mktCode);
		quotEvent.setAction(action);
		for (QuotListener quotListener : quotListeners) {
			quotListener.updated(quotEvent);
		}
	}

	@Override
	public Set<String> getGenTsTime() {
		return genTsTime;
	}
}
