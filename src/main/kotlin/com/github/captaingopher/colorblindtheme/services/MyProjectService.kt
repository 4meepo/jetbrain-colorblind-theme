package com.github.captaingopher.colorblindtheme.services

import com.intellij.openapi.project.Project
import com.github.captaingopher.colorblindtheme.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
