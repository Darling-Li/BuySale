package com.rice.trade.mapper;

import com.rice.trade.entity.AuditLog;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuditLogMapper {

    @Insert("""
            insert into audit_logs (
                username, role_name, method, path, action_name, request_params,
                status_code, ip_address, user_agent, occurred_at
            )
            values (
                #{username}, #{roleName}, #{method}, #{path}, #{actionName}, #{requestParams},
                #{statusCode}, #{ipAddress}, #{userAgent}, #{occurredAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog auditLog);

    @Select("select * from audit_logs order by occurred_at desc, id desc limit 200")
    List<AuditLog> findRecent();
}
