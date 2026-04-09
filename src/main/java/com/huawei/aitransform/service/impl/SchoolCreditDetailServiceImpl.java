package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.entity.SchoolCreditDetailRequestVO;
import com.huawei.aitransform.entity.SchoolCreditDetailResponseVO;
import com.huawei.aitransform.entity.SchoolCreditDetailVO;
import com.huawei.aitransform.mapper.SchoolCreditDetailMapper;
import com.huawei.aitransform.service.SchoolCreditDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * AI School学分数据明细查询Service实现类
 */
@Service
public class SchoolCreditDetailServiceImpl implements SchoolCreditDetailService {

    @Autowired
    private SchoolCreditDetailMapper schoolCreditDetailMapper;

    @Override
    public SchoolCreditDetailResponseVO getCreditDetailList(SchoolCreditDetailRequestVO request) {
        // 计算分页偏移量
        int offset = (request.getPageNum() - 1) * request.getPageSize();

        // 查询数据列表
        List<SchoolCreditDetailVO> records = schoolCreditDetailMapper.getCreditDetailList(request, offset);

        // 查询总记录数
        Long total = schoolCreditDetailMapper.getCreditDetailCount(request);

        // 计算总页数
        int pages = (int) Math.ceil((double) total / request.getPageSize());

        // 组装响应
        SchoolCreditDetailResponseVO response = new SchoolCreditDetailResponseVO();
        response.setRecords(records);
        response.setTotal(total);
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        response.setPages(pages);

        // 基于当前查询结果构建前端筛选项（初始化时的“全量可选项”）
        Set<String> jobFamilies = new LinkedHashSet<>();
        Set<String> jobCategories = new LinkedHashSet<>();
        Set<String> jobSubCategories = new LinkedHashSet<>();

        for (SchoolCreditDetailVO vo : records) {
            if (vo.getJobFamily() != null && !vo.getJobFamily().isEmpty()) {
                jobFamilies.add(vo.getJobFamily());
            }
            if (vo.getJobCategory() != null && !vo.getJobCategory().isEmpty()) {
                jobCategories.add(vo.getJobCategory());
            }
            if (vo.getJobSubCategory() != null && !vo.getJobSubCategory().isEmpty()) {
                jobSubCategories.add(vo.getJobSubCategory());
            }
        }

        response.setJobFamilies(new ArrayList<>(jobFamilies));
        response.setJobCategories(new ArrayList<>(jobCategories));
        response.setJobSubCategories(new ArrayList<>(jobSubCategories));

        return response;
    }
}
