package com.vygovskiy.vpplugin

import com.vp.plugin.ApplicationManager
import com.vp.plugin.action.VPAction
import com.vp.plugin.action.VPActionController

class HotReloadAction : VPActionController {
    override fun performAction(action: VPAction) {
        val appManager = ApplicationManager.instance();
        appManager.pluginInfos.forEach {info ->
            appManager.reloadPluginClasses(info.pluginId)
            ApplicationManager.instance()
                .viewManager.showMessage("${info.pluginId} in dir ${info.pluginDir} is reloaded")
        }
    }

    override fun update(action: VPAction) {

    }
}