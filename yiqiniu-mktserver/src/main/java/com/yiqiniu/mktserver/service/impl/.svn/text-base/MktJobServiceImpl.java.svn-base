package com.yiqiniu.mktserver.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiqiniu.api.mktinfo.util.AggregateOrderBook;
import com.yiqiniu.api.mktinfo.vo.ChannelCommunicateVO;
import com.yiqiniu.api.mktinfo.vo.HkCurrencyRateVO;
import com.yiqiniu.api.mktinfo.vo.LatestAdjustFactorVO;
import com.yiqiniu.api.mktinfo.vo.QuotationVO;
import com.yiqiniu.api.mktinfo.vo.SecType;
import com.yiqiniu.api.mktinfo.vo.StkDailyVO;
import com.yiqiniu.api.mktinfo.vo.StkInfo;
import com.yiqiniu.api.mktinfo.vo.TimeSharingVO;
import com.yiqiniu.api.mktinfo.vo.TimesharingHis;
import com.yiqiniu.api.mktinfo.vo.enums.EAction;
import com.yiqiniu.api.mktinfo.vo.enums.EQuotAdjustType;
import com.yiqiniu.api.mktserver.ClearTsKey;
import com.yiqiniu.api.mktserver.HkRtTime;
import com.yiqiniu.api.mktserver.HkTsData;
import com.yiqiniu.api.mktserver.HkTsHisData;
import com.yiqiniu.common.forkjoin.ThreadPool;
import com.yiqiniu.common.forkjoin.ThreadPoolInfo;
import com.yiqiniu.common.forkjoin.threadpool.impl.ThreadPoolImpl;
import com.yiqiniu.common.utils.ArithmeticUtil;
import com.yiqiniu.common.utils.CollectionTransformUtil;
import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.common.utils.DateUtils.TimeFormatter;
import com.yiqiniu.common.utils.JSONUtil;
import com.yiqiniu.common.utils.QuoteUtil;
import com.yiqiniu.mktinfo.persist.po.HkAssetInfo;
import com.yiqiniu.mktinfo.persist.po.HkStkShrInfo;
import com.yiqiniu.mktinfo.persist.po.HkStkSusp;
import com.yiqiniu.mktinfo.persist.po.StkDayHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkTimesharingHk;
import com.yiqiniu.mktinfo.persist.po.StkTrdCale;
import com.yiqiniu.mktinfo.persist.po.StkWeekHk;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkYearHk;
import com.yiqiniu.mktinfo.persist.po.StkYearHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkYearHkFwd;
import com.yiqiniu.mktinfo.persist.po.SysJobLog;
import com.yiqiniu.mktserver.control.MktControlService;
import com.yiqiniu.mktserver.dao.AssetInfoDao;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;
import com.yiqiniu.mktserver.dao.StkHisQuoteDao;
import com.yiqiniu.mktserver.dao.TrdDayInfoDao;
import com.yiqiniu.mktserver.service.JobLogService;
import com.yiqiniu.mktserver.service.MktJobService;
import com.yiqiniu.mktserver.service.QuoteHisService;
import com.yiqiniu.mktserver.service.TimeShareGenerateService;
import com.yiqiniu.mktserver.service.vo.StkBaseHistQuotContainer;
import com.yiqiniu.mktserver.service.vo.StkBasePersistContainer;
import com.yiqiniu.mktserver.service.vo.StkHkPersistContainer;
import com.yiqiniu.mktserver.service.vo.StkHkQuotContainer;
import com.yiqiniu.mktserver.util.Constants;
import com.yiqiniu.mktserver.util.LocalCache;
import com.yiqiniu.odps.po.Pair;
import com.yiqiniu.odps.service.RedisMapService;

/**
 * <code>MktJobServiceImpl<code> 任务处理类 (开盘 、 收盘)
 * 
 * @author Colin
 * @since Yiqiniu v0.0.1 (2014-12-26)
 * 
 */
@Service
public class MktJobServiceImpl extends MktInfoBeanRepository implements MktJobService {

	static Logger LOG = LoggerFactory.getLogger(MktJobServiceImpl.class);
	/**
	 * KDJ任务的线程池名称
	 */
	private static final String THREAD_POOL_NAME_TECHNICAL_INDICATOR = "TP_TCHIND";
	/**
	 * 默认初始KDJ
	 */
	private static final double DEFAULT_KDJ_VAL =  100.0;
	/**
	 * 默认技术指标值 - 0 
	 */
	private static final double DEFAULT_TECH_IDCT_ZERO = 0.0;
	
	@Resource
	protected JobLogService jobLogService;
//	@Resource
//	private ZKMaster zkMaster;
	@Resource
	private AssetInfoDao assetInfoDao;
	@Resource
	protected TrdDayInfoDao trdDayInfoDao;
	@Resource
	private StkHisQuoteDao stkHisQuoteDao;
	@Resource
	private QuoteHisService quoteHisService;
	@Resource
	private RedisMapService redisMapService;
	@Resource
	private MktControlService mktControlService;
	@Resource
	private TimeShareGenerateService timeShareGenerateService;

	private String cleanChannel;
	private int closeWorkCoreThread;
	private int closeWorkMaxThread;
	private int closeWorkQueueSize;
	private long closeWorkTimeout;

	@PostConstruct
	protected void preInit() {
		DateConverter converter = new DateConverter();
		String[] patterns = new String[] { TimeFormatter.YYYY_MM_DD,
				TimeFormatter.YYYY_MM_DD_HH_MM_SS, TimeFormatter.YYYY_MM_DD_HH_MM };
		converter.setPatterns(patterns);
		ConvertUtils.register(converter, Date.class);
		
		cleanChannel = defaultConfig.getVal("clean.channel");
		
		closeWorkCoreThread = defaultConfig.getInt("close.work.coreThread");
		closeWorkMaxThread = defaultConfig.getInt("close.work.maxThread");
		closeWorkQueueSize = defaultConfig.getInt("close.work.queueSize");
		closeWorkTimeout = defaultConfig.getLong("close.work.timeout");
	}

	/**
	 * 
	 * 开盘工作
	 */
	@Override
	public void openWork() {
		LOG.info("更新本地股票缓存");
		try {
			Map<String, StkInfo> stkInfoMap = redisMapService.findAllObject(StkInfo.class);
			if (!stkInfoMap.isEmpty()) {
				synchronized (LocalCache.STOCK_MAP) {
					LocalCache.STOCK_MAP.clear();
					LocalCache.STOCK_MAP.putAll(stkInfoMap);
				}
			}
		} catch (Exception e) {
			LOG.error("从redis获取股票数据异常", e);
		}
		
		Date date = new Date();
		String tdDate = DateUtils.dateToString(date, DateUtils.TimeFormatter.YYYY_MM_DD);
		//tdDate============2015-08-05
		String regionCode = defaultConfig.getVal("region.code");
	//regionCode======HK
		SysJobLog sysJobLog = null;
		try {
			StkTrdCale stkTrdCale = trdDayInfoDao.findTrdByNormalDay(tdDate, regionCode);
			if (stkTrdCale == null) {
				LOG.error("获取交易日为空, 开盘作业失败");
				return;
			}
			// 判断当天是否为交易日
			if (stkTrdCale.getIsTradeDay()) {
				System.out.println("今天是交易日！！！！！");
				LocalCache.STK_TRD_CAL_MAP.put(tdDate, stkTrdCale);
			}else{
				LOG.info("不是交易的日， 不会进行开盘作业");
				return;
			}
			// 从机不用启动
//			if (!zkMaster.isMaster()) {
//				cleanLocalCache();
//				LOG.info("从机不用进行开盘作业，仅仅清空本地数据");
//				return;
//			}
			sysJobLog = jobLogService.findSysJobLog("mktinfo", "mktOpenJob", tdDate);
			if (sysJobLog != null) {
				cleanLocalCache();
				LOG.info("不用进行开盘作业, 目前开盘作业的状态为:" + sysJobLog.getJobStatus());
				return;
			}
			LOG.info("开盘作业开始");
			sysJobLog = new SysJobLog();
			sysJobLog.setJobName("mktOpenJob");
			sysJobLog.setSysName("mktinfo");
			jobLogService.saveJob(sysJobLog);
			cleanCache();
			jobLogService.updateJob(sysJobLog);
			
			// 通知Redis监听器
			ChannelCommunicateVO communicateVO = new ChannelCommunicateVO();
			communicateVO.setAction(EAction.HCLEAN);
			communicateVO.setTime(date);
			redisTpl.convertAndSend(cleanChannel, JSONUtil.toJson(communicateVO));
			
			LOG.info("从实时行情中剔除退市");
			// 股票信息数据 
			Map<String, StkInfo> stkInfoMap = LocalCache.STOCK_MAP;
					
			// 实时行情数据
			Map<String, HkTsData> tsDataMap = null;
			try {
				tsDataMap = redisMapService.findAllObject(HkTsData.class);
			} catch (Exception e) {
				LOG.error("从redis获取实时行情数据异常", e);
				tsDataMap = Collections.emptyMap();
			}
			for (Iterator<Entry<String, HkTsData>> iterator = tsDataMap.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, HkTsData> entry = iterator.next();
				String key = entry.getKey();
				if (!stkInfoMap.containsKey(key)) {
					LOG.info("退市的资产ID"+key);
					redisMapService.delete(HkTsData.class, key);
					iterator.remove();
				}
			}
			LocalCache.HK_TS_DATA.putAll(tsDataMap);
			
			LOG.info("开盘作业结束");
		} catch (Exception e) {
			// 确保出异常也要把本地的数据清空
			cleanLocalCache();
			LOG.error("开盘作业异常", e);
			if (sysJobLog == null) {
				sysJobLog = new SysJobLog();
				sysJobLog.setJobName("mktOpenJob");
				sysJobLog.setSysName("mktinfo");
				jobLogService.saveJob(sysJobLog);
			}
			jobLogService.updateError(sysJobLog, e);
		}
	}

