/**
 * Copyright (c) 2016-2019, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.module.crawler.controller;

import cn.hutool.core.util.RandomUtil;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.exception.JbootException;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.validate.EmptyValidate;
import io.jboot.web.validate.Form;
import io.jpress.JPressConsts;
import io.jpress.core.menu.annotation.AdminMenu;
import io.jpress.module.crawler.crawler.*;
import io.jpress.module.crawler.enums.ProxySite;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.service.ScheduleTaskService;
import io.jpress.module.crawler.service.SpiderService;
import io.jpress.web.base.AdminControllerBase;


@RequestMapping(value = "/admin/crawler/spider", viewPath = JPressConsts.DEFAULT_ADMIN_VIEW)
public class _SpiderController extends AdminControllerBase {

    private static final Log _LOG = Log.getLog(_SpiderController.class);

    @Inject
    private SpiderService spiderService;
    @Inject
    private ScheduleTaskService scheduleTaskService;

    @AdminMenu(text = "网站爬虫模板", groupId = "crawler", order = 0)
    public void index() {
        Page<Spider> page = spiderService.paginate(getPagePara(), 10);
        setAttr("page", page);
        render("crawler/spider_list.html");
    }

    @AdminMenu(text = "新增代理模板", groupId = "crawler", order = 1)
    public void add() {
        Page<Spider> page = spiderService.paginate(getPagePara(), 10);
        setAttr("page", page);
        render("crawler/proxy_ip_edit.html");
    }
   
    public void edit() {
        int entryId = getParaToInt(0, 0);

        Spider entry = entryId > 0 ? spiderService.findById(entryId) : null;
        if (entry == null) {
            entry = new Spider();
            entry.setUserAgent(RandomUtil.randomEle(CrawlerConsts.USER_AGENT));
        }
        setAttr("spider", entry);
        render("crawler/spider_edit.html");
    }

    @EmptyValidate({
        @Form(name = "spider.site_name", message = "网站名称不能为空"),
        @Form(name = "spider.domain", message = "网站域名不能为空"),
        @Form(name = "spider.start_url", message = "起始链接不能为空")
    })
    public void doSave() {
        Spider entry = getModel(Spider.class,"spider");
        spiderService.saveOrUpdate(entry);
        renderJson(Ret.ok().set("id", entry.getId()));
    }

    public void doDel() {
        Long id = getIdPara();
        render(spiderService.deleteById(id) ? Ret.ok() : Ret.fail());
    }

    public void task() {

        Object id = getPara("id");
        ScheduleTask scheduleTask = scheduleTaskService.findBySpiderId(id);
        if (scheduleTask == null) {
            scheduleTask = new ScheduleTask();
        }

        setAttr("scheduleTask", scheduleTask);
        keepPara();
        render("crawler/spider_task.html");
    }

    public void start() {
        Object id = getPara(0);
        if (id == null) {
            throw new JbootException("id 不能为空");
        }

        Spider spider = spiderService.findById(id);
        spider.setIsStartCrawler(true);

        if (spiderService.update(spider)) {
            int depth = 1;
            String key = null;
            AbstractBreadthCrawler crawler = null;

            if (spider.getStartUrl().contains(ProxySite.kuai.getKey())) {
                key = ProxySite.kuai.getKey();
                crawler = new ProxyKuaiCrawler("crawler/kuai", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.xiaoshu.getKey())) {
                depth = 2;
                key = ProxySite.xiaoshu.getKey();
                crawler = new ProxyXiaoShuCrawler("crawler/xiaoshu", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.xici.getKey())) {
                key = ProxySite.xici.getKey();
                crawler = new ProxyXiciCrawler("crawler/xici", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.nima.getKey())) {
                key = ProxySite.nima.getKey();
                crawler = new ProxyNiMaCrawler("crawler/nima", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.coderbusy.getKey())) {
                key = ProxySite.coderbusy.getKey();
                crawler = new ProxyCoderBusyCrawler("crawler/coderbusy", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.data5u.getKey())) {
                key = ProxySite.data5u.getKey();
                crawler = new ProxyData5uCrawler("crawler/data5u", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.ip3366.getKey())) {
                key = ProxySite.ip3366.getKey();
                crawler = new ProxyYunCrawler("crawler/yun", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.qiyun.getKey())) {
                key = ProxySite.qiyun.getKey();
                crawler = new ProxyQiyunCrawler("crawler/qiyun", false, spider);
            }

            try {

                if (crawler == null) {
                    throw new JbootException("crawler don't init.");
                }

                ProxyCrawlerManager.me().start(key, crawler, depth);
            } catch (Exception e) {
                _LOG.error("crawler:" + crawler.getClass().getName() + " start failure. ", e);
                renderFailJson();
                return ;
            }

            renderOkJson();
        }
        renderFailJson();
    }

    public void stop() {

        Object id = getPara(0);
        if (id == null) {
            throw new JbootException("id 不能为空");
        }

        Spider spider = spiderService.findById(id);
        spider.setIsStartCrawler(false);

        if (spiderService.update(spider)) {
            if (spider.getStartUrl().contains(ProxySite.kuai.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.kuai.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.xiaoshu.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.kuai.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.xici.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.xici.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.nima.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.nima.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.coderbusy.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.coderbusy.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.data5u.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.data5u.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.ip3366.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.ip3366.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.qiyun.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.qiyun.getKey());
            }
        }
        renderOkJson();
    }
}