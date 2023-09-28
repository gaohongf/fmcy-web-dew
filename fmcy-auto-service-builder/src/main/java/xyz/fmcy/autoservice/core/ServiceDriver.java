package xyz.fmcy.autoservice.core;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import xyz.fmcy.autoservice.annotation.EntityService;
import xyz.fmcy.autoservice.annotation.Mapper;
import xyz.fmcy.autoservice.annotation.Service;
import xyz.fmcy.entity.EMParser;
import xyz.fmcy.entity.annotation.EM;
import xyz.fmcy.server.database.XService;
import xyz.fmcy.util.auto.PackageScanner;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Configuration
public class ServiceDriver implements EMParser {
    private final EMParser parser;
    private final XServiceHelper serviceHelper;

    private final XMapperHelper mapperHelper;

    public ServiceDriver(@Qualifier("entityMapperPool") EMParser parser,
                         @Qualifier("entityMapperScanPackages") @Nullable String[] entityMapperScanPackages,
                         XServiceHelper serviceHelper,
                         XMapperHelper mapperHelper) {
        this.parser = parser;
        this.serviceHelper = serviceHelper;
        this.mapperHelper = mapperHelper;
        List<String> packages = Arrays.stream(Objects.requireNonNullElseGet(entityMapperScanPackages, () -> new String[0])).toList();
        PackageScanner.build(this::classScan).filter(PackageScanner.hasAnnotation(EntityService.class))
                .excludeClass("xyz.fmcy.util.entity.auto.EntityMapperTools")
                .scanPacket(packages.toArray(new String[0]));
    }

    @SuppressWarnings("unchecked")
    <O, T> void classScan(Class<O> clazz) {
        EntityService entityService = clazz.getAnnotation(EntityService.class);
        EM value = entityService.value();
        Service service = entityService.service();
        Mapper mapper = entityService.mapper();
        runMapping(clazz, value);
        Class<T> target = (Class<T>) value.target();
        try {
            if (mapper.enable()) {
                mapperHelper.createMapper(clazz, mapper.value());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (service.enable()) {
            try {
                serviceHelper.proxy(clazz, target, (Class<? extends XService<T>>) service.extend(), service.value());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void runMapping(Class<?> source, EM em) {
        parser.runMapping(source, em);
    }

}
