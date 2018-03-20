package com.yiqiniu.hkquot.util;

import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description 提供港股服务所需要的方法
 * 
 * @author xiongpan
 * @date 2015-11-26 下午10:26:00
 * @version v1.0
 */
public class HKquotUtil {
	private static Logger LOG = LoggerFactory.getLogger(HKquotUtil.class);

	/**
	 * 写12字节新文件
	 * 
	 * @author xiongpan
	 * */
	public static void writeFile(int seqNum, long time,
			MappedByteBuffer mapBuffer) {
		try {
			mapBuffer.order(ByteOrder.LITTLE_ENDIAN);
			mapBuffer.rewind();
			mapBuffer.putInt(seqNum);
			mapBuffer.putLong(time);
		} catch (Exception e) {
			LOG.error("序列号为{}的message写文件Message.hkdata失败", seqNum);
			LOG.error("", e);
		}
	}

	// 读取文件中的十二字节 此文件只有十二字节
	public static byte[] getPointData(String fullPath) throws Exception {
		byte[] b = null;
		int curPosition = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(fullPath, "r");
			if (raf.length() == 12) {
				MappedByteBuffer buffer = raf.getChannel().map(
						MapMode.READ_ONLY, 0, raf.length());
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				buffer.position(curPosition);
				// int seq = buffer.getInt(buffer.position() + 4);
				int seq = buffer.getInt(buffer.position());
				// long msgTime = buffer.getLong(buffer.position() + 12);
				long msgTime = buffer.getLong(buffer.position() + 4);
				raf.close();
				LOG.info("断开之后重连服务器发送12字节,seqNum={},currentTime={} {}", seq,
						msgTime, new java.util.Date(msgTime / 1000000));

				b = new byte[12];
				buffer.get(b);
			}
		} catch (Exception e) {
			LOG.error("error occured at " + curPosition, e);
		}
		return b;
	}

}
