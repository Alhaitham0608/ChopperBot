package org.example.config;

import org.example.bean.ConfigFile;
import org.example.bean.FileType;
import org.example.constpool.CreeperModuleConstPool;
import org.example.constpool.HotModuleConstPool;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Genius
 * @date 2023/07/20 00:20
 **/
public class HotModuleConfig extends ConfigFile<Map<String,Object>> {
    private static final int FiveMinute = 0x493E0;
    private static final long OneDay = 0x5265C00;
    private static final String fileName = "hotModuleConfig.json";

    public HotModuleConfig(){
        super(HotModuleConstPool.HOT_MODULE_CONFIG_ROOT,fileName,
                Map.of("Module", List.of(
                        new HotModuleSetting(CreeperModuleConstPool.DOUYU,2,true, true,true, new ArrayList<>(), false,
                                List.of(allLiveDog()), OneDay, FiveMinute),
                        new HotModuleSetting(CreeperModuleConstPool.BILIBILI, 2,true,true,true, new ArrayList<>(), false,
                                List.of(allLiveDog()), OneDay, FiveMinute),
                        new HotModuleSetting(CreeperModuleConstPool.HUYA,2, true,true,true, new ArrayList<>(), false,
                                List.of(allLiveDog()), OneDay, FiveMinute),
                        new HotModuleSetting(CreeperModuleConstPool.DOUYING, 2,true,true,true, new ArrayList<>(), false,
                                List.of(allLiveDog()), OneDay, FiveMinute)
                        ),
                        "GuardNum",10
                ), FileType.HOT
        );
    }

    public static String getFullFilePath(){
        return Paths.get(HotModuleConstPool.HOT_MODULE_CONFIG_ROOT,fileName).toString();
    }

    private static FollowDog allLiveDog(){
        return new FollowDog(FollowDog.ALL_LIVES,6,new ArrayList<>());
    }
}