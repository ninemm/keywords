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
package io.jpress.module.route.directive;

import com.jfinal.aop.Inject;
import com.jfinal.template.Env;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Scope;
import io.jboot.web.directive.annotation.JFinalDirective;
import io.jboot.web.directive.base.JbootDirectiveBase;
import io.jpress.module.article.model.ArticleCategory;
import io.jpress.module.article.service.ArticleCategoryService;
import io.jpress.module.route.service.TRouteCategoryService;

import java.util.List;

/**
 * @author Eric Huang 黄鑫 （ninemm@126.com）
 * @version V1.0
 * @Package io.jpress.module.route.directive
 */
@JFinalDirective("routeCategories")
public class RouteCategoriesDirective extends JbootDirectiveBase {

    @Inject
    private TRouteCategoryService categoryService;

    @Override
    public void onRender(Env env, Scope scope, Writer writer) {

        Long id = getParaToLong(0, scope);
        String type = getPara(1, scope);

        if (id == null || type == null) {
            throw new IllegalArgumentException("#articleCategories() args error. id or type must not be null." + getLocation());
        }


        List<ArticleCategory> categories = categoryService.findListByRouteId(id, type);
        if (categories == null || categories.isEmpty()) {
            return;
        }

        scope.setLocal("categories", categories);
        renderBody(env, scope, writer);
    }

    @Override
    public boolean hasEnd() {
        return true;
    }
}
