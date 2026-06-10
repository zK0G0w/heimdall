package top.wain.heimdall.system.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.api.system.DictItemApi;
import top.wain.heimdall.system.service.DictItemService;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;

/**
 * 字典项业务 API 实现
 *
 * @author WainZeng
 * @since 2025/7/23 20:57
 */
@Service
@RequiredArgsConstructor
public class DictItemApiImpl implements DictItemApi {

    private final DictItemService baseService;

    @Override
    public List<LabelValueResp> listByDictCode(String dictCode) {
        return baseService.listByDictCode(dictCode);
    }
}