	/**
	 * 
	 * 收盘工作
	 */
	@Override
	public void closeWork() {
		Date date = new Date();
		String tdDate = DateUtils.dateToString(date, DateUtils.TimeFormatter.YYYY_MM_DD);
		String regionCode = defaultConfig.getVal("region.code");
		StkTrdCale stkTrdCale = LocalCache.STK_TRD_CAL_MAP.get(tdDate);
		if (stkTrdCale == null) {
			try {
				stkTrdCale = trdDayInfoDao.findTrdByNormalDay(tdDate, regionCode);
			} catch(Exception e) {
				LOG.error("获取交易日异常", e);
			}
			stkTrdCale = trdDayInfoDao.findTrdByNormalDay(tdDate, regionCode);
			if (stkTrdCale != null) {
				LocalCache.STK_TRD_CAL_MAP.put(tdDate, stkTrdCale);
			}
		}
		// 判断当天是否为交易日
		if (stkTrdCale == null) {
			LOG.error("获取交易日为空");
			return;
		}
		if (!stkTrdCale.getIsTradeDay()) {
			LOG.info("不是交易日， 不会进行收盘作业");
			return;
		}
		SysJobLog sysJobLog = null;
		try {
			sysJobLog = jobLogService.findSysJobLog("mktinfo", "mktcloseJob", tdDate);
		} catch(Exception e) {
			LOG.error("获取任务状态异常", e);
		}
		sysJobLog = jobLogService.findSysJobLog("mktinfo", "mktcloseJob", tdDate);
		if (sysJobLog != null) {
			LOG.info("不用进行收盘作业, 目前收盘作业的状态为:" + sysJobLog.getJobStatus());
			return;
		}
		sysJobLog = new SysJobLog();
		LOG.info("收盘作业开始");
		try {
			sysJobLog.setJobName("mktcloseJob");
			sysJobLog.setSysName("mktinfo");
			jobLogService.saveJob(sysJobLog);
			
			LOG.info("查询历史数据...");
			Map<String, StkDailyVO> hStkDaily = assetInfoDao.findLastStkDailyBeforeTime(tdDate);
			
			// 转化港交所行情
			StkHkPersistContainer hkPersistContainer = hkCloseWork(hStkDaily, tdDate);
			
			// 计算技术指标
			calcTechnicalIndicator(hkPersistContainer, tdDate);
			
			// 保存K线数据
			storeQuotData(hkPersistContainer);
			
			// 保存分时数据
			storeTimesharing();
			
			mktControlService.resetJobLogRecordFlag();
			
			jobLogService.updateJob(sysJobLog);
		} catch (Exception e) {
			LOG.error("收盘作业异常", e);
			jobLogService.updateError(sysJobLog, e);
		} finally {
			LocalCache.stkInfoUpdate = false;
			LOG.info("收盘作业结束");
		}
	}

	@Override
	public void preLoadData() {
		try {
			if (!LocalCache.stkInfoUpdate) {
				LocalCache.STOCK_MAP.putAll(redisMapService.findAllObject(StkInfo.class));
				if (MapUtils.isEmpty(LocalCache.STOCK_MAP)) {
					LocalCache.STOCK_MAP.putAll(uptAllStkInfoToRedis());
				}
				
				LocalCache.stkInfoUpdate = true;
			}
		} catch (Exception e) {
			LOG.error("预加载数据失败", e);
		}
	}

	/**
	 * 删除本地缓存
	 */
	private void cleanLocalCache() {
		LocalCache.HAS_GEN_TIME.clear();
		LocalCache.STK_TRD_CAL_MAP.clear();
		LocalCache.AGGREGATE_ORDER_BOOK_MAP.clear();
		LocalCache.HK_TS_DATA.clear();
	}

	/**
	 * 开盘前清除redis中缓存昨天的数据
	 */
	private void cleanCache() {
		LOG.info(" start clean cache ");
		// 清除分时数据
		try {
			cleanLocalCache();
			Date date = new Date();
			String sDate = DateUtils.dateToString(date, TimeFormatter.YYYY_MM_DD);
			String sTime = DateUtils.dateToString(date, Constants.HH_MM_00);
			resetQuot(HkTsData.class, sDate, sTime);
			redisMapService.delete(HkRtTime.class);
			redisMapService.delete(HkTsHisData.class);
			redisMapService.delete(TimesharingHis.class);
			redisMapService.delete(HkCurrencyRateVO.class);
			redisMapService.delete(AggregateOrderBook.class);
		} catch (Exception e) {
			LOG.error("早上开盘清除前收市数据异常", e);
		}
		LOG.info(" end clean cache ");
	}

	/**
	 * 重置行情
	 * 
	 * @param tsDataClass
	 * @param sMktCode
	 */
	private void resetQuot(Class<? extends QuotationVO> tsDataClass, String sDate, String sTime) {
		try {
			Map<String, ?> mapTsData = redisMapService.findAllObject(tsDataClass);
			List<Pair> lstTsData = new ArrayList<Pair>();
			for (Entry<String, ?> entry : mapTsData.entrySet()) {
				QuotationVO oQuot = (QuotationVO) entry.getValue();
				oQuot.setPrevClose(oQuot.getPrice());
				oQuot.setAvgPrice(0d);
				oQuot.setChange(0);
				oQuot.setChangePct(0);
				oQuot.setB1(0);
				oQuot.setB1Price(0);
				oQuot.setB2(0);
				oQuot.setB2Price(0);
				oQuot.setB3(0);
				oQuot.setB3Price(0);
				oQuot.setB4(0);
				oQuot.setB4Price(0);
				oQuot.setB5(0);
				oQuot.setB5Price(0);
				oQuot.setS1(0);
				oQuot.setS1Price(0);
				oQuot.setS2(0);
				oQuot.setS2Price(0);
				oQuot.setS3(0);
				oQuot.setS3Price(0);
				oQuot.setS4(0);
				oQuot.setS4Price(0);
				oQuot.setS5(0);
				oQuot.setS5Price(0);
				oQuot.setHigh(0);
				oQuot.setLow(0);
				oQuot.setOpen(0);
				oQuot.setTotalVolume(0);
				oQuot.setLastVol(0);
				oQuot.setTurnOver(0);
				oQuot.setLastTurnover(0);
				oQuot.setDate(sDate);
				oQuot.setTime(sTime);
				oQuot.setStatus(0);
				lstTsData.add(new Pair(entry.getKey(), oQuot));
			}
			redisMapService.saveUpdateBatch(tsDataClass, lstTsData);
		} catch (Exception e) {
			LOG.error("重置港交所行情异常", e);
		}
	}

