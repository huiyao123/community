package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    //声明主键自动生成
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertloginticket(LoginTicket loginTicket);

    //通过ticket查询
    @Select({"select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket}",
            "<if test=\"ticket!=null\"> ",
            "and 1=1",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);

}
