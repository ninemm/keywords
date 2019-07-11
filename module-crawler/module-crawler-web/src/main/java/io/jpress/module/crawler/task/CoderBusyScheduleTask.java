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

package io.jpress.module.crawler.task;

import com.jfinal.aop.Aop;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import io.jpress.module.crawler.crawler.AbstractBreadthCrawler;
import io.jpress.module.crawler.crawler.ProxyCoderBusyCrawler;
import io.jpress.module.crawler.crawler.ProxyCrawlerManager;
import io.jpress.module.crawler.crawler.ProxyData5uCrawler;
import io.jpress.module.crawler.enums.ProxySite;
import io.jpress.module.crawler.model.ProxyInfo;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.service.SpiderService;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * coderbusy
 *
 * @author: Eric Huang
 * @date: 2019-07-04 17:02
 */
public class CoderBusyScheduleTask extends AbstractScheduleTask<ProxyInfo> {

    private static final Log _LOG = Log.getLog(CoderBusyScheduleTask.class);

    private Object id;

    public CoderBusyScheduleTask(ScheduleTask scheduleTask) {
        super(scheduleTask);
    }

    @Override
    public void run() {

        Spider spider = Aop.get(SpiderService.class).findById(id);
        AbstractBreadthCrawler crawler = new ProxyCoderBusyCrawler("crawler/coderbusy", false, spider);
        try {
            ProxyCrawlerManager.me().start(ProxySite.coderbusy.getKey(), crawler, 1);
        } catch (Exception e) {
            _LOG.error("crawler:" + crawler.getClass().getName() + " start failure. ", e);
        }
    }

    @Override
    protected void execute(ThreadPoolExecutor executor, Integer pageNum) {

    }

    @Override
    protected Page<ProxyInfo> getPageData(Integer pageNum) {
        return null;
    }
}
