package ${packageName}.${subPackageName};

import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import ${packageName}.model.entity.${classNamePrefix}DO;
import ${packageName}.model.query.${classNamePrefix}Query;
import ${packageName}.model.req.${classNamePrefix}Req;
import ${packageName}.model.resp.${classNamePrefix}DetailResp;
import ${packageName}.model.resp.${classNamePrefix}Resp;

import java.util.List;

/**
 * ${businessName}业务接口
 *
 * @author ${author}
 * @since ${datetime}
 */
public interface ${className} extends IService<${classNamePrefix}DO> {

    PageResp<${classNamePrefix}Resp> page(${classNamePrefix}Query query, PageQuery pageQuery);

    ${classNamePrefix}DetailResp get(Long id);

    Long create(${classNamePrefix}Req req);

    void update(${classNamePrefix}Req req, Long id);

    void delete(List<Long> ids);
}
