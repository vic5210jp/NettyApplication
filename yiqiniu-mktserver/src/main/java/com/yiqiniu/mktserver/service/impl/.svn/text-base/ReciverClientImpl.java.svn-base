package com.yiqiniu.mktserver.service.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.hkquot.socket.handler.ByteToDataDecoder;
import com.yiqiniu.hkquot.socket.handler.ReceivedMessageHandle;
import com.yiqiniu.hkquot.socket.handler.ReceivedMessageH;
import com.yiqiniu.hkquot.util.HKquotUtil;
import com.yiqiniu.mktinfo.persist.po.StkTrdCale;
import com.yiqiniu.mktserver.dao.TrdDayInfoDao;
import com.yiqiniu.mktserver.service.IReciverClient;
import com.yiqiniu.mktserver.util.LocalCache;

/**
 * <code>ReciverClientImpl</code> 实时接收港股行情数据 客户端 接口实现类
 *
 * @author xiongpan
 */
@Service
public class ReciverClientImpl implements IReciverClient {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	// 编码方式
	// public String encoding;
	// 港股源 服务器地址
	public String rtServerHosts;

	private Channel channel;
	// 心跳检查频率, 单位秒
	// public int heartBeat=0;

	@Resource
	private DefaultConfig defConfig;
	@Resource
	protected TrdDayInfoDao trdDayInfoDao;

	public String localParentRoute;
	public String msgLocation;// 12字节文件路径
	public int reconnection;// 断开重连服务器间隔 毫秒
	private volatile boolean isRunning = true;

