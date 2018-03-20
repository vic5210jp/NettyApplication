/*
 * FileName: Constants.java
 * Copyright: Copyright 2014-12-26 Yiqiniu Tech. Co. Ltd.All right reserved.
 * Description: 
 *
 */
package com.yiqiniu.mktserver.util;

/**
 * <code>Constants</code>
 *
 * @author Jimmy, 余俊斌
 * @date 2015-7-3 下午8:15:47
 * @version v1.0
 */
public class Constants {
	/** 模块名称 */
	public static final String MODULE_NAME = "hkmktinfo";
	/** 港股实时行情任务记录名 */
	public static final String HK_RT_JOB_NAME = "hkMktRtJob";
	/** Redis Key 分隔符 */
	public static final String KEY_SEQ = "_";
	/** 市场代码 - 香港 */ 
	public static final String MARKET_HK = "HK";
	/** 港股资产ID后缀 */ 
	public static final String ASSET_SUFFIX_HK = ".HK";
//	/** 深交所收市集合竞价开始时间 */
//	public static final String SZ_AGGRAUCTION_START = "14:57:00";
//	/** 开盘集合竞价结束时间 */
//	public static final String OPEN_AGGRAUCTION_END = "09:25:00";
	
//	XXX 暂时不要 public static final String TS_START_TIME_BID = "09:26:00";
	
	public static final String OPEN_TIME = "09:00:00";

	public static final String CLOSE_TIME = "16:00:00";
	
	public static final String TS_START_TIME = "09:30:00";
	
	public static final String HH_MM_00 = "HH:mm:00";
	
	public static final String HKDATA_POSTFIX = ".hkdata";//xiongpan 港股行情原始数据落地到本地文件后缀
	
	/**
	 * @description 港交所消息类型
	 *
	 * @author 余俊斌
	 * @date 2015年11月18日 上午10:12:50
	 * @version v1.0
	 */
	public static enum EHkQuotMsgType {
		LOGON(1101),
		LOGON_RESPONSE_MMDH(1102),
		LOGOUT(1103),
		SENDKEY(1105),
		REFRESH_REQUEST(1201),
		REFRESH_RESPONSE(1202),
		REFRESH_COMPLETE(203),
		MARKET_DEFINITION(10),
		SECURITY_DEFINITION(11),
		LIQUIDITY_PROVIDER(13),
		CURRENCY_RATE(14),
		TRADING_SESSION_STATUS(20),
		SECURITY_STATUS(21),
		ADD_ODD_LOT_ODER(33),
		DELETE_ODD_LOT_ORDER(34),
		AGGREGATE_ORDER_BOOK_UPDATE(53),
		BROKER_QUEUE(54),
		ORDER_IMBALANCE(56),
		TRADE_TICKER(52),
		CLOSING_PRICE(62),
		NOMINAL_PRICE(40),
		INDICATIVE_EQUILIBRIUM_PRICE(41),
		REFERENCE_PRICE(43),
		VCM_TRIGGER(23),
		STATISTICS(60),
		MARKET_TURNOVER(61),
		YIELD(44),
		NEWS(22),
		INDEX_DEFINITION(70),
		INDEX_DATA(71),
		;
		
		private int originalMsgType;
		
		EHkQuotMsgType(int originalMsgType) {
			this.originalMsgType = originalMsgType;
		}
		
		public int getOriginalMsgTypeValue() {
			return originalMsgType; 
		}
		
		
		
		@Override
		public String toString() {
			return name() + "[" + originalMsgType + "]";
		}

		public static EHkQuotMsgType valueOf(int originalMsgType) {
			for (EHkQuotMsgType quotMsgType : values()) {
				if (quotMsgType.originalMsgType == originalMsgType) {
					return quotMsgType;
				}
			}
			return null;
		}
	}
	
	/**
	 * @description 港交所行情证券类型
	 *
	 * @author 余俊斌
	 * @date 2015年11月18日 下午3:02:29
	 * @version v1.0
	 */
	public static enum EHkInstrumentType {
		/** Bonds */
		BOND,
		/** Basket Warrants */
		BWRT,
		/** Equities */
		EQTY,
		/** Trusts */
		TRST,
		/** Warrants & structured products (DW & CBBC) */
		WRNT,
		;
	}
	
