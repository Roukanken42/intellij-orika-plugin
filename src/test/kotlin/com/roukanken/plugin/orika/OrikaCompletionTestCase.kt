package com.roukanken.plugin.orika

import com.intellij.codeInsight.completion.LightFixtureCompletionTestCase
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.LanguageLevelModuleExtension
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.util.PathUtil
import java.io.File

private const val BUILD_LIBS_DIRECTORY = "build/libs"
private const val BUILD_MOCK_JDK_DIRECTORY = "build/mockJDK-"

abstract class OrikaCompletionTestCase : LightFixtureCompletionTestCase() {

    override fun setUp() {
        super.setUp()

        val libPath = PathUtil.toSystemIndependentName(File(BUILD_LIBS_DIRECTORY).absolutePath)
        VfsRootAccess.allowRootAccess(testRootDisposable, libPath)
        PsiTestUtil.addLibrary(
            myFixture.projectDisposable,
            myFixture.module,
            "Orika",
            libPath,
            "orika.jar"
        )
    }

    protected open fun addDirectoryToProject(directory: String) {
        myFixture.copyDirectoryToProject(directory, StringUtil.getShortName(directory, '/'))
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        val languageLevel = getLanguageLevel()
        return object : DefaultLightProjectDescriptor() {
            override fun getSdk(): Sdk? {
                val compilerOption = languageLevel.toJavaVersion().toString()
                return JavaSdk.getInstance()
                    .createJdk("java $compilerOption", BUILD_MOCK_JDK_DIRECTORY + compilerOption, false)
            }

            override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
                model.getModuleExtension(LanguageLevelModuleExtension::class.java).languageLevel = languageLevel
            }
        }
    }

    protected open fun getLanguageLevel(): LanguageLevel {
        return LanguageLevel.JDK_1_8
    }
}