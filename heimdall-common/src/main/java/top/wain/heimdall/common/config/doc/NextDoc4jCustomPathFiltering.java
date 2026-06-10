package top.wain.heimdall.common.config.doc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.starter.auth.satoken.autoconfigure.SaTokenExtensionProperties;
import top.nextdoc4j.security.core.enhancer.NextDoc4jPathExcluder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * NextDoc4j 自定义路径过滤
 *
 * @author echo
 * @since 2025/12/18
 */
@Component
@RequiredArgsConstructor
public class NextDoc4jCustomPathFiltering implements NextDoc4jPathExcluder {

    private final SaTokenExtensionProperties saTokenExtensionProperties;

    @Override
    public Set<String> getExcludedPaths() {
        Set<String> paths = new HashSet<>();
        this.addConfiguredExcludes(paths);
        return paths;
    }

    /**
     * 添加 Sa-Token 配置中的排除路径
     */
    private void addConfiguredExcludes(Set<String> paths) {
        if (saTokenExtensionProperties == null || saTokenExtensionProperties
            .getSecurity() == null || saTokenExtensionProperties.getSecurity().getExcludes() == null) {
            return;
        }

        paths.addAll(Arrays.asList(saTokenExtensionProperties.getSecurity().getExcludes()));
    }

    @Override
    public int getOrder() {
        // 在 RequestMappingHandlerMapping Excluder 之后执行
        return 200;
    }
}
