package com.sie.iidp.example.mixmodel.service;

import com.sie.snest.engine.model.PropertyMeta;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.ArrayUtils;
import com.sie.snest.engine.utils.Utils;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.RequestModel;
import com.sie.snest.sdk.SdkService;
import com.sie.snest.sdk.annotation.meta.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MBM自身行业通用service层处理
 */
@Service
public class MbmService<T extends BaseModel> extends SdkService<T> {
    static Logger logger = LoggerFactory.getLogger(MbmService.class);


    /**
     * 重新定义更新方法
     *
     * @param req 更新对象DTO
     */
    public <D extends RequestModel> boolean mbmUpdate(D req, Class<T> clazz) {
//        return super.update(req);
        return false;
    }

    /**
     * 根据条件删除
     *
     * @param filter 筛选条件
     */
    public <D extends RequestModel> void delete(Filter filter) {
//        List<T> list = super.search(filter, ArrayUtils.asList(PropertyMeta.ID), 0, 0, null);
//        if (Utils.isNotBlank(list)) {
//            list.forEach(t -> super.delete(t.getId()));
//        }
    }

}