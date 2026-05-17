package com.sie.iidp.example.utils.dto;

import com.sie.snest.engine.utils.Options;
import lombok.Data;

/**
 * 通用Options
 *
 * @author cxh
 * @date 2024年03月28日 13:49
 */
@Data
public class ExampleBaseOptions extends Options {

    private String id;

    private String code;

    private String name;

}
