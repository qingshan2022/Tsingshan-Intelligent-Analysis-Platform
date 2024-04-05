package com.qingshan.qsbi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingshan.qsbi.model.dto.chart.ChartQueryRequest;
import com.qingshan.qsbi.model.entity.Chart;

/**
* @author 罗宇楠
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-11-29 22:22:32
*/
public interface ChartService extends IService<Chart> {
    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    public void handleChartUpdateError(long chartId, String message);

}
