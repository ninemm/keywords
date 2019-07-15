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
package io.jpress.module.crawler.controller;

import com.google.common.collect.ImmutableMap;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.Jboot;
import io.jboot.utils.StrUtil;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.validate.EmptyValidate;
import io.jboot.web.validate.Form;
import io.jpress.JPressConsts;
import io.jpress.core.menu.annotation.AdminMenu;
import io.jpress.module.crawler.ScheduleTaskManager;
import io.jpress.module.crawler.model.ProxyInfo;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.service.ScheduleTaskService;
import io.jpress.web.base.AdminControllerBase;

import java.util.Date;
import java.util.Map;
import java.util.Set;


@RequestMapping(value = "/admin/crawler/task", viewPath = JPressConsts.DEFAULT_ADMIN_VIEW)
public class _ScheduleTaskController extends AdminControllerBase {

    private static final Log _LOG = Log.getLog(_ScheduleTaskController.class);

    @Inject
    private ScheduleTaskService taskService;

    @AdminMenu(text = "定时任务管理", groupId = "crawler", order = 4)
    public void index() {
        // Page<ScheduleTask> page = taskService.paginate(getPagePara(), 10);
        // setAttr("page", page);
        render("crawler/schedule_task_list.html");
    }

    public void paginate() {

        /*Integer response = getInt("response");
        Integer isEnable = getInt("isEnable", 1);
        String protocol = getPara("protocol");
        String anonymityType = getPara("anonymityType");*/

        Page<ScheduleTask> page = taskService.paginate(getPagePara(), getPageSizePara());
        Map<String, Object> map = ImmutableMap.of("total", page.getTotalRow(), "rows", page.getList());
        renderJson(map);
    }

    public void edit() {
        int entryId = getParaToInt(0, 0);
        ScheduleTask entry = entryId > 0 ? taskService.findById(entryId) : new ScheduleTask();
        setAttr("scheduleTask", entry);
        render("crawler/schedule_task_edit.html");
    }
   
    public void doSave() {
        ScheduleTask task = getModel(ScheduleTask.class,"scheduleTask");

        if (StrUtil.isBlank(task.getTaskName())) {
            renderJson(Ret.fail().set("message", "任务名称不能为空。"));
            return;
        }

        if (StrUtil.isBlank(task.getTaskClass())) {
            renderJson(Ret.fail().set("message", "任务类名不能为空。"));
            return;
        }

        if (StrUtil.isBlank(task.getCron())) {
            renderJson(Ret.fail().set("message", "Cron表达式不能为空。"));
            return;
        }

        if (task.isDistributed() && Jboot.getRedis() == null) {
            // _LOG.error("redis is null, can not use DistributedScheduleTask or config redis info in jboot.properties");
            renderJson(Ret.fail().set("message", "redis 为空，不能启用分布式，请进行配置！"));
            return;
        }

        ScheduleTask backupTask = task.copyModel();

        if (task.getId() != null) {

            try {
                task.setModified(new Date());
                taskService.update(task);
                ScheduleTaskManager.me().removeTask(task);
                ScheduleTaskManager.me().addTask(task);
            } catch (Exception e) {
                taskService.update(backupTask);
                _LOG.error(e.getMessage(), e);
                renderJson(Ret.fail().set("message", e.getMessage()));
                return;
            }

        } else {

            try {
                task.setCreated(new Date());
                task.setTaskId(StrUtil.uuid());
                taskService.save(task);
                ScheduleTaskManager.me().addTask(task);
            } catch (Exception e) {
                if (task.getId() != null) {
                    task.delete();
                }

                _LOG.error(e.getMessage(), e);
                renderJson(Ret.fail().set("message", e.getMessage()));
                return;
            }
        }

        renderOkJson();
    }

    /**
     * 启动任务
     */
    public void startTask(){

        String id = getPara("id");

        if (StrUtil.notBlank(id)) {
            ScheduleTask task = taskService.findById(id);
            try {
                if (ScheduleTaskManager.me().start(task)) {
                    task.setIsStart(true);
                    task.setCreated(new Date());
                    taskService.update(task);
                    renderOkJson();
                    return;
                }
                _LOG.error("task start fail");
                renderJson(Ret.fail().set("message", "task start fail"));
                return;
            } catch (Exception e) {
                _LOG.error(e.getMessage(), e);
                renderJson(Ret.fail().set("message", e.getMessage()));
                return;
            }
        }

        _LOG.error("task id can't empty");
        renderJson(Ret.fail().set("message", "task id can't empty"));
    }

    @EmptyValidate({
        @Form(name = "scheduleTask.cron", message = "Cron表达式不能为空"),
        @Form(name = "scheduleTask.task_name", message = "任务名称不能为空"),
        @Form(name = "scheduleTask.task_class", message = "任务类名不能为空")
    })
    public void doSaveAndStart() {
        ScheduleTask task = getModel(ScheduleTask.class,"scheduleTask");
        try {
            task.setCreated(new Date());
            task.setTaskId(StrUtil.uuid());
            taskService.save(task);
            if (ScheduleTaskManager.me().start(task)) {
                task.setIsStart(true);
                task.setCreated(new Date());
                taskService.update(task);
                renderOkJson();
                return;
            }
            _LOG.error("task start fail");
            renderJson(Ret.fail().set("message", "task start fail"));
            return;
        } catch (Exception e) {
            if (task.getId() != null) {
                task.delete();
            }

            _LOG.error(e.getMessage(), e);
            renderJson(Ret.fail().set("message", e.getMessage()));
            return;
        }

    }

