/**
 * @Title: ByteBufferUtil.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @description ByteBuffer工具类
 *
 * @author 余俊斌
 * @date 2015年11月18日 下午2:39:48
 * @version v1.0
 */

public class ByteBufferUtil {
	
	private static Charset UTF16LE_ENCODING = Charset.forName("UTF-16LE");

	/**
	 * 从ByteBuffer中读取无符号的byte数值
	 * @param byteBuffer
	 * @return
	 */
	public static int getUnsignedByte(ByteBuffer byteBuffer) {
		return byteBuffer.get() & 0xFF;
	}
	
	/**
	 * 从ByteBuffer中读取无符号的short数值
	 * @param byteBuffer
	 * @return
	 */
	public static int getUnsignedShort(ByteBuffer byteBuffer) {
		return byteBuffer.getShort() & 0xFFFF;
	}
	
	/**
	 * 从ByteBuffer中读取无符号的int数值
	 * @param byteBuffer
	 * @return
	 */
	public static long getUnsignedInt(ByteBuffer byteBuffer) {
		return byteBuffer.getInt() & 0xFFFFFFFF;
	}
	
	/**
	 * 从ByteBuffer中读取隐含指定小数位的double数值
	 * @param byteBuffer
	 * @param impliedDecimal 隐含的小数位
	 * @return
	 */
	public static double getDoubleFromInt(ByteBuffer byteBuffer, int impliedDecimal) {
		double divisor = Math.pow(10, impliedDecimal);
		return (byteBuffer.getInt() / divisor);
	}
	
	/**
	 * 从ByteBuffer中读取隐含指定小数位的double数值
	 * @param byteBuffer
	 * @param impliedDecimal 隐含的小数位
	 * @return
	 */
	public static double getDoubleFromLong(ByteBuffer byteBuffer, int impliedDecimal) {
		double divisor = Math.pow(10, impliedDecimal);
		return (byteBuffer.getLong() / divisor);
	}
	
	/**
	 * 跳过若干个字节
	 * @param byteBuffer
	 * @param skipLength
	 */
	public static void skip(ByteBuffer byteBuffer, int skipLength) {
		byteBuffer.position(byteBuffer.position() + skipLength);
	}
	
	/**
	 * 读取指定长度范围内去掉前后全角和半角空格的字符串
	 * @param byteBuffer
	 * @param bytesLength
	 * @return
	 */
	public static String getFullTrimUnicodeString(ByteBuffer byteBuffer, int bytesLength) {
		if (bytesLength <= 0 || bytesLength % 2 != 0) {
			throw new IllegalArgumentException("用于获取Unicode字符串的字节长度必须为正偶数");
		}
		
		int iBufferPosition = byteBuffer.position();
		int iActualLen = bytesLength;
		while (iActualLen > 0) {
			char curChar = byteBuffer.getChar(iBufferPosition + iActualLen - 2);
			if (curChar == '\u0000' || curChar == '\u3000' || curChar == '\u0020') {
				iActualLen -= 2;
			} else {
				break;
			}
		}
		int iStart = byteBuffer.position();
		while (iActualLen > 0) {
			char curChar = byteBuffer.getChar(iStart);
			if (curChar == '\u0000' || curChar == '\u3000' || curChar == '\u0020') {
				iActualLen -= 2;
				iStart += 2;
			} else {
				break;
			}
		}
		
		int iSkipStart = iStart - iBufferPosition;
		skip(byteBuffer, iSkipStart);
		byte[] rawActualBytes = new byte[iActualLen];
		byteBuffer.get(rawActualBytes);
		skip(byteBuffer, bytesLength - iActualLen - iSkipStart);
		
		String strRet = new String(rawActualBytes, UTF16LE_ENCODING);
		return strRet;
	}
}
