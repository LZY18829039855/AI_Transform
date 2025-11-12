package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.AiCertStatisticsVO;
import com.huawei.aitransform.mapper.AiCertStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI认证统计服务类
 */
@Service
public class AiCertStatisticsService {

    @Autowired
    private AiCertStatisticsMapper aiCertStatisticsMapper;

    /**
     * 统计指定层级部门的AI认证数据
     * @param deptLevel 部门层级（2-4）
     * @param personType 人员类型（0-全员，1-干部，2-专家，3-基层主管）
     * @return 统计结果列表
     */
    public List<AiCertStatisticsVO> getAiCertStatistics(Integer deptLevel, Integer personType) {
        // 参数校验
        if (deptLevel == null || deptLevel < 2 || deptLevel > 4) {
            throw new IllegalArgumentException("部门层级参数必须在2-4之间");
        }
        
        if (personType == null || personType < 0 || personType > 3) {
            throw new IllegalArgumentException("人员类型参数必须在0-3之间（0-全员，1-干部，2-专家，3-基层主管）");
        }

        // 根据人员类型进行不同的统计
        List<AiCertStatisticsVO> totalList;
        List<AiCertStatisticsVO> certList;
        
        if (personType == 0) {
            // 全员统计
            totalList = aiCertStatisticsMapper.countTotalEmployeesByDept(deptLevel);
            certList = aiCertStatisticsMapper.countCertEmployeesByDept(deptLevel);
        } else if (personType == 1) {
            // 干部统计（待实现）
            throw new IllegalArgumentException("干部统计功能暂未实现");
        } else if (personType == 2) {
            // 专家统计（待实现）
            throw new IllegalArgumentException("专家统计功能暂未实现");
        } else if (personType == 3) {
            // 基层主管统计（待实现）
            throw new IllegalArgumentException("基层主管统计功能暂未实现");
        } else {
            throw new IllegalArgumentException("不支持的人员类型");
        }

        // 将证书统计结果转换为Map，以部门编码为key
        Map<String, AiCertStatisticsVO> certMap = new HashMap<>();
        for (AiCertStatisticsVO cert : certList) {
            if (cert.getDeptCode() != null) {
                certMap.put(cert.getDeptCode(), cert);
            }
        }

        // 合并数据，计算比例
        List<AiCertStatisticsVO> resultList = new ArrayList<>();
        for (AiCertStatisticsVO total : totalList) {
            AiCertStatisticsVO result = new AiCertStatisticsVO();
            result.setDeptCode(total.getDeptCode());
            result.setDeptName(total.getDeptName());
            result.setTotalCount(total.getTotalCount());

            // 获取该部门的证书人数
            AiCertStatisticsVO cert = certMap.get(total.getDeptCode());
            if (cert != null) {
                result.setCertCount(cert.getCertCount());
            } else {
                result.setCertCount(0);
            }

            // 计算比例
            if (result.getTotalCount() != null && result.getTotalCount() > 0) {
                BigDecimal rate = new BigDecimal(result.getCertCount())
                        .divide(new BigDecimal(result.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                result.setCertRate(rate);
            } else {
                result.setCertRate(BigDecimal.ZERO);
            }

            resultList.add(result);
        }

        return resultList;
    }
}