    /**
     * 停止任务
     */
    public void stopTask(){
        String id = getPara("id");
        if (StrUtil.notBlank(id)) {
            ScheduleTask task = taskService.findById(id);
            if (task != null) {
                try {

                    if (ScheduleTaskManager.me().stop(task.getTaskId())) {
                        task.setIsStart(false);
                        task.setModified(new Date());
                        taskService.update(task);
                        renderOkJson();
                        return;
                    }

                    _LOG.error("task stop fail");
                    renderJson(Ret.fail().set("message", "task stop fail"));
                    return;
                } catch (Exception e) {
                    _LOG.error(e.getMessage(), e);
                    renderJson(Ret.fail().set("message", e.getMessage()));
                    return;
                }
            }

            _LOG.error("can't find task in database");
            renderJson(Ret.fail().set("message", "Can't find task in database"));
            return;
        }

        _LOG.error("task id can't empty");
        renderJson(Ret.fail().set("message", "task id can't empty"));
    }

    public void disableTask(){

        String id = getPara("id");

        if (StrUtil.notBlank(id)) {

            ScheduleTask task = taskService.findById(id);
            if (task !=null) {
                try {
                    ScheduleTaskManager.me().removeTask(task);
                    task.setIsActive(false);
                    task.setModified(new Date());
                    taskService.update(task);

                    renderOkJson();
                    return;
                } catch (Exception e) {
                    _LOG.error(e.getMessage(), e);
                    renderJson(Ret.fail().set("message", e.getMessage()));
                    return;
                }
            }

            _LOG.error("can't find task in database");
            renderJson(Ret.fail().set("message", "Can't find task in database"));
            return;
        }

        _LOG.error("task id can't empty");
        renderJson(Ret.fail().set("message", "task id can't empty"));
    }

    public void activeTask(){
        String id = getPara("id");

        if (StrUtil.notBlank(id)) {
            ScheduleTask task = taskService.findById(id);
            if (task != null) {
                task.setIsActive(true);
                task.setModified(new Date());
                taskService.update(task);

                try {
                    ScheduleTaskManager.me().addTask(task);
                    renderOkJson();
                    return;
                } catch (Exception e) {
                    _LOG.error(e.getMessage(), e);
                    renderJson(Ret.fail().set("message", e.getMessage()));
                    return;
                }
            }

            _LOG.error("can't find task in database");
            renderJson(Ret.fail().set("message", "can't find task in database"));
            return;
        }

        _LOG.error("task id can't empty");
        renderJson(Ret.fail().set("message", "task id can't be empty"));
    }

    /**
     * 立即执行一次任务
     */
    public void trigger(){
        try {
            String id = getPara("id");
            if (StrUtil.notBlank(id)) {
                ScheduleTask task = taskService.findById(id);
                if (task != null) {
                    if (ScheduleTaskManager.me().trigger(task)) {
                        renderJson(Ret.ok().set("message", "Execute success"));
                        return;
                    }

                    _LOG.error("Trigger task fail");
                    renderJson(Ret.fail().set("message", "Trigger fail"));
                    return;
                }

                _LOG.error("can't find task in database");
                renderJson(Ret.fail().set("message", "Can't find task in database"));
                return;
            }

            _LOG.error("task id can't empty");
            renderJson(Ret.fail().set("message", "task id can't empty"));

        } catch (Exception e) {
            _LOG.error(e.getMessage(), e);
            renderJson(Ret.fail().set("message", e.getMessage()));
        }
    }

    public void doDel() {

        String id = getPara("id");
        if (StrUtil.isBlank(id)) {
            _LOG.error("can't find task in database");
            renderJson(Ret.fail().set("message", "task id can't empty"));
            return;
        }

        ScheduleTask task = taskService.findById(id);
        if (task == null) {
            _LOG.error("can't find task in database");
            renderJson(Ret.fail().set("message", "can't find task in database"));
            return;
        }

        try {
            taskService.delete(task);
            ScheduleTaskManager.me().removeTask(task);
            renderOkJson();
        } catch (Exception e) {
            _LOG.error(e.getMessage(), e);
            renderJson(Ret.fail().set("message", e.getMessage()));
        }
    }

    public void doDelByIds(){
        try {
            Set<String> idsSet = getParaSet("ids");
            for (String id : idsSet) {
                ScheduleTask task = taskService.findById(id);
                if (task == null) {
                    continue;
                }

                taskService.delete(task);
                ScheduleTaskManager.me().removeTask(task);
            }

            renderOkJson();
        } catch (Exception e) {
            _LOG.error(e.getMessage(), e);
            renderJson(Ret.fail().set("message", e.getMessage()));
        }
    }
}