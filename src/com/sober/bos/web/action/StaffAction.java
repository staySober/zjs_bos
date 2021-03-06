package com.sober.bos.web.action;

import com.sober.bos.domain.Staff;
import com.sober.bos.service.base.IStaffService;
import com.sober.bos.utils.PageBean;
import com.sober.bos.web.action.base.BaseAction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import sun.jvm.hotspot.debugger.Page;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 取派员(Staff)的action
 */

@Controller
@Scope("prototype")
public class StaffAction extends BaseAction<Staff>{


    //保存取派员的方法
    public String save(){
        staffService.save(model);
        //保存成功
        return "list";
    }


    //查询取派员的方法
    public String pageQuery() throws IOException {
        //初始化一个pagebean模型
        PageBean pageBean=new PageBean();
        pageBean.setCurrentPage(page);//当前的页码
        pageBean.setPageSize(rows);//每页显示的记录数
        //使用离线条件查询对象包装查询条件
        DetachedCriteria detachedCriteria=DetachedCriteria.forClass(Staff.class);
        pageBean.setDetachedCriteria(detachedCriteria);
        //调用service层去做分页查询
        staffService.pageQuery(pageBean);
        //查询之后向页面返回数据  数据类型为json格式
        JsonConfig jsonConfig=new JsonConfig();
        jsonConfig.setExcludes(new String[]{"detachedCriteria","pageSize","currentPage","decidedzones"});
        JSONObject jsonObject  = JSONObject.fromObject(pageBean, jsonConfig);
        String json = jsonObject.toString();
        //将json数据通过输出流写到客户端
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().print(json);
        return NONE;
    }

     private String ids;

    public void setIds(String ids) {

        this.ids = ids;
    }

    @RequiresPermissions("staff-delete")
    //作废操作的方法
    public String delete(){
        staffService.delete(ids);
        return "list";
    }

    //还原操作
    public String restore(){
        staffService.restore(ids);
        return "list";
    }


    //修改操作
    public String edit(){
        //先查出来原有的信息
        Staff staff=staffService.findById(model.getId());
        //重新设置信息
        staff.setName(model.getName());
        staff.setDeltag(model.getDeltag());
        staff.setHaspda(model.getHaspda());
        staff.setStandard(model.getStandard());
        staff.setTelephone(model.getTelephone());
        staff.setStation(model.getStation());
        //保存修改
        staffService.update(staff);

        //返回页面
        return "list";
    }


    //查询所有没有被作废并且没有关联定区的取派员信息 并以json的形式返回
    public String findByAjax(){
        DetachedCriteria dc = DetachedCriteria.forClass(Staff.class);
        dc.add(Restrictions.eq("deltag","0"));
        //没有关联定区的取派员
       // dc.add(Restrictions.isEmpty("decidedzones"));
        //调用service层查询
       List<Staff> list= staffService.findByCondition(dc);
        String[] excludes=new String[]{"decidedzones","telephone","haspda","deltag","standard","station"};
        writeList2Json(list,excludes);
        return NONE;

    }

}