	@Override
	public StkHkPersistContainer hkCloseWork(Map<String, StkDailyVO> hStkDaily, String trdDate) throws Exception {
		LOG.info("--- start hk mkt close work--- ");
		long start = System.currentTimeMillis();
		preLoadData();
		
		Map<String, HkTsData> mapAllQuots = redisMapService.findAllObject(HkTsData.class);
		
		// 保存或更新日、周、月、年K的列表
		List<StkDayHk> daySaveList = new ArrayList<StkDayHk>();

		StkBasePersistContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw> weekPersist = new StkBasePersistContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw>();
		StkBasePersistContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw> monthPersist = new StkBasePersistContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw>();
		StkBasePersistContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw> yearPersist = new StkBasePersistContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw>();

		// 准备收盘用的数据
		LOG.info("从redis取停牌信息");
		Map<String, HkStkSusp> suspMap = null;
		try {
			suspMap = redisMapService.findAllObject(HkStkSusp.class);
		} catch (Exception e) {
			LOG.error("从redis获取停牌数据异常", e);
		}
		// 周、月、年K
		StkBaseHistQuotContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw> weekHist = new StkBaseHistQuotContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw>(
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkWeekQuotes(), "assetId"), 
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkWeekFwdQuotes(), "assetId"), 
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkWeekBkwQuotes(), "assetId"));

		StkBaseHistQuotContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw> monthHist = new StkBaseHistQuotContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw>(
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkMonthQuotes(), "assetId"), 
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkMonthFwdQuotes(), "assetId"), 
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkMonthBkwQuotes(), "assetId"));

		StkBaseHistQuotContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw> yearHist = new StkBaseHistQuotContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw>(
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkYearQuotes(), "assetId"), 
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkYearFwdQuotes(), "assetId"), 
				CollectionTransformUtil.listToMap(stkHisQuoteDao.findLastHkYearBkwQuotes(), "assetId"));
		
		LOG.info("从redis中获取股本信息...");
		Map<String, HkStkShrInfo> mapStkShr = null;
		try{
			mapStkShr = redisMapService.findAllObject(HkStkShrInfo.class);
		} catch (Exception e) {
			LOG.error("从redis获取股本信息异常", e);
			throw new RuntimeException("从redis获取股本信息异常", e);
		}
		
		// 当日所在的周、月、年
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int iCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
		int iCurrentMonth = cal.get(Calendar.MONTH);
		int iCurrentYear = cal.get(Calendar.YEAR);
		
		Map<String, StkHkQuotContainer> mapQuotContainer = new HashMap<String, StkHkQuotContainer>();
		
		for (Entry<String, HkTsData> entry : mapAllQuots.entrySet()) {
			String assetId = entry.getKey();

			StkInfo stkInfo = LocalCache.STOCK_MAP.get(assetId);
			// 我们库没有的或者退市的, 直接跳过
			if (stkInfo == null || !stkInfo.getIsStatus()) {
				continue;
			}
			
			HkTsData quote = entry.getValue();
			boolean processDayQuotOnly = false;
			// 判断停牌，不对些股票进行收盘工作，直接返回
			// 但是如果又停牌, 又除权, 则复权因子会发生变化，需要收盘，但只处理日K
			if (MapUtils.isNotEmpty(suspMap) && suspMap.containsKey(assetId) && quote.getTurnOver() == 0D) {
				// 停牌
				processDayQuotOnly = true;
				if (MapUtils.isEmpty(hStkDaily) || !hStkDaily.containsKey(assetId)) {
					LOG.info("停牌，且历史行情无此数据，忽略，assetId={}", assetId);
					continue;
				}
				// 分红,是通过昨天的收盘价与今天的昨收价进行对比, 如果不同就发生了除权, 否则没有发生除权
				if (hStkDaily.get(assetId).getClose() == quote.getPrevClose() && quote.getPrevClose() != 0) {
					LOG.info("停牌，但未分红，忽略，assetId={}", assetId);
					continue;
				}
				LOG.info("HK停牌的股票, 但是又发生除权, 将会进行收盘作业. assetId=" + assetId);
			} else {
				// 今天未停牌，但还要排除非交易（比如：未上市）的情况
				String strListingDate = stkInfo.getListingDate();
				if (quote.getTurnOver() == 0 && strListingDate != null && trdDate.compareTo(strListingDate) < 0) {
					LOG.info("非交易状态，忽略，assetId={}", assetId);
					continue;
				}
			}
			if (quote.getTurnOver() == 0D) {
				// 成交额为零时, 修正开高低收
				double dPreClose = quote.getPrevClose();
				double dOpen = quote.getOpen();
				double dClose = quote.getPrice();
				if (dOpen != 0 && dClose != 0) {
					// 开盘价、收盘价都不为0的，最高为0时取开、收二者高的为最高价，最低为0时取开、收二者低的为最低价
					if (quote.getHigh() == 0) {
						quote.setHigh(Math.max(dOpen, dClose));
					}
					if (quote.getLow() == 0) {
						quote.setLow(Math.min(dOpen, dClose));
					}
				} else {
					// 其他情况，开高低收都取昨收价
					quote.setPrice(dPreClose);
					quote.setHigh(dPreClose);
					quote.setLow(dPreClose);
					quote.setOpen(dPreClose);
				}
			} else {
				// 成交额不为零时，也有可能需要修正开高低收，但此时只对为零的开高低进行修改
				double dClose = quote.getPrice();
				if (quote.getOpen() == 0) {
					quote.setOpen(dClose);
				}
				if (quote.getHigh() == 0) {
					quote.setHigh(dClose);
				}
				if (quote.getLow() == 0) {
					quote.setLow(dClose);
				}
			}
			// 不停牌，或者停牌但发生了除权，则存储对象 -- 【停牌除权要存储，是因为复权因子有变更】
			StkDayHk stkDayHk = QuoteUtil.parseHkDaily(quote);
			// 复权因子
			double af = 1.0;
			LatestAdjustFactorVO adjFactorVO = redisMapService.findObject(
					LatestAdjustFactorVO.class, assetId);
			if (adjFactorVO != null) {
				af = adjFactorVO.getAdjustFactor();
			} else {
				LOG.error("未取到最新的复权调整因子，使用默认值[1.0]，assetId={}", assetId);
			}
			stkDayHk.setAdjFactor(af);
			
			// 对股票设置换手率
			if (stkInfo.getSecType() == SecType.STK) {
				HkStkShrInfo stkShrInfo = mapStkShr.get(assetId);
				if (stkShrInfo == null) {
					LOG.error("没有{}的股本信息，换手率保存为NULL", assetId);
				} else {
					Double flShr = stkShrInfo.getFlShr();
					if (flShr == null || flShr == 0) {
						LOG.error("{}有股本信息，但流通股本为空或为0，换手率保存为NULL", assetId);
					} else {
						double dVol = ArithmeticUtil.mul(quote.getTotalVolume(), quote.getLotSize()); // 单位是手
						stkDayHk.setTurnRate(ArithmeticUtil.div(dVol, flShr));
					}
				}
			}

			// 添加到保存列表
			daySaveList.add(stkDayHk);
			
			// 如果停牌，则不处理周、月、年的数据
			if (processDayQuotOnly) {
				continue;
			}
			
			StkHkQuotContainer quotContainer = new StkHkQuotContainer();
			quotContainer.setAssetId(assetId);
			mapQuotContainer.put(assetId, quotContainer);
			quotContainer.setStkDay(stkDayHk);
			// 继续处理周、月、年的数据

			// 处理周K数据
			processWeekHk(weekPersist, weekHist, quotContainer, iCurrentYear, iCurrentWeek, assetId, stkDayHk, adjFactorVO);

			// 处理月K数据
			processMonthHk(monthPersist, monthHist, quotContainer, iCurrentYear, iCurrentMonth, assetId, stkDayHk, adjFactorVO);

			// 处理年K数据
			processYearHk(yearPersist, yearHist, quotContainer, iCurrentYear, assetId, stkDayHk, adjFactorVO);
		}
		
		LOG.info(" hk mkt close job useTime:" + (System.currentTimeMillis() - start));

		StkHkPersistContainer persistContainer = new StkHkPersistContainer();
		persistContainer.setDayContainer(daySaveList);
		persistContainer.setWeekContainer(weekPersist);
		persistContainer.setMonthContainer(monthPersist);
		persistContainer.setYearContainer(yearPersist);
		persistContainer.setQuotContainer(mapQuotContainer);
		
		LOG.info(" end hk mkt close work ");
		return persistContainer;
	}

	@Override
	public void calcTechnicalIndicator(StkHkPersistContainer hkContainer, String date) {
		List<Callable<Void>> lstTasks = new LinkedList<Callable<Void>>();
		
		LOG.info("分配技术指标任务...");
		addHkTechnicalIndicatorTask(lstTasks, hkContainer, date);
		
		ThreadPoolInfo tpInfo = new ThreadPoolInfo();
		tpInfo.setName(THREAD_POOL_NAME_TECHNICAL_INDICATOR);
		tpInfo.setCoreSize(closeWorkCoreThread);
		tpInfo.setMaxSize(closeWorkMaxThread);
		tpInfo.setQueueSize(closeWorkQueueSize);
		ThreadPool pool = new ThreadPoolImpl(tpInfo);
		
		LOG.info("多线程计算技术指标...");
		List<Future<Void>> lstFuture = pool.invokeAll(lstTasks, closeWorkTimeout, TimeUnit.MINUTES, THREAD_POOL_NAME_TECHNICAL_INDICATOR);
		LOG.info("汇总技术指标计算结果...");
		for (Future<Void> future : lstFuture) {
			try {
				future.get();
			} catch (InterruptedException e) {
				LOG.error("收到中断请求，忽略...", e);
			} catch (ExecutionException e) {
				LOG.error("技术指标计算执行出错", e);
			}
		}
		
		LOG.info("技术指标计算完毕!!!");
	}

	@Override
	public void storeQuotData(final StkHkPersistContainer hkContainer) {
		List<Callable<Void>> lstTasks = new LinkedList<Callable<Void>>();
		
		LOG.info("分配保存任务...");
		
		lstTasks.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					quoteHisService.saveList(hkContainer.getDayContainer(), 500);
				} catch (Exception e) {
					LOG.error("保存港交所日K出错!");
					throw new RuntimeException("保存港交所日K出错!", e);
				}
				return null;
			}
		});
		
		lstTasks.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					saveHisQuotData(hkContainer.getWeekContainer());
				} catch (Exception e) {
					LOG.error("保存港交所周K出错!");
					throw new RuntimeException("保存港交所周K出错!", e);
				}
				return null;
			}
		});
		
		lstTasks.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					saveHisQuotData(hkContainer.getMonthContainer());
				} catch (Exception e) {
					LOG.error("保存港交所月K出错!");
					throw new RuntimeException("保存港交所月K出错!", e);
				}
				return null;
			}
		});
		
		lstTasks.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					saveHisQuotData(hkContainer.getYearContainer());
				} catch (Exception e) {
					LOG.error("保存港交所年K出错!");
					throw new RuntimeException("保存港交所年K出错!", e);
				}
				return null;
			}
		});
		
		ThreadPoolInfo tpInfo = new ThreadPoolInfo();
		tpInfo.setName(THREAD_POOL_NAME_TECHNICAL_INDICATOR);
		tpInfo.setCoreSize(closeWorkCoreThread);
		tpInfo.setMaxSize(closeWorkMaxThread);
		tpInfo.setQueueSize(closeWorkQueueSize);
		ThreadPool pool = new ThreadPoolImpl(tpInfo);
		
		LOG.info("多线程保存K线数据...");
		List<Future<Void>> lstFuture = pool.invokeAll(lstTasks, closeWorkTimeout, TimeUnit.MINUTES, THREAD_POOL_NAME_TECHNICAL_INDICATOR);
		LOG.info("汇总保存结果...");
		for (Future<Void> future : lstFuture) {
			try {
				future.get();
			} catch (InterruptedException e) {
				LOG.error("收到中断请求，忽略...", e);
			} catch (ExecutionException e) {
				LOG.error("K线数据保存出错", e);
			}
		}
		
		LOG.info("K线数据保存完毕!!!");
	}

	@Override
	public void storeTimesharing() {
		LOG.info("开始处理分时数据");
		long lStart = System.currentTimeMillis();
		// 停牌数据
		Map<String, HkStkSusp> mapSusp = null;
		try {
			mapSusp = redisMapService.findAllObject(HkStkSusp.class);
		} catch (Exception e) {
			LOG.error("从redis获取停牌数据异常，记录分时数据时忽略停牌信息", e);
			mapSusp = Collections.emptyMap();
		}
		// 将收盘前临停的个股从停牌Map中去掉，因为这部分个股有成交量，需要保存分时
		LOG.info("从停牌Map中剔除临时停牌");
		Map<String, HkTsData> mapQuotation = new HashMap<String, HkTsData>(LocalCache.HK_TS_DATA);
		// 用iterator而不是foreach，是因为要在遍历过程中删除数据
		for (Iterator<Entry<String, HkStkSusp>> iterator = mapSusp.entrySet().iterator(); iterator.hasNext();) {
			String key = iterator.next().getKey();
			if (mapQuotation.containsKey(key)) {
				HkTsData quot = mapQuotation.get(key);
				if (quot.getTurnOver() != 0) {
					// 收盘处于停牌状态，但当天有成交额，则是收盘前临停，此处不算停牌
					iterator.remove();
				}
			}
		}
		
		LOG.info("计算需要保存分时的资产...");
		Set<String> assetsNeedSaving = new HashSet<String>(mapQuotation.keySet());
		assetsNeedSaving.removeAll(mapSusp.keySet());
		
		ThreadPoolInfo tpInfo = new ThreadPoolInfo();
		tpInfo.setName(THREAD_POOL_NAME_TECHNICAL_INDICATOR);
		tpInfo.setCoreSize(closeWorkCoreThread);
		tpInfo.setMaxSize(closeWorkMaxThread);
		tpInfo.setQueueSize(closeWorkQueueSize);
		ThreadPool pool = new ThreadPoolImpl(tpInfo);
		
		final String strDate = DateUtils.dateToString(new Date(), DateUtils.TimeFormatter.YYYY_MM_DD);
		List<Callable<Void>> lstSaveTasks = new ArrayList<Callable<Void>>(assetsNeedSaving.size());
		LOG.info("分配保存分时数据任务...");
		for (final String assetId : assetsNeedSaving) {
			lstSaveTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkTimesharingHk> lstTs = getPersistentTsByAssetId(assetId, strDate);
						if (CollectionUtils.isNotEmpty(lstTs)) {
							quoteHisService.storeList(lstTs, 500);
						}
					} catch (Exception e) {
						LOG.error("保存港交所分时数据期间出错!", e);
						throw new RuntimeException("保存港交所分时数据期间出错!", e);
					}
					return null;
				}
			});
		}
		
		if (CollectionUtils.isNotEmpty(lstSaveTasks)) {
			LOG.info("多线程保存分时数据...");
			List<Future<Void>> lstSaveFuture = pool.invokeAll(lstSaveTasks, closeWorkTimeout, TimeUnit.MINUTES, THREAD_POOL_NAME_TECHNICAL_INDICATOR);
			LOG.info("汇总保存分时数据结果...");
			for (Future<Void> future : lstSaveFuture) {
				try {
					future.get();
				} catch (InterruptedException e) {
					LOG.error("收到中断请求，忽略...", e);
				} catch (ExecutionException e) {
					LOG.error("分时数据保存出错", e);
				}
			}
		} else {
			LOG.info("没有需要保存的分时数据...");
		}
		
		LOG.info("分时数据保存完毕!!!");
		
		LOG.info("查询需要清理的分时数据");
		// 删除过时的数据
		// XXX 暂时沿用a股，写死5日
		int delExpireDay = 5;
		List<ClearTsKey> lstDelKeys = stkHisQuoteDao.findTsKeysToClear(delExpireDay);

		List<Callable<Void>> lstClearTasks = new ArrayList<Callable<Void>>(assetsNeedSaving.size());
		LOG.info("分配清理分时数据任务...");
		
		for (final ClearTsKey clearTsKey : lstDelKeys) {
			lstClearTasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						quoteHisService.clearTimeSharingData(clearTsKey.getAssetId(), clearTsKey.getDate());
					} catch (Exception e) {
						LOG.error("删除过时分时数据期间出错!", e);
						throw new RuntimeException("删除过时分时数据期间出错!", e);
					}
					return null;
				}
			});
		}
		
		if (CollectionUtils.isNotEmpty(lstClearTasks)) {
			LOG.info("多线程删除过时分时数据...");
			List<Future<Void>> lstClearFuture = pool.invokeAll(lstClearTasks, closeWorkTimeout, TimeUnit.MINUTES, THREAD_POOL_NAME_TECHNICAL_INDICATOR);
			LOG.info("汇总删除过时分时数据结果...");
			for (Future<Void> future : lstClearFuture) {
				try {
					future.get();
				} catch (InterruptedException e) {
					LOG.error("收到中断请求，忽略...", e);
				} catch (ExecutionException e) {
					LOG.error("删除过时分时数据出错", e);
				}
			}
		} else {
			LOG.info("没有需要删除的分时数据...");
		}
		
		LOG.info("完成删除过时的五日分时数据...");
		
		long lEnd = System.currentTimeMillis();
		LOG.info("结束处理分时数据，用时：" + (lEnd - lStart));
	}
	
	/**
	 * 按资产id和日期查找缓存中的分时数据
	 * @param assetId
	 * @param date 日期
	 * @return
	 */
	private List<StkTimesharingHk> getPersistentTsByAssetId(String assetId, String date) {
		Set<String> genTsTime = timeShareGenerateService.getGenTsTime();
		List<String> lstKeyForTs = new ArrayList<String>(genTsTime.size());
		
		for (String strTime : genTsTime) {
			lstKeyForTs.add(assetId + Constants.KEY_SEQ + date + Constants.KEY_SEQ + strTime);
		}
		List<HkTsHisData> lstTs = redisMapService.findListObject(HkTsHisData.class, lstKeyForTs.toArray(new String[genTsTime.size()]));

		List<StkTimesharingHk> lstRet = new ArrayList<StkTimesharingHk>(lstTs.size());
		for (HkTsHisData hkTsHisData : lstTs) {
			if (hkTsHisData != null) {
				lstRet.add(convertToPersistTs(hkTsHisData));
			}
		}
	
		return lstRet;
	}
	
	/**
	 * 加载所有的Stkinfo到Redis
	 * 
	 * @param assetList
	 */
	private Map<String, StkInfo> uptAllStkInfoToRedis() {
		List<HkAssetInfo> assetList = db.select(tHkAssetInfo.all).from(tHkAssetInfo).where(tHkAssetInfo.isStatus.eq(true)).queryObjectsForList(HkAssetInfo.class);
		Map<String, StkInfo> hData = new HashMap<String, StkInfo>();
		for (HkAssetInfo assetInfo : assetList) {
			StkInfo stkInfoVO = new StkInfo();
			String mktCode = assetInfo.getMktCode();
			String assetId = assetInfo.getAssetId();
			stkInfoVO.setAssetId(assetId);
			stkInfoVO.setIsStatus(assetInfo.getIsStatus());
			stkInfoVO.setStkCode(assetInfo.getStkCode());
			stkInfoVO.setMarket(mktCode);
			if (assetInfo.getCorpId() != null) {
				stkInfoVO.setCorpCode(assetInfo.getCorpId());
			}
			stkInfoVO.setStkName(assetInfo.getStkName());
			stkInfoVO.setStkNameLong(assetInfo.getStkNameLong());
			stkInfoVO.setEngName(assetInfo.getEngName());

			Integer secType = assetInfo.getSecType();
			stkInfoVO.setSecType(secType);
			// 设置证券子类型
			stkInfoVO.setSecSType(assetInfo.getSecStype());

			if (null != assetInfo.getBoardCode()) {
				stkInfoVO.setBoardCode(assetInfo.getBoardCode());
			}
			if (null != assetInfo.getLotSize()) {
				stkInfoVO.setLotSize(assetInfo.getLotSize());
			}
			stkInfoVO.setListingDate(DateUtils.dateToString(assetInfo.getListingDate(),
					DateUtils.TimeFormatter.YYYY_MM_DD));
			stkInfoVO.setDelistDate(DateUtils.dateToString(assetInfo.getDelistDate(),
					DateUtils.TimeFormatter.YYYY_MM_DD));
			stkInfoVO.setCcyType(assetInfo.getCcyType());
			stkInfoVO.setIssuePrice(assetInfo.getIssuePrice());
			hData.put(stkInfoVO.getAssetId(), stkInfoVO);
			try {
				redisMapService.saveUpdate(stkInfoVO, stkInfoVO.getAssetId());
			} catch (Exception e) {
				LOG.error("redisRpcSerice saveUpdate 异常", e);
			}
		}
		return hData;
	}

	/**
	 * 转换Redis的分时对象为持久化的分时数据对象
	 * 
	 * @param t
	 * @return
	 */
	private <T extends TimeSharingVO> StkTimesharingHk convertToPersistTs(T t) {
		StkTimesharingHk stkTs = new StkTimesharingHk();
		stkTs.setAssetId(t.getAssetId());
		stkTs.setAvgPrice(t.getAvgPrice());
		stkTs.setDate(DateUtils.stringToDate(t.getDate(), TimeFormatter.YYYY_MM_DD));
		stkTs.setTime(DateUtils.stringToDate(t.getTime(), TimeFormatter.HH_MM_SS));
		stkTs.setVolume(t.getVolume());
		stkTs.setPrice(t.getLastPrice());
		return stkTs;
	}

	/**
	 * 处理周K
	 * 
	 * @param persistVO
	 * @param quotVO
	 * @param iCurrentWeek
	 * @param assetId
	 * @param stkDayHk
	 * @param adjFactorVO
	 * @throws Exception
	 */
	private void processWeekHk(StkBasePersistContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw> persistVO,
			StkBaseHistQuotContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw> quotVO, StkHkQuotContainer quotContainer, int iCurrentYear, int iCurrentWeek,
			String assetId, StkDayHk stkDayHk, LatestAdjustFactorVO adjFactorVO) throws Exception {
		StkDailyVO fwdDaily = quotCommService.adjustDailyVO(assetId,
				QuoteUtil.convertHkTo(stkDayHk), EQuotAdjustType.F, adjFactorVO);
		StkDailyVO bkwDaily = quotCommService.adjustDailyVO(assetId,
				QuoteUtil.convertHkTo(stkDayHk), EQuotAdjustType.B, adjFactorVO);

		if (quotVO.getNonAdjQuotMap().containsKey(assetId)) {
			// 有最近的周K记录
			StkWeekHk stkWeekHk = quotVO.getNonAdjQuotMap().get(assetId);
			StkWeekHkFwd stkWeekHkFwd = quotVO.getForwardAdjQuotMap().get(assetId);
			StkWeekHkBkw stkWeekHkBkw = quotVO.getBackwardAdjQuotMap().get(assetId);

			Calendar cal = Calendar.getInstance();
			cal.setTime(stkWeekHk.getDate());
			int iStkYear = cal.get(Calendar.YEAR);
			int iStkWeek = cal.get(Calendar.WEEK_OF_YEAR);

			if (iCurrentYear == iStkYear && iCurrentWeek == iStkWeek) {
				// 如果在同一周，则更新日期、收盘、最高、最低，累加成交量、成交额、换手率
				Date date = stkDayHk.getDate();
				stkWeekHk.setDate(date);
				stkWeekHkFwd.setDate(date);
				stkWeekHkBkw.setDate(date);
				// 收盘
				stkWeekHk.setClose(stkDayHk.getClose());
				stkWeekHkFwd.setClose(fwdDaily.getClose());
				stkWeekHkBkw.setClose(bkwDaily.getClose());

				// 最高
				if (stkDayHk.getHigh() > stkWeekHk.getHigh()) {
					stkWeekHk.setHigh(stkDayHk.getHigh());
				}
				if (fwdDaily.getHigh() > stkWeekHkFwd.getHigh()) {
					stkWeekHkFwd.setHigh(fwdDaily.getHigh());
				}
				if (bkwDaily.getHigh() > stkWeekHkBkw.getHigh()) {
					stkWeekHkBkw.setHigh(bkwDaily.getHigh());
				}
				// 最低
				if (stkDayHk.getLow() != 0 && stkDayHk.getLow() < stkWeekHk.getLow()) {
					stkWeekHk.setLow(stkDayHk.getLow());
				}
				if (fwdDaily.getLow() != 0 && fwdDaily.getLow() < stkWeekHkFwd.getLow()) {
					stkWeekHkFwd.setLow(fwdDaily.getLow());
				}
				if (bkwDaily.getLow() != 0 && bkwDaily.getLow() < stkWeekHkBkw.getLow()) {
					stkWeekHkBkw.setLow(bkwDaily.getLow());
				}
				double volume = ArithmeticUtil.add(stkWeekHk.getVolume(), stkDayHk.getVolume());
				stkWeekHk.setVolume(volume);
				stkWeekHkFwd.setVolume(volume);
				stkWeekHkBkw.setVolume(volume);
				double turnover = ArithmeticUtil.add(stkWeekHk.getTurnover(),
						stkDayHk.getTurnover());
				stkWeekHk.setTurnover(turnover);
				stkWeekHkFwd.setTurnover(turnover);
				stkWeekHkBkw.setTurnover(turnover);

				Double weekTurnRate = stkWeekHk.getTurnRate();
				Double dayTurnRate = stkDayHk.getTurnRate();
				if (weekTurnRate != null || dayTurnRate != null) {
					double turnRate = ArithmeticUtil.add(weekTurnRate == null ? 0 : weekTurnRate, dayTurnRate == null ? 0 : dayTurnRate);
					stkWeekHk.setTurnRate(turnRate);
					stkWeekHkFwd.setTurnRate(turnRate);
					stkWeekHkBkw.setTurnRate(turnRate);
				}

				persistVO.getNonAdjUpdateList().add(stkWeekHk);
				persistVO.getForwardAdjUpdateList().add(stkWeekHkFwd);
				persistVO.getBackwardAdjUpdateList().add(stkWeekHkBkw);
				
				quotContainer.setStkWeek(stkWeekHk);
				quotContainer.setStkWeekFwd(stkWeekHkFwd);
				quotContainer.setStkWeekBkw(stkWeekHkBkw);
			} else {
				// 不在同一周，新建一条周K，用当天的数据填充
				stkWeekHk = new StkWeekHk();
				BeanUtils.copyProperties(stkWeekHk, stkDayHk);
				persistVO.getNonAdjSaveList().add(stkWeekHk);

				stkWeekHkFwd = new StkWeekHkFwd();
				BeanUtils.copyProperties(stkWeekHkFwd, fwdDaily);
				persistVO.getForwardAdjSaveList().add(stkWeekHkFwd);

				stkWeekHkBkw = new StkWeekHkBkw();
				BeanUtils.copyProperties(stkWeekHkBkw, bkwDaily);
				persistVO.getBackwardAdjSaveList().add(stkWeekHkBkw);
				
				quotContainer.setStkWeek(stkWeekHk);
				quotContainer.setStkWeekFwd(stkWeekHkFwd);
				quotContainer.setStkWeekBkw(stkWeekHkBkw);
			}
		} else {
			// 无最近的周K记录，新建一条周K，用当天的数据填充
			StkWeekHk stkWeekHk = new StkWeekHk();
			BeanUtils.copyProperties(stkWeekHk, stkDayHk);
			persistVO.getNonAdjSaveList().add(stkWeekHk);

			StkWeekHkFwd stkWeekHkFwd = new StkWeekHkFwd();
			BeanUtils.copyProperties(stkWeekHkFwd, fwdDaily);
			persistVO.getForwardAdjSaveList().add(stkWeekHkFwd);

			StkWeekHkBkw stkWeekHkBkw = new StkWeekHkBkw();
			BeanUtils.copyProperties(stkWeekHkBkw, bkwDaily);
			persistVO.getBackwardAdjSaveList().add(stkWeekHkBkw);
			
			quotContainer.setStkWeek(stkWeekHk);
			quotContainer.setStkWeekFwd(stkWeekHkFwd);
			quotContainer.setStkWeekBkw(stkWeekHkBkw);
		}
	}

	/**
	 * 处理月K
	 * 
	 * @param persistVO
	 * @param quotVO
	 * @param iCurrentWeek
	 * @param assetId
	 * @param stkDayHk
	 * @param adjFactorVO
	 * @throws Exception
	 */
	private void processMonthHk(StkBasePersistContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw> persistVO,
			StkBaseHistQuotContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw> quotVO, StkHkQuotContainer quotContainer, int iCurrentYear, int iCurrentMonth,
			String assetId, StkDayHk stkDayHk, LatestAdjustFactorVO adjFactorVO) throws Exception {
		StkDailyVO fwdDaily = quotCommService.adjustDailyVO(assetId,
				QuoteUtil.convertHkTo(stkDayHk), EQuotAdjustType.F, adjFactorVO);
		StkDailyVO bkwDaily = quotCommService.adjustDailyVO(assetId,
				QuoteUtil.convertHkTo(stkDayHk), EQuotAdjustType.B, adjFactorVO);

		if (quotVO.getNonAdjQuotMap().containsKey(assetId)) {
			// 有最近的月K记录
			StkMonthHk stkMonthHk = quotVO.getNonAdjQuotMap().get(assetId);
			StkMonthHkFwd stkMonthHkFwd = quotVO.getForwardAdjQuotMap().get(assetId);
			StkMonthHkBkw stkMonthHkBkw = quotVO.getBackwardAdjQuotMap().get(assetId);

			Calendar cal = Calendar.getInstance();
			cal.setTime(stkMonthHk.getDate());
			int iStkYear = cal.get(Calendar.YEAR);
			int iStkMonth = cal.get(Calendar.MONTH);

			if (iCurrentYear == iStkYear && iCurrentMonth == iStkMonth) {
				// 如果在同一月，则更新日期、收盘、最高、最低，累加成交量、成交额、换手率
				Date date = stkDayHk.getDate();
				stkMonthHk.setDate(date);
				stkMonthHkFwd.setDate(date);
				stkMonthHkBkw.setDate(date);
				// 收盘
				stkMonthHk.setClose(stkDayHk.getClose());
				stkMonthHkFwd.setClose(fwdDaily.getClose());
				stkMonthHkBkw.setClose(bkwDaily.getClose());
				
				// 最高
				if (stkDayHk.getHigh() > stkMonthHk.getHigh()) {
					stkMonthHk.setHigh(stkDayHk.getHigh());
				}
				if (fwdDaily.getHigh() > stkMonthHkFwd.getHigh()) {
					stkMonthHkFwd.setHigh(fwdDaily.getHigh());
				}
				if (bkwDaily.getHigh() > stkMonthHkBkw.getHigh()) {
					stkMonthHkBkw.setHigh(bkwDaily.getHigh());
				}
				// 最低
				if (stkDayHk.getLow() != 0 && stkDayHk.getLow() < stkMonthHk.getLow()) {
					stkMonthHk.setLow(stkDayHk.getLow());
				}
				if (fwdDaily.getLow() != 0 && fwdDaily.getLow() < stkMonthHkFwd.getLow()) {
					stkMonthHkFwd.setLow(fwdDaily.getLow());
				}
				if (bkwDaily.getLow() != 0 && bkwDaily.getLow() < stkMonthHkBkw.getLow()) {
					stkMonthHkBkw.setLow(bkwDaily.getLow());
				}
				double volume = ArithmeticUtil.add(stkMonthHk.getVolume(), stkDayHk.getVolume());
				stkMonthHk.setVolume(volume);
				stkMonthHkFwd.setVolume(volume);
				stkMonthHkBkw.setVolume(volume);
				double turnover = ArithmeticUtil.add(stkMonthHk.getTurnover(),
						stkDayHk.getTurnover());
				stkMonthHk.setTurnover(turnover);
				stkMonthHkFwd.setTurnover(turnover);
				stkMonthHkBkw.setTurnover(turnover);

				Double monthTurnRate = stkMonthHk.getTurnRate();
				Double dayTurnRate = stkDayHk.getTurnRate();
				if (monthTurnRate != null || dayTurnRate != null) {
					double turnRate = ArithmeticUtil.add(monthTurnRate == null ? 0 : monthTurnRate, dayTurnRate == null ? 0 : dayTurnRate);
					stkMonthHk.setTurnRate(turnRate);
					stkMonthHkFwd.setTurnRate(turnRate);
					stkMonthHkBkw.setTurnRate(turnRate);
				}

				persistVO.getNonAdjUpdateList().add(stkMonthHk);
				persistVO.getForwardAdjUpdateList().add(stkMonthHkFwd);
				persistVO.getBackwardAdjUpdateList().add(stkMonthHkBkw);
				
				quotContainer.setStkMonth(stkMonthHk);
				quotContainer.setStkMonthFwd(stkMonthHkFwd);
				quotContainer.setStkMonthBkw(stkMonthHkBkw);
			} else {
				// 不在同一月，新建一条月K，用当天的数据填充
				stkMonthHk = new StkMonthHk();
				BeanUtils.copyProperties(stkMonthHk, stkDayHk);
				persistVO.getNonAdjSaveList().add(stkMonthHk);

				stkMonthHkFwd = new StkMonthHkFwd();
				BeanUtils.copyProperties(stkMonthHkFwd, fwdDaily);
				persistVO.getForwardAdjSaveList().add(stkMonthHkFwd);

				stkMonthHkBkw = new StkMonthHkBkw();
				BeanUtils.copyProperties(stkMonthHkBkw, bkwDaily);
				persistVO.getBackwardAdjSaveList().add(stkMonthHkBkw);
				
				quotContainer.setStkMonth(stkMonthHk);
				quotContainer.setStkMonthFwd(stkMonthHkFwd);
				quotContainer.setStkMonthBkw(stkMonthHkBkw);
			}
		} else {
			// 无最近的月K记录，新建一条月K，用当天的数据填充
			StkMonthHk stkMonthHk = new StkMonthHk();
			BeanUtils.copyProperties(stkMonthHk, stkDayHk);
			persistVO.getNonAdjSaveList().add(stkMonthHk);

			StkMonthHkFwd stkMonthHkFwd = new StkMonthHkFwd();
			BeanUtils.copyProperties(stkMonthHkFwd, fwdDaily);
			persistVO.getForwardAdjSaveList().add(stkMonthHkFwd);

			StkMonthHkBkw stkMonthHkBkw = new StkMonthHkBkw();
			BeanUtils.copyProperties(stkMonthHkBkw, bkwDaily);
			persistVO.getBackwardAdjSaveList().add(stkMonthHkBkw);
			
			quotContainer.setStkMonth(stkMonthHk);
			quotContainer.setStkMonthFwd(stkMonthHkFwd);
			quotContainer.setStkMonthBkw(stkMonthHkBkw);
		}
	}

	/**
	 * 处理年K
	 * 
	 * @param persistVO
	 * @param quotVO
	 * @param iCurrentWeek
	 * @param assetId
	 * @param stkDayHk
	 * @param adjFactorVO
	 * @throws Exception
	 */
	private void processYearHk(StkBasePersistContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw> persistVO,
			StkBaseHistQuotContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw> quotVO, StkHkQuotContainer quotContainer, int iCurrentYear,
			String assetId, StkDayHk stkDayHk, LatestAdjustFactorVO adjFactorVO) throws Exception {
		StkDailyVO fwdDaily = quotCommService.adjustDailyVO(assetId,
				QuoteUtil.convertHkTo(stkDayHk), EQuotAdjustType.F, adjFactorVO);
		StkDailyVO bkwDaily = quotCommService.adjustDailyVO(assetId,
				QuoteUtil.convertHkTo(stkDayHk), EQuotAdjustType.B, adjFactorVO);

		if (quotVO.getNonAdjQuotMap().containsKey(assetId)) {
			// 有最近的年K记录
			StkYearHk StkYearHk = quotVO.getNonAdjQuotMap().get(assetId);
			StkYearHkFwd StkYearHkFwd = quotVO.getForwardAdjQuotMap().get(assetId);
			StkYearHkBkw StkYearHkBkw = quotVO.getBackwardAdjQuotMap().get(assetId);

			Calendar cal = Calendar.getInstance();
			cal.setTime(StkYearHk.getDate());
			int iStkMonth = cal.get(Calendar.YEAR);

			if (iCurrentYear == iStkMonth) {
				// 如果在同一年，则更新日期、收盘、最高、最低，累加成交量、成交额、换手率
				Date date = stkDayHk.getDate();
				StkYearHk.setDate(date);
				StkYearHkFwd.setDate(date);
				StkYearHkBkw.setDate(date);
				// 收盘
				StkYearHk.setClose(stkDayHk.getClose());
				StkYearHkFwd.setClose(fwdDaily.getClose());
				StkYearHkBkw.setClose(bkwDaily.getClose());

				// 最高
				if (stkDayHk.getHigh() > StkYearHk.getHigh()) {
					StkYearHk.setHigh(stkDayHk.getHigh());
				}
				if (fwdDaily.getHigh() > StkYearHkFwd.getHigh()) {
					StkYearHkFwd.setHigh(fwdDaily.getHigh());
				}
				if (bkwDaily.getHigh() > StkYearHkBkw.getHigh()) {
					StkYearHkBkw.setHigh(bkwDaily.getHigh());
				}
				// 最低
				if (stkDayHk.getLow() != 0 && stkDayHk.getLow() < StkYearHk.getLow()) {
					StkYearHk.setLow(stkDayHk.getLow());
				}
				if (fwdDaily.getLow() != 0 && fwdDaily.getLow() < StkYearHkFwd.getLow()) {
					StkYearHkFwd.setLow(fwdDaily.getLow());
				}
				if (bkwDaily.getLow() != 0 && bkwDaily.getLow() < StkYearHkBkw.getLow()) {
					StkYearHkBkw.setLow(bkwDaily.getLow());
				}
				double volume = ArithmeticUtil.add(StkYearHk.getVolume(), stkDayHk.getVolume());
				StkYearHk.setVolume(volume);
				StkYearHkFwd.setVolume(volume);
				StkYearHkBkw.setVolume(volume);
				double turnover = ArithmeticUtil.add(StkYearHk.getTurnover(),
						stkDayHk.getTurnover());
				StkYearHk.setTurnover(turnover);
				StkYearHkFwd.setTurnover(turnover);
				StkYearHkBkw.setTurnover(turnover);
				
				Double yearTurnRate = StkYearHk.getTurnRate();
				Double dayTurnRate = stkDayHk.getTurnRate();
				if (yearTurnRate != null || dayTurnRate != null) {
					double turnRate = ArithmeticUtil.add(yearTurnRate == null ? 0 : yearTurnRate, dayTurnRate == null ? 0 : dayTurnRate);
					StkYearHk.setTurnRate(turnRate);
					StkYearHkFwd.setTurnRate(turnRate);
					StkYearHkBkw.setTurnRate(turnRate);
				}

				persistVO.getNonAdjUpdateList().add(StkYearHk);
				persistVO.getForwardAdjUpdateList().add(StkYearHkFwd);
				persistVO.getBackwardAdjUpdateList().add(StkYearHkBkw);
				
				quotContainer.setStkYear(StkYearHk);
				quotContainer.setStkYearFwd(StkYearHkFwd);
				quotContainer.setStkYearBkw(StkYearHkBkw);
			} else {
				// 不在同一年，新建一条年K，用当天的数据填充
				StkYearHk = new StkYearHk();
				BeanUtils.copyProperties(StkYearHk, stkDayHk);
				persistVO.getNonAdjSaveList().add(StkYearHk);

				StkYearHkFwd = new StkYearHkFwd();
				BeanUtils.copyProperties(StkYearHkFwd, fwdDaily);
				persistVO.getForwardAdjSaveList().add(StkYearHkFwd);

				StkYearHkBkw = new StkYearHkBkw();
				BeanUtils.copyProperties(StkYearHkBkw, bkwDaily);
				persistVO.getBackwardAdjSaveList().add(StkYearHkBkw);
				
				quotContainer.setStkYear(StkYearHk);
				quotContainer.setStkYearFwd(StkYearHkFwd);
				quotContainer.setStkYearBkw(StkYearHkBkw);
			}
		} else {
			// 无最近的年K记录，新建一条年K，用当天的数据填充
			StkYearHk StkYearHk = new StkYearHk();
			BeanUtils.copyProperties(StkYearHk, stkDayHk);
			persistVO.getNonAdjSaveList().add(StkYearHk);

			StkYearHkFwd StkYearHkFwd = new StkYearHkFwd();
			BeanUtils.copyProperties(StkYearHkFwd, fwdDaily);
			persistVO.getForwardAdjSaveList().add(StkYearHkFwd);

			StkYearHkBkw StkYearHkBkw = new StkYearHkBkw();
			BeanUtils.copyProperties(StkYearHkBkw, bkwDaily);
			persistVO.getBackwardAdjSaveList().add(StkYearHkBkw);
			
			quotContainer.setStkYear(StkYearHk);
			quotContainer.setStkYearFwd(StkYearHkFwd);
			quotContainer.setStkYearBkw(StkYearHkBkw);
		}
	}
	
	/**
	 * 计算日KDJ
	 * @param lstStkDay 最近9条日K记录，按时间倒序。此方法必须在K线保存完毕之后调用，故可保证输入的列表至少有一条记录
	 */
	private <T> void calcOneDayKDJ(List<T> lstStkDay) throws Exception {
		T stkCurr = lstStkDay.get(0);
		
		if (lstStkDay.size() == 1) {
			// 只有一条记录，新上市，默认100
			PropertyUtils.setProperty(stkCurr, "KVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "DVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "JVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "KValAdj", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "DValAdj", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "JValAdj", DEFAULT_KDJ_VAL);
			return;
		}
		
		double lowNormal = Double.MAX_VALUE;
		double highNormal = Double.MIN_VALUE;
		double lowAdj = Double.MAX_VALUE;
		double highAdj = Double.MIN_VALUE;
		
		for(int i = 0; i < lstStkDay.size() && i < 9; i++) {
			T stkOneDay = lstStkDay.get(i);
			if (stkOneDay == null) {
				continue;
			}
			Double lowOneDay = Double.class.cast(PropertyUtils.getProperty(stkOneDay, "low"));
			if (lowOneDay < lowNormal) {
				lowNormal = lowOneDay;
			}
			Double highOneDay = Double.class.cast(PropertyUtils.getProperty(stkOneDay, "high"));
			if (highOneDay > highNormal) {
				highNormal = highOneDay;
			}
			
			double af = Double.class.cast(PropertyUtils.getProperty(stkOneDay, "adjFactor"));
			double lowAdjOneDay = ArithmeticUtil.mul(lowOneDay, af);
			if (lowAdjOneDay < lowAdj) {
				lowAdj = lowAdjOneDay;
			}
			double highAdjOneDay = ArithmeticUtil.mul(highOneDay, af);
			if (highAdjOneDay > highAdj) {
				highAdj = highAdjOneDay;
			}
		}
		
		// 最高和最低相等，100
		if (highNormal == lowNormal) {
			PropertyUtils.setProperty(stkCurr, "KVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "DVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "JVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "KValAdj", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "DValAdj", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "JValAdj", DEFAULT_KDJ_VAL);
			return;
		}
		
		// 列表记录数小于1和等于1的情况都不会到这里
		T stkLastDay = lstStkDay.get(1);
		double lastK = 100;
		double lastKAdj = 100;
		if (stkLastDay != null && PropertyUtils.getProperty(stkLastDay, "KVal") != null) {
			lastK = Double.class.cast(PropertyUtils.getProperty(stkLastDay, "KVal"));
			lastKAdj = Double.class.cast(PropertyUtils.getProperty(stkLastDay, "KValAdj"));
		}
		double lastD = 100;
		double lastDAdj = 100;
		if (stkLastDay != null && PropertyUtils.getProperty(stkLastDay, "DVal")  != null) {
			lastD = Double.class.cast(PropertyUtils.getProperty(stkLastDay, "DVal"));
			lastDAdj = Double.class.cast(PropertyUtils.getProperty(stkLastDay, "DValAdj"));
		}
		
		double close = Double.class.cast(PropertyUtils.getProperty(stkCurr, "close"));
		double rsv = ArithmeticUtil.mul(ArithmeticUtil.div(ArithmeticUtil.sub(close, lowNormal), ArithmeticUtil.sub(highNormal, lowNormal)), 100);
		double k = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(2, lastK), rsv), 3);
		double d = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(2, lastD), k), 3);
		double j = ArithmeticUtil.sub(ArithmeticUtil.mul(3, k), ArithmeticUtil.mul(2, d));
		PropertyUtils.setProperty(stkCurr, "KVal", k);
		PropertyUtils.setProperty(stkCurr, "DVal", d);
		PropertyUtils.setProperty(stkCurr, "JVal", j);
		
		// 不复权的最高和最低不等，而复权后的最高和最低相等，也取默认值
		if (highAdj == lowAdj) {
			PropertyUtils.setProperty(stkCurr, "KValAdj", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "DValAdj", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "JValAdj", DEFAULT_KDJ_VAL);
		} else {
			double af = Double.class.cast(PropertyUtils.getProperty(stkCurr, "adjFactor"));
			double closeAdj = ArithmeticUtil.mul(close, af);
			double rsvAdj = ArithmeticUtil.mul(ArithmeticUtil.div(ArithmeticUtil.sub(closeAdj, lowAdj), ArithmeticUtil.sub(highAdj, lowAdj)), 100);
			double kAdj = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(2, lastKAdj), rsvAdj), 3);
			double dAdj = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(2, lastDAdj), kAdj), 3);
			double jAdj = ArithmeticUtil.sub(ArithmeticUtil.mul(3, kAdj), ArithmeticUtil.mul(2, dAdj));
			PropertyUtils.setProperty(stkCurr, "KValAdj", kAdj);
			PropertyUtils.setProperty(stkCurr, "DValAdj", dAdj);
			PropertyUtils.setProperty(stkCurr, "JValAdj", jAdj);
		}
	}
	
	/**
	 * 计算KDJ（非日K数据）
	 * @param lstHistQuot 最近9条K线记录，按时间倒序。此方法必须在K线保存完毕之后调用，故可保证输入的列表至少有一条记录
	 */
	private <T> void calcOtherKDJ(List<T> lstHistQuot) throws Exception {
		T stkCurr = lstHistQuot.get(0);
		
		if (lstHistQuot.size() == 1) {
			// 只有一条记录，新上市，默认100
			PropertyUtils.setProperty(stkCurr, "KVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "DVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "JVal", DEFAULT_KDJ_VAL);
			return;
		}
		
		double low = Double.MAX_VALUE;
		double high = Double.MIN_VALUE;
		
		for(int i = 0; i < lstHistQuot.size() && i < 9; i++) {
			T stkQuot = lstHistQuot.get(i);
			if (stkQuot == null) {
				continue;
			}
			Double lowOneDay = Double.class.cast(PropertyUtils.getProperty(stkQuot, "low"));
			if (lowOneDay < low) {
				low = lowOneDay;
			}
			Double highOneDay = Double.class.cast(PropertyUtils.getProperty(stkQuot, "high"));
			if (highOneDay > high) {
				high = highOneDay;
			}
		}
		
		// 最高和最低相等，100
		if (high == low) {
			PropertyUtils.setProperty(stkCurr, "KVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "DVal", DEFAULT_KDJ_VAL);
			PropertyUtils.setProperty(stkCurr, "JVal", DEFAULT_KDJ_VAL);
			return;
		}
		
		// 列表记录数小于1和等于1的情况都不会到这里
		T stkLast = lstHistQuot.get(1);
		double lastK = 100;
		if (stkLast != null && PropertyUtils.getProperty(stkLast, "KVal") != null) {
			lastK = Double.class.cast(PropertyUtils.getProperty(stkLast, "KVal"));
		}
		double lastD = 100;
		if (stkLast != null && PropertyUtils.getProperty(stkLast, "DVal")  != null) {
			lastD = Double.class.cast(PropertyUtils.getProperty(stkLast, "DVal"));
		}
		
		double close = Double.class.cast(PropertyUtils.getProperty(stkCurr, "close"));
		double rsv = ArithmeticUtil.mul(ArithmeticUtil.div(ArithmeticUtil.sub(close, low), ArithmeticUtil.sub(high, low)), 100);
		double k = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(2, lastK), rsv), 3);
		double d = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(2, lastD), k), 3);
		double j = ArithmeticUtil.sub(ArithmeticUtil.mul(3, k), ArithmeticUtil.mul(2, d));
		PropertyUtils.setProperty(stkCurr, "KVal", k);
		PropertyUtils.setProperty(stkCurr, "DVal", d);
		PropertyUtils.setProperty(stkCurr, "JVal", j);
	}
	
	/**
	 * 计算MACD数据
	 * @param lstHistQuot
	 * @throws Exception
	 */
	private <T> void calcMACDField(List<T> lstHistQuot) throws Exception {
		T currQuot = lstHistQuot.get(0);
		
		if (lstHistQuot.size() == 1) {
			// 只有一条记录，为新股上市
			// 上市第一条记录，ema1、ema2、dea都取0
			PropertyUtils.setProperty(currQuot, "ema1", DEFAULT_TECH_IDCT_ZERO);
			PropertyUtils.setProperty(currQuot, "ema2", DEFAULT_TECH_IDCT_ZERO);
			PropertyUtils.setProperty(currQuot, "dea", DEFAULT_TECH_IDCT_ZERO);
			// 日K需要处理复权
			if (currQuot.getClass() == StkDayHk.class) {
				PropertyUtils.setProperty(currQuot, "ema1Bkw", DEFAULT_TECH_IDCT_ZERO);
				PropertyUtils.setProperty(currQuot, "ema2Bkw", DEFAULT_TECH_IDCT_ZERO);
				PropertyUtils.setProperty(currQuot, "deaBkw", DEFAULT_TECH_IDCT_ZERO);
			}
		} else {
			T prevQuot = lstHistQuot.get(1);
			double currPrice = Double.class.cast(PropertyUtils.getProperty(currQuot, "close"));
			double prevClose = Double.class.cast(PropertyUtils.getProperty(prevQuot, "close"));
			double prevEma1 = Double.class.cast(PropertyUtils.getProperty(prevQuot, "ema1"));
			if (prevEma1 == 0) {
				// 上市第二条记录，ema1取上市第一条记录收盘价
				prevEma1 = prevClose;
			}
			double prevEma2 = Double.class.cast(PropertyUtils.getProperty(prevQuot, "ema2"));
			if (prevEma2 == 0) {
				// 上市第二条记录，ema2取上市第一条记录收盘价
				prevEma2 = prevClose;
			}
			double prevDea = Double.class.cast(PropertyUtils.getProperty(prevQuot, "dea"));
			
			// 今日ema1 = 昨日ema1 * 11/13 + 现价 * 2/13 = (昨日ema1 * 11 +  现价 * 2) / 13
			double ema1 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevEma1, 11), ArithmeticUtil.mul(currPrice, 2)), 13);
			// 今日ema2 = 昨日ema2 * 25/27 + 现价 * 2/27 = (昨日ema2 * 25 +  现价 * 2) / 27
			double ema2 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevEma2, 25), ArithmeticUtil.mul(currPrice, 2)), 27);
			// 今日dif = ema1 - ema2
			double dif = ArithmeticUtil.sub(ema1, ema2);
			// 今日dea = 昨日dea * 8/10 + 今日dif * 2/10 = (昨日dea * 4 +　今日dif) / 5
			double dea = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDea, 4), dif), 5);
			
			PropertyUtils.setProperty(currQuot, "ema1", ema1);
			PropertyUtils.setProperty(currQuot, "ema2", ema2);
			PropertyUtils.setProperty(currQuot, "dea", dea);
			
			// 日K还要处理复权
			if (currQuot.getClass() == StkDayHk.class) {
				double adjFactor = Double.class.cast(PropertyUtils.getProperty(currQuot, "adjFactor"));
				double currPriceBkw = ArithmeticUtil.mul(Double.class.cast(PropertyUtils.getProperty(currQuot, "close")), adjFactor);
				double currPrevCloseBkw = ArithmeticUtil.mul(Double.class.cast(PropertyUtils.getProperty(currQuot, "prevClose")), adjFactor);
				double prevEma1Bkw = Double.class.cast(PropertyUtils.getProperty(prevQuot, "ema1Bkw"));
				if (prevEma1Bkw == 0) {
					prevEma1Bkw = currPrevCloseBkw;
				}
				double prevEma2Bkw = Double.class.cast(PropertyUtils.getProperty(prevQuot, "ema2Bkw"));
				if (prevEma2Bkw == 0) {
					prevEma2Bkw = currPrevCloseBkw;
				}
				double prevDeaBkw = Double.class.cast(PropertyUtils.getProperty(prevQuot, "deaBkw"));
				
				// 今日ema1 = 昨日ema1 * 11/13 + 现价 * 2/13 = (昨日ema1 * 11 +  现价 * 2) / 13
				double ema1Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevEma1Bkw, 11), ArithmeticUtil.mul(currPriceBkw, 2)), 13);
				// 今日ema2 = 昨日ema2 * 25/27 + 现价 * 2/27 = (昨日ema2 * 25 +  现价 * 2) / 27
				double ema2Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevEma2Bkw, 25), ArithmeticUtil.mul(currPriceBkw, 2)), 27);
				// 今日dif = ema1 - ema2
				double difBkw = ArithmeticUtil.sub(ema1Bkw, ema2Bkw);
				// 今日dea = 昨日dea * 8/10 + 今日dif * 2/10 = (昨日dea * 4 +　今日dif) / 5
				double deaBkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDeaBkw, 4), difBkw), 5);
				
				PropertyUtils.setProperty(currQuot, "ema1Bkw", ema1Bkw);
				PropertyUtils.setProperty(currQuot, "ema2Bkw", ema2Bkw);
				PropertyUtils.setProperty(currQuot, "deaBkw", deaBkw);
			}
		}
	}
	
	/**
	 * 计算RSI数据
	 * @param lstHistQuot
	 * @throws Exception
	 */
	private <T> void calcRSIField(List<T> lstHistQuot, String assetId) throws Exception {
		T currQuot = lstHistQuot.get(0);
		
		// 上市第一条记录，昨日序列值为0
		double prevUpSeq1 = 0;
		double prevDownSeq1 = 0;
		double prevUpSeq2 = 0;
		double prevDownSeq2 = 0;
		double prevUpSeq3 = 0;
		double prevDownSeq3 = 0;
		double prevUpSeq1Bkw = 0;
		double prevDownSeq1Bkw = 0;
		double prevUpSeq2Bkw = 0;
		double prevDownSeq2Bkw = 0;
		double prevUpSeq3Bkw = 0;
		double prevDownSeq3Bkw = 0;
		
		// 不是上市第一条，直接取昨日序列值
		// 上市后前若干天因为特殊处理为负数，所以需要取绝对值
		if (lstHistQuot.size() > 1) {
			T prevQuot = lstHistQuot.get(1);
			prevUpSeq1 = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "upSeq1")));
			prevDownSeq1 = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "downSeq1")));
			prevUpSeq2 = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "upSeq2")));
			prevDownSeq2 = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "downSeq2")));
			prevUpSeq3 = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "upSeq3")));
			prevDownSeq3 = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "downSeq3")));
			
			// 日K还要处理复权
			if (currQuot.getClass() == StkDayHk.class) {
				prevUpSeq1Bkw = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "upSeq1Bkw")));
				prevDownSeq1Bkw = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "downSeq1Bkw")));
				prevUpSeq2Bkw = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "upSeq2Bkw")));
				prevDownSeq2Bkw = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "downSeq2Bkw")));
				prevUpSeq3Bkw = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "upSeq3Bkw")));
				prevDownSeq3Bkw = Math.abs(Double.class.cast(PropertyUtils.getProperty(prevQuot, "downSeq3Bkw")));
			}
		}
		
		double currPrice = Double.class.cast(PropertyUtils.getProperty(currQuot, "close"));
		double currPrevClose = Double.class.cast(PropertyUtils.getProperty(currQuot, "prevClose"));
		double chg = ArithmeticUtil.sub(currPrice, currPrevClose);
		double upChg = chg > 0 ? chg : 0;
		double downChg = chg < 0 ? Math.abs(chg) : 0;
		// 涨(跌)幅序列 = 昨日序列 * (N-1)/N + 今日涨(跌) / N = (昨日序列 * (N-1) + 今日涨(跌) ) / N
		
		double upSeq1 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevUpSeq1, 5), upChg), 6);
		double downSeq1 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDownSeq1, 5), downChg), 6);
		double upSeq2 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevUpSeq2, 11), upChg), 12);
		double downSeq2 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDownSeq2, 11), downChg), 12);
		double upSeq3 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevUpSeq3, 23), upChg), 24);
		double downSeq3 = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDownSeq3, 23), downChg), 24);
		
		PropertyUtils.setProperty(currQuot, "upSeq1", upSeq1);
		PropertyUtils.setProperty(currQuot, "downSeq1", downSeq1);
		PropertyUtils.setProperty(currQuot, "upSeq2", upSeq2);
		PropertyUtils.setProperty(currQuot, "downSeq2", downSeq2);
		PropertyUtils.setProperty(currQuot, "upSeq3", upSeq3);
		PropertyUtils.setProperty(currQuot, "downSeq3", downSeq3);
		
		// 上市后的第6、12、24条记录才开始有RSI，后台数据使用负数做标记
		if (lstHistQuot.size() < 6) {
			PropertyUtils.setProperty(currQuot, "upSeq1", ArithmeticUtil.mul(upSeq1, -1));
			PropertyUtils.setProperty(currQuot, "downSeq1", ArithmeticUtil.mul(upSeq1, -1));
		}
		if (lstHistQuot.size() < 12) {
			PropertyUtils.setProperty(currQuot, "upSeq2", ArithmeticUtil.mul(upSeq2, -1));
			PropertyUtils.setProperty(currQuot, "downSeq2", ArithmeticUtil.mul(upSeq2, -1));
		}
		if (lstHistQuot.size() < 24) {
			PropertyUtils.setProperty(currQuot, "upSeq3", ArithmeticUtil.mul(upSeq3, -1));
			PropertyUtils.setProperty(currQuot, "downSeq3", ArithmeticUtil.mul(upSeq3, -1));
		}
		
		// 日K还要处理复权
		if (currQuot.getClass() == StkDayHk.class) {
			double adjFactor = Double.class.cast(PropertyUtils.getProperty(currQuot, "adjFactor"));
			
			// 当日的收盘减去当日昨收所计算出来的涨跌额，可以直接复权，如果是减去昨日的收盘，则需要先分别复权
			double upChgBkw = ArithmeticUtil.mul(upChg, adjFactor);
			double downChgBkw = ArithmeticUtil.mul(downChg, adjFactor);
			double upSeq1Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevUpSeq1Bkw, 5), upChgBkw), 6);
			double downSeq1Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDownSeq1Bkw, 5), downChgBkw), 6);
			double upSeq2Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevUpSeq2Bkw, 11), upChgBkw), 12);
			double downSeq2Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDownSeq2Bkw, 11), downChgBkw), 12);
			double upSeq3Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevUpSeq3Bkw, 23), upChgBkw), 24);
			double downSeq3Bkw = ArithmeticUtil.div(ArithmeticUtil.add(ArithmeticUtil.mul(prevDownSeq3Bkw, 23), downChgBkw), 24);
			PropertyUtils.setProperty(currQuot, "upSeq1Bkw", upSeq1Bkw);
			PropertyUtils.setProperty(currQuot, "downSeq1Bkw", downSeq1Bkw);
			PropertyUtils.setProperty(currQuot, "upSeq2Bkw", upSeq2Bkw);
			PropertyUtils.setProperty(currQuot, "downSeq2Bkw", downSeq2Bkw);
			PropertyUtils.setProperty(currQuot, "upSeq3Bkw", upSeq3Bkw);
			PropertyUtils.setProperty(currQuot, "downSeq3Bkw", downSeq3Bkw);
			
			// 上市后的第6、12、24条记录才开始有RSI，后台数据使用负数做标记
			if (lstHistQuot.size() < 6) {
				PropertyUtils.setProperty(currQuot, "upSeq1Bkw", ArithmeticUtil.mul(upSeq1Bkw, -1));
				PropertyUtils.setProperty(currQuot, "downSeq1Bkw", ArithmeticUtil.mul(downSeq1Bkw, -1));
			}
			if (lstHistQuot.size() < 12) {
				PropertyUtils.setProperty(currQuot, "upSeq2Bkw", ArithmeticUtil.mul(upSeq2Bkw, -1));
				PropertyUtils.setProperty(currQuot, "downSeq2Bkw", ArithmeticUtil.mul(downSeq2Bkw, -1));
			}
			if (lstHistQuot.size() < 24) {
				PropertyUtils.setProperty(currQuot, "upSeq3Bkw", ArithmeticUtil.mul(upSeq3Bkw, -1));
				PropertyUtils.setProperty(currQuot, "downSeq3Bkw", ArithmeticUtil.mul(downSeq3Bkw, -1));
			}
		}
	}
	
	/**
	 * 添加技术指标任务
	 * @param tasks
	 * @param shContainer
	 * @param date
	 */
	private void addHkTechnicalIndicatorTask(List<Callable<Void>> tasks, final StkHkPersistContainer hkContainer, final String date) {
		for (Entry<String, StkHkQuotContainer> entry : hkContainer.getQuotContainer().entrySet()) {
			final String assetId = entry.getKey();
			final StkHkQuotContainer quotContainer = entry.getValue();

			// 注意：取历史数据必须要24条，用于确定RSI上市初不返回的情况
			
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkDayHk> lstStkDay = stkHisQuoteDao.findStkDayHkBeforeTime(assetId, date, 24);
						lstStkDay.add(0, quotContainer.getStkDay());
						calcOneDayKDJ(lstStkDay);
						calcMACDField(lstStkDay);
						calcRSIField(lstStkDay, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算日技术指标出错，assetId=" + assetId);
						throw new RuntimeException("计算日技术指标出错，assetId=" + assetId, e);
					}
				}
			});
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkWeekHk> lstStkWeek = stkHisQuoteDao.findStkWeekHkBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkWeek(), "weekId", lstStkWeek);
						calcOtherKDJ(lstStkWeek);
						calcMACDField(lstStkWeek);
						calcRSIField(lstStkWeek, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算周技术指标（不复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算周技术指标（不复权）出错，assetId=" + assetId, e);
					}
				}
			});
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkWeekHkFwd> lstStkWeekFwd = stkHisQuoteDao.findStkWeekHkFwdBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkWeekFwd(), "weekId", lstStkWeekFwd);
						calcOtherKDJ(lstStkWeekFwd);
						calcMACDField(lstStkWeekFwd);
						calcRSIField(lstStkWeekFwd, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算周技术指标（前复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算周技术指标（前复权）出错，assetId=" + assetId, e);
					}
				}
			});
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkWeekHkBkw> lstStkWeekBkw = stkHisQuoteDao.findStkWeekHkBkwBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkWeekBkw(), "weekId", lstStkWeekBkw);
						calcOtherKDJ(lstStkWeekBkw);
						calcMACDField(lstStkWeekBkw);
						calcRSIField(lstStkWeekBkw, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算周技术指标（后复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算周技术指标（后复权）出错，assetId=" + assetId, e);
					}
				}
			});

			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkMonthHk> lstStkMonth = stkHisQuoteDao.findStkMonthHkBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkMonth(), "monthId", lstStkMonth);
						calcOtherKDJ(lstStkMonth);
						calcMACDField(lstStkMonth);
						calcRSIField(lstStkMonth, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算月技术指标（不复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算月技术指标（不复权）出错，assetId=" + assetId, e);
					}
				}
			});

			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkMonthHkFwd> lstStkMonthFwd = stkHisQuoteDao.findStkMonthHkFwdBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkMonthFwd(), "monthId", lstStkMonthFwd);
						calcOtherKDJ(lstStkMonthFwd);
						calcMACDField(lstStkMonthFwd);
						calcRSIField(lstStkMonthFwd, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算月技术指标（前复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算月技术指标（前复权）出错，assetId=" + assetId, e);
					}
				}
			});

			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkMonthHkBkw> lstStkMonthBkw = stkHisQuoteDao.findStkMonthHkBkwBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkMonthBkw(), "monthId", lstStkMonthBkw);
						calcOtherKDJ(lstStkMonthBkw);
						calcMACDField(lstStkMonthBkw);
						calcRSIField(lstStkMonthBkw, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算月技术指标（后复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算月技术指标（后复权）出错，assetId=" + assetId, e);
					}
				}
			});

			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkYearHk> lstStkYear = stkHisQuoteDao.findStkYearHkBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkYear(), "yearId", lstStkYear);
						calcOtherKDJ(lstStkYear);
						calcMACDField(lstStkYear);
						calcRSIField(lstStkYear, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算年技术指标（不复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算年技术指标（不复权）出错，assetId=" + assetId, e);
					}
				}
			});

			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkYearHkFwd> lstStkYearFwd = stkHisQuoteDao.findStkYearHkFwdBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkYearFwd(), "yearId", lstStkYearFwd);
						calcOtherKDJ(lstStkYearFwd);
						calcMACDField(lstStkYearFwd);
						calcRSIField(lstStkYearFwd, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算年技术指标（前复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算年技术指标（前复权）出错，assetId=" + assetId, e);
					}
				}
			});

			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						List<StkYearHkBkw> lstStkYearBkw = stkHisQuoteDao.findStkYearHkBkwBeforeTime(assetId, date, 24);
						configureTargetList(quotContainer.getStkYearBkw(), "yearId", lstStkYearBkw);
						calcOtherKDJ(lstStkYearBkw);
						calcMACDField(lstStkYearBkw);
						calcRSIField(lstStkYearBkw, assetId);
						return null;
					} catch (Exception e) {
						LOG.error("计算年技术指标（后复权）出错，assetId=" + assetId);
						throw new RuntimeException("计算年技术指标（后复权）出错，assetId=" + assetId, e);
					}
				}
			});
		}
	}
	
	/**
	 * 在目标列表中检查是否有与给定对象的给定属性相等的对象，有则替换首记录，无则插入到首记录
	 * @param candidate 检查的目标对象
	 * @param checkField 检查的属性名
	 * @param targetList 目标列表
	 * @throws Exception
	 */
	private <T> void configureTargetList(T candidate, String checkField, List<T> targetList) throws Exception {
		boolean needToReplace = false;
		Object valCandidate = PropertyUtils.getProperty(candidate, checkField);
		if (valCandidate != null) {
			for (T stk : targetList) {
				Object valStk = PropertyUtils.getProperty(stk, checkField);
				if (valStk != null && valStk.equals(valCandidate)) {
					// 对应字段相等才替换，否则增加
					needToReplace = true;
				}
			}
		}
		if (needToReplace) {
			targetList.set(0, candidate);
		} else {
			targetList.add(0, candidate);
		}
	}
	
	/**
	 * 保存数据
	 * @param data
	 */
	private <N extends Serializable, F extends Serializable, B extends Serializable> void saveHisQuotData(StkBasePersistContainer<N, F, B> data) {
		if (CollectionUtils.isNotEmpty(data.getNonAdjSaveList())) {
			LOG.debug("保存数据...");
			quoteHisService.storeList(data.getNonAdjSaveList(), 500);
			quoteHisService.storeList(data.getForwardAdjSaveList(), 500);
			quoteHisService.storeList(data.getBackwardAdjSaveList(), 500);
		}
		if (CollectionUtils.isNotEmpty(data.getNonAdjUpdateList())) {
			LOG.debug("更新数据...");
			quoteHisService.updateListWithinTrans(data.getNonAdjUpdateList());
			quoteHisService.updateListWithinTrans(data.getForwardAdjUpdateList());
			quoteHisService.updateListWithinTrans(data.getBackwardAdjUpdateList());
		}
	}

}
