/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.bootstrap.util.spring.PropertyPlaceholderHelper;
import com.navercorp.pinpoint.common.util.PropertyUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 * @author netspider
 */
public class ProfilerConfig {
    private static final Logger logger = Logger.getLogger(ProfilerConfig.class.getName());
    private static final String DEFAULT_IP = "127.0.0.1";

    private final Properties properties;
    private final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");

    public static interface ValueResolver {
        String resolve(String value, Properties properties);
    }

    private static class BypassResolver implements ValueResolver {
        public static final ValueResolver RESOLVER = new BypassResolver();

        @Override
        public String resolve(String value, Properties properties) {
            return value;
        }
    }

    private class PlaceHolderResolver implements ValueResolver {
        @Override
        public String resolve(String value, Properties properties) {
            if (value == null) {
                return null;
            }
            return propertyPlaceholderHelper.replacePlaceholders(value, properties);
        }
    }

    public static ProfilerConfig load(String pinpointConfigFileName) throws IOException {
        try {
            Properties properties = PropertyUtils.loadProperty(pinpointConfigFileName);
            return new ProfilerConfig(properties);
        } catch (FileNotFoundException fe) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, pinpointConfigFileName + " file does not exist. Please check your configuration.");
            }
            throw fe;
        } catch (IOException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, pinpointConfigFileName + " file I/O error. Error:" + e.getMessage(), e);
            }
            throw e;
        }
    }

    private boolean profileEnable = false;

    private String collectorSpanServerIp = DEFAULT_IP;
    private int collectorSpanServerPort = 9996;

    private String collectorStatServerIp = DEFAULT_IP;
    private int collectorStatServerPort = 9995;

    private String collectorTcpServerIp = DEFAULT_IP;
    private int collectorTcpServerPort = 9994;

    private int spanDataSenderWriteQueueSize = 1024 * 5;
    private int spanDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    private int spanDataSenderSocketTimeout = 1000 * 3;
    private int spanDataSenderChunkSize = 1024 * 16;

    private int statDataSenderWriteQueueSize = 1024 * 5;
    private int statDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    private int statDataSenderSocketTimeout = 1000 * 3;
    private int statDataSenderChunkSize = 1024 * 16;
    
    private boolean tcpDataSenderCommandAcceptEnable = false;

    private boolean traceAgentActiveThread = true;

    private int callStackMaxDepth = 512;
    
    private int jdbcSqlCacheSize = 1024;

    private boolean tomcatHidePinpointHeader = true;
    private Filter<String> tomcatExcludeUrlFilter = new SkipFilter<String>();

    private boolean ibatis = true;

    private boolean mybatis = true;

    private boolean redis = true;
    private boolean redisPipeline = true;

    private boolean tomcatProfileGetParameter = false;
    private boolean tomcatProfilePostParameter = false;

    /**
     * apache http client 3
     */
    private boolean apacheHttpClient3Profile = true;
    private boolean apacheHttpClient3ProfileCookie = false;
    private DumpType apacheHttpClient3ProfileCookieDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient3ProfileCookieSamplingRate = 1;
    private boolean apacheHttpClient3ProfileEntity = false;
    private DumpType apacheHttpClient3ProfileEntityDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient3ProfileEntitySamplingRate = 1;
    private boolean apacheHttpClient3ProfileIo = true;
    
    /**
     * apache http client 4
     */
    private boolean apacheHttpClient4Profile = true;
    private boolean apacheHttpClient4ProfileCookie = false;
    private DumpType apacheHttpClient4ProfileCookieDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient4ProfileCookieSamplingRate = 1;
    private boolean apacheHttpClient4ProfileEntity = false;
    private DumpType apacheHttpClient4ProfileEntityDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient4ProfileEntitySamplingRate = 1;
    private boolean apacheHttpClient4ProfileStatusCode = true;
    private boolean apacheHttpClient4ProfileIo = true;

    /**
     * apache nio http client
     */
    private boolean apacheNIOHttpClient4Profile = true;

    // Sampling
    private boolean samplingEnable = true;
    private int samplingRate = 1;

    // span buffering
    private boolean ioBufferingEnable;
    private int ioBufferingBufferSize;

    private int profileJvmCollectInterval;

    private Filter<String> profilableClassFilter = new SkipFilter<String>();

    private final long DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL = 5 * 60 * 1000L;
    private long agentInfoSendRetryInterval = DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL;

    private String applicationServerType;
    private List<String> applicationTypeDetectOrder = Collections.emptyList();
    private List<String> disabledPlugins = Collections.emptyList();
    private boolean log4jLoggingTransactionInfo;
    private boolean logbackLoggingTransactionInfo;
    
    private boolean propagateInterceptorException = false;

    public ProfilerConfig() {
        this.properties = new Properties();
    }

    public ProfilerConfig(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }
        this.properties = properties;
        readPropertyValues();
    }

    public String getCollectorSpanServerIp() {
        return collectorSpanServerIp;
    }

    public int getCollectorSpanServerPort() {
        return collectorSpanServerPort;
    }

    public String getCollectorStatServerIp() {
        return collectorStatServerIp;
    }

    public int getCollectorStatServerPort() {
        return collectorStatServerPort;
    }

    public String getCollectorTcpServerIp() {
        return collectorTcpServerIp;
    }

    public int getCollectorTcpServerPort() {
        return collectorTcpServerPort;
    }

    public int getStatDataSenderWriteQueueSize() {
        return statDataSenderWriteQueueSize;
    }

    public int getStatDataSenderSocketSendBufferSize() {
        return statDataSenderSocketSendBufferSize;
    }

    public int getStatDataSenderSocketTimeout() {
        return statDataSenderSocketTimeout;
    }

    public int getSpanDataSenderWriteQueueSize() {
        return spanDataSenderWriteQueueSize;
    }

    public int getSpanDataSenderSocketSendBufferSize() {
        return spanDataSenderSocketSendBufferSize;
    }

    public boolean isTcpDataSenderCommandAcceptEnable() {
        return tcpDataSenderCommandAcceptEnable;
    }

    public boolean isTraceAgentActiveThread() {
        return traceAgentActiveThread;
    }

    public int getSpanDataSenderSocketTimeout() {
        return spanDataSenderSocketTimeout;
    }

    public int getSpanDataSenderChunkSize() {
        return spanDataSenderChunkSize;
    }

    public int getStatDataSenderChunkSize() {
        return statDataSenderChunkSize;
    }

    public boolean isProfileEnable() {
        return profileEnable;
    }

    public int getJdbcSqlCacheSize() {
        return jdbcSqlCacheSize;
    }

    public boolean isSamplingEnable() {
        return samplingEnable;
    }


    public int getSamplingRate() {
        return samplingRate;
    }

    public boolean isIoBufferingEnable() {
        return ioBufferingEnable;
    }

    public int getIoBufferingBufferSize() {
        return ioBufferingBufferSize;
    }

    public int getProfileJvmCollectInterval() {
        return profileJvmCollectInterval;
    }

    public long getAgentInfoSendRetryInterval() {
        return agentInfoSendRetryInterval;
    }

    public boolean isTomcatHidePinpointHeader() {
        return tomcatHidePinpointHeader;
    }

    public Filter<String> getTomcatExcludeUrlFilter() {
        return tomcatExcludeUrlFilter;
    }

    //-----------------------------------------
    // http apache client 3

    public boolean isApacheHttpClient3Profile() {
        return apacheHttpClient3Profile;
    }
    
    public boolean isApacheHttpClient3ProfileCookie() {
        return apacheHttpClient3ProfileCookie;
    }

    public DumpType getApacheHttpClient3ProfileCookieDumpType() {
        return apacheHttpClient3ProfileCookieDumpType;
    }

    public int getApacheHttpClient3ProfileCookieSamplingRate() {
        return apacheHttpClient3ProfileCookieSamplingRate;
    }

    public boolean isApacheHttpClient3ProfileEntity() {
        return apacheHttpClient3ProfileEntity;
    }

    public DumpType getApacheHttpClient3ProfileEntityDumpType() {
        return apacheHttpClient3ProfileEntityDumpType;
    }

    public int getApacheHttpClient3ProfileEntitySamplingRate() {
        return apacheHttpClient3ProfileEntitySamplingRate;
    }
    
    public boolean isApacheHttpClient3ProfileIo() {
        return apacheHttpClient3ProfileIo;
    }
    
    //-----------------------------------------
    // http apache client 4
    public boolean isApacheHttpClient4Profile() {
        return apacheHttpClient4Profile;
    }

    public boolean isApacheHttpClient4ProfileCookie() {
        return apacheHttpClient4ProfileCookie;
    }

    public DumpType getApacheHttpClient4ProfileCookieDumpType() {
        return apacheHttpClient4ProfileCookieDumpType;
    }

    public int getApacheHttpClient4ProfileCookieSamplingRate() {
        return apacheHttpClient4ProfileCookieSamplingRate;
    }

    public boolean isApacheHttpClient4ProfileEntity() {
        return apacheHttpClient4ProfileEntity;
    }

    public DumpType getApacheHttpClient4ProfileEntityDumpType() {
        return apacheHttpClient4ProfileEntityDumpType;
    }

    public int getApacheHttpClient4ProfileEntitySamplingRate() {
        return apacheHttpClient4ProfileEntitySamplingRate;
    }
    
    public boolean isApacheHttpClient4ProfileStatusCode() {
        return apacheHttpClient4ProfileStatusCode;
    }
    
    public boolean isApacheHttpClient4ProfileIo() {
        return apacheHttpClient4ProfileIo;
    }

    //-----------------------------------------
    // org/apache/http/impl/nio/*
    public boolean getApacheNIOHttpClient4Profile() {
        return apacheNIOHttpClient4Profile;
    }

    public boolean isIBatisEnabled() {
        return ibatis;
    }

    public boolean isMyBatisEnabled() {
        return mybatis;
    }

    public boolean isRedisEnabled() {
        return redis;
    }

    public boolean isRedisPipelineEnabled() {
        return redisPipeline;
    }

    public boolean isTomcatProfileGetParameter() {
        return tomcatProfileGetParameter;
    }

    public boolean isTomcatProfilePostParameter() {
        return tomcatProfilePostParameter;
    }

    public Filter<String> getProfilableClassFilter() {
        return profilableClassFilter;
    }
    
    public List<String> getApplicationTypeDetectOrder() {
        return applicationTypeDetectOrder;
    }
    
    public List<String> getDisabledPlugins() {
        return disabledPlugins;
    }

    public void setDisabledPlugins(List<String> disabledPlugins) {
        this.disabledPlugins = disabledPlugins;
    }

    public String getApplicationServerType() {
        return applicationServerType;
    }

    public void setApplicationServerType(String applicationServerType) {
        this.applicationServerType = applicationServerType;
    }
    
    public boolean isLog4jLoggingTransactionInfo() {
        return this.log4jLoggingTransactionInfo;
    }
    

    public boolean isLogbackLoggingTransactionInfo() {
        return this.logbackLoggingTransactionInfo;
    }
    
    public int getCallStackMaxDepth() {
        return callStackMaxDepth;
    }

    public void setCallStackMaxDepth(int callStackMaxDepth) {
        this.callStackMaxDepth = callStackMaxDepth;
    }
    
    public boolean isPropagateInterceptorException() {
        return propagateInterceptorException;
    }

    // for test
    void readPropertyValues() {
        // TODO : use Properties' default value instead of using a temp variable.
        final ValueResolver placeHolderResolver = new PlaceHolderResolver();

        this.profileEnable = readBoolean("profiler.enable", true);


        this.collectorSpanServerIp = readString("profiler.collector.span.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorSpanServerPort = readInt("profiler.collector.span.port", 9996);

        this.collectorStatServerIp = readString("profiler.collector.stat.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorStatServerPort = readInt("profiler.collector.stat.port", 9995);

        this.collectorTcpServerIp = readString("profiler.collector.tcp.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorTcpServerPort = readInt("profiler.collector.tcp.port", 9994);

        this.spanDataSenderWriteQueueSize = readInt("profiler.spandatasender.write.queue.size", 1024 * 5);
        this.spanDataSenderSocketSendBufferSize = readInt("profiler.spandatasender.socket.sendbuffersize", 1024 * 64 * 16);
        this.spanDataSenderSocketTimeout = readInt("profiler.spandatasender.socket.timeout", 1000 * 3);
        this.spanDataSenderChunkSize = readInt("profiler.spandatasender.chunk.size", 1024 * 16);

        this.statDataSenderWriteQueueSize = readInt("profiler.statdatasender.write.queue.size", 1024 * 5);
        this.statDataSenderSocketSendBufferSize = readInt("profiler.statdatasender.socket.sendbuffersize", 1024 * 64 * 16);
        this.statDataSenderSocketTimeout = readInt("profiler.statdatasender.socket.timeout", 1000 * 3);
        this.statDataSenderChunkSize = readInt("profiler.statdatasender.chunk.size", 1024 * 16);

        this.tcpDataSenderCommandAcceptEnable = readBoolean("profiler.tcpdatasender.command.accept.enable", false);

        this.traceAgentActiveThread = readBoolean("profiler.pinpoint.activethread", true);

        // CallStck
        this.callStackMaxDepth = readInt("profiler.callstack.max.depth", 64);
        if(this.callStackMaxDepth < 2) {
            this.callStackMaxDepth = 2;
        }
        
        // JDBC
        this.jdbcSqlCacheSize = readInt("profiler.jdbc.sqlcachesize", 1024);

        this.tomcatHidePinpointHeader = readBoolean("profiler.tomcat.hidepinpointheader", true);
        final String tomcatExcludeURL = readString("profiler.tomcat.excludeurl", "");
        if (!tomcatExcludeURL.isEmpty()) {
            this.tomcatExcludeUrlFilter = new ExcludeUrlFilter(tomcatExcludeURL);
        }
        this.tomcatProfileGetParameter = readBoolean("profiler.tomcat.getparameter", false);
        this.tomcatProfilePostParameter = readBoolean("profiler.tomcat.postparameter", false);

        /**
         * apache http client 3
         */
        this.apacheHttpClient3Profile = readBoolean("profiler.apache.httpclient3", true);
        this.apacheHttpClient3ProfileCookie = readBoolean("profiler.apache.httpclient3.cookie", false);
        this.apacheHttpClient3ProfileCookieDumpType = readDumpType("profiler.apache.httpclient3.cookie.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient3ProfileCookieSamplingRate = readInt("profiler.apache.httpclient3.cookie.sampling.rate", 1);

        this.apacheHttpClient3ProfileEntity = readBoolean("profiler.apache.httpclient3.entity", false);
        this.apacheHttpClient3ProfileEntityDumpType = readDumpType("profiler.apache.httpclient3.entity.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient3ProfileEntitySamplingRate = readInt("profiler.apache.httpclient3.entity.sampling.rate", 1);
        this.apacheHttpClient3ProfileIo = readBoolean("profiler.apache.httpclient3.io", true);
        /**
         * apache http client 4
         */
        this.apacheHttpClient4Profile = readBoolean("profiler.apache.httpclient4", true);
        this.apacheHttpClient4ProfileCookie = readBoolean("profiler.apache.httpclient4.cookie", false);
        this.apacheHttpClient4ProfileCookieDumpType = readDumpType("profiler.apache.httpclient4.cookie.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient4ProfileCookieSamplingRate = readInt("profiler.apache.httpclient4.cookie.sampling.rate", 1);

        this.apacheHttpClient4ProfileEntity = readBoolean("profiler.apache.httpclient4.entity", false);
        this.apacheHttpClient4ProfileEntityDumpType = readDumpType("profiler.apache.httpclient4.entity.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient4ProfileEntitySamplingRate = readInt("profiler.apache.httpclient4.entity.sampling.rate", 1);

        this.apacheHttpClient4ProfileStatusCode = readBoolean("profiler.apache.httpclient4.entity.statuscode", true);
        this.apacheHttpClient4ProfileIo = readBoolean("profiler.apache.httpclient4.io", true);
        /**
         * apache nio http client
         */
        this.apacheNIOHttpClient4Profile = readBoolean("profiler.apache.nio.httpclient4", true);

        /**
         * log4j
         */
        this.log4jLoggingTransactionInfo = readBoolean("profiler.log4j.logging.transactioninfo", false);
        
        /**
         * logback
         */
        this.logbackLoggingTransactionInfo = readBoolean("profiler.logback.logging.transactioninfo", false);
        
        // redis & nBase-ARC
        this.redis = readBoolean("profiler.redis", true);
        this.redisPipeline = readBoolean("profiler.redis.pipeline", true);

        this.ibatis = readBoolean("profiler.orm.ibatis", true);

        this.mybatis = readBoolean("profiler.orm.mybatis", true);

        this.samplingEnable = readBoolean("profiler.sampling.enable", true);
        this.samplingRate = readInt("profiler.sampling.rate", 1);

        // configuration for sampling and IO buffer 
        this.ioBufferingEnable = readBoolean("profiler.io.buffering.enable", true);

        // it may be a problem to be here.  need to modify(delete or move or .. )  this configuration.
        this.ioBufferingBufferSize = readInt("profiler.io.buffering.buffersize", 20);

        // JVM
        this.profileJvmCollectInterval = readInt("profiler.jvm.collect.interval", 1000);

        this.agentInfoSendRetryInterval = readLong("profiler.agentInfo.send.retry.interval", DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL);

        // service type
        this.applicationServerType = readString("profiler.applicationservertype", null);

        // application type detector order
        this.applicationTypeDetectOrder = readList("profiler.type.detect.order");
        
        this.disabledPlugins = readList("profiler.plugin.disable");
        
        // TODO have to remove        
        // profile package included in order to test "call stack view".
        // this config must not be used in service environment because the size of  profiling information will get heavy.
        // We may need to change this configuration to regular expression.
        final String profilableClass = readString("profiler.include", "");
        if (!profilableClass.isEmpty()) {
            this.profilableClassFilter = new ProfilableClassFilter(profilableClass);
        }
        
        this.propagateInterceptorException = readBoolean("profiler.interceptor.exception.propagate", false);

        logger.info("configuration loaded successfully.");
    }


    public String readString(String propertyName, String defaultValue) {
        return readString(propertyName, defaultValue, BypassResolver.RESOLVER);
    }

    public String readString(String propertyName, String defaultValue, ValueResolver valueResolver) {
        if (valueResolver == null) {
            throw new NullPointerException("valueResolver must not be null");
        }
        String value = properties.getProperty(propertyName, defaultValue);
        value = valueResolver.resolve(value, properties);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(propertyName + "=" + value);
        }
        return value;
    }

    public int readInt(String propertyName, int defaultValue) {
        String value = properties.getProperty(propertyName);
        int result = NumberUtils.parseInteger(value, defaultValue);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    public DumpType readDumpType(String propertyName, DumpType defaultDump) {
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            propertyValue = defaultDump.name();
        }
        String value = propertyValue.toUpperCase();
        DumpType result;
        try {
            result = DumpType.valueOf(value);
        } catch (IllegalArgumentException e) {
            result = defaultDump;
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    public long readLong(String propertyName, long defaultValue) {
        String value = properties.getProperty(propertyName);
        long result = NumberUtils.parseLong(value, defaultValue);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    public List<String> readList(String propertyName) {
        String value = properties.getProperty(propertyName);
        if (value == null) {
            return Collections.emptyList();
        }
        String[] orders = value.trim().split(",");
        return Arrays.asList(orders);
    }

    public boolean readBoolean(String propertyName, boolean defaultValue) {
        String value = properties.getProperty(propertyName, Boolean.toString(defaultValue));
        boolean result = Boolean.parseBoolean(value);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{properties=");
        builder.append(properties);
        builder.append(", propertyPlaceholderHelper=");
        builder.append(propertyPlaceholderHelper);
        builder.append(", profileEnable=");
        builder.append(profileEnable);
        builder.append(", collectorSpanServerIp=");
        builder.append(collectorSpanServerIp);
        builder.append(", collectorSpanServerPort=");
        builder.append(collectorSpanServerPort);
        builder.append(", collectorStatServerIp=");
        builder.append(collectorStatServerIp);
        builder.append(", collectorStatServerPort=");
        builder.append(collectorStatServerPort);
        builder.append(", collectorTcpServerIp=");
        builder.append(collectorTcpServerIp);
        builder.append(", collectorTcpServerPort=");
        builder.append(collectorTcpServerPort);
        builder.append(", spanDataSenderWriteQueueSize=");
        builder.append(spanDataSenderWriteQueueSize);
        builder.append(", spanDataSenderSocketSendBufferSize=");
        builder.append(spanDataSenderSocketSendBufferSize);
        builder.append(", spanDataSenderSocketTimeout=");
        builder.append(spanDataSenderSocketTimeout);
        builder.append(", spanDataSenderChunkSize=");
        builder.append(spanDataSenderChunkSize);
        builder.append(", statDataSenderWriteQueueSize=");
        builder.append(statDataSenderWriteQueueSize);
        builder.append(", statDataSenderSocketSendBufferSize=");
        builder.append(statDataSenderSocketSendBufferSize);
        builder.append(", statDataSenderSocketTimeout=");
        builder.append(statDataSenderSocketTimeout);
        builder.append(", statDataSenderChunkSize=");
        builder.append(statDataSenderChunkSize);
        builder.append(", tcpDataSenderCommandAcceptEnable=");
        builder.append(tcpDataSenderCommandAcceptEnable);
        builder.append(", traceAgentActiveThread=");
        builder.append(traceAgentActiveThread);
        builder.append(", callStackMaxDepth=");
        builder.append(callStackMaxDepth);
        builder.append(", jdbcSqlCacheSize=");
        builder.append(jdbcSqlCacheSize);
        builder.append(", tomcatHidePinpointHeader=");
        builder.append(tomcatHidePinpointHeader);
        builder.append(", tomcatExcludeUrlFilter=");
        builder.append(tomcatExcludeUrlFilter);
        builder.append(", ibatis=");
        builder.append(ibatis);
        builder.append(", mybatis=");
        builder.append(mybatis);
        builder.append(", redis=");
        builder.append(redis);
        builder.append(", redisPipeline=");
        builder.append(redisPipeline);
        builder.append(", apacheHttpClient3Profile=");
        builder.append(apacheHttpClient3Profile);
        builder.append(", apacheHttpClient3ProfileCookie=");
        builder.append(apacheHttpClient3ProfileCookie);
        builder.append(", apacheHttpClient3ProfileCookieDumpType=");
        builder.append(apacheHttpClient3ProfileCookieDumpType);
        builder.append(", apacheHttpClient3ProfileCookieSamplingRate=");
        builder.append(apacheHttpClient3ProfileCookieSamplingRate);
        builder.append(", apacheHttpClient3ProfileEntity=");
        builder.append(apacheHttpClient3ProfileEntity);
        builder.append(", apacheHttpClient3ProfileEntityDumpType=");
        builder.append(apacheHttpClient3ProfileEntityDumpType);
        builder.append(", apacheHttpClient3ProfileEntitySamplingRate=");
        builder.append(apacheHttpClient3ProfileEntitySamplingRate);
        builder.append(", apacheHttpClient3ProfileIo=");
        builder.append(apacheHttpClient3ProfileIo);
        builder.append(", apacheHttpClient4Profile=");
        builder.append(apacheHttpClient4Profile);
        builder.append(", apacheHttpClient4ProfileCookie=");
        builder.append(apacheHttpClient4ProfileCookie);
        builder.append(", apacheHttpClient4ProfileCookieDumpType=");
        builder.append(apacheHttpClient4ProfileCookieDumpType);
        builder.append(", apacheHttpClient4ProfileCookieSamplingRate=");
        builder.append(apacheHttpClient4ProfileCookieSamplingRate);
        builder.append(", apacheHttpClient4ProfileEntity=");
        builder.append(apacheHttpClient4ProfileEntity);
        builder.append(", apacheHttpClient4ProfileEntityDumpType=");
        builder.append(apacheHttpClient4ProfileEntityDumpType);
        builder.append(", apacheHttpClient4ProfileEntitySamplingRate=");
        builder.append(apacheHttpClient4ProfileEntitySamplingRate);
        builder.append(", apacheHttpClient4ProfileStatusCode=");
        builder.append(apacheHttpClient4ProfileStatusCode);
        builder.append(", apacheHttpClient4ProfileIo=");
        builder.append(apacheHttpClient4ProfileIo);
        builder.append(", apacheNIOHttpClient4Profile=");
        builder.append(apacheNIOHttpClient4Profile);
        builder.append(", samplingEnable=");
        builder.append(samplingEnable);
        builder.append(", samplingRate=");
        builder.append(samplingRate);
        builder.append(", ioBufferingEnable=");
        builder.append(ioBufferingEnable);
        builder.append(", ioBufferingBufferSize=");
        builder.append(ioBufferingBufferSize);
        builder.append(", profileJvmCollectInterval=");
        builder.append(profileJvmCollectInterval);
        builder.append(", profilableClassFilter=");
        builder.append(profilableClassFilter);
        builder.append(", DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL=");
        builder.append(DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL);
        builder.append(", agentInfoSendRetryInterval=");
        builder.append(agentInfoSendRetryInterval);
        builder.append(", applicationServerType=");
        builder.append(applicationServerType);
        builder.append(", applicationTypeDetectOrder=");
        builder.append(applicationTypeDetectOrder);
        builder.append(", disabledPlugins=");
        builder.append(disabledPlugins);
        builder.append(", log4jLoggingTransactionInfo=");
        builder.append(log4jLoggingTransactionInfo);
        builder.append(", logbackLoggingTransactionInfo=");
        builder.append(logbackLoggingTransactionInfo);
        builder.append("}");
        return builder.toString();
    }
}