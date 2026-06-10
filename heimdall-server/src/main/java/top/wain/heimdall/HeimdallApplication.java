package top.wain.heimdall;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import top.continew.starter.extension.crud.autoconfigure.CrudProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.starter.core.ContiNewStarterVersion;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;
import top.wain.heimdall.common.model.R;
import top.nextdoc4j.core.configuration.NextDoc4jProperties;

/**
 * 启动程序
 *
 * @author WainZeng
 * @since 2022/12/8 23:15
 */
@Slf4j
@EnableFileStorage
@EnableMethodCache(basePackages = "top.wain.heimdall")
@EnableConfigurationProperties(CrudProperties.class)
@EnableFeignClients
@RestController
@SpringBootApplication
@RequiredArgsConstructor
public class HeimdallApplication implements ApplicationRunner {

    private final ApplicationProperties applicationProperties;
    private final ServerProperties serverProperties;

    public static void main(String[] args) {
        // 禁用 AWS SDK for Java 1.x 弃用提示（1.x 由 x-file-storage 等依赖引入，计划后续迁移至 2.x）
        System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
        SpringApplication application = new SpringApplication(HeimdallApplication.class);
        application.setDefaultProperties(MapUtil.of("continew-starter.version", ContiNewStarterVersion.getVersion()));
        application.run(args);
    }

    @Hidden
    @SaIgnore
    @GetMapping("/")
    public R index() {
        return R.ok(applicationProperties);
    }

    @Override
    public void run(ApplicationArguments args) {
        String hostAddress = NetUtil.getLocalhostStr();
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        String baseUrl = URLUtil.normalize("%s:%s%s".formatted(hostAddress, port, contextPath));
        log.info("--------------------------------------------------------");
        log.info("{} server started successfully.", applicationProperties.getName());
        log.info("ContiNew Starter: v{} (Spring Boot: v{})", ContiNewStarterVersion.getVersion(), SpringBootVersion
            .getVersion());
        log.info("当前版本: v{} (Profile: {})", applicationProperties.getVersion(), SpringUtil
            .getProperty("spring.profiles.active"));
        log.info("服务地址: {}", baseUrl);
        NextDoc4jProperties docProperties = SpringUtil.getBean(NextDoc4jProperties.class);
        if (!docProperties.isProduction()) {
            log.info("接口文档: {}/doc.html", baseUrl);
        }
        log.info("--------------------------------------------------------");
    }
}