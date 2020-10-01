package com.roukanken.plugin.orika.codeinsight.references

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PsiJavaPatterns.literalExpression
import com.intellij.patterns.PsiJavaPatterns.psiMethod
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import ma.glasnost.orika.impl.DefaultMapperFactory.MapperFactoryBuilder

/**
 * Registers references for completion
 */
class OrikaReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(pattern(), OrikaReferenceProvider { psiLiteral -> OrikaReference.create(psiLiteral) })
    }

    private fun pattern(): ElementPattern<out PsiElement> {
        val fieldMethodPattern = psiMethod().withName("field").definedInClass(MapperFactoryBuilder::class.qualifiedName)
        return literalExpression().methodCallParameter(fieldMethodPattern)
    }

}