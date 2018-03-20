package com.yiqiniu.hkquot.socket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.yiqiniu.api.mktserver.HkQuotMsg;

/**
 * author xiongpan
 * The <code>ReceivedMessageHandle</code> 处理港股行情源发来的message。
 * 
 */
//需要此注解
@Component
public class ReceivedMessageHandle extends
		SimpleChannelInboundHandler<HkQuotMsg> implements ApplicationContextAware{
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	private static ApplicationContext applicationContext;
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
		LOG.error("读取数据异常, Channel将会关闭", cause);
	}

	/*
	 * (non-JAVADOC)
	 * 
	 * @see
	 * io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel
	 * .ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HkQuotMsg hkMsg)
			throws Exception {
		// 转码消息
		/*MessageHandleServer msgHandleServer = ReceivedMessageHandle.applicationContext.getBean(MessageHandleServer.class);
		msgHandleServer.transAndWrite(hkMsg);*/
		ReceivedMessageH msgHandleS = ReceivedMessageHandle.applicationContext.getBean(ReceivedMessageH.class);
		//LOG.error("seqNum = {} ,sendTime={} ",hkMsg.getSeqNum(),hkMsg.getSendTime());
		msgHandleS.transAndWrite(hkMsg);
		
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ReceivedMessageHandle.applicationContext = applicationContext;
	}
	
}
