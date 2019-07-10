/**
 * Copyright (c) 2018-2019, Eric 黄鑫 (ninemm@126.com).
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
package io.jpress.module.keyword;

import com.jfinal.core.Controller;
import io.jboot.Jboot;
import io.jboot.core.listener.JbootAppListenerBase;
import io.jboot.db.model.Columns;
import io.jpress.core.menu.MenuGroup;
import io.jpress.core.module.ModuleListener;
import java.util.List;

/**
 * @author Gavin lau 刘洪义 （logicbiz@qq.com）
 * @version V1.0
 * @Title: Module 监听器
 * @Description: 每个 module 都应该有这样的一个监听器，用来配置自身Module的信息，比如后台菜单等
 * @Package io.jpress.module.keyword
 */
public class KeywordModuleListener extends JbootAppListenerBase implements ModuleListener {


    @Override
    public String onRenderDashboardBox(Controller controller) {
        return null;
    }

    @Override
    public String onRenderToolsBox(Controller controller) {
        return null;
    }

    @Override
    public void onConfigAdminMenu(List<MenuGroup> adminMenus) {
		MenuGroup menuGroup = new MenuGroup();
        menuGroup.setId("keyword");
        menuGroup.setText("关键词");
        menuGroup.setIcon("<i class=\"fa fa-fw fa-file-text\"></i>");
        menuGroup.setOrder(3);
        adminMenus.add(menuGroup);
    }

    @Override
    public void onConfigUcenterMenu(List<MenuGroup> ucenterMenus) {

      
    }


}