---
title: 日期类型
date: 2023-09-25 17:31:34
permalink: /pages/edf963/
---
# 模型属性

```java
package com.sie.app.newsdk.test.model.demo;

import com.sie.iidp.sdk.BaseModel;
import com.sie.iidp.sdk.DataType;
import com.sie.iidp.sdk.annotation.meta.Model;
import com.sie.iidp.sdk.annotation.meta.Property;
import java.sql.Timestamp;
import java.util.Date;

@Model(displayName = "日期Demo")
public class DateDemo extends BaseModel<DateDemo> {

    @Property(displayName = "年", dataType = DataType.DATE, dateFormat = "yyyy")
    private Date year;

    @Property(displayName = "月", dataType = DataType.DATE, dateFormat = "yyyy-MM")
    private Date month;

    @Property(displayName = "月范围", dataType = DataType.DATE, dateFormat = "yyyy-MM")
    private Date monthRange;

    @Property(displayName = "日", dataType = DataType.DATE)
    private Date date;

    @Property(displayName = "日范围", dataType = DataType.DATE)
    private Date dateRange;

    @Property(displayName = "时间", dataType = DataType.DATE_TIME, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Timestamp datetime;

    @Property(displayName = "时间范围", dataType = DataType.DATE_TIME, dateFormat = "yyyy-MM-dd "
        + "HH:mm:ss")
    private Timestamp datetimeRange;

    public Date getYear() {
        return getDate("year");
    }

    public Date getMonth() {
        return getDate("month");
    }

    public Date getDate() {
        return getDate("date");
    }

    public Timestamp getDatetime() {
        return getTimestamp("datetime");
    }
}
```

注意

1. 请使用 getDate 或 getTimestamp 从 BaseModel 中获取日期、时间属性
2. 年、月、时间，需要在 @Property 指定格式化。

# 视图文件

```json
{
  "views": {
    "datedemo_grid": {
      "body": {
        "buttons": [
          {
            "action": "preview",
            "auth": "read",
            "name": "详情"
          },
          {
            "action": "edit",
            "auth": "update",
            "name": "编辑"
          }
        ],
        "columns": [
          {
            "label": "年",
            "name": "year"
          },
          {
            "label": "月",
            "name": "month"
          },
          {
            "label": "日",
            "name": "date"
          },
          {
            "label": "时间",
            "name": "datetime"
          },
          {
            "label": "月范围",
            "name": "monthRange"
          },
          {
            "label": "日范围",
            "name": "dateRange"
          },
          {
            "label": "时间范围",
            "name": "datetimeRange"
          }
        ],
        "tbar": [
          {
            "action": "create",
            "auth": "create",
            "name": "新增"
          },
          {
            "action": "delete",
            "auth": "delete",
            "name": "删除"
          }
        ],
        "type": "grid"
      },
      "mode": "primary",
      "model": "DateDemo",
      "name": "日期Demo-表格",
      "type": "grid"
    },
    "datedemo_form": {
      "body": {
        "columns": [
          {
            "label": "年",
            "name": "year"
          },
          {
            "label": "月",
            "name": "month"
          },
          {
            "label": "日",
            "name": "date"
          },
          {
            "label": "时间",
            "name": "datetime"
          },
          {
            "label": "月范围",
            "name": "monthRange"
          },
          {
            "label": "日范围",
            "name": "dateRange"
          },
          {
            "label": "时间范围",
            "name": "datetimeRange"
          }
        ],
        "tabs": [],
        "type": "form"
      },
      "mode": "primary",
      "model": "DateDemo",
      "name": "日期Demo-表单",
      "type": "form"
    },
    "datedemo_search": {
      "body": {
        "columns": [
          {
            "label": "年",
            "name": "year",
            "widget": "year"
          },
          {
            "label": "月",
            "name": "month",
            "widget": "month"
          },
          {
            "label": "月范围",
            "name": "monthRange",
            "widget": "monthrange"
          },
          {
            "label": "日",
            "name": "date",
            "widget": "date"
          },
          {
            "label": "日范围",
            "name": "dateRange",
            "widget": "daterange"
          },
          {
            "label": "时间",
            "name": "datetime",
            "widget": "datetime"
          },
          {
            "label": "时间范围",
            "name": "datetimeRange",
            "widget": "datetimerange"
          }
        ],
        "type": "search"
      },
      "mode": "primary",
      "model": "DateDemo",
      "name": "日期Demo-搜索",
      "type": "search"
    }
  }
}
```

注意

1. 月、日期、时间范围只能用于搜索条件。不能用于表单、表格。

# 效果

选择年份

![选择年份](./images/select_year.png)

选择月份

![选择月份](./images/select_month.png)

选择日期

![选择日期](./images/select_date.png)

选择时间

![选择时间](./images/select_time.png)

月份范围

![月份范围](./images/scope_month.png)

日期范围

![日期范围](./images/scope_date.png)

时间范围

![时间范围](./images/scope_time.png)
