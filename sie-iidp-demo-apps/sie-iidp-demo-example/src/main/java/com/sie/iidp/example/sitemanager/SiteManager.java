package com.sie.iidp.example.sitemanager;

import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;


@Model(name = "hcm_site_manager", tableName = "hcm_site_manager", type = Model.ModelType.Buss, displayName =
        "边端注册信息@云端", description = "维护边端的信息", isAutoLog = Bool.True)
public class SiteManager extends BaseModel<SiteManager> {

    @MethodService(name = "tryConnection", description = "连接测试", auth = "search")
    public Boolean tryConnection(String cloudAddress, String protocol, String applicationId) {
        return true;
    }
}