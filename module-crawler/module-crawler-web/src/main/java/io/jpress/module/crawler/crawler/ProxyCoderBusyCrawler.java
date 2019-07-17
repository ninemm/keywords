/*
 * Copyright (c) 2018-2019, Eric 黄鑫 (ninemm@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jpress.module.crawler.crawler;

import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.jboot.Jboot;
import io.jpress.commons.utils.DateUtils;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.request.ProxyRequester;
import okhttp3.Headers;
import org.joda.time.LocalDateTime;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

/**
 * coderbusy
 *
 * http://proxy.coderbusy.com//
 *
 * 定时任务：* /3 * * * *
 *
 * @author: Eric Huang
 * @date: 2019-07-03 17:54
 */
public class ProxyCoderBusyCrawler extends AbstractBreadthCrawler {

    public ProxyCoderBusyCrawler(String crawlPath, boolean autoParse, Spider spider) {
        super(crawlPath, autoParse, spider);

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Host", "proxy.coderbusy.com");
        headerMap.put("Referer", "http://proxy.coderbusy.com/");
        Headers headers = Headers.of(headerMap);

        // 设置HTTP代理插件
        if (spider != null && spider.isEnableProxy()) {
            setRequester(new ProxyRequester("http", headers));
        }
    }

    @Override
    protected void parse(Page page, CrawlDatums next) {
        List<String> proxyList = Lists.newArrayList();
        String crawlerTime = LocalDateTime.now().toString(DateUtils.DEFAULT_FORMATTER);
        Elements elements = page.select("table > tbody > tr");

        for (Element ele : elements) {
            String ip = ele.child(0).text();
            String port = ele.child(1).text();
            String anonymity = ele.child(2).text();
            String protocol = ele.child(3).text();

            if ("Transparent".equals(anonymity)) {
                anonymity = "透明";
            } else if ("HighAnonymous".equals(anonymity)) {
                anonymity = "高匿";
            }

            if ("√".equals(protocol)) {
                protocol = "https";
            } else {
                protocol = null;
            }

            String response = ele.child(5).text();
            double respTime = Double.valueOf(response);
            respTime = NumberUtil.div(respTime, 1000d, 4);

            String location = ele.child(7).text();
            String isp = ele.child(8).text();

            if (StrUtil.isBlank(ip) || StrUtil.isBlank(port) || StrUtil.isBlank(protocol)) {
                continue;
            }

            StringBuilder sqlBuilder = new StringBuilder("insert into proxy_info(`ip`, `port`, `location`, `response`,");
            sqlBuilder.append(" `protocol`, `anonymity_type`, `crawler_time`, `website`) values(");
            sqlBuilder.append("'").append(ip).append("', ");
            sqlBuilder.append(port).append(", ");

            sqlBuilder.append("'").append(location).append("', ");
            sqlBuilder.append(respTime).append(", ");
            sqlBuilder.append("'").append(protocol).append("', ");
            sqlBuilder.append("'").append(anonymity).append("', ");

            sqlBuilder.append("'").append(crawlerTime).append("', ");
            sqlBuilder.append("'").append("proxy.coderbusy.com").append("'");
            sqlBuilder.append(")");
            sqlBuilder.append(" on duplicate key update response = " + respTime);

            proxyList.add(sqlBuilder.toString());
        }

        Jboot.sendEvent(CrawlerConsts.QIYUNPROXY_EVENT_NAME, proxyList);
    }

    @Override
    protected void initConfig() {
        if (spider != null) {
            Configuration conf = Configuration.copyDefault();

            conf.setExecuteInterval(spider.getSleep());
            conf.setMaxExecuteCount(spider.getRetry());
            conf.setConnectTimeout(spider.getTimeout());
            conf.setDefaultUserAgent(spider.getUserAgent());

            this.setConf(conf);
            if (spider.isResumable()) {
                this.setResumable(true);
            }
            this.setThreads(spider.getThread());
            this.addSeedAndReturn(spider.getStartUrl());
        }
    }

    public static void main(String[] args) throws Exception {

        String startUrl = "http://proxy.coderbusy.com/";
        ProxyCoderBusyCrawler crawler = new ProxyCoderBusyCrawler("crawler/coderbusy", false, null);

        Configuration conf = Configuration.copyDefault();
        conf.setExecuteInterval(5000);
        // conf.setThreadKiller(1);
        // 设置爬取URL数量的上限
        // conf.setTopN(1000);
        conf.setReadTimeout(30000);
        // 连接超时时间
        conf.setConnectTimeout(50000);
        // 最多执行次数
        conf.setMaxExecuteCount(2);
        // conf.setWaitThreadEndTime(5000);

        conf.setDefaultUserAgent(RandomUtil.randomEle(CrawlerConsts.USER_AGENT));

        crawler.setConf(conf);
        crawler.setThreads(1);
        crawler.addSeedAndReturn(startUrl);

        // crawler.addRegex("https://www.xicidaili.com/wn/*");

        //crawler.setResumable(true);
        // 设置爬取深度
        crawler.start(1);
    }
}
