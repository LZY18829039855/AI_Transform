package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 员工下钻查询响应VO（支持分页，对齐学分/训战明细）
 */
public class EmployeeDrillDownResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工详细信息列表（当前页）
     */
    private List<EmployeeDetailVO> employeeDetails;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer pages;

    public EmployeeDrillDownResponseVO() {
        this.employeeDetails = Collections.emptyList();
        this.total = 0L;
        this.pageNum = 1;
        this.pageSize = 50;
        this.pages = 0;
    }

    public List<EmployeeDetailVO> getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(List<EmployeeDetailVO> employeeDetails) {
        this.employeeDetails = employeeDetails == null ? Collections.emptyList() : employeeDetails;
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
