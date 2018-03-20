/**
 * @Title: OrderbookManageUtil.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yiqiniu.api.mktinfo.util.AggregateOrderBook;
import com.yiqiniu.api.mktinfo.util.AggregateOrderBookEntry;
import com.yiqiniu.api.mktinfo.util.InnerOrderBook;

/**
 * @description management utility for aggregate order book
 *
 * @author 余俊斌
 * @date 2015年11月23日 上午9:20:15
 * @version v1.0
 */

public class OrderbookManageUtil {
	
	/**
	 * operate new(add) action
	 * @param entry
	 */
	public static void newEntry(InnerOrderBook orderBook, AggregateOrderBookEntry entry) {
		Double key = entry.getPrice();
		orderBook.put(key, entry);
		// adjust for implicit delete
		ensureQuantity(orderBook);
		// update price level for higher entries
		updatePriceLevel(orderBook, key, 1);
	}
	
	/**
	 * operate delete action
	 * @param entry
	 */
	public static void deleteEntry(InnerOrderBook orderBook, AggregateOrderBookEntry entry) {
		Double key = entry.getPrice();
		orderBook.remove(key);
		// update price level for higher entries
		updatePriceLevel(orderBook, key, -1);
	}
	
	/**
	 * operate change action
	 * @param entry
	 */
	public static void changeEntry(InnerOrderBook orderBook, AggregateOrderBookEntry entry) {
		Double key = entry.getPrice();
		AggregateOrderBookEntry orderBookEntry = orderBook.get(key);
		if (orderBookEntry == null) {
			// XXX 异常之后可能导致现有数据丢失，如果修改的条目不存在，使用传入的条目补回，应该不影响数据正确性
			orderBookEntry = new AggregateOrderBookEntry();
			orderBookEntry.setPriceLevel(entry.getPriceLevel());
			orderBook.put(key, orderBookEntry);
		}
		orderBookEntry.setAggregateQuantity(entry.getAggregateQuantity());
		orderBookEntry.setNumberOfOrders(entry.getNumberOfOrders());
	}
	
	/**
	 * operate clear action
	 */
	public static void clearOrderBook(InnerOrderBook orderBook) {
		orderBook.clear();
	}
	
	/**
	 * get all entry entry in natural order as list
	 * @return
	 */
	public static List<AggregateOrderBookEntry> getOrderBook(InnerOrderBook orderBook) {
		List<AggregateOrderBookEntry> lstRet = new ArrayList<AggregateOrderBookEntry>(AggregateOrderBook.MAX_ORDER_BOOK_TICK_DEPTH);
		lstRet.addAll(orderBook.values());
		while(lstRet.size() < AggregateOrderBook.MAX_ORDER_BOOK_TICK_DEPTH) {
			lstRet.add(null);
		}
		return lstRet;
	}
	
	/**
	 * get all entry entry in reversed order as list
	 * @return
	 */
	public static List<AggregateOrderBookEntry> getReversedOrderBook(InnerOrderBook orderBook) {
		List<AggregateOrderBookEntry> lstRet = new ArrayList<AggregateOrderBookEntry>(AggregateOrderBook.MAX_ORDER_BOOK_TICK_DEPTH);
		lstRet.addAll(orderBook.descendingMap().values());
		while(lstRet.size() < AggregateOrderBook.MAX_ORDER_BOOK_TICK_DEPTH) {
			lstRet.add(null);
		}
		return lstRet;
	}
	
	/**
	 * ensure that the order book is correct in entry amount
	 */
	private static void ensureQuantity(InnerOrderBook orderBook) {
		while (orderBook.size() > AggregateOrderBook.MAX_ORDER_BOOK_TICK_DEPTH) {
			orderBook.pollLastEntry();
		}
	}
	
	/**
	 * update price level for higher level entries, with given increment
	 * @param currentKey key for positioning higher entries, excluded
	 * @param increment 1 or -1, may be other value in the future
	 */
	private static void updatePriceLevel(InnerOrderBook orderBook, Double currentKey, int increment) {
		Map.Entry<Double, AggregateOrderBookEntry> currentMapEntry = null;
		
		while ((currentMapEntry = orderBook.higherEntry(currentKey)) != null) {
			currentKey = currentMapEntry.getKey();
			AggregateOrderBookEntry currOrderBook = currentMapEntry.getValue();
			currOrderBook.setPriceLevel(currOrderBook.getPriceLevel() + increment);
		}
	}
}
