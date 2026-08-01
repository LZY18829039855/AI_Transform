package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 部门全员训战总览（下钻）分页响应
 */
public class DepartmentEmployeeTrainingOverviewResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<DepartmentEmployeeTrainingOverviewVO> records;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;

    public DepartmentEmployeeTrainingOverviewResponseVO() {
        this.records = Collections.emptyList();
        this.total = 0L;
        this.pageNum = 1;
        this.pageSize = 50;
        this.pages = 0;
    }

    public List<DepartmentEmployeeTrainingOverviewVO> getRecords() {
        return records;
    }

    public void setRecords(List<DepartmentEmployeeTrainingOverviewVO> records) {
        this.records = records == null ? Collections.emptyList() : records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }
}
