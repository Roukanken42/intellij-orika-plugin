package com.github.roukanken42.intellijorikaplugin.services

import com.intellij.openapi.project.Project
import com.github.roukanken42.intellijorikaplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
