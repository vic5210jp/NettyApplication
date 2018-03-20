package com.yiqiniu.hkquot.socket.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.api.mktserver.HkQuotMsg;
import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.hkquot.util.HKquotUtil;
import com.yiqiniu.mktserver.service.MktServerService;

@Component
public class ReceivedMessageH {
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	public static String msgLocation;// 12字节文件路径

	public static RandomAccessFile raf;
	public static MappedByteBuffer mapBuffer = null;

	@Resource
	private MktServerService mktServer;
	@Resource
	private DefaultConfig defConfig;

	// 转码message,并落地12字节的文件
	public void transAndWrite(HkQuotMsg hkQuotMsg) {
		try {
			// LOG.error("mktServer===" + mktServer);
			mktServer.processReceivedQuotMsg(hkQuotMsg);
		} catch (Exception e) {
			LOG.error("", e);
			LOG.error("seqNum为{}sendTime为{}的message处理异常....",
					hkQuotMsg.getSeqNum(), hkQuotMsg.getSendTime());
		}

		int seqNum1 = (int) hkQuotMsg.getSeqNum();
		if (mapBuffer == null) {
			openRandomChannelMapped();
		}
		HKquotUtil.writeFile(seqNum1, hkQuotMsg.getSendTime(), mapBuffer);
	}

	/*
	 * private static MappedByteBuffer getMapBuffer() { if (mapBuffer == null)
	 * openRandomChannelMapped(); return mapBuffer; }
	 */

	// xiongpan 实例化一个mapBuffer通道
	private void openRandomChannelMapped() {
		File file0 = new File(defConfig.getVal("messagePath"));
		if (!file0.exists()) {
			try {
				file0.createNewFile();
			} catch (IOException e) {
				LOG.error("", e);
			}
		}

		try {
			if (raf == null)
				raf = new RandomAccessFile(file0, "rw");
			try {
				mapBuffer = raf.getChannel().map(MapMode.READ_WRITE, 0, 12);
			} catch (IOException e) {
				LOG.error("", e);
			}
		} catch (FileNotFoundException e) {
			LOG.error("", e);
		}
	}
}
