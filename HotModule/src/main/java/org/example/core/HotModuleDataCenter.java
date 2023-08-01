package org.example.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.api.HotModuleApi;
import org.example.bean.Live;
import org.example.bean.HotModule;
import org.example.bean.hotmodule.HotModuleList;
import org.example.constpool.ConstPool;
import org.example.log.ChopperLogFactory;
import org.example.log.LoggerType;
import org.example.thread.oddjob.OddJobBoy;
import org.example.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Genius
 * @date 2023/07/21 18:44
 **/

/**
 * 热门模块的数据中心
 */
public class HotModuleDataCenter{

    /**
     * 设计这个参数的原因是，虽然HotModule可以存放HotLive数组，但是每天模块更新后会将整个hotModuleListPool替换，导致模块下的热门直播清空，
     * 于是便用hotModuleLivePool来存放每个模块下的热门直播，为了保证访问效率和数据的新鲜度，这里采用 Lazy Delete的方式，
     * 不用每次爬虫获取模块下的热门直播，而是查看是否过期，如果过期了才会去爬虫
     */
    private static final long HOT_MODULE_LIVE_TTL = 60; //热门模块直播过期时间

    private static volatile HotModuleDataCenter dataCenter;


    public static HotModuleDataCenter DataCenter(){
        if(dataCenter==null){
            synchronized (HotModuleDataCenter.class){
                if (dataCenter==null){
                    dataCenter = new HotModuleDataCenter();
                }
            }
        }
        return dataCenter;
    }

    private HotModuleDataCenter(){
        hotModuleListPool = new ConcurrentHashMap<>();

        hotModuleLivePool = new ConcurrentHashMap<>();
        for (ConstPool.PLATFORM value : ConstPool.PLATFORM.values()) {
            hotModuleLivePool.put(value.getName(),new ConcurrentHashMap<>());
        }
        hotLiveListPool = new ConcurrentHashMap<>();
    }

    private ConcurrentHashMap<String, HotModuleList> hotModuleListPool;   //各个平台热门模块列表

    public  ConcurrentHashMap<String,ConcurrentHashMap<HotModule,ModuleLives>> hotModuleLivePool;      //各个平台热门模块直播列表

    private ConcurrentHashMap<String, List<? extends Live>> hotLiveListPool;     //各个平台热门直播列表

    public void addModuleList(String platform,HotModuleList hotModuleList){
        hotModuleListPool.put(platform,hotModuleList);
    }

    public void addLiveList(String platform,List<? extends Live> liveList){
        hotLiveListPool.put(platform, liveList);
    }

    public void addModuleLiveList(String platform,HotModule hotModule,List<? extends Live> liveList){
        ModuleLives moduleLives = new ModuleLives(liveList, LocalDateTime.now());
        hotModule.setHotLives(liveList);
        hotModuleLivePool.get(platform).put(hotModule,moduleLives);
    }

    public HotModuleList getModuleList(String platform){
        return hotModuleListPool.get(platform);
    }

    public List<? extends Live> getLiveList(String platform){
        return hotLiveListPool.get(platform);
    }

    public List<? extends Live> getModuleLiveList(String platform, HotModule hotModule) throws InterruptedException {
        ModuleLives moduleLives = hotModuleLivePool.get(platform).get(hotModule);
        if(moduleLives!=null){
            if (moduleLives.isExpire()) {
                //TODO 改为爬虫请求
                OddJobBoy.Boy().addWork(()->{
                    addModuleLiveList(platform,hotModule,
                            HotModuleApi.getDouyuHotLive(Integer.parseInt(hotModule.getTagId())));
                    ChopperLogFactory.getLogger(LoggerType.Hot).info("platform:{} module:{} hot lives refresh~",platform,hotModule.getTagName());
                });
            }
            return moduleLives.lives;
        }else{
            //TODO多次访问改方法时会导致链接中断
            List<? extends Live> lives = new ArrayList<>();
            if(ConstPool.PLATFORM.DOUYU.getName().equals(platform)){
                lives = HotModuleApi.getDouyuHotLive(Integer.parseInt(hotModule.getTagId()));
            }
            addModuleLiveList(platform,hotModule, lives);
            return lives;
        }
    }

    //根据平台的模块名获取某模块
    public HotModule getModule(String platform,String moduleName){
        return this.getModuleList(platform).findHotModule(moduleName);
    }

    //根据平台的模块ID获取某模块
    public HotModule getModule(String platform,int moduleId){
        return this.getModuleList(platform).findHotModule(moduleId);
    }

    public boolean init() {
        return HotModuleDataCenter.DataCenter()!=null;
    }

    @Data
    @AllArgsConstructor
    class ModuleLives{
        private List<? extends Live> lives;
        private LocalDateTime updateTime;

        public boolean isExpire(){
            long now = TimeUtil.getCurrentSecond();
            return now - TimeUtil.getSecond(updateTime)>HOT_MODULE_LIVE_TTL;
        }
    }
}
