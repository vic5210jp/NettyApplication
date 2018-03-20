package com.yiqiniu.hkquot.socket.handler;

import java.nio.MappedByteBuffer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.api.mktserver.HkQuotMsg;
import com.yiqiniu.hkquot.util.HKquotUtil;
import com.yiqiniu.mktserver.service.IReciverClient;
import com.yiqiniu.mktserver.service.MktServerService;
//此类暂未用
@Component
public class MessageHandleServer {
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	static MappedByteBuffer mapBuffer = null;
	
	@Resource
	private MktServerService mktServer;
	@Resource
	public IReciverClient ireciverClient;
	
	/*@PostConstruct
	protected void preInit() {
		 mapBuffer = ireciverClient.getMapBuffer();
	}*/
	
	//转码message,并落地12字节的文件 
	public void transAndWrite(HkQuotMsg hkQuotMsg) {
		try {
		//LOG.error("mktServer===" + mktServer);
			mktServer.processReceivedQuotMsg(hkQuotMsg);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			LOG.error("seqNum为{}sendTime为{}的message处理异常....",
					hkQuotMsg.getSeqNum(), hkQuotMsg.getSendTime());
		}
		
		int seqNum1 = (int) hkQuotMsg.getSeqNum();
		HKquotUtil.writeFile(seqNum1, hkQuotMsg.getSendTime(),mapBuffer);
	}

}
