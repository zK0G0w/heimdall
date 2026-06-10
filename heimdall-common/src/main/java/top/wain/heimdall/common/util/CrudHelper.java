package top.wain.heimdall.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.crane4j.core.support.OperateTemplate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.data.domain.Sort;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.ReflectUtils;
import top.continew.starter.core.util.TreeUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.DictModel;
import top.continew.starter.extension.crud.annotation.TreeField;
import top.continew.starter.extension.crud.autoconfigure.CrudProperties;
import top.continew.starter.extension.crud.autoconfigure.CrudTreeDictModelProperties;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

/**
 * @Description: CRUD 通用工具类，提供排序校验、数据填充等能力
 * @Author: WainZeng
 * @Date: 2026/04/16
 */
public final class CrudHelper {

    private CrudHelper() {
    }

    /**
     * 安全排序（校验排序字段是否为实体类字段，防止 SQL 注入）
     *
     * @param queryWrapper 查询条件封装对象
     * @param sortQuery    排序查询条件
     * @param entityClass  实体类（用于白名单校验）
     */
    public static <T> void sort(QueryWrapper<T> queryWrapper, SortQuery sortQuery, Class<?> entityClass) {
        if (sortQuery == null || sortQuery.getSort().isUnsorted()) {
            return;
        }
        List<Field> entityFields = ReflectUtils.getNonStaticFields(entityClass);
        Sort sort = sortQuery.getSort();
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String checkProperty;
            if (property.contains(StringConstants.DOT)) {
                checkProperty = CollUtil.getLast(CharSequenceUtil.split(property, StringConstants.DOT));
            } else {
                checkProperty = property;
            }
            Optional<Field> optional = entityFields.stream()
                .filter(field -> checkProperty.equals(field.getName()))
                .findFirst();
            ValidationUtils.throwIf(optional.isEmpty(), "无效的排序字段 [{}]", property);
            queryWrapper.orderBy(true, order.isAscending(), CharSequenceUtil.toUnderlineCase(property));
        }
    }

    /**
     * Crane4j 数据填充（单个对象）
     *
     * @param obj 待填充对象
     */
    public static void fill(Object obj) {
        if (obj == null) {
            return;
        }
        OperateTemplate operateTemplate = SpringUtil.getBean(OperateTemplate.class);
        operateTemplate.execute(obj);
    }

    /**
     * Crane4j 数据填充（集合）
     *
     * @param list 待填充集合
     */
    public static void fillAll(Collection<?> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        OperateTemplate operateTemplate = SpringUtil.getBean(OperateTemplate.class);
        list.forEach(operateTemplate::execute);
    }

    /**
     * 根据 @DictModel 注解构建字典列表
     *
     * @param list        实体列表
     * @param entityClass 实体类（需标注 @DictModel）
     * @param <T>         实体类型
     * @return 字典列表
     */
    public static <T> List<LabelValueResp> buildDict(List<T> list, Class<T> entityClass) {
        DictModel dictModel = entityClass.getDeclaredAnnotation(DictModel.class);
        CheckUtils.throwIfNull(dictModel, "请添加并配置 @DictModel 字典结构信息");
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        String labelKey = dictModel.labelKey().contains(StringConstants.DOT)
            ? CharSequenceUtil.subAfter(dictModel.labelKey(), StringConstants.DOT, true)
            : dictModel.labelKey();
        String valueKey = dictModel.valueKey().contains(StringConstants.DOT)
            ? CharSequenceUtil.subAfter(dictModel.valueKey(), StringConstants.DOT, true)
            : dictModel.valueKey();
        List<String> extraFieldNames = Arrays.stream(dictModel.extraKeys())
            .map(extraKey -> extraKey.contains(StringConstants.DOT)
                ? CharSequenceUtil.subAfter(extraKey, StringConstants.DOT, true)
                : extraKey)
            .map(CharSequenceUtil::toCamelCase)
            .toList();
        List<LabelValueResp> respList = new ArrayList<>(list.size());
        for (T entity : list) {
            LabelValueResp<Object> resp = new LabelValueResp<>();
            resp.setLabel(Convert.toStr(ReflectUtil.getFieldValue(entity, CharSequenceUtil.toCamelCase(labelKey))));
            resp.setValue(ReflectUtil.getFieldValue(entity, CharSequenceUtil.toCamelCase(valueKey)));
            if (CollUtil.isNotEmpty(extraFieldNames)) {
                Map<String, Object> extraMap = MapUtil.newHashMap(dictModel.extraKeys().length);
                for (String extraFieldName : extraFieldNames) {
                    extraMap.put(extraFieldName, ReflectUtil.getFieldValue(entity, extraFieldName));
                }
                resp.setExtra(extraMap);
            }
            respList.add(resp);
        }
        return respList;
    }

    /**
     * 构建树结构
     *
     * @param list      平铺列表
     * @param respClass 响应类型（需标注 @TreeField）
     * @param isSimple  是否简单树（下拉选项等场景）
     * @param <L>       列表数据类型
     * @return 树形结构列表
     */
    public static <L> List<Tree<Long>> buildTree(List<L> list, Class<L> respClass, boolean isSimple) {
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        TreeField treeField = respClass.getDeclaredAnnotation(TreeField.class);
        CheckUtils.throwIfNull(treeField, "请添加并配置 @TreeField 树结构信息");
        // 简单树使用配置默认值，完整树使用 @TreeField 注解值
        CrudProperties crudProperties = SpringUtil.getBean(CrudProperties.class);
        CrudTreeDictModelProperties treeDictModel = crudProperties.getTreeDictModel();
        TreeNodeConfig treeNodeConfig = isSimple ? treeDictModel.genTreeNodeConfig() : genTreeNodeConfig(treeField);
        Function<L, Long> getId = ReflectUtils.createMethodReference(respClass, CharSequenceUtil.genGetter(treeField
            .value()));
        Function<L, Long> getParentId = ReflectUtils.createMethodReference(respClass, CharSequenceUtil
            .genGetter(treeField.parentIdKey()));
        return TreeUtils.buildMultiRoot(list, getId, getParentId, treeNodeConfig, (node,
                                                                                   tree) -> buildTreeField(isSimple, node, tree, treeField, respClass));
    }

    /**
     * 根据 @TreeField 注解构建 TreeNodeConfig
     */
    public static TreeNodeConfig genTreeNodeConfig(TreeField treeField) {
        return new TreeNodeConfig().setIdKey(treeField.value())
            .setParentIdKey(treeField.parentIdKey())
            .setNameKey(treeField.nameKey())
            .setWeightKey(treeField.weightKey())
            .setChildrenKey(treeField.childrenKey())
            .setDeep(treeField.deep() < 0 ? null : treeField.deep());
    }

    /**
     * 构建树节点字段
     */
    private static <L> void buildTreeField(boolean isSimple,
                                           L node,
                                           Tree<Long> tree,
                                           TreeField treeField,
                                           Class<L> respClass) {
        tree.setId(ReflectUtil.invoke(node, CharSequenceUtil.genGetter(treeField.value())));
        tree.setParentId(ReflectUtil.invoke(node, CharSequenceUtil.genGetter(treeField.parentIdKey())));
        tree.setName(ReflectUtil.invoke(node, CharSequenceUtil.genGetter(treeField.nameKey())));
        tree.setWeight(ReflectUtil.invoke(node, CharSequenceUtil.genGetter(treeField.weightKey())));
        if (!isSimple) {
            List<Field> fieldList = ReflectUtils.getNonStaticFields(respClass);
            fieldList.removeIf(f -> CharSequenceUtil.equalsAnyIgnoreCase(f.getName(), treeField.value(), treeField
                .parentIdKey(), treeField.nameKey(), treeField.weightKey(), treeField.childrenKey()));
            fieldList.forEach(f -> tree.putExtra(f.getName(), ReflectUtil.invoke(node, CharSequenceUtil.genGetter(f
                .getName()))));
        }
    }
}
