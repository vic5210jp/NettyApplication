/**
 * @Title: MktServerServiceImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.impl;

import static com.yiqiniu.mktserver.util.Constants.ASSET_SUFFIX_HK;
import static com.yiqiniu.mktserver.util.Constants.MARKET_HK;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yiqiniu.api.mktinfo.util.AggregateOrderBook;
import com.yiqiniu.api.mktinfo.util.AggregateOrderBookEntry;
import com.yiqiniu.api.mktinfo.util.AskSideOrderBook;
import com.yiqiniu.api.mktinfo.util.BidSideOrderBook;
import com.yiqiniu.api.mktinfo.util.InnerOrderBook;
import com.yiqiniu.api.mktinfo.vo.HkCurrencyRateVO;
import com.yiqiniu.api.mktinfo.vo.SecType;
import com.yiqiniu.api.mktinfo.vo.StkInfo;
import com.yiqiniu.api.mktinfo.vo.enums.EAction;
import com.yiqiniu.api.mktinfo.vo.enums.EHkCurrencyType;
import com.yiqiniu.api.mktinfo.vo.enums.EHkSubMarket;
import com.yiqiniu.api.mktserver.HkQuotMsg;
import com.yiqiniu.api.mktserver.HkTsData;
import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.common.utils.ArithmeticUtil;
import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.common.utils.DateUtils.TimeFormatter;
import com.yiqiniu.common.utils.QuoteUtil;
import com.yiqiniu.mktinfo.persist.po.HkStkSusp;
import com.yiqiniu.mktserver.control.MktControlService;
import com.yiqiniu.mktserver.dao.SuspDao;
import com.yiqiniu.mktserver.listener.QuotListener;
import com.yiqiniu.mktserver.listener.event.QuotEvent;
import com.yiqiniu.mktserver.service.MktJobService;
import com.yiqiniu.mktserver.service.MktServerService;
import com.yiqiniu.mktserver.service.SynchCacheService;
import com.yiqiniu.mktserver.service.TimeShareGenerateService;
import com.yiqiniu.mktserver.service.vo.InnerAggregateOrderBookEntry;
import com.yiqiniu.mktserver.util.ByteBufferUtil;
import com.yiqiniu.mktserver.util.Constants;
import com.yiqiniu.mktserver.util.Constants.EHkAggregateOrderSide;
import com.yiqiniu.mktserver.util.Constants.EHkAggregateOrderUpdateAction;
import com.yiqiniu.mktserver.util.Constants.EHkIndexStatus;
import com.yiqiniu.mktserver.util.Constants.EHkInstrumentType;
import com.yiqiniu.mktserver.util.Constants.EHkQuotMsgType;
import com.yiqiniu.mktserver.util.Constants.EHkSecurityTradingStatus;
import com.yiqiniu.mktserver.util.Constants.EHkTickerTrdType;
import com.yiqiniu.mktserver.util.Constants.EHkTradingSessionStatus;
import com.yiqiniu.mktserver.util.LocalCache;
import com.yiqiniu.mktserver.util.OrderbookManageUtil;
import com.yiqiniu.odps.po.Pair;
import com.yiqiniu.odps.service.RedisMapService;
import com.yiqiniu.protocol.MktStaticType.StkStatusType;

/**
 * @description 港股行情转码机服务实现
 *
 * @author 余俊斌
 * @date 2015年11月16日 下午4:17:50
 * @version v1.0
 */
@Service
public class MktServerServiceImpl implements MktServerService {
	
	/**
	 * 港交所行情数据中64位整型对应的空值
	 */
	public static final long HKEX_INT64_NULL = 0x8000000000000000L;
	
	private Logger LOG = LoggerFactory.getLogger(getClass());

	/**
	 * 指数每手的数量
	 */
	public static final int INDEX_LOT_SIZE = 1; // XXX 暂定1

	/**
	 * ASCII encoding
	 */
	private Charset asciiEncoding = Charset.forName("ASCII");
	
	private Properties indexCodeForAssetId;
	
	/**
	 * 用于注册和删除行情变更监听器
	 */
	private List<QuotListener> quotListeners = new ArrayList<QuotListener>();
	
	/**
	 * 是否处于正常接收状态
	 */
	private boolean isInNormalState = true;
	/**
	 * 判断进入恢复模式的时间差阈值，单位：毫秒
	 */
	private long recoverTimeSpanThreshold = 0;
	/**
	 * 最近缓存更新时间
	 */
	private long updateCacheTime = 0;
	/**
	 * 交易时段开始时间（HH:mm:ss）
	 */
	private String rtTimeAreaBegin;
	/**
	 * 交易时段结束时间（HH:mm:ss）
	 */
	private String rtTimeAreaEnd;
	
	@Resource
	private DefaultConfig defaultConfig;
	@Resource
	private RedisMapService redisMapService;
	@Resource
	private MktJobService mktJobService;
	@Resource
	private SuspDao suspDao;
	@Resource
	private TimeShareGenerateService tsGenService;
	@Resource
	private MktControlService mktControlService;
	@Resource
	private SynchCacheService synchCacheService;
	
	@PostConstruct
	public void inititate() throws Exception {
		mktJobService.preLoadData();
		recoverTimeSpanThreshold = defaultConfig.getLong("mktserver.recover.timeThreshold");
		
		indexCodeForAssetId = new Properties();
		indexCodeForAssetId.load(getClass().getClassLoader().getResourceAsStream("indexCodeForAsset.properties"));
		
		String rtTimeArea = defaultConfig.getVal("rt.timeArea");
		String[] aRtTimeArea = rtTimeArea.split("\\|");
		rtTimeAreaBegin = aRtTimeArea[0];
		rtTimeAreaEnd = aRtTimeArea[1];
		
		if (MapUtils.isEmpty(LocalCache.HK_TS_DATA)) {
			LocalCache.HK_TS_DATA.putAll(redisMapService.findAllObject(HkTsData.class));
		}
		
		if (MapUtils.isEmpty(LocalCache.AGGREGATE_ORDER_BOOK_MAP)) {
			LocalCache.AGGREGATE_ORDER_BOOK_MAP.putAll(redisMapService.findAllObject(AggregateOrderBook.class));
		}
	}
	
