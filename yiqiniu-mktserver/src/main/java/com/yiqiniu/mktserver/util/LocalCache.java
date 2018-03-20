package com.yiqiniu.mktserver.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yiqiniu.api.mktinfo.util.AggregateOrderBook;
import com.yiqiniu.api.mktinfo.vo.StkInfo;
import com.yiqiniu.api.mktserver.HkRtTime;
import com.yiqiniu.api.mktserver.HkTsData;
import com.yiqiniu.mktinfo.persist.po.StkTrdCale;

/**
 * <code>LocalCache<code>  本地缓存 , 注意定时清理， 否则数据会越来越大
 *
 * @author Colin
 * @since  Yiqiniu v0.0.1 (2014-12-29)
 *
 */
public class LocalCache {
	public static final Map<String, StkTrdCale> STK_TRD_CAL_MAP = new ConcurrentHashMap<String, StkTrdCale>();
	public static final Map<String, StkInfo> STOCK_MAP = new ConcurrentHashMap<String, StkInfo>();
	public static final Map<String, AggregateOrderBook> AGGREGATE_ORDER_BOOK_MAP = new ConcurrentHashMap<String, AggregateOrderBook>();
	public static final Map<String, HkTsData> HK_TS_DATA = new ConcurrentHashMap<String, HkTsData>();
	
	/** 记录已经生成分时的时间*/
	public static final Map<String, HkRtTime> HAS_GEN_TIME = new ConcurrentHashMap<String, HkRtTime>();

	public static boolean stkInfoUpdate = false;
	
}
