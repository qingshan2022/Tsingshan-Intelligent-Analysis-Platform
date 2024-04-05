package com.qingshan.qsbi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingshan.qsbi.mapper.ChartMapper;
import com.qingshan.qsbi.model.dto.chart.ChartQueryRequest;
import com.qingshan.qsbi.model.entity.Chart;
import com.qingshan.qsbi.model.enums.ChartStateEnum;
import com.qingshan.qsbi.service.ChartService;
import com.qingshan.qsbi.utils.SqlUtils;
import com.qingshan.qsbi.constant.CommonConstant;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 罗宇楠
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-11-29 22:22:32
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {
    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long id = chartQueryRequest.getId();
        Long userId = chartQueryRequest.getUserId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        String genResult = chartQueryRequest.getGenResult();
        String name = chartQueryRequest.getName();

        queryWrapper.eq(id != null && id > 0 , "id",id);
        queryWrapper.like(StringUtils.isNotBlank(name) , "name",name);
        queryWrapper.eq(StringUtils.isNotBlank(chartType) , "chartType",chartType);
        queryWrapper.like(StringUtils.isNotBlank(goal),"goal",goal);


        queryWrapper.like(StringUtils.isNotBlank(genResult), "genResult", genResult);

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void handleChartUpdateError(long chartId, String message) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setState(ChartStateEnum.FAIL.getValue());
        updateChart.setExecMessage(message);
        boolean updateById = updateById(updateChart);
        if (!updateById) {
            log.debug("更新图片状态失败处理失败");
        }
    }


}




