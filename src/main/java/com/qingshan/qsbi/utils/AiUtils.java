package com.qingshan.qsbi.utils;

import com.qingshan.qsbi.model.dto.chart.AIResultDto;
import org.redisson.api.RedissonClient;

public class AiUtils {
    private final RedissonClient redissonClient;
    public AiUtils(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }
    public AIResultDto getAns(long chartId, String question) {
        BigModelChar bigModelChar = new BigModelChar(chartId,redissonClient);
        bigModelChar.getResult(question);
        String aReturn = bigModelChar.getReturn();

        String[] splits = aReturn.split("---");
        StringBuffer genChart1 = new StringBuffer("");
        StringBuffer genResult1 = new StringBuffer("");
        if (splits.length != 3) {
            genChart1.append("服务错误");;
            genResult1.append("服务错误");
        }else {
            genChart1.append(splits[2].trim());
            genResult1.append(splits[1].trim());
        }
        String genChart = genChart1.toString();
        String genResult = genResult1.toString();
        if(genChart.contains("```json")){
            String[] split = genChart.split("```json");
            if(split.length == 2){
                genChart = split[1].substring(0,split[1].indexOf("```"));
            }
        }
        AIResultDto aiResultDto = new AIResultDto();
        aiResultDto.setChartData(genChart);
        aiResultDto.setOnAnalysis(genResult);

        return aiResultDto;
    }
}
