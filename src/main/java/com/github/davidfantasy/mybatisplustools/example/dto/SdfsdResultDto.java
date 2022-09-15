package com.github.davidfantasy.mybatisplustools.example.dto;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
/**
 * sdfsdf.sdfsd 코드의 쿼리 결과 세트는 mybatis-plus-generator-ui에 의해 자동으로 생성됩니다.
 *
 * @author ㅇㄴㄹㄴ
 * @since 2022-09-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SdfsdResultDto {

    private String id;

    private String name;

    private String keyWord;

    private Date punchTime;

    private BigDecimal salaryMoney;

    private Double bonusMoney;

    private String sex;

    private Integer age;

    private Date birthday;

    private String email;

    private String content;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private String sysOrgCode;

    private Integer tenantId;

}
