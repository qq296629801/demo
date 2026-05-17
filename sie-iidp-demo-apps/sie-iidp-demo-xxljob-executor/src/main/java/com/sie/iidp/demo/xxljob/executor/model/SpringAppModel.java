package com.sie.iidp.demo.xxljob.executor.model;

import com.sie.iidp.demo.xxljob.executor.spring.SpringApp;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author chun
 * @Date 2024/04/23/22:36
 */
@Model(name = "xxljob_spring_app", type = Model.ModelType.Data)
public class SpringAppModel extends BaseModel<SpringAppModel> {
    private final static Logger logger = LoggerFactory.getLogger(SpringAppModel.class);
    /**
     * 启动 在app.json的启动事件里配置
     */
    public void start() {
        logger.info("启动xxljob-spring");
        SpringApp.start(new String[0], SpringAppModel.class.getClassLoader());
    }

    /**
     * 停止 在app.json的启动事件里配置
     */
    public void stop() {
        logger.info("停止xxljob-spring");
        SpringApp.stop();
    }
}