	/**
	 * @description 港交所交易状态
	 *
	 * @author 余俊斌
	 * @date 2015年11月19日 上午10:55:24
	 * @version v1.0
	 */
	public static enum EHkTradingSessionStatus {
		/** 未开盘 */
		UNKNOWN(0),
		/** 交易所服务故障 */
		HALTED(1),
		/** 开盘 */
		OPEN(2),
		/** 收盘 */
		CLOSED(3),
		/** 收盘前阶段 */
		PRE_CLOSE(5),
		/** 日终 */
		DAY_CLOSED(100),
		;
		
		private int statusValue;
		
		EHkTradingSessionStatus(int statusValue) {
			this.statusValue = statusValue;
		}
		
		public int getStatusValue() {
			return statusValue;
		}
		
		public static EHkTradingSessionStatus valueOf(int statusValue) {
			for (EHkTradingSessionStatus status : values()) {
				if (status.statusValue == statusValue) {
					return status;
				}
			}
			return null;
		}
	}
	
	/**
	 * @description 港交所证券交易状态
	 *
	 * @author 余俊斌
	 * @date 2015年11月19日 下午7:42:02
	 * @version v1.0
	 */
	public static enum EHkSecurityTradingStatus {
		/** 停牌 */
		TRADING_HALT(2),
		/** 复牌 */
		RESUME(3),
		;
		
		private int statusValue;
		
		EHkSecurityTradingStatus(int statusValue) {
			this.statusValue = statusValue;
		}
		
		public int getStatusValue() {
			return statusValue;
		}
		
		public static EHkSecurityTradingStatus valueOf(int statusValue) {
			for (EHkSecurityTradingStatus status : values()) {
				if (status.statusValue == statusValue) {
					return status;
				}
			}
			return null;
		}
	}
	
	/**
	 * @description 港交所汇总委托方向
	 *
	 * @author 余俊斌
	 * @date 2015年11月19日 下午10:00:10
	 * @version v1.0
	 */
	public static enum EHkAggregateOrderSide {
		/** 买 */
		BID(0),
		/** 卖 */
		OFFER(1),
		;
		
		private int value;
		
		EHkAggregateOrderSide(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static EHkAggregateOrderSide valueOf(int value) {
			for (EHkAggregateOrderSide val : values()) {
				if (val.value == value) {
					return val;
				}
			}
			return null;
		}
	}
	
	/**
	 * @description 港交所汇总委托调整操作
	 *
	 * @author 余俊斌
	 * @date 2015年11月19日 下午10:00:10
	 * @version v1.0
	 */
	public static enum EHkAggregateOrderUpdateAction {
		/** 增加 */
		NEW(0),
		/** 修改 */
		CHANGE(1),
		/** 删除 */
		DELETE(2),
		/** 清除 */
		ORDERBOOK_CLEAR(74),
		;
		
		private int value;
		
		EHkAggregateOrderUpdateAction(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static EHkAggregateOrderUpdateAction valueOf(int value) {
			for (EHkAggregateOrderUpdateAction val : values()) {
				if (val.value == value) {
					return val;
				}
			}
			return null;
		}
	}
	
	/**
	 * @description 港交所Trade Ticker消息中的交易类型
	 *
	 * @author 余俊斌
	 * @date 2015年11月20日 下午5:05:55
	 * @version v1.0
	 */
	public static enum EHkTickerTrdType {
		/** Automatch normal */
		AMS(0),
		/** Late Trade */
		AMS_P(4),
		/** Non-direct Off-Exchange Trade */
		AMS_M(22),
		/** Automatch internalized */
		AMS_Y(100),
		/** Direct off-exchange Trade */
		AMS_X(101),
		/** Odd-Lot Trade */
		AMS_D(102),
		/** Auction Trade */
		AMS_U(103),
		;
		
		private int value;
		
		EHkTickerTrdType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static EHkTickerTrdType valueOf(int value) {
			for (EHkTickerTrdType val : values()) {
				if (val.value == value) {
					return val;
				}
			}
			return null;
		}
	}
	
	/**
	 * @description 港交所指数状态
	 *
	 * @author 余俊斌
	 * @date 2015年11月20日 下午8:13:50
	 * @version v1.0
	 */
	public static enum EHkIndexStatus {
		/** Closing value */
		C,
		/** Indicative */
		I,
		/** Opening index */
		O,
		/** Last close value (prev. ses.) */
		P,
		/** Preliminary close */
		R,
		/** Stop loss index */
		S,
		/** Real-time index value */
		T,
		;
	}
	
}
