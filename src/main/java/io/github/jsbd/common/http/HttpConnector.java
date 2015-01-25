package io.github.jsbd.common.http;

import io.github.jsbd.common.lang.Holder;
import io.github.jsbd.common.lang.KeyTransformer;
import io.github.jsbd.common.lang.transport.DefaultHolder;
import io.github.jsbd.common.lang.transport.Receiver;
import io.github.jsbd.common.lang.transport.Sender;
import io.github.jsbd.common.lang.transport.SenderSync;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

public class HttpConnector implements Sender, SenderSync {

  private final Logger             logger           = LoggerFactory.getLogger(HttpConnector.class);

  private String                   name             = "HTTPConnector";
  private String                   destIp           = null;
  private int                      destPort         = -1;

  private ClientBootstrap          bootstrap;

  private ScheduledExecutorService exec             = Executors.newSingleThreadScheduledExecutor();

  private ChannelDownstreamHandler encoder;
  private ChannelUpstreamHandler   decoder;

  private Channel                  channel;
  private Receiver                 messageClosure;
  // 默认使用长连接
  private boolean                  keepAlive        = false;
  private long                     retryTimeout     = 1000;

  private KeyTransformer           keyTransformer   = new KeyTransformer();
  private Holder                   context          = null;

  private int                      waitTimeout      = 100 * 1000;
  // 100M
  private int                      maxContentLength = 100 * 1024 * 1024;

  class ResponseFuture<V> extends FutureTask<V> {

    public ResponseFuture() {
      super(new Callable<V>() {
        public V call() throws Exception {
          return null;
        }
      });
    }

    public void set(V v) {
      super.set(v);
    }
  }

  public HttpConnector(String name) {
    this.name = name;
    this.bootstrap = new ClientBootstrap();
  }

  public void start() {

    if (null == destIp || destIp.equals("")) {
      logger.warn(" destIp is null, disable this connector.");
      return;
    }

    bootstrap.setFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = new DefaultChannelPipeline();
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpChunkAggregator(maxContentLength));

        pipeline.addLast("nettyEncoder", encoder);
        pipeline.addLast("nettyDecoder", decoder);

        pipeline.addLast("handler", new HttpResponseHandler());
        return pipeline;
      }
    });

    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("keepAlive", keepAlive);

    doConnect();
  }

  public void stop() {
    this.exec.shutdownNow();
    this.channel.disconnect();
    this.bootstrap.releaseExternalResources();
  }

  public void send(Object message) {
    if (message != null) {
      if (channel != null) {
        channel.write(message);
      } else {
        logger.warn("missing channel, message droped.", message);
      }
    }
  }

  @Override
  public void send(Object bean, Receiver receiver) {
    if (null != bean) {
      if (channel != null) {
        Object key = keyTransformer.transform(bean);
        getContext().put(key, receiver);
        channel.write(bean);
      } else {
        logger.warn("missing channel, message droped.", bean);
      }
    }
  }

  @Override
  public Object sendAndWait(Object bean) {
    return sendAndWait(bean, waitTimeout, TimeUnit.MILLISECONDS);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object sendAndWait(Object bean, long duration, TimeUnit units) {
    if (null == bean) {
      return null;
    }

    if (channel != null) {
      Object key = keyTransformer.transform(bean);
      ResponseFuture responseFuture = new ResponseFuture();
      getContext().put(key, responseFuture);

      channel.write(bean);
      try {
        return responseFuture.get(duration, units);
      } catch (Exception e) {
        logger.error("", e);
        return null;
      } finally {
        responseFuture = (ResponseFuture) getContext().getAndRemove(key);
        if (responseFuture != null) {
          responseFuture.cancel(false);
        }
      }
    } else {
      logger.warn("missing channel, message droped.", bean);
      return null;
    }
  }

  private class HttpResponseHandler extends SimpleChannelUpstreamHandler {
    private final Logger logger = LoggerFactory.getLogger(HttpResponseHandler.class);

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      logger.debug("channelConnected: " + e.getChannel());
      channel = e.getChannel();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
      logger.debug("channelDisconnected: " + e.getChannel());
      channel = null;
      exec.submit(new Runnable() {
        public void run() {
          onSessionClosed(e.getChannel());
        }
      });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      logger.error("exceptionCaught:", e.getCause());
      e.getChannel().close();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      if (logger.isTraceEnabled()) {
        logger.trace("messageReceived: " + e.getMessage());
      }

      Object key = keyTransformer.transform(e.getMessage());
      if (key != null) {
        Object context = getContext().getAndRemove(key);
        if (null != context) {
          try {
            if (context instanceof ResponseFuture) {
              ((ResponseFuture) context).set(e.getMessage());
            }
            if (context instanceof Receiver) {
              ((Receiver) context).messageReceived(e.getMessage());
              return;
            }
          } catch (Exception e1) {
            logger.error("messageReceived error.", e1);
          }
        } else {
          if (null != messageClosure) {
            messageClosure.messageReceived(e.getMessage());
          } else {
            logger.warn("missing closure, ignore incoming msg:" + e.getMessage());
          }
        }
      }

    }
  }

  private void onSessionClosed(Channel channel) {
    if (logger.isInfoEnabled()) {
      logger.info(getName() + " channel : " + channel + " closed, retry connect...");
    }
    doConnect();
  }

  private void doConnect() {
    if (null == destIp || destIp.equals("")) {
      logger.warn(getName() + " destIp is null, disable this connector.");
      return;
    }

    ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(destIp, destPort));
    connectFuture.addListener(new ChannelFutureListener() {

      @Override
      public void operationComplete(final ChannelFuture connectFuture) throws Exception {
        exec.submit(new Runnable() {

          public void run() {
            onConnectComplete(connectFuture);
          }
        });
      }
    });
  }

  private void onConnectComplete(ChannelFuture connectFuture) {
    if (!connectFuture.isSuccess()) {
      if (logger.isInfoEnabled()) {
        logger.info(getName() + " connect [" + this.destIp + ":" + this.destPort + "] failed, retry...");
      }
      exec.schedule(new Runnable() {

        public void run() {
          doConnect();
        }
      }, retryTimeout, TimeUnit.MILLISECONDS);
    }
  }

  public void setMessageClosure(Receiver messageClosure) {
    this.messageClosure = messageClosure;
  }

  public void setRetryTimeout(long retryTimeout) {
    this.retryTimeout = retryTimeout;
  }

  public Channel getChannel() {
    return channel;
  }

  public String getName() {
    return this.name;
  }

  public void setDestIp(String destIp) {
    this.destIp = destIp;
  }

  public void setDestPort(int destPort) {
    this.destPort = destPort;
  }

  public void setKeepAlive(boolean keepAlive) {
    this.keepAlive = keepAlive;
  }

  public void setEncoder(ChannelDownstreamHandler encoder) {
    this.encoder = encoder;
  }

  public void setDecoder(ChannelUpstreamHandler decoder) {
    this.decoder = decoder;
  }
  public void setContext(Holder context) {
    this.context = context;
  }

  public Holder getContext() {
    if (this.context == null) {
      context = new DefaultHolder();
    }
    return context;
  }

  public void setKeyTransformer(KeyTransformer keyTransformer) {
    this.keyTransformer = keyTransformer;
  }

  public void setMaxContentLength(int maxContentLength) {
    this.maxContentLength = maxContentLength;
  }
}