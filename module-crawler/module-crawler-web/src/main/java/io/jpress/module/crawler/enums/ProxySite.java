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

package io.jpress.module.crawler.enums;

/**
 * 代理网站
 *
 * @author: Eric Huang
 * @date: 2019/6/25 18:33
 */
public enum ProxySite {

    xiciProxy("xicidaili","https://www.xicidaili.com", "西刺免费代理IP"),
    kuaiProxy("kuaidaili","https://www.kuaidaili.com/", "快代理IP"),
    xsProxy("xsdaili","http://www.xsdaili.com/", "小舒免费代理IP"),
    // goubanjia("goubanjia","www.goubanjia.com", "全网代理IP"),
    qiyun("qiyun","www.qydaili.com", "旗云代理"),
    nima("nima","www.nimadaili.com", "泥马代理"),
    ip3366("goubanjia", "www.ip3366.net", "云代理IP"),
    // freeProxyList("goubanjia","free-proxy-list.net", "Free Proxy List"),
    data5u("goubanjia","www.data5u.com", "无忧代理IP"),
    xdaili("goubanjia","www.xdaili.cn", "讯代理IP"),
    nianshao("goubanjia","www.nianshao.me", "年少HTTP PROXY"),
    proxydb("goubanjia","proxydb.net", "proxydb"),
    kxdaili("kx","kxdaili.com", "开心代理"),
    coderbusy("goubanjia","proxy.coderbusy.com", "coderbusy");

    private String key;
    private String domain;
    private String name;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    ProxySite(String key, String domain, String name) {
        this.key = key;
        this.domain = domain;
        this.name = name;
    }
}
