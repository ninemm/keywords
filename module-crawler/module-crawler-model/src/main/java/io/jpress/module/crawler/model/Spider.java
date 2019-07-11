package io.jpress.module.crawler.model;

import io.jboot.db.annotation.Table;
import io.jpress.module.crawler.model.base.BaseSpider;

/**
 * Generated by Jboot.
 */
@Table(tableName = "c_spider", primaryKey = "id")
public class Spider extends BaseSpider<Spider> {

    /**
     * 爬虫是否启动(单次抓取)
     *
     * @author Eric
     * @date 11:54 2019-07-04
     * @param null
     * @return
     */
    public Boolean isStartCrawler() {
        Boolean isStartCrawler = getIsStartCrawler();
        return isStartCrawler != null && isStartCrawler == true;
    }

    /**
     * 是否启用代理
     *
     * @author Eric
     * @date 11:53 2019-07-04
     * @param null
     * @return
     */
    public Boolean isEnableProxy() {
        Boolean isEnableProxy = getIsEnableProxy();
        return isEnableProxy != null && isEnableProxy == true;
    }

    /**
     * 是否断点续传
     *
     * @author Eric
     * @date 11:53 2019-07-04
     * @param null
     * @return
     */
    public Boolean isResumable() {
        Boolean isResumable = getIsResumable();
        return isResumable != null && isResumable == true;
    }

}
