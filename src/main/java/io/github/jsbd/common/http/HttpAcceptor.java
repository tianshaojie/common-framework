package io.github.jsbd.common.http;

import io.github.jsbd.common.http.endpoint.DefaultEndpointFactory;
import io.github.jsbd.common.http.endpoint.Endpoint;
import io.github.jsbd.common.http.endpoint.EndpointFactory;
import io.github.jsbd.common.http.reactor.ConstantResponseReactor;
import io.github.jsbd.common.http.reactor.HttpReactor;
import io.github.jsbd.common.http.response.ConstantResponse;
import io.github.jsbd.common.lang.Holder;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.lang.transport.Receiver;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.DefaultServerSocketChannelConfig;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpAcceptor {

  private static final Logger              logger           = LoggerFactory.getLogger(HttpAcceptor.class);

  private static final int                 MAX_RETRY        = 20;
  // 30s
  private static final long                RETRY_TIMEOUT    = 30 * 1000;

  private ServerBootstrap                  bootstrap;
  private Channel                          channel;
  private String                           acceptIp         = "0.0.0.0";
  private int                              acceptPort       = 8080;
  // in seconds
  private int                              idleTime         = 0;

  private Transformer<HttpRequest, Object> requestDecoder   = null;

  private HttpReactor                      errorReactor     = new ConstantResponseReactor(ConstantResponse.get400NobodyResponse());

  private EndpointFactory                  endpointFactory  = new DefaultEndpointFactory();

  // 100M
  private int                              maxContentLength = 100 * 1024 * 1024;

  public HttpAcceptor() {
    bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
  }

  public void start() throws IOException {

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = new DefaultChannelPipeline();
        // HttpServerCodec是非线程安全的,不能所有Channel使用同一个
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpChunkAggregator(maxContentLength));

        // pipeline.addLast("idleHandler", new IdleStateHandler(new
        // HashedWheelTimer(), 0, 0, idleTime, TimeUnit.SECONDS));
        pipeline.addLast("handler", new HttpRequestHandler());
        return pipeline;
      }
    });

    bootstrap.setOption("allIdleTime", idleTime);

    bootstrap.setOption("child.keepAlive", true);
    bootstrap.setOption("child.tcpNoDelay", true);
    bootstrap.setOption("child.soLinger", -1);
    bootstrap.setOption("child.sendBufferSize", 32 * 1024);

    int retryCount = 0;
    boolean binded = false;
    do {
      try {
        channel = bootstrap.bind(new InetSocketAddress(this.acceptIp, this.acceptPort));
        binded = true;
      } catch (ChannelException e) {
        logger.warn("start failed : " + e + ", and retry...");

        // 对绑定异常再次进行尝试
        retryCount++;
        if (retryCount >= MAX_RETRY) {
          // 超过最大尝试次数
          throw e;
        }
        try {
          Thread.sleep(RETRY_TIMEOUT);
        } catch (InterruptedException e1) {
        }
      }
    } while (!binded);

    DefaultServerSocketChannelConfig config = (DefaultServerSocketChannelConfig) (channel.getConfig());
    config.setBacklog(10240);
    config.setReuseAddress(true);
    config.setReceiveBufferSize(1024);

    logger.info("start succeed in " + acceptIp + ":" + acceptPort);
  }

  public void stop() {
    if (null != channel) {
      channel.unbind();
      channel = null;
    }
  }

  private class HttpRequestHandler extends IdleStateAwareChannelUpstreamHandler {
    private final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      if (logger.isTraceEnabled()) {
        logger.trace("message received {}", e.getMessage());
      }

      DefaultHttpRequest request = (DefaultHttpRequest) e.getMessage();
      Object signal = requestDecoder.transform(request);
      if (null != signal) {
        Endpoint endpoint = TransportUtil.getEndpointOfSession(e.getChannel());
        if (null != endpoint) {
          TransportUtil.attachSender(signal, endpoint);
          TransportUtil.attachRequest(signal, request);
          endpoint.messageReceived(signal);
        } else {
          logger.warn("missing endpoint, ignore incoming msg:", signal);
        }
      } else {
        if (null != errorReactor) {
          logger.error("content is null, try send back client empty HttpResponse.");
          errorReactor.onHttpRequest(null, request);
        } else {
          logger.error("Can not transform bean for req [" + request + "], and missing errorHandler.");
        }
      }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      if (!(e.getCause() instanceof IOException)) {
        logger.error("exceptionCaught: ", e.getCause());
      }
      ctx.getChannel().close();
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      if (logger.isDebugEnabled()) {
        logger.debug("channelClosed: [" + e.getChannel().getRemoteAddress() + "]");
      }
      Endpoint endpoint = TransportUtil.getEndpointOfSession(e.getChannel());
      if (null != endpoint) {
        endpoint.stop();
      }
      TransportUtil.detachEndpointToSession(e.getChannel());
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      if (logger.isDebugEnabled()) {
        logger.debug("channelOpen: [" + e.getChannel().getRemoteAddress() + "]");
      }
      Endpoint endpoint = endpointFactory.createEndpoint(e.getChannel());
      if (null != endpoint) {
        TransportUtil.attachEndpointToSession(e.getChannel(), endpoint);
        endpoint.setAddr((InetSocketAddress) e.getChannel().getRemoteAddress());
      }
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
      if (logger.isInfoEnabled()) {
        logger.info("channelIdle: " + e.getState().name() + " for " + (System.currentTimeMillis() - e.getLastActivityTimeMillis())
            + " milliseconds, close channel[" + e.getChannel().getRemoteAddress() + "]");
      }
      e.getChannel().close();
    }

  }

  public void setMaxContentLength(int maxContentLength) {
    this.maxContentLength = maxContentLength;
  }

  public void setAcceptIp(String acceptIp) {
    this.acceptIp = acceptIp;
  }

  public void setAcceptPort(int acceptPort) {
    this.acceptPort = acceptPort;
  }

  public void setIdleTime(int idleTime) {
    this.idleTime = idleTime;
  }

  public void setRequestDecoder(Transformer<HttpRequest, Object> requestDecoder) {
    this.requestDecoder = requestDecoder;
  }

  public void setErrorReactor(HttpReactor errorReactor) {
    this.errorReactor = errorReactor;
  }

  public void setMessageClosure(Receiver messageClosure) {
    endpointFactory.setMessageClosure(messageClosure);
  }

  public void setResponseContext(Holder responseContext) {
    endpointFactory.setResponseContext(responseContext);
  }

  public void setResponseEncoder(Transformer<Object, HttpResponse> responseEncoder) {
    endpointFactory.setResponseEncoder(responseEncoder);
  }

  public void setEndpointFactory(EndpointFactory endpointFactory) {
    this.endpointFactory = endpointFactory;
  }

}