	@Override
	//@Transactional(value="transactionManager")
	public void processReceivedQuotMsg(HkQuotMsg receivedMsg) {
		long msgTimeMills = receivedMsg.getSendTime() / 1000000;
		long currentTimeMillis = System.currentTimeMillis();
		
		//long start1 = System.nanoTime();
		if (isInNormalState) {
			// 正常模式下，当前机器时间在消息时间之后，且时间差超过配置的阈值，则认为正在异常恢复，进入恢复模式
			if (currentTimeMillis - msgTimeMills > recoverTimeSpanThreshold) {
				isInNormalState = false;
			}
		} else {
			// 如已经处于恢复模式，且时间差小于阈值时，解除恢复模式
			if (Math.abs(currentTimeMillis - msgTimeMills) < recoverTimeSpanThreshold) {
				isInNormalState = true;
			}
		}
		// 必须先触发生成分时，因为后面处理可能修改行情时间，导致分时处理逻辑更加复杂
		tsGenService.generateTimeShare(msgTimeMills, isInNormalState);
		//long start2 = System.nanoTime();
		//LOG.info("++++++++++++++完成生成分时,耗时"+(start2-start1) * 1.0/1000000+"ms+++++++++++++++++");
		//每隔固定时间同步本地缓存到redis
		long updatePeriod = defaultConfig.getLong("synch.cache.period");
		if (msgTimeMills - updateCacheTime > updatePeriod) {
			// 同步本地缓存到redis
			synchCacheService.synchCache();
			updateCacheTime = msgTimeMills;
			// 如果是正常状态，且在交易时段，则推送行情
			if (isInNormalState && isInRtTimeArea(msgTimeMills)) {
				notifyListener(msgTimeMills, Constants.MARKET_HK, EAction.HQUOTUPDATED);
			}
		}
		//long start3 = System.nanoTime();
		//LOG.info("++++++++++++++完成同步redis,耗时"+(start3-start2) * 1.0/1000000+"ms+++++++++++++++++");
		// 对于每天第一次收到消息，记录任务日志
		if (!mktControlService.getJobLogRecordFlag()) {
			mktControlService.recordJobLog(msgTimeMills);
		}
		//long start4 = System.nanoTime();
		//LOG.info("++++++++++++++完成记录任务日志,耗时"+(start4-start3) * 1.0/1000000+"ms+++++++++++++++++");
		long start = System.nanoTime();
		
		byte[] rawBody = receivedMsg.getBody();
		// 心跳没有消息体
		if (rawBody == null) {
			return;
		}
		ByteBuffer msgBody = ByteBuffer.wrap(rawBody);
		
		
		msgBody.order(ByteOrder.LITTLE_ENDIAN);
		
		int iDeclaredMsgSize = msgBody.getShort(0) & 0xFFFF;
		if (iDeclaredMsgSize != msgBody.remaining()) {
			throw new RuntimeException("消息体的实际可读长度[" + msgBody.remaining() + "]与消息所声明的长度[" + iDeclaredMsgSize + "]不一致");
		}
		ByteBufferUtil.skip(msgBody, 2);
		
		EHkQuotMsgType msgType = EHkQuotMsgType.valueOf(ByteBufferUtil.getUnsignedShort(msgBody));
		switch (msgType) {
		case SECURITY_DEFINITION:
			processSecurityDef(msgBody, msgTimeMills);
			break;
		case CURRENCY_RATE:
			processCurrencyRate(msgBody, msgTimeMills);
			break;
		case TRADING_SESSION_STATUS:
			processTradingSessionStatus(msgBody);
			break;
		case SECURITY_STATUS:
			processSecurityStatus(msgBody, msgTimeMills);
			break;
		case AGGREGATE_ORDER_BOOK_UPDATE:
			processAggregateOrderBook(msgBody, msgTimeMills);
			break;
		case TRADE_TICKER:
			processTradeTicker(msgBody, msgTimeMills);
			break;
		case CLOSING_PRICE:
			processClosingPrice(msgBody, msgTimeMills);
			break;
		case NOMINAL_PRICE:
			processNominalPrice(msgBody, msgTimeMills);
			break;
		case INDICATIVE_EQUILIBRIUM_PRICE:
			processIEP(msgBody, msgTimeMills);
			break;
		case STATISTICS:
			processStatics(msgBody, msgTimeMills);
			break;
		case INDEX_DEFINITION:
			processIndexDef(msgBody, msgTimeMills);
			break;
		case INDEX_DATA:
			processIndexDat(msgBody, msgTimeMills);
			break;
			
			
		default:
			LOG.debug("ignore message with type {}", msgType);
			break;
		}
		
		long end = System.nanoTime();
		double timeElapse = (end-start) * 1.0/1000000;
		if (timeElapse > 1.0) {
			LOG.info("处理时间偏长，消息时间：{}，耗时：{}ms，类型:{}", DateUtils.dateToString(msgTimeMills, "yyyy-MM-dd HH:mm:ss.SSS"), timeElapse, msgType);
		}
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
	 * process Security Definition (11) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processSecurityDef(ByteBuffer msgBody, long msgTime) {
		// 取本地缓存中的证券信息
		Map<String, StkInfo> stockMap = LocalCache.STOCK_MAP;
		// SecurityCode(4)
		String strStkCode = extractHkStockCode(msgBody);
		String assetId = strStkCode + ASSET_SUFFIX_HK;
		// 不支持的证券，直接返回
		// 对于不支持的类型，在这里直接结束，不会进入后续流程
		if (!isSecuritySupported(assetId)) {
			return;
		}

		// 判断支持用的是stockMap，如果支持，取出来的值一定非空
		StkInfo stkInfo = stockMap.get(assetId);
		Date msgDate = new Date(msgTime);
		String strMsgDate = DateUtils.dateToString(msgDate, TimeFormatter.YYYY_MM_DD);
		String strMsgTime = DateUtils.dateToString(msgDate, TimeFormatter.HH_MM_SS);
		
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);
		if (quot == null) {
			quot = new HkTsData();
			quot.setCorpCode(stkInfo.getCorpCode());
			quot.setAssetId(assetId);
			quot.setStkCode(stkInfo.getStkCode());
			quot.setStkName(stkInfo.getStkName());
			quot.setSecType(stkInfo.getSecType());
			quot.setSecSType(stkInfo.getSecSType());
			if (stkInfo.getListingDate() != null && strMsgDate.compareTo(stkInfo.getListingDate()) == 0) {
				quot.setStatus(StkStatusType.DURING_IPO);
			}
		}
		quot.setUpdateTime(msgTime);
		quot.setDate(strMsgDate);
		quot.setTime(strMsgTime);
		
		// MarketCode(4)
		byte[] rawMarketCode = new byte[4];
		msgBody.get(rawMarketCode);
		String strMarketCode = new String(rawMarketCode, asciiEncoding).trim();
		EHkSubMarket subMarket = EHkSubMarket.valueOf(strMarketCode);
		quot.setSubMarket(subMarket);
		
		// ISINCode(12) 
		ByteBufferUtil.skip(msgBody, 12);
		
		// InstrumentType(4)
		byte[] rawInstrumentType = new byte[4];
		msgBody.get(rawInstrumentType);
		String strInstrumentType = new String(rawInstrumentType, asciiEncoding).trim();
		EHkInstrumentType instrumentType = EHkInstrumentType.valueOf(strInstrumentType);
		
		// SpreadTableCode(2)
		ByteBufferUtil.skip(msgBody, 2);
		
		// SecurityShortName(40) XXX ignored
		byte[] rawSecurityShortName = new byte[40];
		msgBody.get(rawSecurityShortName);
		String strSecurityShortName = new String(rawSecurityShortName, asciiEncoding).trim();
		LOG.info("SecurityShortName for {}:{}", assetId, strSecurityShortName);
		// CurrencyCode(3)
		byte[] rawCurrencyCode = new byte[3];
		msgBody.get(rawCurrencyCode);
		String strCurrencyCode = new String(rawCurrencyCode, asciiEncoding).trim();
		quot.setCurrency(EHkCurrencyType.valueOf(strCurrencyCode));
		// SecurityNameGCCS(60) XXX ignored
		String strSecurityNameGCCS = ByteBufferUtil.getFullTrimUnicodeString(msgBody, 60);
		LOG.info("SecurityNameGCCS for {}:{}", assetId, strSecurityNameGCCS);
		// SecurityNameGB(60) XXX ignored
		String strSecurityNameGB = ByteBufferUtil.getFullTrimUnicodeString(msgBody, 60);
		LOG.info("SecurityNameGB for {}:{}", assetId, strSecurityNameGB);
		// LotSize(4) Uint32 - 每手数量在一帮情况下不会超出带符号的32位，故此处不考虑无符号
		int lotSize = msgBody.getInt();
		quot.setLotSize(lotSize);
		// PreviousClosingPrice(4) Int32 - 3 implied decimal places
		double prevClose = ByteBufferUtil.getDoubleFromInt(msgBody, 3);
		if (stkInfo.getSecType() == SecType.SHR_WARRANT || stkInfo.getSecType() == SecType.BOND) {
			// XXX 权证和债券暂不处理，交易所推送什么就设成什么
			quot.setPrevClose(prevClose);
		} else {
			// 股票、基金、指数，有判断逻辑
			// 港交所的昨收是有为0的情况：新股上市或者除权价不能厘定
			if (prevClose == 0) {
				if (strMsgDate.equals(stkInfo.getListingDate())) {
					// 上市首日，以发行价作为昨收
					Double issuePrice = stkInfo.getIssuePrice();
					if (issuePrice != null && issuePrice != 0) {
						// 有发行价，则取作上市首日的昨收
						quot.setPrevClose(issuePrice);
					} else {
						// 发行价不对，报错并取开盘价，这里只要置为0，在设置开盘价的消息中处理价格
						LOG.error("{}上市首日，但资讯数据中没有对应的发行价！将以开盘价作为昨收！", assetId);
						quot.setPrevClose(0);
					}
				} else {
					// 不是上市首日，则因除权价不能厘定而为0，取上一日的收盘价作为昨收
					// 因为同一证券在同一天可能收到多次此消息，但只在盘前收到时设置即可，而此时的最新价其实是上一日的收盘
					if (strMsgTime.compareTo(Constants.OPEN_TIME) < 0) {
						quot.setPrevClose(quot.getPrice());
					}
				}
			} else {
				// 昨收不为0，则取为当日昨收
				quot.setPrevClose(prevClose);
			}
		}
		
		int ignoredLength = 0;
		// VCMFlag(1)
		ignoredLength += 1;
		// ShortSellFlag(1)
		ignoredLength += 1;
		// CASFlag(1)
		ignoredLength += 1;
		// CCASSFlag(1)
		ignoredLength += 1;
		// DummySecurityFlag(1)
		ignoredLength += 1;
		// TestSecurityFlag(1)
		ignoredLength += 1;
		// StampDutyFlag(1)
		ignoredLength += 1;
		// Filler(1)
		ignoredLength += 1;
		// ListingDate(4)
		ignoredLength += 4;
		// DelistingDate(4)
		ignoredLength += 4;
		// FreeText(38)
		ignoredLength += 38;
		ByteBufferUtil.skip(msgBody, ignoredLength);
		
		// Bonds Specific Data
		if (instrumentType == EHkInstrumentType.BOND) {
			// XXX 如果是债券，以下为特有信息 - 目前忽略
			ignoredLength = 0;
			// EFNFlag(1)
			ignoredLength += 1;
			// AccruedInterest(4)
			ignoredLength += 4;
			// CouponRate(4)
			ignoredLength += 4;
			ByteBufferUtil.skip(msgBody, ignoredLength);
		} else {
			ByteBufferUtil.skip(msgBody, 9);
		}
		
		// Warrants, Basket Warrants and Structured Product specific data
		if (instrumentType == EHkInstrumentType.WRNT || instrumentType == EHkInstrumentType.BWRT) {
			// XXX 如果是权证，以下为特有信息 - 目前忽略
			ignoredLength = 0;
			// ConversionRatio(4)
			ignoredLength += 4;
			// StrikePrice(4)
			ignoredLength += 4;
			// MaturityDate(4)
			ignoredLength += 4;
			// CallPutFlag(1)
			ignoredLength += 1;
			// Style(1)
			ignoredLength += 1;
			ByteBufferUtil.skip(msgBody, ignoredLength);
			
			// NoUnderlyingSecurities(2) Uint16 - 0 to 20 for Basket Warrants, 1 for Warrants and Structured Product
			// 因此无需考虑符号
			int iNumUnderlyingSec = msgBody.getShort();
			// XXX 记录权证下相关的股票 - 目前忽略
			for (int i = 0; i < iNumUnderlyingSec; i++) {
				ignoredLength = 0;
				// UnderlyingSecurityCode(4)
				int iUnderlyingSecurityCode = msgBody.getInt();
				String strUnderlyingSecurityCode = String.format("%05d", iUnderlyingSecurityCode);
				String strAssetIdForUnderlyingSecurityCode = strUnderlyingSecurityCode + ASSET_SUFFIX_HK;
				// UnderlyingSecurityWeight(4) 3 implied decimal places - 权重小于100，故不考虑符号
				double dWeightForUnderlyingSecurity = ByteBufferUtil.getDoubleFromInt(msgBody, 3);
				LOG.info("warrant {} <- {}, weight={}", assetId, strAssetIdForUnderlyingSecurityCode, dWeightForUnderlyingSecurity);
			}
		}
		
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存Security Definition到redis失败，assetId=" + assetId, e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Security Definition消息失败，assetId=" + assetId, e);
		}
	}

	/**
	 * process Currency Rate (14) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processCurrencyRate(ByteBuffer msgBody, long msgTime) {
		HkCurrencyRateVO ccyRateVO = new HkCurrencyRateVO();
		ccyRateVO.setUpdateTime(new Date(msgTime));
		// CurrencyCode(3)
		byte[] rawCurrencyCode = new byte[3];
		msgBody.get(rawCurrencyCode);
		String strCurrencyCode = new String(rawCurrencyCode, asciiEncoding);
		ccyRateVO.setCurrencyCode(strCurrencyCode);
		// Filler(1)
		ByteBufferUtil.skip(msgBody, 1);
		// CurrencyFactor(2)
		int iCurrencyFactor = ByteBufferUtil.getUnsignedShort(msgBody);
		// Filler(2)
		ByteBufferUtil.skip(msgBody, 2);
		// CurrencyRate(4)
		long lCurrencyRate = ByteBufferUtil.getUnsignedInt(msgBody);
		
		// CurrencyFactor + 4 decimals implied from CurrencyRate
		char[] cFactor = new char[1 + iCurrencyFactor + 4];
		cFactor[0] = '1';
		for (int i = 1; i < cFactor.length; i++) {
			cFactor[i] = '0'; 
		}
		BigDecimal factor = new BigDecimal(cFactor);
		BigDecimal hkdInFactorUnit = new BigDecimal(lCurrencyRate);
		String strCcyRate = hkdInFactorUnit.divide(factor).toString();
		ccyRateVO.setCurrencyRate(strCcyRate);
		LOG.info("ccy:{}, 1 unit for {} HKD", strCurrencyCode, strCcyRate);
		
		// 保存到redis
		try {
			redisMapService.saveUpdate(ccyRateVO, strCurrencyCode);
		} catch (Exception e) {
			LOG.error("保存汇率到redis失败，CCY=" + strCurrencyCode, e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Currency Rate消息失败，CCY=" + strCurrencyCode, e);
		}
	}
	
	/**
	 * process Trading Session Status (20) message
	 * @param msgBody
	 */
	private void processTradingSessionStatus(ByteBuffer msgBody) {
		// MarketCode(4)
		byte[] rawMarketCode = new byte[4];
		msgBody.get(rawMarketCode);
		String strMarketCode = new String(rawMarketCode, asciiEncoding).trim();
		EHkSubMarket subMarket = EHkSubMarket.valueOf(strMarketCode);
		
		int ignoreLength = 0;
		// TradingSessionID(1)
		ignoreLength += 1;
		// TradingSessionSubID(1)
		ignoreLength += 1;
		ByteBufferUtil.skip(msgBody, ignoreLength);
		
		// TradingSesStatus(1)
		int iSessionStatus = ByteBufferUtil.getUnsignedByte(msgBody);
		EHkTradingSessionStatus tradingSessionStatus = EHkTradingSessionStatus.valueOf(iSessionStatus);
		
		// 后续的字段处理 - 目前忽略

		LOG.info("mkt:{}, status:{}", strMarketCode, tradingSessionStatus);
		
		if (tradingSessionStatus == EHkTradingSessionStatus.UNKNOWN) {
			return;
		}
		
		// 取redis所有行情
		Map<String, HkTsData> mapAllQuots = LocalCache.HK_TS_DATA;//redisMapService.findAllObject(HkTsData.class);
		
		// 修改相应市场开盘/收盘状态
		List<Pair> lstNeedUpdate = new ArrayList<Pair>(mapAllQuots.size());
		for (Entry<String, HkTsData> entry : mapAllQuots.entrySet()) {
			HkTsData quot = entry.getValue();
			if (quot.getSubMarket() == subMarket) {
				if (tradingSessionStatus == EHkTradingSessionStatus.CLOSED || tradingSessionStatus == EHkTradingSessionStatus.DAY_CLOSED) {
					if (!quot.isOpenMkt()) {
						// 如果已经记录过状态，则无需继续
						break;
					}
					quot.setOpenMkt(false);
					lstNeedUpdate.add(new Pair(entry.getKey(), quot));
				} else if (tradingSessionStatus == EHkTradingSessionStatus.OPEN || tradingSessionStatus == EHkTradingSessionStatus.PRE_CLOSE) {
					if (quot.isOpenMkt()) {
						// 如果已经记录过状态，则无需继续
						break;
					}
					quot.setOpenMkt(true);
					lstNeedUpdate.add(new Pair(entry.getKey(), quot));
				} else {
					// 其他交易状态不改变本地状态
				}
			}
		}
		
		// 保存到redis
		try {
			if (CollectionUtils.isNotEmpty(lstNeedUpdate)) {
				redisMapService.saveUpdateBatch(HkTsData.class, lstNeedUpdate);
			}
		} catch (Exception e) {
			LOG.error("保存交易状态到redis失败", e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Trading Session Status消息失败", e);
		}
	}

	/**
	 * process Security Status (21) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	//@Transactional(value="transactionManager")
	private void processSecurityStatus(ByteBuffer msgBody, long msgTime) {
		// SecurityCode(4)
		String strStockCode = extractHkStockCode(msgBody);
		String assetId = strStockCode + ASSET_SUFFIX_HK;
		
		if (!isSecuritySupported(assetId)) {
			return;
		}
		
		// 判断支持用的是stockMap，如果支持，取出来的值一定非空
		StkInfo stkInfo = LocalCache.STOCK_MAP.get(assetId);
		
		// SecurityTradingStatus(1)
		EHkSecurityTradingStatus securityStatus = EHkSecurityTradingStatus.valueOf(ByteBufferUtil.getUnsignedByte(msgBody));
		
		// 从redis取停牌信息
		HkStkSusp stkSusp = redisMapService.findObject(HkStkSusp.class, assetId);
		
		// 在redis中处理停牌信息
		try {
			Date sysDate = new Date();
			Date quotDate = org.apache.commons.lang.time.DateUtils.truncate(new Date(msgTime), Calendar.DATE);

			if (securityStatus == EHkSecurityTradingStatus.TRADING_HALT) {
				boolean needSave = false;
				// 停牌
				if (stkSusp == null) {
					// redis中没有记录，则说明是新增的停牌，需要保存
					stkSusp = new HkStkSusp();
					stkSusp.setAssetId(assetId);
					stkSusp.setSuspDate(quotDate);
					stkSusp.setMktCode(MARKET_HK);
					stkSusp.setStkCode(strStockCode);
					stkSusp.setCreateTime(sysDate);
					stkSusp.setUpdateTime(sysDate);
					stkSusp.setExtTime(sysDate);

					needSave = true;
				} else if (!stkSusp.getSuspDate().equals(quotDate)) {
					// redis中有记录，但记录是旧的，则替换之，并保存当日停牌信息到数据库
					stkSusp.setSuspDate(quotDate);
					stkSusp.setCreateTime(sysDate);
					stkSusp.setUpdateTime(sysDate);
					stkSusp.setExtTime(sysDate);
					
					needSave = true;
				} else {
					// reids中有最新的记录，无需修改
				}
				
				if (needSave) {
					suspDao.saveSusp(stkSusp);
					// 保存到redis
					redisMapService.saveUpdate(stkSusp, assetId);
					
					HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);
					if (quot == null) {
						LOG.info("本地缓存不存在对应的行情，临时构建，assetId={}", assetId);
						quot = new HkTsData();
						quot.setCorpCode(stkInfo.getCorpCode());
						quot.setAssetId(assetId);
						quot.setStkCode(stkInfo.getStkCode());
						quot.setStkName(stkInfo.getStkName());
						quot.setSecType(stkInfo.getSecType());
						quot.setSecSType(stkInfo.getSecSType());
					} 
					quot.setStatus(StkStatusType.SUSP);
					LocalCache.HK_TS_DATA.put(assetId, quot);
					
					LOG.info("{}于{}记录停牌", assetId, sysDate);
				}
			} else if (securityStatus == EHkSecurityTradingStatus.RESUME) {
				// 复牌
				if (stkSusp != null) {
					if (stkSusp.getSuspDate().equals(quotDate)) {
						// redis中存在，且复牌日期与停牌日期相同，则为临时停牌，需要删除数据库记录
						suspDao.deleteByAssetIdAndSuspendDate(assetId, quotDate);
					} else {
						// redis中存在，但停牌时间并非当日，则数据库中无记录
					}
					// 从redis中移除停牌
					redisMapService.delete(HkStkSusp.class, assetId);
					
					HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);
					if (quot == null) {
						LOG.info("本地缓存不存在对应的行情，临时构建，assetId={}", assetId);
						quot = new HkTsData();
						quot.setCorpCode(stkInfo.getCorpCode());
						quot.setAssetId(assetId);
						quot.setStkCode(stkInfo.getStkCode());
						quot.setStkName(stkInfo.getStkName());
						quot.setSecType(stkInfo.getSecType());
						quot.setSecSType(stkInfo.getSecSType());
					}
					if (quot.getStatus() == StkStatusType.SUSP) {
						quot.setStatus(StkStatusType.NORMAL);
					}
					LocalCache.HK_TS_DATA.put(assetId, quot);
					
					LOG.info("{}于{}记录复牌", assetId, sysDate);
				}
			} else {
				LOG.error("错误的状态{}", securityStatus);
			}
		} catch (Exception e) {
			LOG.error("保存证券状态失败", e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理 Security Status消息失败", e);
		}
	}
	
	/**
	 * process Aggregate Order Book Update (53) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processAggregateOrderBook(ByteBuffer msgBody, long msgTime) {
		// SecurityCode(4)
		String assetId = extractHkStockAssetId(msgBody);
		
		if (!isSecuritySupported(assetId)) {
			return;
		}
		
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
		if (quot == null) {
			LOG.error("处理Aggregate Order Book Update时未找到对应的行情，忽略之，assetId={}", assetId);
			return;
		}
		quot.setUpdateTime(msgTime);
		Date msgDate = new Date(msgTime);
		quot.setDate(DateUtils.dateToString(msgDate, TimeFormatter.YYYY_MM_DD));
		quot.setTime(DateUtils.dateToString(msgDate, TimeFormatter.HH_MM_SS));
		
		AggregateOrderBook orderBook = LocalCache.AGGREGATE_ORDER_BOOK_MAP.get(assetId);
		if (orderBook == null) {
			orderBook = new AggregateOrderBook(assetId);
			LocalCache.AGGREGATE_ORDER_BOOK_MAP.put(assetId, orderBook);
		}
		orderBook.setUpdateTime(msgTime);
		
		// Filler(3)
		ByteBufferUtil.skip(msgBody, 3);
		// NoEntries(1)
		int entryCount = ByteBufferUtil.getUnsignedByte(msgBody);
		InnerAggregateOrderBookEntry[] arrEntries = new InnerAggregateOrderBookEntry[entryCount];
		// extract entries
		for (int i = 0; i < entryCount; i++) {
			InnerAggregateOrderBookEntry entry = new InnerAggregateOrderBookEntry();
			arrEntries[i] = entry;
			// AggregateQuantity(8)
			// 单位转成手
			entry.setAggregateQuantity(msgBody.getLong() / quot.getLotSize());
			// Price(4) - 3 implied decimal places
			entry.setPrice(ByteBufferUtil.getDoubleFromInt(msgBody, 3));
			// NumberOfOrders(4)
			entry.setNumberOfOrders(ByteBufferUtil.getUnsignedInt(msgBody));
			// Side(2)
			entry.setSide(EHkAggregateOrderSide.valueOf(ByteBufferUtil.getUnsignedShort(msgBody)));
			// PriceLevel(1)
			entry.setPriceLevel(ByteBufferUtil.getUnsignedByte(msgBody));
			// UpdateAction(1)
			entry.setUpdateAction(EHkAggregateOrderUpdateAction.valueOf(ByteBufferUtil.getUnsignedByte(msgBody)));
			// Filler(4) 
			ByteBufferUtil.skip(msgBody, 4);
		}
		
		// process entries
		for (InnerAggregateOrderBookEntry entry : arrEntries) {
			EHkAggregateOrderUpdateAction updateAction = entry.getUpdateAction();
			EHkAggregateOrderSide side = entry.getSide();
			InnerOrderBook innerOrderBook = side == EHkAggregateOrderSide.BID ? orderBook.getBidSide() : orderBook.getAskSide();
			
			switch (updateAction) {
			case NEW:
				OrderbookManageUtil.newEntry(innerOrderBook, entry);
				break;
			case CHANGE:
				OrderbookManageUtil.changeEntry(innerOrderBook, entry);
				break;
			case DELETE:
				OrderbookManageUtil.deleteEntry(innerOrderBook, entry);
				break;
			case ORDERBOOK_CLEAR:
				OrderbookManageUtil.clearOrderBook(innerOrderBook);
				break;

			default:
				LOG.error("不支持的Aggregate Order Book update操作{}", updateAction);
				break;
			}
		}
		
		// 将价格映射到买卖档
		mapOrderBookToQuot(orderBook, quot);
		
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存aggregate order book行情到redis失败", e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Aggregate Order Book Update消息失败", e);
		}
	}

	/**
	 * process Trade Ticker (52) message<br>
	 * 根向港股行情开发人员了解，可取Trade Ticker中TrdType为Auction Trade（103）的Price作为开盘价
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processTradeTicker(ByteBuffer msgBody, long msgTime) {
		// SecurityCode(4)
		String assetId = extractHkStockAssetId(msgBody);
		
		if (!isSecuritySupported(assetId)) {
			return;
		}
				
		// TickerID(4)
		ByteBufferUtil.skip(msgBody, 4);
		// Price(4) - 3 implied decimal places
		double price = ByteBufferUtil.getDoubleFromInt(msgBody, 3);
		
		int ignoreLength = 0;
		// AggregateQuantity(8)
		ignoreLength += 8;
		// TradeTime(8)
		ignoreLength += 8;
		ByteBufferUtil.skip(msgBody, ignoreLength);
		
		// TrdType(2)
		EHkTickerTrdType trdTyp = EHkTickerTrdType.valueOf(msgBody.getShort());
		
		// XXX 目前只处理竞价产生的价格，用来作为开盘价
		if (trdTyp == EHkTickerTrdType.AMS_U) {
			// 从redis取实时行情
			HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
			if (quot == null) {
				LOG.error("处理Trade Ticker时未找到对应的行情，忽略之，assetId={}", assetId);
				return;
			}
			
			quot.setOpen(price);
			// XXX 如果昨收为0，则是上市首日无发行价，取开盘价作为昨收
			if (quot.getPrevClose() == 0) {
				quot.setPrevClose(price);
			}
			
			quot.setUpdateTime(msgTime);
			Date msgDate = new Date(msgTime);
			quot.setDate(DateUtils.dateToString(msgDate, TimeFormatter.YYYY_MM_DD));
			quot.setTime(DateUtils.dateToString(msgDate, TimeFormatter.HH_MM_SS));
			
			// 保存到redis
			try {
				//redisMapService.saveUpdate(quot, assetId);
				LocalCache.HK_TS_DATA.put(assetId, quot);
			} catch (Exception e) {
				LOG.error("保存开盘价到redis失败，assetId=" + assetId, e);
				// 捕获到异常再次抛出，由调用方决定如何处理
				throw new RuntimeException("处理Trade Ticker消息失败，assetId=" + assetId, e);
			}
		}
	}
	
	/**
	 * process Closing Price (62) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processClosingPrice(ByteBuffer msgBody, long msgTime) {
		// SecurityCode(4)
		String assetId = extractHkStockAssetId(msgBody);
		
		if (!isSecuritySupported(assetId)) {
			return;
		}
		
		// 从redis取实时行情
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
		if (quot == null) {
			LOG.error("处理收盘价时未找到对应的行情，忽略之，assetId={}", assetId);
			return;
		}
		
		// ClosingPrice(4) - 3 implied decimal places
		double closePrice = ByteBufferUtil.getDoubleFromInt(msgBody, 3);
		quot.setPrice(closePrice);
		double prevClose = quot.getPrevClose();
		if (prevClose != 0) {
			double change = ArithmeticUtil.sub(closePrice, prevClose);
			quot.setChange(change);
			quot.setChangePct(ArithmeticUtil.div(change, prevClose, 4));
		}
		
		quot.setUpdateTime(msgTime);
		Date quotDate = new Date(msgTime);
		quot.setDate(DateUtils.dateToString(quotDate, TimeFormatter.YYYY_MM_DD));
		quot.setTime(DateUtils.dateToString(quotDate, TimeFormatter.HH_MM_SS));
		
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存收盘价到redis失败", e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Closing Price消息失败", e);
		}
	}

	/**
	 * process Nominal Price (40) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processNominalPrice(ByteBuffer msgBody, long msgTime) {
		// SecurityCode(4)
		String assetId = extractHkStockAssetId(msgBody);
		
		if (!isSecuritySupported(assetId)) {
			return;
		}
		
		// 从redis取实时行情
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
		if (quot == null) {
			LOG.error("处理按盘价时未找到对应的行情，忽略之，assetId={}", assetId);
			return;
		}
		
		// NominalPrice(4) - 3 implied decimal places
		double nominalPrice = ByteBufferUtil.getDoubleFromInt(msgBody, 3);
		quot.setPrice(nominalPrice);
		double prevClose = quot.getPrevClose();
		if (prevClose != 0) {
			double change = ArithmeticUtil.sub(nominalPrice, prevClose);
			quot.setChange(change);
			quot.setChangePct(ArithmeticUtil.div(change, prevClose, 4));
		}
		
		quot.setUpdateTime(msgTime);
		Date quotDate = new Date(msgTime);
		quot.setDate(DateUtils.dateToString(quotDate, TimeFormatter.YYYY_MM_DD));
		quot.setTime(DateUtils.dateToString(quotDate, TimeFormatter.HH_MM_SS));
		
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存按盘价到redis失败", e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Nominal Price消息失败", e);
		}
	}

	/**
	 * process Indicative Equilibrium Price (41) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processIEP(ByteBuffer msgBody, long msgTime) {
		// SecurityCode(4)
		String assetId = extractHkStockAssetId(msgBody);
		
		if (!isSecuritySupported(assetId)) {
			return;
		}
		
		// 从redis取实时行情
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
		if (quot == null) {
			LOG.error("处理参考平衡价时未找到对应的行情，忽略之，assetId={}", assetId);
			return;
		}
		
		// Price(4) - 3 implied decimal places
		// Value is 0 if IEP is not available
		double iep = ByteBufferUtil.getDoubleFromInt(msgBody, 3);
		if (iep == 0) {
			return;
		}
		quot.setPrice(iep);
		double prevClose = quot.getPrevClose();
		if (prevClose != 0) {
			double change = ArithmeticUtil.sub(iep, prevClose);
			quot.setChange(change);
			quot.setChangePct(ArithmeticUtil.div(change, prevClose, 4));
		}
		
		quot.setUpdateTime(msgTime);
		Date quotDate = new Date(msgTime);
		quot.setDate(DateUtils.dateToString(quotDate, TimeFormatter.YYYY_MM_DD));
		quot.setTime(DateUtils.dateToString(quotDate, TimeFormatter.HH_MM_SS));
		
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存参考平衡价到redis失败", e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Indicative Equilibrium Price消息失败", e);
		}
	}

	/**
	 * process Statistics (60) message
	 * @param msgBody
	 * @param msgTime millisecond since unix epoch
	 */
	private void processStatics(ByteBuffer msgBody, long msgTime) {
		// SecurityCode(4)
		String assetId = extractHkStockAssetId(msgBody);
		
		if (!isSecuritySupported(assetId)) {
			return;
		}
		
		// 从redis取实时行情
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
		if (quot == null) {
			LOG.error("处理统计信息时未找到对应的行情，忽略之，assetId={}", assetId);
			return;
		}
		
		quot.setUpdateTime(msgTime);
		Date quotDate = new Date(msgTime);
		quot.setDate(DateUtils.dateToString(quotDate, TimeFormatter.YYYY_MM_DD));
		quot.setTime(DateUtils.dateToString(quotDate, TimeFormatter.HH_MM_SS));
		
		// SharesTraded(8)
		double totalTradedShr = ArithmeticUtil.mul(msgBody.getLong(), 1);
		double totalVol = ArithmeticUtil.div(totalTradedShr, quot.getLotSize());
		quot.setTotalVolume(totalVol);
		// Turnover(8)
		double turnover = ByteBufferUtil.getDoubleFromLong(msgBody, 3);
		quot.setTurnOver(turnover);
		// HighPrice(4)
		quot.setHigh(ByteBufferUtil.getDoubleFromInt(msgBody, 3));
		// LowPrice(4)
		quot.setLow(ByteBufferUtil.getDoubleFromInt(msgBody, 3));
		// 如果此时没有开盘价，有成交量，则将最近成交价作为开盘价
		// LastPrice(4)
		if (quot.getOpen() == 0 && totalVol != 0) {
			double openFromLastPrice = ByteBufferUtil.getDoubleFromInt(msgBody, 3);
			if (openFromLastPrice != 0) {
				LOG.info("尚未有开盘价，但已经有成交，使用最近成交价作为开盘价，assetId={}", assetId);
				quot.setOpen(openFromLastPrice);
				// XXX 如果昨收为0，则是上市首日无发行价，取开盘价作为昨收
				if (quot.getPrevClose() == 0) {
					quot.setPrevClose(openFromLastPrice);
				}
			}
		} else {
			ByteBufferUtil.skip(msgBody, 4);
		}
		// 设置均价 - 如果没有成交，则用现价作为均价，如果有，则计算均价
		if (quot.getTurnOver() == 0 || quot.getTotalVolume() == 0) {
			quot.setAvgPrice(quot.getPrice());
		} else {
			// 指数使用加权算法，其他使用成交额除以成交量
			if (quot.getSecSType() == SecType.IDX) {
				quot.setAvgPrice(QuoteUtil.calcIndexAvgPrice(quot, 4));
			} else {
				quot.setAvgPrice(ArithmeticUtil.div(turnover, totalTradedShr, 4));
			}
		}
		
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存统计信息到redis失败", e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Statistics消息失败", e);
		}
	}

	/**
	 * process Index Definition (70) message
	 * @param msgBody
	 * @param sendTime millisecond since unix epoch
	 */
	private void processIndexDef(ByteBuffer msgBody, long msgTime) {
		// 取本地缓存中的证券信息
		Map<String, StkInfo> stockMap = LocalCache.STOCK_MAP;
		// IndexCode(11)
		String strIdxCode = extractHkIndexCode(msgBody);
		String assetId = indexCodeForAssetId.getProperty(strIdxCode, strIdxCode + ASSET_SUFFIX_HK);
		// 不支持的证券，直接返回
		// 来控制需要处理的证券类型，对于不支持的类型，在这里直接结束，不会进入后续流程
		if (!isSecuritySupported(assetId)) {
			return;
		}
		StkInfo stkInfo = stockMap.get(assetId);
		// 取redis缓存中的行情对象，有则更新，无则新增
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
		if(quot == null) {
			quot = new HkTsData();
			quot.setCorpCode(stkInfo.getCorpCode());
			quot.setAssetId(assetId);
			quot.setStkCode(stkInfo.getStkCode());
			quot.setStkName(stkInfo.getStkName());
			quot.setSecType(stkInfo.getSecType());
			quot.setSecSType(stkInfo.getSecSType());
		}
		quot.setLotSize(INDEX_LOT_SIZE);
		quot.setUpdateTime(msgTime);
		Date msgDate = new Date(msgTime);
		quot.setDate(DateUtils.dateToString(msgDate, TimeFormatter.YYYY_MM_DD));
		quot.setTime(DateUtils.dateToString(msgDate, TimeFormatter.HH_MM_SS));
		
		// IndexSource(1)
		ByteBufferUtil.skip(msgBody, 1);
		// CurrencyCode(3)
		byte[] rawCurrencyCode = new byte[3];
		msgBody.get(rawCurrencyCode);
		String strCurrencyCode = new String(rawCurrencyCode, asciiEncoding).trim();
		if (StringUtils.isNotEmpty(strCurrencyCode)) {
			quot.setCurrency(EHkCurrencyType.valueOf(strCurrencyCode));
		}

		
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存Index Definition到redis失败，assetId=" + assetId, e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Index Definition消息失败，assetId=" + assetId, e);
		}
	}

	/**
	 * process Index Data (71) message
	 * @param msgBody
	 * @param sendTime millisecond since unix epoch
	 */
	private void processIndexDat(ByteBuffer msgBody, long msgTime) {
		// IndexCode(11)
		String strIdxCode = extractHkIndexCode(msgBody);
		String assetId = indexCodeForAssetId.getProperty(strIdxCode, strIdxCode + ASSET_SUFFIX_HK);
		// 不支持的证券，直接返回
		// 来控制需要处理的证券类型，对于不支持的类型，在这里直接结束，不会进入后续流程
		if (!isSecuritySupported(assetId)) {
			return;
		}
		
		/*
		 * 官方文档Note: For IndexCode “CSCSHQ” which is for Shanghai-Hong Kong Stock Connect Northbound Daily Quota Balance, 
		 * only MsgSize, MsgType, IndexCode, IndexTime and IndexVolume will be populated to provide the daily quota balance 
		 * whereas the other fields are irrelevant and can be ignored
		 * 此外，还有其他比如CES100.HK，状态都是不正确的，所以这里统一标记为特殊处理
		 */
		boolean needSpecialPorcessForCscshq = "CSCSHQ.HK".equals(assetId);
		
		// IndexStatus(1)
		byte[] rawIndexStatus = new byte[1];
		msgBody.get(rawIndexStatus);
		EHkIndexStatus status = null;
		String strStatus = new String(rawIndexStatus, asciiEncoding).trim();
		if (StringUtils.isNotEmpty(strStatus)) {
			status = EHkIndexStatus.valueOf(strStatus);
		}
		
		// XXX 暂不处理Preliminary close和Stop loss index状态
		if (status == EHkIndexStatus.R || status == EHkIndexStatus.S) {
			return;
		}
		
		// 取redis缓存中的行情对象，有则更新，无则新增
		HkTsData quot = LocalCache.HK_TS_DATA.get(assetId);//redisMapService.findObject(HkTsData.class, assetId);
		if(quot == null) {
			LOG.error("redis中不存在该指数的行情，忽略之，assetId={}", assetId);
			return;
		}
		
		// IndexTime(8)
		long indexTimeMills = msgBody.getLong() / 1000000;
		quot.setUpdateTime(indexTimeMills);
		Date idxDate = new Date(indexTimeMills);
		quot.setDate(DateUtils.dateToString(idxDate, TimeFormatter.YYYY_MM_DD));
		quot.setTime(DateUtils.dateToString(idxDate, TimeFormatter.HH_MM_SS));
		// IndexValue(8) - 4 implied decimal places
		Double dblIdxVal = null;
		long lTmpIdxVal = msgBody.getLong();
		if (lTmpIdxVal != HKEX_INT64_NULL) {
			dblIdxVal = ArithmeticUtil.div(lTmpIdxVal, Math.pow(10, 4));
		}
		
		// XXX 对于指数的开盘、收盘、昨收，以收到IndexStatus的消息为准
		// 对于CSCSHQ，无需处理
		// 对于未能解析出状态的，无需处理
		if (!needSpecialPorcessForCscshq && status != null && dblIdxVal != null) {
			if (status == EHkIndexStatus.O) {
				quot.setOpen(dblIdxVal);
				// XXX 如果昨收为0，则是上市首日无发行价，取开盘价作为昨收
				if (quot.getPrevClose() == 0) {
					quot.setPrevClose(dblIdxVal);
				}
			} else if (status == EHkIndexStatus.P) {
				quot.setPrevClose(dblIdxVal);
			} else {
				// I,C,T --> price
				// 不用设置涨跌幅，指数的数据有
				quot.setPrice(dblIdxVal);
			}
		}
		
		// NetChgPrevDay(8) - 4 implied decimal places
		long lTmpChange = msgBody.getLong();
		if (lTmpChange != HKEX_INT64_NULL) {
			quot.setChange(ArithmeticUtil.div(lTmpChange, Math.pow(10, 4)));
		}
		// HighValue(8) - 4 implied decimal places
		long lTmpHigh = msgBody.getLong();
		if (lTmpHigh != HKEX_INT64_NULL) {
			quot.setHigh(ArithmeticUtil.div(lTmpHigh, Math.pow(10, 4)));
		}
		// LowValue(8) - 4 implied decimal places
		long lTmpLow = msgBody.getLong();
		if (lTmpLow != HKEX_INT64_NULL) {
			quot.setLow(ArithmeticUtil.div(lTmpLow, Math.pow(10, 4)));
		}
		// EASValue(8) - 2 implied decimal places
		ByteBufferUtil.skip(msgBody, 8);
		// IndexTurnover(8) - 4 implied decimal places
		long lTmpTurnover = msgBody.getLong();
		if (lTmpTurnover != HKEX_INT64_NULL) {
			quot.setTurnOver(ArithmeticUtil.div(lTmpTurnover, Math.pow(10, 4)));
		}
		
		// XXX 如果错过了对应消息导致未设置，则用消息后面带的OpeningValue、ClosingValue、PreviousSesClose相应修复
		
		// OpeningValue(8) - 4 implied decimal places
		long lTmpOpen = msgBody.getLong();
		if (!needSpecialPorcessForCscshq && lTmpOpen != HKEX_INT64_NULL && quot.getOpen() == 0) {
			double open = ArithmeticUtil.div(lTmpOpen, Math.pow(10, 4));
			quot.setOpen(open);
		}
		// ClosingValue(8) - 4 implied decimal places
		// 信任行情源的数据，只要不为空，便设为收盘值
		long lTmpClose = msgBody.getLong();
		if (!needSpecialPorcessForCscshq && lTmpClose != HKEX_INT64_NULL) {
			quot.setPrice(ArithmeticUtil.div(lTmpClose, Math.pow(10, 4)));
		}
		// PreviousSesClose(8) - 4 implied decimal places
		long lTmpPrevClose = msgBody.getLong();
		if (!needSpecialPorcessForCscshq && lTmpPrevClose != HKEX_INT64_NULL && quot.getPrevClose() == 0) {
			quot.setPrevClose(ArithmeticUtil.div(lTmpPrevClose, Math.pow(10, 4)));
		}
		
		// 如果昨收仍然为0，并且已产生开盘价，则是上市首日无发行价，取开盘价作为昨收
		if (quot.getPrevClose() == 0 && quot.getOpen() != 0) {
			quot.setPrevClose(quot.getOpen());
		}
		
		// XXX 并非所有指数都有成交量，而且成交量含义也不一样，注意区分处理，目前先原样保存
		// IndexVolume(8)
		long lTmpVolume = msgBody.getLong();
		if (lTmpVolume != HKEX_INT64_NULL) {
			quot.setTotalVolume(ArithmeticUtil.div(lTmpVolume, quot.getLotSize()));
		}
		
		// NetChgPrevDayPct(4) - 4 implied decimal places
		// 转成无百分号，加多两位小数
		quot.setChangePct(ByteBufferUtil.getDoubleFromInt(msgBody, 6));
		
		// 计算均价，不为零则计算，为零则使用现价
		if (quot.getTurnOver() != 0) {
			quot.setAvgPrice(QuoteUtil.calcIndexAvgPrice(quot, 4));
		} else {
			quot.setAvgPrice(quot.getPrice());
		}
	
		// 保存到redis
		try {
			//redisMapService.saveUpdate(quot, assetId);
			LocalCache.HK_TS_DATA.put(assetId, quot);
		} catch (Exception e) {
			LOG.error("保存Index Data到redis失败，assetId=" + assetId, e);
			// 捕获到异常再次抛出，由调用方决定如何处理
			throw new RuntimeException("处理Index Data消息失败，assetId=" + assetId, e);
		}
	}
	

	
	
	
	
	/**
	 * 系统是否支持处理指定的证券<br>
	 * 在此可以通过ETL和人工的干预，来控制需要处理的证券类型
	 * @param assetId
	 * @return
	 */
	private boolean isSecuritySupported(String assetId) {
		return LocalCache.STOCK_MAP.containsKey(assetId);
	}
	
	/**
	 * 从消息体当前位置抽取资产id
	 * @param msgBuf 消息体
	 * @return
	 */
	private String extractHkStockAssetId(ByteBuffer msgBuf) {
		// SecurityCode(4) 取证券代码拼接资产id，虽然是Uint32，但港交所限制为1–99999，故无需考虑符号
		int iSecurityCode = msgBuf.getInt();
		return String.format("%05d%s", iSecurityCode, ASSET_SUFFIX_HK);
	}
	
	/**
	 * 从消息体当前位置抽取股票代码
	 * @param msgBuf 消息体
	 * @return
	 */
	private String extractHkStockCode(ByteBuffer msgBuf) {
		// SecurityCode(4) 取证券代码拼接资产id，虽然是Uint32，但港交所限制为1–99999，故无需考虑符号
		int iSecurityCode = msgBuf.getInt();
		return String.format("%05d", iSecurityCode);
	}
	
	/**
	 * 从消息体当前文职抽取指数的资产id
	 * @param msgBuf
	 * @return
	 */
	private String extractHkIndexCode(ByteBuffer msgBuf) {
		// IndexCode(11)
		byte[] rawIndexCode = new byte[11];
		msgBuf.get(rawIndexCode);
		return new String(rawIndexCode, asciiEncoding).trim();
	}
	

	/**
	 * 将aggregate order book内容映射到买卖十档
	 * @param orderBook
	 * @param quot
	 */
	private void mapOrderBookToQuot(AggregateOrderBook orderBook, HkTsData quot) {
		BidSideOrderBook bidSide = orderBook.getBidSide();
		List<AggregateOrderBookEntry> lstBidEntry = OrderbookManageUtil.getOrderBook(bidSide);
		AskSideOrderBook askSide = orderBook.getAskSide();
		List<AggregateOrderBookEntry> lstAskEntry = OrderbookManageUtil.getOrderBook(askSide);
		int lotSize = quot.getLotSize();
		
		AggregateOrderBookEntry entry = null;
		// 买盘
		entry = lstBidEntry.get(0);
		if (entry == null) {
			quot.setB1Price(0);
			quot.setB1(0);
		} else {
			quot.setB1Price(entry.getPrice());
			quot.setB1(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(1);
		if (entry == null) {
			quot.setB2Price(0);
			quot.setB2(0);
		} else {
			quot.setB2Price(entry.getPrice());
			quot.setB2(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(2);
		if (entry == null) {
			quot.setB3Price(0);
			quot.setB3(0);
		} else {
			quot.setB3Price(entry.getPrice());
			quot.setB3(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(3);
		if (entry == null) {
			quot.setB4Price(0);
			quot.setB4(0);
		} else {
			quot.setB4Price(entry.getPrice());
			quot.setB4(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(4);
		if (entry == null) {
			quot.setB5Price(0);
			quot.setB5(0);
		} else {
			quot.setB5Price(entry.getPrice());
			quot.setB5(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(5);
		if (entry == null) {
			quot.setB6Price(0);
			quot.setB6(0);
		} else {
			quot.setB6Price(entry.getPrice());
			quot.setB6(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(6);
		if (entry == null) {
			quot.setB7Price(0);
			quot.setB7(0);
		} else {
			quot.setB7Price(entry.getPrice());
			quot.setB7(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(7);
		if (entry == null) {
			quot.setB8Price(0);
			quot.setB8(0);
		} else {
			quot.setB8Price(entry.getPrice());
			quot.setB8(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(8);
		if (entry == null) {
			quot.setB9Price(0);
			quot.setB9(0);
		} else {
			quot.setB9Price(entry.getPrice());
			quot.setB9(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstBidEntry.get(9);
		if (entry == null) {
			quot.setB10Price(0);
			quot.setB10(0);
		} else {
			quot.setB10Price(entry.getPrice());
			quot.setB10(entry.getAggregateQuantity() / lotSize);
		}
		// 买盘
		entry = lstAskEntry.get(0);
		if (entry == null) {
			quot.setS1Price(0);
			quot.setS1(0);
		} else {
			quot.setS1Price(entry.getPrice());
			quot.setS1(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(1);
		if (entry == null) {
			quot.setS2Price(0);
			quot.setS2(0);
		} else {
			quot.setS2Price(entry.getPrice());
			quot.setS2(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(2);
		if (entry == null) {
			quot.setS3Price(0);
			quot.setS3(0);
		} else {
			quot.setS3Price(entry.getPrice());
			quot.setS3(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(3);
		if (entry == null) {
			quot.setS4Price(0);
			quot.setS4(0);
		} else {
			quot.setS4Price(entry.getPrice());
			quot.setS4(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(4);
		if (entry == null) {
			quot.setS5Price(0);
			quot.setS5(0);
		} else {
			quot.setS5Price(entry.getPrice());
			quot.setS5(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(5);
		if (entry == null) {
			quot.setS6Price(0);
			quot.setS6(0);
		} else {
			quot.setS6Price(entry.getPrice());
			quot.setS6(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(6);
		if (entry == null) {
			quot.setS7Price(0);
			quot.setS7(0);
		} else {
			quot.setS7Price(entry.getPrice());
			quot.setS7(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(7);
		if (entry == null) {
			quot.setS8Price(0);
			quot.setS8(0);
		} else {
			quot.setS8Price(entry.getPrice());
			quot.setS8(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(8);
		if (entry == null) {
			quot.setS9Price(0);
			quot.setS9(0);
		} else {
			quot.setS9Price(entry.getPrice());
			quot.setS9(entry.getAggregateQuantity() / lotSize);
		}
		entry = lstAskEntry.get(9);
		if (entry == null) {
			quot.setS10Price(0);
			quot.setS10(0);
		} else {
			quot.setS10Price(entry.getPrice());
			quot.setS10(entry.getAggregateQuantity() / lotSize);
		}
	}
	
	/**
	 * 判断某个时间戳是否在交易时段
	 * @param timeMills
	 * @return
	 */
	private boolean isInRtTimeArea(long timeMills) {
		String sTime = DateUtils.dateToString(new Date(timeMills), TimeFormatter.HH_MM_SS);
		return rtTimeAreaBegin.compareTo(sTime) <= 0 && rtTimeAreaEnd.compareTo(sTime) >= 0;
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
}
