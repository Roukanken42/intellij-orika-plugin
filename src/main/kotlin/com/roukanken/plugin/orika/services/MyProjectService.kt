package com.roukanken.plugin.orika.services

import com.intellij.openapi.project.Project
import com.roukanken.plugin.orika.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