	// 预加载参数
	@PostConstruct
	protected void preInit() {
		rtServerHosts = defConfig.getVal("rtServerHosts");
		localParentRoute = defConfig.getVal("localParentRoute");
		msgLocation = defConfig.getVal("messagePath");
		reconnection = defConfig.getInt("reconnection");
		LOG.error("断开重连服务器时间间隔=", reconnection);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fortune.client.main.IGatewayClient#asyncStart()
	 */
	@Override
	public void asyncStart() {
		if (!isTradeDay()) {
			return;
		}

		isRunning = true;
		Thread oTh = new Thread(new Runnable() {
			@Override
			public void run() {
				start();
			}
		});
		oTh.setDaemon(true);
		oTh.setName("港股接收客户端 - Thread");
		LOG.info("港股接收客户端 - Thread Name={}！", Thread.currentThread().getName());
		oTh.start();
	}

	public void start() {
		String[] asHosts = rtServerHosts.split(";");
		for (String host : asHosts) {
			String[] asHost = host.split(":");
			final String sHostAddr = asHost[0];
			final int iPort = Integer.parseInt(asHost[1]);
			innerStart(sHostAddr, iPort);
		}
	}

	private void innerStart(final String sHost, final int iPort) {
		// EventLoopGroup oWorkerG = new OioEventLoopGroup(10);
		EventLoopGroup oWorkerG = new NioEventLoopGroup(10);
		try {
			// set up the client connector <Bootstrap>
			Bootstrap oBS = new Bootstrap();
			oBS.group(oWorkerG);
			// oBS.channel(OioSocketChannel.class);
			oBS.channel(NioSocketChannel.class);
			oBS.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_REUSEADDR, true);
			oBS.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					// ch.config().setMaxMessagesPerRead(1000);
					// ch.pipeline().addLast("timeout", new IdleStateHandler(0,
					// heartBeat, 0));
					ch.pipeline().addLast("decoder", new ByteToDataDecoder());

					ch.pipeline().addLast("received",
							new ReceivedMessageHandle());

					ch.closeFuture().addListener(new ChannelFutureListener() {
						public void operationComplete(ChannelFuture cf)
								throws Exception {
							// 注销掉当前的连接
							LOG.info("连接断开，注销掉当前的连接= {}:{}", sHost, iPort);
							cf.channel().close();
							channel = null;
						}
					});
				}
			});
			// 以下实现客户端重连
			while (isRunning) {
				try {
					LOG.info("尝试连接到 {}:{}", sHost, iPort);
					ChannelFuture f = oBS.connect(sHost, iPort).sync();
					if (f.isSuccess()) {
						writeToServer(f);
						f.addListener(new ChannelFutureListener() {
							public void operationComplete(ChannelFuture cf)
									throws Exception {
								channel = cf.channel();
							}
						});
					}
					LOG.info("连接到港股行情源{}:{}成功!", sHost, iPort);

					// 等待直到连接断开
					f.channel().closeFuture().sync();
				} catch (Exception e) {
					LOG.error("连接到服务器异常", e);
				}
				Thread.sleep(reconnection);
				// Thread.sleep(2000);
			}
		} catch (Exception e) {
			LOG.error("客户端重连异常");
		} finally {
			oWorkerG.shutdownGracefully();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fortune.client.main.IGatewayClient#stop()
	 */
	@Override
	public void stop() {
		// HKquotUtil.deleteHisData(path0);//保留适当的历史数据 可配
		closeMapbufferAndRaf();
		isRunning = false;// stop之后不会重连
		if (null != channel)
			channel.close();
		LOG.info("channel close success!");
	}

	// 判断当天是否为港股交易日
	private boolean isTradeDay() {
		Date date = new Date();
		String tdDate = DateUtils.dateToString(date,
				DateUtils.TimeFormatter.YYYY_MM_DD);
		String regionCode = defConfig.getVal("region.code");
		StkTrdCale stkTrdCale = LocalCache.STK_TRD_CAL_MAP.get(tdDate);
		if (stkTrdCale == null) {
			try {
				stkTrdCale = trdDayInfoDao.findTrdByNormalDay(tdDate,
						regionCode);
			} catch (Exception e) {
				LOG.error("获取港股交易日异常", e);
			}
			if (stkTrdCale != null) {
				LocalCache.STK_TRD_CAL_MAP.put(tdDate, stkTrdCale);
			}
		}
		if (stkTrdCale == null) {
			LOG.error("获取港股交易日为空...");
			return false;
		}
		// 判断当天是否为交易日
		if (!stkTrdCale.getIsTradeDay()) {
			LOG.info("{}为港股非交易日...", tdDate);
			return false;
		}
		return true;
	}

	// 发送12字节报文到服务器 配置文件直接配置文件名路径 2016-01-12
	public void writeToServer(ChannelFuture f) throws Exception {
		LOG.info("12字节文件路径:msgLocation={}", msgLocation);
		File file = new File(msgLocation);// 文件夹
		if (!file.exists()) {
			LOG.info("文件{}不存在,连接服务器成功发送12字节的0");
			byte[] data = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			f.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
		} else {
			byte[] target = HKquotUtil.getPointData(msgLocation);
			if (target.length > 0) {
				f.channel().writeAndFlush(Unpooled.wrappedBuffer(target));
			} else {
				LOG.info("文件{}为空,发送12字节的0", msgLocation);
				byte[] data = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
				f.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
			}
		}
	}

	// 关闭MappedByteBuffer通道
	public void closeMapbufferAndRaf() {
		if (null != ReceivedMessageH.mapBuffer) {
			try {
				// 被MappedByteBuffer打开的文件只有在垃圾收集时才会被关闭，而这个点是不确定的。故主动关闭
				java.lang.reflect.Method getCleanerMethod = ReceivedMessageH.mapBuffer
						.getClass().getMethod("cleaner", new Class[0]);
				getCleanerMethod.setAccessible(true);
				sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod
						.invoke(ReceivedMessageH.mapBuffer, new Object[0]);
				cleaner.clean();
				LOG.info("clean MappedByteBuffer success!");
			} catch (Exception e) {
				LOG.error("MappedByteBuffer mapBuffer close Exception....", e);
			}
		}
		ReceivedMessageH.mapBuffer = null;
		if (null != ReceivedMessageH.raf) {
			try {
				ReceivedMessageH.raf.close();
				LOG.info("close RandomAccessFile success!");
			} catch (IOException e) {
				LOG.error("RandomAccessFile raf close Exception....", e);
			}
		}
		ReceivedMessageH.raf = null;

	}

}
