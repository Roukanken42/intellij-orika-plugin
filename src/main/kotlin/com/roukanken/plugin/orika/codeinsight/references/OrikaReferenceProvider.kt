package com.roukanken.plugin.orika.codeinsight.references

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteral
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import java.util.function.Function

internal class OrikaReferenceProvider(private val reference: Function<PsiLiteral, Array<PsiReference>>) : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return reference.apply(element as PsiLiteral)
    }
}

