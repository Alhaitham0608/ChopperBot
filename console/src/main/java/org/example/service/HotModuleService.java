package org.example.service;

import org.example.bean.HotModule;
import org.example.bean.hotmodule.HotModuleList;
import org.springframework.stereotype.Service;

import java.util.List;


public interface HotModuleService {


    HotModule getModuleHotLives(String platform, int moduleId);
}