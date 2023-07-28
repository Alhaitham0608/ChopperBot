package org.example.init;

import org.example.log.ChopperLogFactory;
import org.example.log.LoggerType;

import java.util.List;

/**
 * @author Genius
 * @date 2023/07/22 18:47
 **/
public class FileModuleInitMachine extends ModuleInitMachine{

    public FileModuleInitMachine() {
        super(List.of(
                new ModuleSrcConfigFileInit(),
                new FileCacheManagerInit()
        ), "FileModule",  ChopperLogFactory.getLogger(LoggerType.File));
    }

}